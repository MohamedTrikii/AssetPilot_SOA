#!/usr/bin/env node
// AssetPilot — outil de gestion du Product Backlog (Node 18+, aucune dépendance).
//
//   node scripts/backlog/backlog.mjs validate                 Vérifie docs/backlog/backlog.json
//   node scripts/backlog/backlog.mjs render                   Régénère docs/backlog/PRODUCT_BACKLOG.md
//   node scripts/backlog/backlog.mjs add [options]            Ajoute un élément (ID auto)
//   node scripts/backlog/backlog.mjs set US-031 [--status done --commit sha ...]   Modifie un élément
//   node scripts/backlog/backlog.mjs add-member [options]     Ajoute un collaborateur
//   node scripts/backlog/backlog.mjs sync [--dry-run] [--repo owner/name] [--project N --owner LOGIN]
//                                                             Crée / met à jour labels, milestones et Issues GitHub
//
// Voir docs/backlog/README.md pour le détail.

import { readFileSync, writeFileSync, mkdtempSync, mkdirSync, rmSync } from 'node:fs';
import { execFileSync } from 'node:child_process';
import { tmpdir } from 'node:os';
import { join, dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..');
const BACKLOG = join(ROOT, 'docs', 'backlog', 'backlog.json');
const MARKDOWN = join(ROOT, 'docs', 'backlog', 'PRODUCT_BACKLOG.md');
const SPRINTS_DIR = join(ROOT, 'docs', 'backlog', 'sprints');
const MARKER = 'backlog-id';
const MANAGED_PREFIXES = ['type:', 'priority:', 'component:', 'status:', 'epic:', 'grading:'];

// ---------------------------------------------------------------- utilitaires

function parseArgs(argv) {
  const args = { _: [] };
  for (let i = 0; i < argv.length; i++) {
    const a = argv[i];
    if (!a.startsWith('--')) { args._.push(a); continue; }
    const key = a.slice(2);
    const next = argv[i + 1];
    if (next === undefined || next.startsWith('--')) args[key] = true;
    else { args[key] = args[key] === undefined ? next : [].concat(args[key], next); i++; }
  }
  return args;
}
const list = (v) => (v === undefined || v === true ? [] : [].concat(v).flatMap((s) => String(s).split(',')).map((s) => s.trim()).filter(Boolean));

const load = () => JSON.parse(readFileSync(BACKLOG, 'utf8'));
const save = (data) => writeFileSync(BACKLOG, JSON.stringify(data, null, 2) + '\n', 'utf8');
const die = (msg) => { console.error(`✖ ${msg}`); process.exit(1); };

// ---------------------------------------------------------------- validation

function validate(data) {
  const errors = [];
  const { enums } = data;
  const members = new Set(data.members.map((m) => m.id));
  const sprints = new Set(data.sprints.map((s) => s.id));
  const epics = new Set(data.epics.map((e) => e.id));
  const grading = new Set((data.grading || []).map((g) => g.id));
  const ids = new Set();

  const check = (cond, msg) => { if (!cond) errors.push(msg); };
  for (const m of data.members) {
    check(/^[a-z0-9-]+$/.test(m.id), `membre "${m.id}" : id invalide (minuscules, chiffres, tirets)`);
  }
  for (const e of data.epics) {
    check(/^EP-\d{2,}$/.test(e.id), `épic "${e.id}" : format attendu EP-01`);
    check(!ids.has(e.id), `id dupliqué : ${e.id}`); ids.add(e.id);
    check(enums.component[e.component], `${e.id} : component inconnu "${e.component}"`);
    check(enums.priority[e.priority], `${e.id} : priority inconnue "${e.priority}"`);
  }
  for (const it of data.items) {
    const p = it.id || '(sans id)';
    check(/^[A-Z]+-\d{3,}$/.test(it.id), `${p} : format attendu US-001`);
    check(!ids.has(it.id), `id dupliqué : ${it.id}`); ids.add(it.id);
    check(it.title, `${p} : title obligatoire`);
    check(epics.has(it.epic), `${p} : épic inconnu "${it.epic}"`);
    check(enums.type[it.type] && it.type !== 'epic', `${p} : type inconnu "${it.type}"`);
    check(enums.priority[it.priority], `${p} : priority inconnue "${it.priority}"`);
    check(enums.status[it.status], `${p} : status inconnu "${it.status}"`);
    check(enums.component[it.component], `${p} : component inconnu "${it.component}"`);
    check(sprints.has(it.sprint) || it.sprint === null || it.sprint === undefined, `${p} : sprint inconnu "${it.sprint}"`);
    check(it.points === undefined || it.points === null || Number.isInteger(it.points), `${p} : points doit être un entier`);
    for (const a of it.assignees || []) check(members.has(a), `${p} : membre inconnu "${a}"`);
    for (const g of it.grading || []) check(grading.has(g), `${p} : critère de grille inconnu "${g}"`);
    check(it.status === 'backlog' || (it.assignees || []).length > 0, `${p} : un élément planifié doit avoir au moins un responsable`);
  }
  for (const it of data.items) for (const d of it.depends_on || []) check(ids.has(d), `${it.id} : dépendance inconnue "${d}"`);
  return errors;
}

function validateOrDie(data) {
  const errors = validate(data);
  if (errors.length) die(`backlog.json invalide :\n  - ${errors.join('\n  - ')}`);
}

// ---------------------------------------------------------------- rendu Markdown

const memberName = (data, id) => data.members.find((m) => m.id === id)?.name ?? id;
const owners = (data, it) => ((it.assignees || []).length ? it.assignees.map((a) => memberName(data, a)).join(', ') : '—');
const STATUS_ICON = { backlog: '⚪', todo: '🔵', 'in-progress': '🟡', 'in-review': '🟣', blocked: '🔴', done: '🟢' };

function render(data) {
  const { enums } = data;
  const L = [];
  const items = data.items;
  const sum = (arr) => arr.reduce((s, i) => s + (i.points || 0), 0);

  L.push(`# Product Backlog — ${data.project.name}`, '');
  L.push(`> Fichier **généré** depuis [backlog.json](backlog.json) par \`node scripts/backlog/backlog.mjs render\` — ne pas modifier à la main.`);
  L.push(`> Comment ajouter un élément ou un membre : [README.md](README.md).`, '');

  const done = items.filter((i) => i.status === 'done');
  L.push('## Vue d’ensemble', '');
  L.push(`- **${items.length}** éléments, **${sum(items)}** points — **${done.length}** terminés (${sum(done)} pts, ${Math.round((100 * sum(done)) / Math.max(1, sum(items)))} %)`);
  L.push(`- Statuts : ${Object.keys(enums.status).map((s) => `${STATUS_ICON[s] ?? ''} ${enums.status[s]} (${items.filter((i) => i.status === s).length})`).join(' · ')}`, '');

  L.push('## Équipe et charge', '');
  L.push('| Membre | GitHub | Rôle | Éléments | Points | Terminés |', '|---|---|---|---:|---:|---:|');
  for (const m of data.members) {
    const mine = items.filter((i) => (i.assignees || []).includes(m.id));
    L.push(`| ${m.name} | ${m.github ? `@${m.github}` : '—'} | ${m.role ?? ''} | ${mine.length} | ${sum(mine)} | ${mine.filter((i) => i.status === 'done').length} |`);
  }
  L.push('', '_Un élément affecté à plusieurs membres est une tâche collective ; le premier nommé est le responsable principal._', '');

  L.push('## Sprints', '');
  L.push('| Sprint | Période | Objectif | Éléments | Points |', '|---|---|---|---:|---:|');
  for (const s of data.sprints) {
    const its = items.filter((i) => i.sprint === s.id);
    L.push(`| **${s.title}** | ${s.start} → ${s.end} | ${s.goal} | ${its.length} | ${sum(its)} |`);
  }
  L.push('');

  if ((data.grading || []).length) {
    L.push('## Couverture de la grille d’évaluation', '');
    L.push('| Critère | Barème | Éléments | Terminés | Restants |', '|---|---:|---|---:|---|');
    for (const g of data.grading) {
      const its = items.filter((i) => (i.grading || []).includes(g.id));
      const left = its.filter((i) => i.status !== 'done');
      L.push(`| ${g.id} — ${g.title} | ${g.points} | ${its.map((i) => i.id).join(', ') || '⚠️ aucun'} | ${its.length - left.length}/${its.length} | ${left.map((i) => i.id).join(', ') || '✅'} |`);
    }
    L.push('');
  }

  L.push('## Backlog par épic', '');
  for (const e of data.epics) {
    const its = items.filter((i) => i.epic === e.id);
    L.push(`### ${e.id} — ${e.title}`, '', `${e.description}  `, `Composant : ${enums.component[e.component].label} · ${its.length} éléments · ${sum(its)} pts`, '');
    if (!its.length) { L.push('_Aucun élément._', ''); continue; }
    L.push('| ID | Élément | Type | Priorité | Pts | Sprint | Responsable(s) | Statut | Réf. |', '|---|---|---|---|---:|---|---|---|---|');
    for (const i of its) {
      L.push(`| ${i.id} | ${i.title} | ${enums.type[i.type]} | ${enums.priority[i.priority]} | ${i.points ?? '—'} | ${i.sprint ?? '—'} | ${owners(data, i)} | ${STATUS_ICON[i.status] ?? ''} ${enums.status[i.status]} | ${(i.refs || []).join(', ')} |`);
    }
    L.push('');
  }

  L.push('## Détail des éléments', '');
  for (const i of items) {
    L.push(`<details><summary><b>${i.id}</b> — ${i.title}</summary>`, '');
    if (i.story) L.push(`_${i.story}_`, '');
    L.push(`**Épic :** ${i.epic} · **Sprint :** ${i.sprint ?? '—'} · **Points :** ${i.points ?? '—'} · **Responsable(s) :** ${owners(data, i)}${(i.grading || []).length ? ` · **Grille :** ${i.grading.join(', ')}` : ''}`, '');
    if ((i.acceptance || []).length) {
      L.push('**Critères d’acceptation**', '');
      for (const c of i.acceptance) L.push(`- [${i.status === 'done' ? 'x' : ' '}] ${c}`);
      L.push('');
    }
    if ((i.depends_on || []).length) L.push(`**Dépend de :** ${i.depends_on.join(', ')}`, '');
    if (i.branch || (i.commits || []).length) L.push(`**Réalisation :** ${i.branch ? `branche \`${i.branch}\`` : ''}${i.branch && (i.commits || []).length ? ' · ' : ''}${(i.commits || []).map((c) => (data.project.repo ? `[\`${c}\`](https://github.com/${data.project.repo}/commit/${c})` : `\`${c}\``)).join(', ')}`, '');
    L.push('</details>', '');
  }

  L.push('## Définition de « Terminé » (DoD)', '');
  L.push('- Code relu et intégré dans `develop` via Pull Request approuvée par au moins un autre membre.');
  L.push('- Tests du composant passants (`mvn test`) et critères d’acceptation satisfaits.');
  L.push('- Contrats (OpenAPI, .proto, XSD/WSDL) et documentation à jour.', '');
  return L.join('\n');
}

// ---------------------------------------------------------------- Sprint Backlog

function renderSprint(data, s) {
  const { enums } = data;
  const its = data.items.filter((i) => i.sprint === s.id);
  const sum = (arr) => arr.reduce((t, i) => t + (i.points || 0), 0);
  const done = its.filter((i) => i.status === 'done');
  const L = [];
  L.push(`# Sprint Backlog — ${s.title}`, '');
  L.push(`> Fichier **généré** depuis [../backlog.json](../backlog.json) par \`node scripts/backlog/backlog.mjs render\`.`);
  L.push(`> Milestone GitHub : « ${s.title} ».`, '');
  L.push(`**Période :** ${s.start} → ${s.end}  `, `**Objectif du sprint :** ${s.goal}`, '');
  L.push(`**Avancement :** ${done.length}/${its.length} éléments · ${sum(done)}/${sum(its)} points (${Math.round((100 * sum(done)) / Math.max(1, sum(its)))} %)`, '');

  L.push('## Éléments engagés', '');
  if (!its.length) L.push('_Aucun élément planifié pour ce sprint._', '');
  else {
    L.push('| ID | Élément | Épic | Priorité | Pts | Responsable(s) | Statut |', '|---|---|---|---|---:|---|---|');
    for (const i of its) L.push(`| ${i.id} | ${i.title} | ${i.epic} | ${enums.priority[i.priority]} | ${i.points ?? '—'} | ${owners(data, i)} | ${STATUS_ICON[i.status] ?? ''} ${enums.status[i.status]} |`);
    L.push('');
  }

  L.push('## Répartition par membre', '');
  L.push('| Membre | Éléments | Points | Terminés |', '|---|---|---:|---:|');
  for (const m of data.members) {
    const mine = its.filter((i) => (i.assignees || []).includes(m.id));
    L.push(`| ${m.name} | ${mine.map((i) => i.id).join(', ') || '—'} | ${sum(mine)} | ${mine.filter((i) => i.status === 'done').length}/${mine.length} |`);
  }
  L.push('');

  L.push('## Tableau', '');
  for (const st of Object.keys(enums.status)) {
    const col = its.filter((i) => i.status === st);
    if (col.length) L.push(`- **${STATUS_ICON[st] ?? ''} ${enums.status[st]}** : ${col.map((i) => `${i.id} ${i.title}`).join(' · ')}`);
  }
  L.push('');

  L.push('## Revue et rétrospective', '');
  L.push(s.review ? s.review : '_À compléter en fin de sprint dans le champ `review` du sprint (backlog.json)._', '');
  return L.join('\n');
}

// ---------------------------------------------------------------- ajout

function nextId(data, prefix) {
  const nums = data.items.filter((i) => i.id.startsWith(prefix + '-')).map((i) => parseInt(i.id.split('-')[1], 10));
  return `${prefix}-${String((nums.length ? Math.max(...nums) : 0) + 1).padStart(3, '0')}`;
}

function cmdAdd(args) {
  const data = load();
  if (!args.title || !args.epic) die('usage : add --title "..." --epic EP-03 [--type story|task|bug|spike] [--priority must|should|could|wont] [--points 3] [--sprint S3] [--component asset] [--assignee yessine,omar] [--status todo] [--story "En tant que ..."] [--ac "critère 1" --ac "critère 2"] [--depends US-008] [--ref B04] [--grading C2,C7]');
  const epic = data.epics.find((e) => e.id === args.epic);
  const type = args.type || 'story';
  const item = {
    id: args.id || nextId(data, type === 'bug' ? 'BUG' : 'US'),
    epic: args.epic,
    type,
    priority: args.priority || 'should',
    points: args.points !== undefined ? parseInt(args.points, 10) : null,
    sprint: args.sprint || null,
    component: args.component || epic?.component,
    assignees: list(args.assignee),
    status: args.status || (list(args.assignee).length ? 'todo' : 'backlog'),
    refs: list(args.ref ?? args.refs),
    depends_on: list(args.depends),
    grading: list(args.grading),
    title: args.title,
    story: args.story || '',
    acceptance: [].concat(args.ac || []),
  };
  data.items.push(item);
  validateOrDie(data);
  save(data);
  console.log(`✔ ${item.id} ajouté : ${item.title}`);
  console.log('  Étapes suivantes : node scripts/backlog/backlog.mjs render && node scripts/backlog/backlog.mjs sync');
}

// Modifie un ou plusieurs éléments existants sans ouvrir le JSON.
function cmdSet(args) {
  const data = load();
  const ids = args._.slice(1);
  if (!ids.length) die('usage : set US-031 [US-032 ...] [--status done] [--assignee a,b | --add-assignee a] [--sprint S4] [--priority must] [--points 5] [--title "..."] [--story "..."] [--ac "critère ajouté"] [--branch feature/x] [--commit sha] [--grading C6] [--depends US-008]');
  const scalar = { status: 'status', sprint: 'sprint', priority: 'priority', title: 'title', story: 'story', component: 'component', epic: 'epic', type: 'type', branch: 'branch' };
  for (const id of ids) {
    const it = data.items.find((i) => i.id === id);
    if (!it) die(`élément inconnu : ${id}`);
    for (const [opt, key] of Object.entries(scalar)) if (typeof args[opt] === 'string') it[key] = args[opt];
    if (args.points !== undefined) it.points = parseInt(args.points, 10);
    if (args.assignee !== undefined) it.assignees = list(args.assignee);
    for (const a of list(args['add-assignee'])) if (!(it.assignees ||= []).includes(a)) it.assignees.push(a);
    if (args.ac !== undefined) it.acceptance = [...(it.acceptance || []), ...[].concat(args.ac)];
    if (args.grading !== undefined) it.grading = list(args.grading);
    if (args.depends !== undefined) it.depends_on = list(args.depends);
    for (const c of list(args.commit)) if (!(it.commits ||= []).includes(c)) it.commits.push(c);
    console.log(`✔ ${id} mis à jour (${E(data, it)})`);
  }
  validateOrDie(data);
  save(data);
  renderAll(data);
}
const E = (data, it) => `${data.enums.status[it.status]}, sprint ${it.sprint ?? '—'}, ${(it.assignees || []).join(', ') || 'non affecté'}`;

function renderAll(d) {
  writeFileSync(MARKDOWN, render(d), 'utf8');
  mkdirSync(SPRINTS_DIR, { recursive: true });
  for (const s of d.sprints) writeFileSync(join(SPRINTS_DIR, `${s.id}.md`), renderSprint(d, s), 'utf8');
  console.log('✔ PRODUCT_BACKLOG.md et sprints/*.md régénérés');
}

function cmdAddMember(args) {
  const data = load();
  if (!args.id || !args.name) die('usage : add-member --id prenom --name "Prénom Nom" [--github login] [--role "..."]');
  if (data.members.some((m) => m.id === args.id)) die(`le membre "${args.id}" existe déjà`);
  data.members.push({ id: args.id, name: args.name, github: args.github || '', role: args.role || '' });
  validateOrDie(data);
  save(data);
  console.log(`✔ membre ${args.id} ajouté. Pensez à l'inviter comme collaborateur du dépôt pour pouvoir lui assigner des Issues.`);
}

// ---------------------------------------------------------------- synchronisation GitHub

function makeGh(dryRun) {
  const run = (args, { mutate = true } = {}) => {
    if (dryRun && mutate) { console.log(`  [dry-run] gh ${args.map((a) => (/\s/.test(a) ? JSON.stringify(a) : a)).join(' ')}`); return ''; }
    return execFileSync('gh', args, { encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }).trim();
  };
  return run;
}

function issueBody(data, it, numbers) {
  const { enums } = data;
  const ref = (id) => (numbers[id] ? `${id} (#${numbers[id]})` : id);
  const B = [];
  B.push(`**${enums.type[it.type]}** · Épic ${ref(it.epic)} · Sprint ${it.sprint ?? '—'} · ${it.points ?? '—'} pts · ${enums.priority[it.priority]}`);
  B.push(`Responsable(s) : ${owners(data, it)}${(it.refs || []).length ? ` · Réf. planification : ${it.refs.join(', ')}` : ''}`, '');
  if (it.story) B.push('### User story', '', it.story, '');
  if ((it.acceptance || []).length) {
    B.push('### Critères d’acceptation', '');
    for (const c of it.acceptance) B.push(`- [${it.status === 'done' ? 'x' : ' '}] ${c}`);
    B.push('');
  }
  if ((it.depends_on || []).length) B.push('### Dépendances', '', ...it.depends_on.map((d) => `- ${ref(d)}`), '');
  if (it.branch || (it.commits || []).length) B.push('### Réalisation', '', ...(it.branch ? [`- Branche : \`${it.branch}\``] : []), ...(it.commits || []).map((c) => `- Commit ${c}`), '');
  B.push(`<!-- ${MARKER}: ${it.id} — généré depuis docs/backlog/backlog.json ; modifier ce fichier plutôt que l'Issue -->`);
  return B.join('\n');
}

function epicBody(data, e, numbers) {
  const children = data.items.filter((i) => i.epic === e.id);
  const B = [`**Épic** · ${data.enums.component[e.component].label} · ${data.enums.priority[e.priority]}`, '', e.description, '', '### Éléments', ''];
  for (const c of children) B.push(`- [${c.status === 'done' ? 'x' : ' '}] ${numbers[c.id] ? `#${numbers[c.id]}` : c.id} ${c.id} — ${c.title}`);
  B.push('', `<!-- ${MARKER}: ${e.id} — généré depuis docs/backlog/backlog.json -->`);
  return B.join('\n');
}

function cmdSync(args) {
  const data = load();
  validateOrDie(data);
  const dryRun = !!args['dry-run'];
  const gh = makeGh(dryRun);
  const repo = args.repo || gh(['repo', 'view', '--json', 'nameWithOwner', '-q', '.nameWithOwner'], { mutate: false });
  const R = ['-R', repo];
  const tmp = mkdtempSync(join(tmpdir(), 'backlog-'));
  const bodyFile = (text, name) => { const f = join(tmp, name); writeFileSync(f, text, 'utf8'); return f; };
  console.log(`→ Dépôt ${repo}${dryRun ? ' (dry-run : aucune modification)' : ''}`);

  try {
    // 1. Labels
    console.log('→ Labels');
    const labels = [
      ...Object.entries(data.enums.type).map(([k, v]) => [`type:${k}`, k === 'bug' ? 'd73a4a' : k === 'epic' ? '3e1f8c' : 'c5def5', v]),
      ...Object.entries(data.enums.priority).map(([k, v]) => [`priority:${k}`, { must: 'b60205', should: 'fbca04', could: '0e8a16', wont: 'cccccc' }[k] || 'ededed', v]),
      ...Object.entries(data.enums.component).map(([k, v]) => [`component:${k}`, v.color, v.label]),
      ...Object.entries(data.enums.status).filter(([k]) => k !== 'done').map(([k, v]) => [`status:${k}`, 'ededed', v]),
      ...(data.grading || []).map((g) => [`grading:${g.id}`, 'f9d0c4', `${g.title} (${g.points} pts)`]),
    ];
    for (const [name, color, desc] of labels) gh(['label', 'create', name, '--color', color, '--description', desc, '--force', ...R]);

    // 2. Milestones (un par sprint)
    console.log('→ Milestones');
    const existingMs = JSON.parse(gh(['api', `repos/${repo}/milestones?state=all&per_page=100`], { mutate: false }) || '[]');
    for (const s of data.sprints) {
      const due = `${s.end}T23:59:59Z`;
      const ms = existingMs.find((m) => m.title === s.title);
      const fields = ['-f', `title=${s.title}`, '-f', `description=${s.goal}`, '-f', `due_on=${due}`];
      if (!ms) gh(['api', '-X', 'POST', `repos/${repo}/milestones`, ...fields, '-f', 'state=open', '--silent']);
      // Les milestones ne sont jamais fermés automatiquement : à clôturer lors de la revue de sprint.
      else if (ms.description !== s.goal || !ms.due_on?.startsWith(s.end)) gh(['api', '-X', 'PATCH', `repos/${repo}/milestones/${ms.number}`, ...fields, '--silent']);
    }

    // 3. Issues existantes (repérées par le marqueur backlog-id)
    const existing = JSON.parse(gh(['issue', 'list', ...R, '--state', 'all', '--limit', '1000', '--json', 'number,title,body,state,labels,milestone,assignees'], { mutate: false }) || '[]');
    const byId = {};
    for (const is of existing) {
      const m = (is.body || '').match(new RegExp(`<!-- ${MARKER}: ([A-Z]+-\\d+)`)) || is.title.match(/^\[([A-Z]+-\d+)\]/);
      if (m) byId[m[1]] = is;
    }
    const numbers = Object.fromEntries(Object.entries(byId).map(([k, v]) => [k, v.number]));
    const logins = Object.fromEntries(data.members.map((m) => [m.id, m.github]));
    const sprintTitle = Object.fromEntries(data.sprints.map((s) => [s.id, s.title]));

    const entries = [
      ...data.items.map((it) => ({ id: it.id, it, labels: [`type:${it.type}`, `priority:${it.priority}`, `component:${it.component}`, ...(it.grading || []).map((g) => `grading:${g}`), ...(it.status !== 'done' ? [`status:${it.status}`] : [])] })),
      ...data.epics.map((e) => ({ id: e.id, epic: e, labels: ['type:epic', `priority:${e.priority}`, `component:${e.component}`] })),
    ];

    // Deux passes : 1) création de toutes les Issues, 2) mise à jour des corps avec les liens #numéro.
    for (const pass of [1, 2]) {
      console.log(`→ Issues (passe ${pass}/2)`);
      for (const en of entries) {
        const src = en.it || en.epic;
        const title = `[${en.id}] ${src.title}`;
        const body = en.it ? issueBody(data, en.it, numbers) : epicBody(data, en.epic, numbers);
        const assignees = en.it ? (en.it.assignees || []).map((a) => logins[a]).filter(Boolean) : [];
        const milestone = en.it?.sprint ? sprintTitle[en.it.sprint] : null;
        const status = en.it ? en.it.status : (data.items.filter((i) => i.epic === en.id).every((i) => i.status === 'done') && data.items.some((i) => i.epic === en.id) ? 'done' : 'open');
        const cur = byId[en.id];

        if (!cur) {
          if (pass === 2) continue;
          const out = gh(['issue', 'create', ...R, '--title', title, '--body-file', bodyFile(body, `${en.id}.md`),
            ...en.labels.flatMap((l) => ['--label', l]), ...assignees.flatMap((a) => ['--assignee', a]),
            ...(milestone ? ['--milestone', milestone] : [])]);
          const num = dryRun ? null : parseInt(out.split('/').pop(), 10);
          if (num) { numbers[en.id] = num; byId[en.id] = { number: num, title, body, state: 'OPEN', labels: en.labels.map((name) => ({ name })), milestone: milestone ? { title: milestone } : null, assignees: assignees.map((login) => ({ login })) }; }
          console.log(`  + ${title}${num ? ` (#${num})` : ''}`);
          if (status === 'done' && num) { gh(['issue', 'close', String(num), ...R, '--reason', 'completed']); byId[en.id].state = 'CLOSED'; }
          continue;
        }

        const curLabels = cur.labels.map((l) => l.name);
        const addL = en.labels.filter((l) => !curLabels.includes(l));
        const rmL = curLabels.filter((l) => MANAGED_PREFIXES.some((p) => l.startsWith(p)) && !en.labels.includes(l));
        const curAss = (cur.assignees || []).map((a) => a.login);
        const addA = assignees.filter((a) => !curAss.includes(a));
        const edit = [];
        if (cur.title !== title) edit.push('--title', title);
        if ((cur.body || '').trim() !== body.trim()) edit.push('--body-file', bodyFile(body, `${en.id}.md`));
        if (addL.length) edit.push('--add-label', addL.join(','));
        if (rmL.length) edit.push('--remove-label', rmL.join(','));
        if (addA.length) edit.push('--add-assignee', addA.join(','));
        if (milestone && cur.milestone?.title !== milestone) edit.push('--milestone', milestone);
        if (edit.length) {
          gh(['issue', 'edit', String(cur.number), ...R, ...edit]);
          console.log(`  ~ #${cur.number} ${title}`);
          Object.assign(cur, { title, body, labels: en.labels.map((name) => ({ name })) });
        }
        if (status === 'done' && cur.state === 'OPEN') { gh(['issue', 'close', String(cur.number), ...R, '--reason', 'completed']); cur.state = 'CLOSED'; console.log(`  ✓ #${cur.number} fermée`); }
        if (status !== 'done' && cur.state === 'CLOSED') { gh(['issue', 'reopen', String(cur.number), ...R]); cur.state = 'OPEN'; console.log(`  ↺ #${cur.number} rouverte`); }
      }
    }

    // 4. Ajout optionnel au tableau GitHub Projects
    if (args.project) {
      const owner = args.owner || repo.split('/')[0];
      console.log(`→ GitHub Projects #${args.project} (${owner})`);
      for (const en of entries) {
        const num = numbers[en.id];
        if (num) gh(['project', 'item-add', String(args.project), '--owner', owner, '--url', `https://github.com/${repo}/issues/${num}`]);
      }
    }
    console.log('✔ Synchronisation terminée');
  } finally {
    rmSync(tmp, { recursive: true, force: true });
  }
}

// ---------------------------------------------------------------- main

const args = parseArgs(process.argv.slice(2));
const cmd = args._[0];
switch (cmd) {
  case 'validate': { const e = validate(load()); if (e.length) die(`backlog.json invalide :\n  - ${e.join('\n  - ')}`); console.log('✔ backlog.json valide'); break; }
  case 'render': { const d = load(); validateOrDie(d); renderAll(d); break; }
  case 'add': cmdAdd(args); renderAll(load()); break;
  case 'set': cmdSet(args); break;
  case 'add-member': cmdAddMember(args); renderAll(load()); break;
  case 'sync': cmdSync(args); break;
  default:
    console.log('Commandes : validate | render | add | set | add-member | sync [--dry-run] [--repo owner/name] [--project N --owner LOGIN]');
    process.exit(cmd ? 1 : 0);
}
