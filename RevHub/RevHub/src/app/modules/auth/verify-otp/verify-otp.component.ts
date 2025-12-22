import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-verify-otp',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="d-flex justify-content-center align-items-center min-vh-100">
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h3 class="card-title text-center mb-4">Verify Email</h3>
            <p class="text-center text-muted mb-4">
              We've sent a 6-digit OTP to your email address. Please enter it below.
            </p>
            <form (ngSubmit)="verifyOTP()">
              <div class="mb-3">
                <input 
                  type="text" 
                  class="form-control text-center" 
                  placeholder="Enter 6-digit OTP" 
                  [(ngModel)]="otp"
                  name="otp"
                  maxlength="6"
                  required>
              </div>
              <div class="mb-3" *ngIf="errorMessage">
                <div class="alert alert-danger">{{errorMessage}}</div>
              </div>
              <div class="mb-3" *ngIf="successMessage">
                <div class="alert alert-success">{{successMessage}}</div>
              </div>
              <button 
                type="submit" 
                class="btn btn-primary w-100" 
                [disabled]="isLoading || !otp || otp.length !== 6">
                <span *ngIf="isLoading" class="spinner-border spinner-border-sm me-2"></span>
                {{isLoading ? 'Verifying...' : 'Verify OTP'}}
              </button>
            </form>
            <div class="text-center mt-3">
              <button class="btn btn-link" (click)="resendOTP()" [disabled]="isLoading">
                Resend OTP
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class VerifyOtpComponent implements OnInit {
  otp = '';
  email = '';
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.email = this.route.snapshot.queryParams['email'] || '';
    if (!this.email) {
      this.router.navigate(['/auth/register']);
    }
  }

  verifyOTP() {
    if (this.otp && this.otp.length === 6) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.authService.verifyOTP(this.email, this.otp).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = 'Email verified successfully! Redirecting to login...';
          setTimeout(() => {
            this.router.navigate(['/auth/login']);
          }, 2000);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error || 'OTP verification failed. Please try again.';
        }
      });
    }
  }

  resendOTP() {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.resendVerificationEmail(this.email).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.successMessage = 'New OTP sent to your email!';
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to resend OTP. Please try again.';
      }
    });
  }
}