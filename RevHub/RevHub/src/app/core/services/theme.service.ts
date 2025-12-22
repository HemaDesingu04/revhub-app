import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private isDarkTheme = new BehaviorSubject<boolean>(false);
  isDarkTheme$ = this.isDarkTheme.asObservable();

  constructor() {
    const savedTheme = localStorage.getItem('theme');
    const isDark = savedTheme === 'dark';
    this.setTheme(isDark);
  }

  toggleTheme() {
    this.setTheme(!this.isDarkTheme.value);
  }

  setTheme(isDark: boolean) {
    this.isDarkTheme.next(isDark);
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    document.body.setAttribute('data-theme', isDark ? 'dark' : 'light');
  }
}