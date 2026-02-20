/**
 * Authentication state management using React Context
 */

import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import type { Account, InstanceInfo, OAuthApp, TokenResponse } from '@/types';
import { auth, oauthApi, instanceApi, accountsApi } from '@/api';

interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: Account | null;
  instance: InstanceInfo | null;
  accessToken: string | null;
  oauthApp: OAuthApp | null;
}

interface AuthContextValue extends AuthState {
  login: () => Promise<void>;
  logout: () => void;
  handleCallback: (code: string) => Promise<void>;
  refreshUser: () => Promise<void>;
  refreshInstance: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

const OAUTH_REDIRECT_KEY = 'oauth_redirect_uri';
const OAUTH_STATE_KEY = 'oauth_state';
const OAUTH_APP_KEY = 'oauth_app';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: auth.isAuthenticated(),
    isLoading: true,
    user: null,
    instance: null,
    accessToken: auth.getToken(),
    oauthApp: null,
  });

  // Load instance info on mount
  useEffect(() => {
    refreshInstance();
  }, []);

  // Load user info if authenticated
  useEffect(() => {
    if (state.isAuthenticated) {
      refreshUser().catch(() => {
        // Token might be invalid, clear it
        auth.clearToken();
        setState(prev => ({
          ...prev,
          isAuthenticated: false,
          accessToken: null,
          user: null,
          isLoading: false,
        }));
      });
    } else {
      setState(prev => ({ ...prev, isLoading: false }));
    }
  }, [state.isAuthenticated]);

  const refreshInstance = useCallback(async () => {
    try {
      const instance = await instanceApi.get();
      setState(prev => ({ ...prev, instance }));
    } catch (error) {
      console.error('Failed to load instance info:', error);
    }
  }, []);

  const refreshUser = useCallback(async () => {
    try {
      const user = await accountsApi.verifyCredentials();
      setState(prev => ({ ...prev, user, isLoading: false }));
    } catch (error) {
      console.error('Failed to load user:', error);
      throw error;
    }
  }, []);

  const login = useCallback(async () => {
    if (!state.instance) {
      console.error('Instance info not loaded');
      return;
    }

    try {
      // Create OAuth app
      const redirectUri = `${window.location.origin}/oauth/callback`;
      const oauthApp = await oauthApi.createApp({
        client_name: 'Mastodon Java UI',
        redirect_uris: redirectUri,
        scopes: 'read write follow push',
      });

      // Store app info for callback
      sessionStorage.setItem(OAUTH_APP_KEY, JSON.stringify(oauthApp));
      sessionStorage.setItem(OAUTH_REDIRECT_KEY, redirectUri);

      // Generate state for CSRF protection
      const stateParam = Math.random().toString(36).substring(7);
      sessionStorage.setItem(OAUTH_STATE_KEY, stateParam);

      // Redirect to authorization page
      const authUrl = oauthApi.getAuthorizationUrl(
        oauthApp.client_id,
        redirectUri,
        'read write follow push',
        stateParam
      );

      window.location.href = authUrl;
    } catch (error) {
      console.error('Failed to initiate login:', error);
    }
  }, [state.instance]);

  const handleCallback = useCallback(async (code: string) => {
    const oauthAppStr = sessionStorage.getItem(OAUTH_APP_KEY);
    const redirectUri = sessionStorage.getItem(OAUTH_REDIRECT_KEY);

    if (!oauthAppStr || !redirectUri) {
      throw new Error('OAuth app info not found');
    }

    const oauthApp: OAuthApp = JSON.parse(oauthAppStr);

    try {
      const tokenResponse: TokenResponse = await oauthApi.getToken({
        client_id: oauthApp.client_id,
        client_secret: oauthApp.client_secret,
        redirect_uri: redirectUri,
        code,
        grant_type: 'authorization_code',
      });

      // Store token
      auth.setToken(tokenResponse.access_token);

      // Clear OAuth session data
      sessionStorage.removeItem(OAUTH_APP_KEY);
      sessionStorage.removeItem(OAUTH_REDIRECT_KEY);
      sessionStorage.removeItem(OAUTH_STATE_KEY);

      // Update state
      setState(prev => ({
        ...prev,
        isAuthenticated: true,
        accessToken: tokenResponse.access_token,
        oauthApp,
      }));
    } catch (error) {
      console.error('Failed to handle OAuth callback:', error);
      throw error;
    }
  }, []);

  const logout = useCallback(() => {
    auth.clearToken();
    setState(prev => ({
      ...prev,
      isAuthenticated: false,
      accessToken: null,
      user: null,
    }));
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    ...state,
    login,
    logout,
    handleCallback,
    refreshUser,
    refreshInstance,
  }), [state, login, logout, handleCallback, refreshUser, refreshInstance]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

export { AuthContext };
