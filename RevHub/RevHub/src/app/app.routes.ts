import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'dashboard', loadComponent: () => import('./dashboard.component').then(m => m.DashboardComponent) },
  { path: 'profile/:username', loadComponent: () => import('./user-profile/user-profile.component').then(m => m.UserProfileComponent) },
  {
    path: 'auth',
    loadChildren: () =>
      import('./modules/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'chat',
    loadChildren: () =>
      import('./modules/chat/chat.routes').then((m) => m.CHAT_ROUTES),
  },
  {
    path: 'notifications',
    loadChildren: () =>
      import('./modules/notifications/notif.routes').then((m) => m.NOTIFICATION_ROUTES),
  },
];
