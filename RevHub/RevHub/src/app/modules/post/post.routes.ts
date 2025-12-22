import { Routes } from '@angular/router';

export const POST_ROUTES: Routes = [
  { path: 'create', loadComponent: () => import('./create/create.component').then(m => m.CreateComponent) },
  { path: '', redirectTo: 'create', pathMatch: 'full' }
];