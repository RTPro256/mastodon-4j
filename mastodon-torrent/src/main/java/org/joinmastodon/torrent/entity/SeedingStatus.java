package org.joinmastodon.torrent.entity;

/**
 * Seeding status for a shared torrent.
 */
public enum SeedingStatus {
    /**
     * Torrent is actively seeding.
     */
    ACTIVE,

    /**
     * Torrent seeding is paused.
     */
    PAUSED,

    /**
     * Torrent seeding has stopped (completed ratio/time limit).
     */
    STOPPED,

    /**
     * Torrent has an error.
     */
    ERROR,

    /**
     * Torrent is being initialized.
     */
    INITIALIZING
}
