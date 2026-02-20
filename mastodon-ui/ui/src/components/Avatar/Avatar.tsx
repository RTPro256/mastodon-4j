/**
 * Avatar component
 */

import React from 'react';
import type { Account } from '@/types';
import styles from './Avatar.module.css';

interface AvatarProps {
  account: Account;
  size?: number;
  className?: string;
  onClick?: () => void;
}

export function Avatar({ account, size = 48, className, onClick }: AvatarProps) {
  const src = account.avatar || '/default-avatar.png';

  return (
    <img
      src={src}
      alt={`${account.display_name || account.username}'s avatar`}
      className={`${styles.avatar} ${className || ''}`}
      style={{ width: size, height: size }}
      onClick={onClick}
      loading="lazy"
    />
  );
}
