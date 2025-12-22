import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService, LoginRequest } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterModule, CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  displayText = '';
  fullText = 'RevHub';
  showForm = false;
  
  loginData: LoginRequest = {
    username: '',
    password: ''
  };
  
  isLoading = false;
  errorMessage = '';
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  ngOnInit() {
    this.typeText();
  }
  

  
  typeText() {
    let i = 0;
    const interval = setInterval(() => {
      this.displayText = this.fullText.substring(0, i + 1);
      i++;
      if (i >= this.fullText.length) {
        clearInterval(interval);
        setTimeout(() => {
          this.showForm = true;
        }, 500);
      }
    }, 300);
  }
  
  onSubmit() {
    if (this.loginData.username && this.loginData.password) {
      this.isLoading = true;
      this.errorMessage = '';
      
      this.authService.login(this.loginData).subscribe({
        next: (response) => {
          this.isLoading = false;
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = 'Invalid credentials. Please try again.';
        }
      });
    }
  }
}
