import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '../../../core/services/profile.service';

@Component({
  selector: 'app-profile-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile-header.component.html',
  styleUrl: './profile-header.component.css'
})
export class ProfileHeaderComponent {
  @Input() user: User | null = null;
}