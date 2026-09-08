import { Routes } from '@angular/router';
import { AuthComponent } from './components/auth/auth';
import { FeedComponent } from './components/feed/feed';
import { ProfileComponent } from './components/profile/profile';

export const routes: Routes = [
  { path: '', redirectTo: '/feed', pathMatch: 'full' },
  { path: 'auth', component: AuthComponent },
  { path: 'feed', component: FeedComponent },
  { path: 'profile', component: ProfileComponent },
  { path: '**', redirectTo: '/feed' }
];