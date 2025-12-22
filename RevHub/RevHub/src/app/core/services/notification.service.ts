import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Notification {
  id: string;
  type: string;
  message: string;
  readStatus: boolean;
  createdDate: string;
  fromUserId?: string;
  fromUsername?: string;
  fromUserProfilePicture?: string;
  followRequestId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = '/api/notifications';

  constructor(private http: HttpClient) { }

  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  markAsRead(id: string): Observable<Notification> {
    return this.http.put<Notification>(`${this.apiUrl}/${id}/read`, {});
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread-count`);
  }

  acceptFollowRequest(followId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/follow-request/${followId}/accept`, {}, { responseType: 'text' });
  }

  rejectFollowRequest(followId: number): Observable<string> {
    return this.http.post(`${this.apiUrl}/follow-request/${followId}/reject`, {}, { responseType: 'text' });
  }

  deleteNotification(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}