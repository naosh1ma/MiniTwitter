export interface User {
    id: number;
    username: string;
    email: string;
    bio?: string;
    avatarUrl?: string;
    createdAt: string;
    followerCount?: number;
    followingCount?: number;
    followedByCurrentUser?: boolean;
  }
