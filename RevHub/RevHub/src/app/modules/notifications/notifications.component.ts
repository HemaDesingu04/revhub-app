import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../core/services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html',
  styleUrl: './notifications.component.css'
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  isLoading = true;

  constructor(private notificationService: NotificationService) {}

  ngOnInit() {
    this.loadNotifications();
  }

  loadNotifications() {
    this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        this.notifications = notifications;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading notifications:', error);
        this.isLoading = false;
      }
    });
  }

  markAsRead(notification: Notification) {
    if (!notification.readStatus) {
      this.notificationService.markAsRead(notification.id).subscribe({
        next: () => {
          notification.readStatus = true;
        },
        error: (error) => {
          console.error('Error marking notification as read:', error);
        }
      });
    }
  }

  acceptFollowRequest(notification: Notification) {
    if (notification.followRequestId) {
      this.notificationService.acceptFollowRequest(notification.followRequestId).subscribe({
        next: () => {
          this.loadNotifications();
        },
        error: (error) => {
          console.error('Error accepting follow request:', error);
        }
      });
    }
  }

  rejectFollowRequest(notification: Notification) {
    if (notification.followRequestId) {
      this.notificationService.rejectFollowRequest(notification.followRequestId).subscribe({
        next: () => {
          this.loadNotifications();
        },
        error: (error) => {
          console.error('Error rejecting follow request:', error);
        }
      });
    }
  }

  isFollowRequest(notification: Notification): boolean {
    return notification.type === 'FOLLOW_REQUEST';
  }
}