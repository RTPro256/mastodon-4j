package org.joinmastodon.torrent.api.dto;

import java.util.List;

/**
 * Request to create a torrent from a media attachment.
 */
public class CreateTorrentRequest {

    /**
     * The media attachment ID to create a torrent from.
     */
    private String mediaAttachmentId;

    /**
     * Additional tracker URLs to include.
     */
    private List<String> trackers;

    /**
     * Whether to include web seed URLs.
     */
    private boolean includeWebSeeds = true;

    /**
     * Custom comment for the torrent.
     */
    private String customComment;

    // Getters and Setters

    public String getMediaAttachmentId() {
        return mediaAttachmentId;
    }

    public void setMediaAttachmentId(String mediaAttachmentId) {
        this.mediaAttachmentId = mediaAttachmentId;
    }

    public List<String> getTrackers() {
        return trackers;
    }

    public void setTrackers(List<String> trackers) {
        this.trackers = trackers;
    }

    public boolean isIncludeWebSeeds() {
        return includeWebSeeds;
    }

    public void setIncludeWebSeeds(boolean includeWebSeeds) {
        this.includeWebSeeds = includeWebSeeds;
    }

    public String getCustomComment() {
        return customComment;
    }

    public void setCustomComment(String customComment) {
        this.customComment = customComment;
    }
}
