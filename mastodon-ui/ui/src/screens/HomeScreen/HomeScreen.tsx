/**
 * Home screen - main timeline view
 */

import React, { useEffect, useCallback } from 'react';
import { useTimeline, useAuth } from '@/stores';
import { Status, Compose } from '@/components';
import styles from './HomeScreen.module.css';

export function HomeScreen() {
  const { isAuthenticated, isLoading: authLoading, login } = useAuth();
  const { 
    statuses, 
    isLoading, 
    isLoadingMore, 
    hasMore, 
    error, 
    loadTimeline, 
    loadMore,
    addStatus,
    refresh 
  } = useTimeline();

  useEffect(() => {
    if (isAuthenticated) {
      loadTimeline('home');
    }
  }, [isAuthenticated, loadTimeline]);

  const handleScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
    const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
    if (scrollHeight - scrollTop - clientHeight < 500 && hasMore && !isLoadingMore) {
      loadMore();
    }
  }, [hasMore, isLoadingMore, loadMore]);

  const handlePosted = useCallback((status: import('@/types').Status) => {
    addStatus(status);
  }, [addStatus]);

  if (authLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.spinner} />
        <p>Loading...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className={styles.loginPrompt}>
        <h1>Welcome to Mastodon</h1>
        <p>A decentralized social network</p>
        <button className={styles.loginButton} onClick={login}>
          Log in
        </button>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.content} onScroll={handleScroll}>
        <div className={styles.composeWrapper}>
          <Compose onPosted={handlePosted} />
        </div>

        {error && (
          <div className={styles.error}>
            <p>{error}</p>
            <button onClick={refresh}>Retry</button>
          </div>
        )}

        {isLoading && statuses.length === 0 ? (
          <div className={styles.loading}>
            <div className={styles.spinner} />
            <p>Loading timeline...</p>
          </div>
        ) : (
          <div className={styles.timeline}>
            {statuses.map(status => (
              <Status key={status.id} status={status} />
            ))}
          </div>
        )}

        {isLoadingMore && (
          <div className={styles.loadingMore}>
            <div className={styles.spinner} />
          </div>
        )}

        {!hasMore && statuses.length > 0 && (
          <div className={styles.endOfTimeline}>
            <p>You've reached the end of the timeline</p>
          </div>
        )}
      </div>
    </div>
  );
}
