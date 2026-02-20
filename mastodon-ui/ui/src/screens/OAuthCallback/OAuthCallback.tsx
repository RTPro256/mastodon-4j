/**
 * OAuth callback screen
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/stores';
import styles from './OAuthCallback.module.css';

export function OAuthCallback() {
  const navigate = useNavigate();
  const { handleCallback } = useAuth();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');
    const errorParam = params.get('error');
    const errorDescription = params.get('error_description');

    if (errorParam) {
      setError(errorDescription || errorParam);
      return;
    }

    if (!code) {
      setError('No authorization code received');
      return;
    }

    handleCallback(code)
      .then(() => {
        navigate('/');
      })
      .catch((err) => {
        console.error('OAuth callback error:', err);
        setError(err instanceof Error ? err.message : 'Authentication failed');
      });
  }, [handleCallback, navigate]);

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <h1>Authentication Failed</h1>
          <p>{error}</p>
          <button onClick={() => navigate('/')}>Go Back</button>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.spinner} />
      <p>Completing authentication...</p>
    </div>
  );
}
