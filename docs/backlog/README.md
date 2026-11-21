# Backlog AssetPilot — mode d'emploi

| Fichier | Rôle |
|---|---|
| [backlog.json](backlog.json) | **Source unique de vérité** : membres, sprints, épics, éléments. C'est le seul fichier à modifier. |
| [PRODUCT_BACKLOG.md](PRODUCT_BACKLOG.md) | Product Backlog complet (généré). |
| [sprints/S0.md … S5.md](sprints/) | Sprint Backlog de chaque sprint : engagement, répartition par membre, tableau, rétrospective (générés). |
| [../../scripts/backlog/backlog.mjs](../../scripts/backlog/backlog.mjs) | Outil : `validate`, `render`, `add`, `add-member`, `sync`. |

Prérequis : Node 18+ et, pour `sync`, la [CLI GitHub](https://cli.github.com/) connectée (`gh auth login`).

## Ajouter un élément

```bash
node scripts/backlog/backlog.mjs add --epic EP-03 --type story --priority should --points 3 --sprint S3 \
  --assignee yessine --title "Exporter l'inventaire en CSV" \
  --story "En tant que gestionnaire, je veux exporter l'inventaire afin de l'archiver." \
  --ac "Endpoint GET /api/v1/equipments/export" --ac "Fichier CSV UTF-8"
```

- L'ID est attribué automatiquement (`US-0xx`, ou `BUG-0xx` pour `--type bug`).
- `--assignee` accepte plusieurs membres (`yessine,mohamed`) ; le premier est le responsable principal.
- Autres options : `--component`, `--status`, `--depends US-008`, `--ref B04`, `--grading C2,C7` (critères de la grille).
- Vous pouvez aussi modifier `backlog.json` à la main : la commande `validate` contrôle les IDs, les épics, les membres, les sprints et les dépendances.

## Modifier un élément (statut, responsables, commits)

```bash
# Je commence une tâche
node scripts/backlog/backlog.mjs set US-031 --status in-progress --branch feature/resilience
# Je l'ai terminée et commitée : statut + traçabilité Git
node scripts/backlog/backlog.mjs set US-031 --status done --commit a1b2c3d
# Plusieurs éléments d'un coup, ajout d'un collaborateur
node scripts/backlog/backlog.mjs set US-025 US-026 --add-assignee omar --sprint S4
```

Options : `--status`, `--assignee a,b` (remplace), `--add-assignee a`, `--sprint`, `--priority`, `--points`, `--title`, `--story`, `--ac` (ajoute un critère), `--branch`, `--commit` (ajoute un SHA), `--grading`, `--depends`.
`set`, `add` et `add-member` régénèrent automatiquement `PRODUCT_BACKLOG.md` et `sprints/*.md` : il ne reste qu'à commiter.
Les champs `branch` et `commits` relient chaque tâche au travail Git de son auteur (utile pour la note individuelle).

## Ajouter un membre, un sprint ou un épic

```bash
node scripts/backlog/backlog.mjs add-member --id sami --name "Sami Ben Ali" --github sami-gh --role "Front-end"
```

Le membre doit aussi être invité comme collaborateur du dépôt (Settings → Collaborators) pour pouvoir recevoir des Issues.
Pour un **sprint** (`S6`…), un **épic** (`EP-11`…) ou une nouvelle valeur de `component`, `status` ou `type`, ajoutez une entrée dans la section correspondante de `backlog.json` : labels et milestones sont créés au prochain `sync`.

## Publier sur GitHub

```bash
node scripts/backlog/backlog.mjs validate
node scripts/backlog/backlog.mjs render
node scripts/backlog/backlog.mjs sync --dry-run      # affiche ce qui serait fait
node scripts/backlog/backlog.mjs sync                # labels + milestones (sprints) + Issues
node scripts/backlog/backlog.mjs sync --project 1    # ajoute aussi les Issues au tableau GitHub Projects n°1
```

`sync` est **idempotent** : chaque Issue porte un marqueur `backlog-id` et est mise à jour (titre, corps, labels, assignés, milestone) au lieu d'être recréée. Un élément `done` ferme son Issue ; le repasser à un autre statut la rouvre.
Les commentaires et discussions sur les Issues ne sont pas touchés. En revanche, ne modifiez pas le corps d'une Issue à la main : il est régénéré à partir de `backlog.json`.

Correspondance avec GitHub :

| backlog.json | GitHub |
|---|---|
| `sprints[]` | Milestones (date d'échéance = fin du sprint) |
| `epics[]` | Issues `type:epic` avec la liste à cocher de leurs éléments |
| `items[]` | Issues (`type:*`, `priority:*`, `component:*`, `status:*`) |
| `assignees` + `members[].github` | Assignés de l'Issue |
| `grading` (C1…C8, BONUS) | Labels `grading:*` — critères de la grille d'évaluation de l'énoncé |
| `status: done` | Issue fermée |

## Cycle de vie d'un élément

`backlog` → `todo` (planifié dans un sprint) → `in-progress` → `in-review` (PR ouverte) → `done` (PR fusionnée dans `develop`). `blocked` peut être utilisé à tout moment.
À chaque changement : modifier `status` dans `backlog.json`, lancer `render`, commiter, puis lancer `sync`. La CI ([.github/workflows/backlog.yml](../../.github/workflows/backlog.yml)) refuse une PR si `backlog.json` est invalide ou si les fichiers Markdown n'ont pas été régénérés.

Pour le tableau GitHub Projects, activez les workflows intégrés « Item closed → Done » et « Pull request merged → Done ».
