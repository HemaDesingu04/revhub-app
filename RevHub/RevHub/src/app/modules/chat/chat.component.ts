import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatMessage } from '../../core/services/chat.service';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit {
  contacts: string[] = [];
  selectedChat: string | null = null;
  messages: { [key: string]: ChatMessage[] } = {};
  newMessage = '';
  currentUser: any = null;
  isLoading = true;
  searchQuery = '';
  searchResults: any[] = [];
  followingList: any[] = [];

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private profileService: ProfileService
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.getCurrentUser();
    this.loadChatContacts();
    this.loadFollowing();
  }

  loadChatContacts() {
    this.chatService.getChatContacts().subscribe({
      next: (contacts) => {
        this.contacts = contacts;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading chat contacts:', error);
        this.isLoading = false;
      }
    });
  }

  loadFollowing() {
    if (this.currentUser?.username) {
      this.profileService.getFollowing(this.currentUser.username).subscribe({
        next: (following) => {
          this.followingList = following;
        },
        error: (error) => {
          console.error('Error loading following:', error);
        }
      });
    }
  }

  onSearchInput() {
    if (!this.searchQuery.trim()) {
      this.searchResults = [];
      return;
    }
    
    this.searchResults = this.followingList.filter(user => 
      user.username.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  startChat(user: any) {
    this.selectedChat = user.username;
    this.searchQuery = '';
    this.searchResults = [];
    this.loadConversation(user.username);
    if (!this.contacts.includes(user.username)) {
      this.contacts.unshift(user.username);
    }
  }

  selectChat(contact: string) {
    this.selectedChat = contact;
    this.loadConversation(contact);
  }

  loadConversation(username: string) {
    this.chatService.getConversation(username).subscribe({
      next: (messages) => {
        this.messages[username] = messages;
      },
      error: (error) => {
        console.error('Error loading conversation:', error);
        this.messages[username] = [];
      }
    });
  }

  sendMessage() {
    if (this.newMessage.trim() && this.selectedChat) {
      this.chatService.sendMessage(this.selectedChat, this.newMessage.trim()).subscribe({
        next: (message) => {
          if (!this.messages[this.selectedChat!]) {
            this.messages[this.selectedChat!] = [];
          }
          this.messages[this.selectedChat!].push(message);
          this.newMessage = '';
        },
        error: (error) => {
          console.error('Error sending message:', error);
        }
      });
    }
  }

  backToContacts() {
    this.selectedChat = null;
  }

  getUserInitial(username?: string): string {
    return username?.charAt(0)?.toUpperCase() || 'U';
  }
}