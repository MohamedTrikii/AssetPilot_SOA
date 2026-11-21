package com.assetpilot.maintenance.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InterventionStatusTest {

    @Test
    void allowedTransitions() {
        assertThat(InterventionStatus.SCHEDULED.canTransitionTo(InterventionStatus.IN_PROGRESS)).isTrue();
        assertThat(InterventionStatus.SCHEDULED.canTransitionTo(InterventionStatus.CANCELLED)).isTrue();
        assertThat(InterventionStatus.IN_PROGRESS.canTransitionTo(InterventionStatus.COMPLETED)).isTrue();
    }

    @Test
    void forbiddenTransitions() {
        assertThat(InterventionStatus.SCHEDULED.canTransitionTo(InterventionStatus.COMPLETED)).isFalse();
        assertThat(InterventionStatus.COMPLETED.canTransitionTo(InterventionStatus.IN_PROGRESS)).isFalse();
        assertThat(InterventionStatus.CANCELLED.allowedTransitions()).isEmpty();
    }

    @Test
    void terminalStatuses() {
        assertThat(InterventionStatus.COMPLETED.isTerminal()).isTrue();
        assertThat(InterventionStatus.CANCELLED.isTerminal()).isTrue();
        assertThat(InterventionStatus.IN_PROGRESS.isTerminal()).isFalse();
    }
}
