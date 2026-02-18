package org.joinmastodon.activitypub.model;

/**
 * Enum representing ActivityPub activity types.
 * @see <a href="https://www.w3.org/TR/activitystreams-vocabulary/#activity-types">Activity Streams Vocabulary</a>
 */
public enum ActivityType {
    // Content activities
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),

    // Relationship activities
    FOLLOW("Follow"),
    ACCEPT("Accept"),
    REJECT("Reject"),
    BLOCK("Block"),
    UNBLOCK("Unblock"),

    // Reaction activities
    LIKE("Like"),
    ANNOUNCE("Announce"),

    // Undo/Redo
    UNDO("Undo"),

    // Other activities
    ADD("Add"),
    REMOVE("Remove"),
    LIKE_UNDO("Like"),  // Undo of Like
    ANNOUNCE_UNDO("Announce"),  // Undo of Announce
    FOLLOW_UNDO("Follow"),  // Undo of Follow

    // Object types that can appear as activities
    NOTE("Note"),
    PERSON("Person"),
    SERVICE("Service"),
    GROUP("Group"),
    ORGANIZATION("Organization"),
    APPLICATION("Application"),
    TOMBSTONE("Tombstone");

    private final String typeName;

    ActivityType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    /**
     * Parse an activity type string to enum.
     * @param type the type string from JSON
     * @return the matching ActivityType, or null if not found
     */
    public static ActivityType fromString(String type) {
        if (type == null) {
            return null;
        }
        for (ActivityType activityType : values()) {
            if (activityType.typeName.equalsIgnoreCase(type)) {
                return activityType;
            }
        }
        return null;
    }
}