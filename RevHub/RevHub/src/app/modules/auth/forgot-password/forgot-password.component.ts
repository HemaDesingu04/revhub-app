import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-container">
      <form [formGroup]="forgotPasswordForm" (ngSubmit)="onSubmit()" class="auth-form">
        <h2>Reset Password</h2>
        <div class="form-group">
          <input type="email" formControlName="email" placeholder="Enter your email" class="form-control">
        </div>
        <button type="submit" [disabled]="forgotPasswordForm.invalid || loading" class="btn-primary">
          {{loading ? 'Sending...' : 'Send Reset Link'}}
        </button>
        <div *ngIf="message" class="message" [class.error]="isError">{{message}}</div>
        <a routerLink="/auth/login" class="link">Back to Login</a>
      </form>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; }
    .auth-form { background: white; padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
    .form-group { margin-bottom: 1rem; }
    .form-control { width: 100%; padding: 0.75rem; border: 1px solid #ddd; border-radius: 4px; }
    .btn-primary { width: 100%; padding: 0.75rem; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
    .btn-primary:disabled { opacity: 0.6; }
    .message { margin-top: 1rem; padding: 0.5rem; border-radius: 4px; background: #d4edda; color: #155724; }
    .message.error { background: #f8d7da; color: #721c24; }
    .link { display: block; text-align: center; margin-top: 1rem; color: #007bff; text-decoration: none; }
  `]
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  loading = false;
  message = '';
  isError = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    if (this.forgotPasswordForm.valid) {
      this.loading = true;
      this.authService.forgotPassword(this.forgotPasswordForm.value.email).subscribe({
        next: (response) => {
          this.message = 'Password reset email sent successfully!';
          this.isError = false;
          this.loading = false;
        },
        error: (error) => {
          this.message = 'Failed to send reset email. Please try again.';
          this.isError = true;
          this.loading = false;
        }
      });
    }
  }
}