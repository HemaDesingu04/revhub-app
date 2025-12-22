import { Component } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, RegisterRequest } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  registerData: RegisterRequest = {
    username: '',
    email: '',
    password: ''
  };
  
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  onSubmit() {
    if (this.registerData.username && this.registerData.email && this.registerData.password) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';
      
      this.authService.register(this.registerData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.successMessage = 'Registration successful! Please check your email for OTP.';
          setTimeout(() => {
            this.router.navigate(['/auth/verify-otp'], { queryParams: { email: this.registerData.email } });
          }, 2000);
        },
        error: (error) => {
          this.isLoading = false;
          
          if (error.error && typeof error.error === 'string') {
            this.errorMessage = error.error;
          } else if (error.status === 0) {
            this.errorMessage = 'Cannot connect to server. Please check if backend is running.';
          } else if (error.status === 400) {
            this.errorMessage = error.error || 'Invalid registration data. Please check your inputs.';
          } else {
            this.errorMessage = `Registration failed (${error.status}): ${error.error || error.message || 'Please try again.'}`;
          }
        }
      });
    }
  }
}
