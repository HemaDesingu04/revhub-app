import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const token = localStorage.getItem('token');
    if (token) {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      this.currentUserSubject.next(user);
    }
  }

  login(credentials: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/login`, credentials)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          localStorage.setItem('user', JSON.stringify({
            id: response.id,
            username: response.username,
            email: response.email
          }));
          this.currentUserSubject.next({
            id: response.id,
            username: response.username,
            email: response.email
          });
        })
      );
  }

  register(userData: RegisterRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/register`, userData, { responseType: 'text' });
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/logout`, {}).pipe(
      tap(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        this.currentUserSubject.next(null);
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): any {
    return this.currentUserSubject.value;
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }, { responseType: 'text' });
  }

  resetPassword(token: string, newPassword: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, { token, newPassword }, { responseType: 'text' });
  }

  searchUsers(query: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/search/users?q=${query || ''}`);
  }
  
  verifyOTP(email: string, otp: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/verify-otp`, { email, otp }, { responseType: 'text' });
  }
  
  resendVerificationEmail(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/send-verification`, null, { 
      params: { email }, 
      responseType: 'text' 
    });
  }
  

}