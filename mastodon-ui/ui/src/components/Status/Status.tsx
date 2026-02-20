/**
 * Status component - displays a single status/toot
 */

import React, { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { formatDistanceToNow, parseISO } from 'date-fns';
import type { Status as StatusType, Visibility } from '@/types';
import { statusesApi } from '@/api';
import { Avatar } from '../Avatar';
import styles from './Status.module.css';

interface StatusProps {
  status: StatusType;
  onReply?: (status: StatusType) => void;
  onDelete?: (status: StatusType) => void;
  showActions?: boolean;
  compact?: boolean;
}

const visibilityIcons: Record<Visibility, string> = {
  public: '🌐',
  unlisted: '🔓',
  private: '🔒',
  direct: '✉️',
};

export function Status({ status, onReply, onDelete, showActions = true, compact = false }: StatusProps) {
  const [isFavourited, setIsFavourited] = useState(status.favourited);
  const [isReblogged, setIsReblogged] = useState(status.reblogged);
  const [isBookmarked, setIsBookmarked] = useState(status.bookmarked);
  const [favouritesCount, setFavouritesCount] = useState(status.favourites_count);
  const [reblogsCount, setReblogsCount] = useState(status.reblogs_count);
  const [isLoading, setIsLoading] = useState(false);
  const [showContent, setShowContent] = useState(!status.sensitive);

  const actualStatus = status.reblog || status;
  const account = actualStatus.account;

  const handleFavourite = useCallback(async () => {
    if (isLoading) return;
    setIsLoading(true);
    try {
      if (isFavourited) {
        await statusesApi.unfavourite(actualStatus.id);
        setFavouritesCount(c => c - 1);
      } else {
        await statusesApi.favourite(actualStatus.id);
        setFavouritesCount(c => c + 1);
      }
      setIsFavourited(!isFavourited);
    } catch (error) {
      console.error('Failed to toggle favourite:', error);
    } finally {
      setIsLoading(false);
    }
  }, [actualStatus.id, isFavourited, isLoading]);

  const handleReblog = useCallback(async () => {
    if (isLoading) return;
    setIsLoading(true);
    try {
      if (isReblogged) {
        await statusesApi.unreblog(actualStatus.id);
        setReblogsCount(c => c - 1);
      } else {
        await statusesApi.reblog(actualStatus.id);
        setReblogsCount(c => c + 1);
      }
      setIsReblogged(!isReblogged);
    } catch (error) {
      console.error('Failed to toggle reblog:', error);
    } finally {
      setIsLoading(false);
    }
  }, [actualStatus.id, isReblogged, isLoading]);

  const handleBookmark = useCallback(async () => {
    if (isLoading) return;
    setIsLoading(true);
    try {
      if (isBookmarked) {
        await statusesApi.unbookmark(actualStatus.id);
      } else {
        await statusesApi.bookmark(actualStatus.id);
      }
      setIsBookmarked(!isBookmarked);
    } catch (error) {
      console.error('Failed to toggle bookmark:', error);
    } finally {
      setIsLoading(false);
    }
  }, [actualStatus.id, isBookmarked, isLoading]);

  const handleReply = useCallback(() => {
    onReply?.(status);
  }, [status, onReply]);

  const handleDelete = useCallback(async () => {
    if (isLoading || !window.confirm('Delete this status?')) return;
    setIsLoading(true);
    try {
      await statusesApi.delete(status.id);
      onDelete?.(status);
    } catch (error) {
      console.error('Failed to delete status:', error);
    } finally {
      setIsLoading(false);
    }
  }, [status, onDelete, isLoading]);

  const toggleContentWarning = useCallback(() => {
    setShowContent(prev => !prev);
  }, []);

  const createdAt = parseISO(actualStatus.created_at);
  const relativeTime = formatDistanceToNow(createdAt, { addSuffix: true });

  return (
    <article className={`${styles.status} ${compact ? styles.compact : ''}`}>
      {status.reblog && (
        <div className={styles.boosted}>
          <span className={styles.boostIcon}>🔁</span>
          <Link to={`/@${status.account.acct}`}>
            {status.account.display_name || status.account.username}
          </Link>
          {' '}boosted
        </div>
      )}

      <div className={styles.header}>
        <Link to={`/@${account.acct}`}>
          <Avatar account={account} size={compact ? 32 : 48} />
        </Link>
        <div className={styles.accountInfo}>
          <div className={styles.names}>
            <Link to={`/@${account.acct}`} className={styles.displayName}>
              {account.display_name || account.username}
            </Link>
            <Link to={`/@${account.acct}`} className={styles.acct}>
              @{account.acct}
            </Link>
          </div>
          <div className={styles.meta}>
            <Link to={`/@${account.acct}/${actualStatus.id}`} className={styles.time}>
              {relativeTime}
            </Link>
            <span className={styles.visibility} title={actualStatus.visibility}>
              {visibilityIcons[actualStatus.visibility]}
            </span>
          </div>
        </div>
      </div>

      {actualStatus.spoiler_text && (
        <div className={styles.contentWarning}>
          <p>{actualStatus.spoiler_text}</p>
          <button onClick={toggleContentWarning} className={styles.toggleButton}>
            {showContent ? 'Show less' : 'Show more'}
          </button>
        </div>
      )}

      {showContent && (
        <>
          <div 
            className={styles.content}
            dangerouslySetInnerHTML={{ __html: actualStatus.content }}
          />

          {actualStatus.media_attachments.length > 0 && (
            <div className={styles.media}>
              {actualStatus.media_attachments.map(media => (
                <MediaAttachment key={media.id} media={media} sensitive={actualStatus.sensitive} />
              ))}
            </div>
          )}

          {actualStatus.poll && (
            <Poll poll={actualStatus.poll} />
          )}
        </>
      )}

      {showActions && (
        <div className={styles.actions}>
          <button 
            className={`${styles.action} ${styles.reply}`}
            onClick={handleReply}
            title="Reply"
          >
            <span className={styles.icon}>💬</span>
            <span className={styles.count}>{status.replies_count || ''}</span>
          </button>

          <button 
            className={`${styles.action} ${isReblogged ? styles.active : ''}`}
            onClick={handleReblog}
            disabled={isLoading || actualStatus.visibility === 'direct' || actualStatus.visibility === 'private'}
            title="Boost"
          >
            <span className={styles.icon}>🔁</span>
            <span className={styles.count}>{reblogsCount || ''}</span>
          </button>

          <button 
            className={`${styles.action} ${isFavourited ? styles.active : ''}`}
            onClick={handleFavourite}
            disabled={isLoading}
            title="Favourite"
          >
            <span className={styles.icon}>{isFavourited ? '⭐' : '☆'}</span>
            <span className={styles.count}>{favouritesCount || ''}</span>
          </button>

          <button 
            className={`${styles.action} ${isBookmarked ? styles.active : ''}`}
            onClick={handleBookmark}
            disabled={isLoading}
            title="Bookmark"
          >
            <span className={styles.icon}>{isBookmarked ? '🔖' : '📑'}</span>
          </button>

          <button 
            className={styles.action}
            title="More"
          >
            <span className={styles.icon}>⋯</span>
          </button>
        </div>
      )}
    </article>
  );
}

// Media attachment component
function MediaAttachment({ media, sensitive }: { media: import('@/types').MediaAttachment; sensitive: boolean }) {
  const [showMedia, setShowMedia] = useState(!sensitive);

  if (!showMedia) {
    return (
      <button 
        className={styles.mediaWarning}
        onClick={() => setShowMedia(true)}
      >
        <span className={styles.warningIcon}>⚠️</span>
        Click to show {media.type}
      </button>
    );
  }

  if (media.type === 'image') {
    return (
      <img
        src={media.preview_url || undefined}
        alt={media.description || ''}
        className={styles.mediaImage}
        loading="lazy"
      />
    );
  }

  if (media.type === 'video' || media.type === 'gifv') {
    return (
      <video
        src={media.url || undefined}
        poster={media.preview_url || undefined}
        className={styles.mediaVideo}
        controls={media.type === 'video'}
        autoPlay={media.type === 'gifv'}
        loop={media.type === 'gifv'}
        muted={media.type === 'gifv'}
      />
    );
  }

  if (media.type === 'audio') {
    return (
      <audio
        src={media.url || undefined}
        controls
        className={styles.mediaAudio}
      />
    );
  }

  return null;
}

// Poll component
function Poll({ poll }: { poll: import('@/types').Poll }) {
  const totalVotes = poll.votes_count;

  return (
    <div className={styles.poll}>
      {poll.options.map((option, index) => {
        const percentage = totalVotes > 0 ? Math.round((option.votes_count / totalVotes) * 100) : 0;
        
        return (
          <div key={index} className={styles.pollOption}>
            <div className={styles.pollLabel}>
              <span>{option.title}</span>
              <span>{percentage}%</span>
            </div>
            <div className={styles.pollBar}>
              <div 
                className={styles.pollBarFill}
                style={{ width: `${percentage}%` }}
              />
            </div>
          </div>
        );
      })}
      <div className={styles.pollMeta}>
        {totalVotes} votes
        {poll.expires_at && !poll.expired && (
          <> · ends {formatDistanceToNow(parseISO(poll.expires_at), { addSuffix: true })}</>
        )}
      </div>
    </div>
  );
}
