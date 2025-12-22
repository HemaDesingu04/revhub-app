import { Routes } from '@angular/router';

export const FEED_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./feed.component').then(m => m.FeedComponent) }
];