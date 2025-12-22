import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  id: string;
  senderId: string;
  senderUsername: string;
  receiverId: string;
  receiverUsername: string;
  content: string;
  timestamp: string;
  read: boolean;
  messageType: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = '/api/chat';

  constructor(private http: HttpClient) { }

  sendMessage(receiverUsername: string, content: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.apiUrl}/send`, {
      receiverUsername,
      content
    });
  }

  getConversation(username: string): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/conversation/${username}`);
  }

  markAsRead(username: string): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/mark-read/${username}`, {});
  }

  getChatContacts(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/contacts`);
  }

  getUnreadCount(username: string): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/unread-count/${username}`);
  }
}