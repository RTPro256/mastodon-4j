/**
 * Main App component
 */

import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider, TimelineProvider } from '@/stores';
import { HomeScreen, OAuthCallback } from '@/screens';
import '@/i18n';
import '@/theme/index.css';

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <TimelineProvider>
          <Routes>
            <Route path="/" element={<HomeScreen />} />
            <Route path="/oauth/callback" element={<OAuthCallback />} />
            <Route path="/@:acct" element={<HomeScreen />} />
            <Route path="/@:acct/:statusId" element={<HomeScreen />} />
            <Route path="/notifications" element={<HomeScreen />} />
            <Route path="/public" element={<HomeScreen />} />
            <Route path="/public/local" element={<HomeScreen />} />
            <Route path="/tags/:hashtag" element={<HomeScreen />} />
          </Routes>
        </TimelineProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
