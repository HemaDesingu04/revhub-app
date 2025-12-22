import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  { path: 'login', loadComponent: () => import('./login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent) },
  { path: 'verify-otp', loadComponent: () => import('./verify-otp/verify-otp.component').then(m => m.VerifyOtpComponent) },
  { path: 'forgot-password', loadComponent: () => import('./forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'reset-password', loadComponent: () => import('./reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];