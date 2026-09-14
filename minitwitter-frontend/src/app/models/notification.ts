import { User } from "./user";

export type NotificationType = 'LIKE' | 'FOLLOW';

export interface AppNotification {
    id: number;
    type: NotificationType;
    actor: User;
    postId: number | null;
    read: boolean;
    createdAt: string;
}
