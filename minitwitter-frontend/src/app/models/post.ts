import { User } from "./user";

export interface Post {
    id: number;
    content: string;
    author: User;
    createdAt: string;
    updatedAt: string;
  }
