/**
 * Compose component for creating new statuses
 */

import React, { useState, useCallback, useRef, useEffect } from 'react';
import type { StatusCreateRequest, Visibility, MediaAttachment } from '@/types';
import { statusesApi, mediaApi } from '@/api';
import { useAuth } from '@/stores';
import { Avatar } from '../Avatar';
import styles from './Compose.module.css';

interface ComposeProps {
  onPosted?: (status: import('@/types').Status) => void;
  replyTo?: import('@/types').Status;
  onClose?: () => void;
  autoFocus?: boolean;
}

const MAX_CHARS = 500;

export function Compose({ onPosted, replyTo, onClose, autoFocus = false }: ComposeProps) {
  const { user, instance } = useAuth();
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [text, setText] = useState(replyTo ? `@${replyTo.account.acct} ` : '');
  const [spoilerText, setSpoilerText] = useState('');
  const [showSpoiler, setShowSpoiler] = useState(false);
  const [visibility, setVisibility] = useState<Visibility>(replyTo?.visibility || 'public');
  const [sensitive, setSensitive] = useState(false);
  const [media, setMedia] = useState<MediaAttachment[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const maxChars = instance?.configuration?.statuses?.max_characters || MAX_CHARS;
  const charsRemaining = maxChars - text.length - spoilerText.length;

  useEffect(() => {
    if (autoFocus && textareaRef.current) {
      textareaRef.current.focus();
    }
  }, [autoFocus]);

  const handleSubmit = useCallback(async () => {
    if (!text.trim() && media.length === 0) return;
    if (isSubmitting) return;

    setIsSubmitting(true);
    setError(null);

    try {
      const request: StatusCreateRequest = {
        status: text,
        visibility,
        sensitive,
        spoiler_text: showSpoiler ? spoilerText : undefined,
        media_ids: media.map(m => m.id),
        in_reply_to_id: replyTo?.id,
      };

      const status = await statusesApi.create(request);
      setText('');
      setSpoilerText('');
      setMedia([]);
      setSensitive(false);
      onPosted?.(status);
      onClose?.();
    } catch (err) {
      console.error('Failed to post status:', err);
      setError(err instanceof Error ? err.message : 'Failed to post');
    } finally {
      setIsSubmitting(false);
    }
  }, [text, visibility, sensitive, showSpoiler, spoilerText, media, replyTo, isSubmitting, onPosted, onClose]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      handleSubmit();
    }
  }, [handleSubmit]);

  const handleFileSelect = useCallback(async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    for (const file of Array.from(files)) {
      if (media.length >= 4) break; // Max 4 attachments

      try {
        const attachment = await mediaApi.upload(file);
        setMedia(prev => [...prev, attachment]);
      } catch (err) {
        console.error('Failed to upload media:', err);
        setError('Failed to upload media');
      }
    }

    // Reset file input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  }, [media.length]);

  const removeMedia = useCallback((id: string) => {
    setMedia(prev => prev.filter(m => m.id !== id));
  }, []);

  const visibilityOptions: Array<{ value: Visibility; label: string; icon: string }> = [
    { value: 'public', label: 'Public', icon: '🌐' },
    { value: 'unlisted', label: 'Unlisted', icon: '🔓' },
    { value: 'private', label: 'Followers only', icon: '🔒' },
    { value: 'direct', label: 'Mentioned people only', icon: '✉️' },
  ];

  return (
    <div className={styles.compose}>
      <div className={styles.header}>
        {user && <Avatar account={user} size={40} />}
        <div className={styles.options}>
          <select
            value={visibility}
            onChange={(e) => setVisibility(e.target.value as Visibility)}
            className={styles.visibilitySelect}
          >
            {visibilityOptions.map(opt => (
              <option key={opt.value} value={opt.value}>
                {opt.icon} {opt.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {showSpoiler && (
        <input
          type="text"
          placeholder="Content warning"
          value={spoilerText}
          onChange={(e) => setSpoilerText(e.target.value)}
          className={styles.spoilerInput}
          maxLength={200}
        />
      )}

      <textarea
        ref={textareaRef}
        value={text}
        onChange={(e) => setText(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={replyTo ? 'Reply...' : "What's on your mind?"}
        className={styles.textarea}
        maxLength={maxChars}
      />

      {media.length > 0 && (
        <div className={styles.mediaPreview}>
          {media.map(m => (
            <div key={m.id} className={styles.mediaItem}>
              {m.type === 'image' ? (
                <img src={m.preview_url || undefined} alt={m.description || ''} />
              ) : (
                <video src={m.url || undefined} />
              )}
              <button
                className={styles.removeMedia}
                onClick={() => removeMedia(m.id)}
                type="button"
              >
                ×
              </button>
            </div>
          ))}
        </div>
      )}

      {error && (
        <div className={styles.error}>{error}</div>
      )}

      <div className={styles.footer}>
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.actionButton}
            onClick={() => fileInputRef.current?.click()}
            title="Add media"
          >
            📎
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*,video/*,audio/*"
            multiple
            onChange={handleFileSelect}
            className={styles.fileInput}
          />

          <button
            type="button"
            className={`${styles.actionButton} ${showSpoiler ? styles.active : ''}`}
            onClick={() => setShowSpoiler(!showSpoiler)}
            title="Content warning"
          >
            ⚠️
          </button>

          {media.length > 0 && (
            <button
              type="button"
              className={`${styles.actionButton} ${sensitive ? styles.active : ''}`}
              onClick={() => setSensitive(!sensitive)}
              title="Mark media as sensitive"
            >
              👁️
            </button>
          )}
        </div>

        <div className={styles.submitArea}>
          <span className={`${styles.charCount} ${charsRemaining < 0 ? styles.overLimit : ''}`}>
            {charsRemaining}
          </span>
          <button
            type="button"
            className={styles.submitButton}
            onClick={handleSubmit}
            disabled={isSubmitting || charsRemaining < 0 || (!text.trim() && media.length === 0)}
          >
            {isSubmitting ? (
              <span className={styles.spinner} />
            ) : replyTo ? (
              'Reply'
            ) : (
              'Post'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
