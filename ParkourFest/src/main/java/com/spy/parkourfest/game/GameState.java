package com.spy.parkourfest.game;

/**
 * Represents the current state of a parkour game session.
 */
public enum GameState {
    /** No game running — stage is idle */
    IDLE,
    /** Players are frozen, countdown timer is active */
    COUNTDOWN,
    /** Game is in progress — structures moving, players running */
    ACTIVE,
    /** Game just ended — cleanup in progress */
    ENDED
}
