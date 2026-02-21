package org.joinmastodon.torrent.magnet;

import org.joinmastodon.torrent.torrentfile.TorrentFileInfo;

import java.util.List;

/**
 * Builds magnet links from torrent file information.
 */
public class MagnetLinkBuilder {

    /**
     * Build a magnet link from torrent file info.
     *
     * @param torrentInfo the torrent file info
     * @return the magnet link
     */
    public MagnetLink build(TorrentFileInfo torrentInfo) {
        MagnetLink magnet = new MagnetLink(torrentInfo.getInfoHash());
        
        // Set display name
        if (torrentInfo.getName() != null) {
            magnet.displayName(torrentInfo.getName());
        }
        
        // Set exact length
        if (torrentInfo.getTotalSize() > 0) {
            magnet.exactLength(torrentInfo.getTotalSize());
        }
        
        // Add trackers
        for (String tracker : torrentInfo.getTrackers()) {
            magnet.tracker(tracker);
        }
        
        return magnet;
    }

    /**
     * Build a magnet link from torrent file info with additional trackers.
     *
     * @param torrentInfo       the torrent file info
     * @param additionalTrackers additional tracker URLs
     * @return the magnet link
     */
    public MagnetLink build(TorrentFileInfo torrentInfo, List<String> additionalTrackers) {
        MagnetLink magnet = build(torrentInfo);
        
        // Add additional trackers
        for (String tracker : additionalTrackers) {
            magnet.tracker(tracker);
        }
        
        return magnet;
    }

    /**
     * Build a magnet link from an infohash.
     *
     * @param infoHash the infohash
     * @return the magnet link
     */
    public MagnetLink buildFromInfoHash(String infoHash) {
        return new MagnetLink(infoHash);
    }

    /**
     * Build a magnet link from an infohash with display name.
     *
     * @param infoHash    the infohash
     * @param displayName the display name
     * @return the magnet link
     */
    public MagnetLink buildFromInfoHash(String infoHash, String displayName) {
        return new MagnetLink(infoHash).displayName(displayName);
    }

    /**
     * Build a magnet link from an infohash with full details.
     *
     * @param infoHash    the infohash
     * @param displayName the display name
     * @param trackers    the tracker URLs
     * @param size        the total size in bytes
     * @return the magnet link
     */
    public MagnetLink buildFromInfoHash(String infoHash, String displayName, 
                                        List<String> trackers, long size) {
        MagnetLink magnet = new MagnetLink(infoHash);
        
        if (displayName != null) {
            magnet.displayName(displayName);
        }
        
        if (trackers != null) {
            magnet.trackers(trackers);
        }
        
        if (size > 0) {
            magnet.exactLength(size);
        }
        
        return magnet;
    }

    /**
     * Build a magnet URI string from torrent file info.
     *
     * @param torrentInfo the torrent file info
     * @return the magnet URI string
     */
    public String buildUri(TorrentFileInfo torrentInfo) {
        return build(torrentInfo).toUri();
    }

    /**
     * Build a magnet URI string from an infohash.
     *
     * @param infoHash the infohash
     * @return the magnet URI string
     */
    public String buildUriFromInfoHash(String infoHash) {
        return buildFromInfoHash(infoHash).toUri();
    }
}
