import { Component, Input, ViewChild, ElementRef, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Post, PostService } from '../../../core/services/post.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './post-card.component.html',
  styleUrl: './post-card.component.css',
  styles: [`
    .cursor-pointer { cursor: pointer; }
    .hover-bg-light:hover { background-color: #f8f9fa !important; }
  `]
})
export class PostCardComponent {
  @Input() post!: Post;
  
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event) {
    this.showPostOptions = false;
  }
  
  @ViewChild('commentTextarea') commentTextarea!: ElementRef;
  @ViewChild('editTextarea') editTextarea!: ElementRef;
  showComments = false;
  comments: any[] = [];
  newComment = '';
  
  showCommentSuggestions = false;
  commentSuggestions: any[] = [];
  selectedCommentSuggestionIndex = -1;
  
  // Edit functionality
  isEditing = false;
  editContent = '';
  editVisibility = 'PUBLIC';
  showEditSuggestions = false;
  editSuggestions: any[] = [];
  selectedEditSuggestionIndex = -1;
  showPostOptions = false;
  
  constructor(private authService: AuthService, private postService: PostService) {}
  
  isVideo(mediaType?: string): boolean {
    return mediaType === 'video' || (mediaType?.startsWith('video/') ?? false);
  }
  
  isImage(mediaType?: string): boolean {
    return !mediaType || mediaType === 'image' || mediaType?.startsWith('image/');
  }
  
  formatContent(content: string): string {
    if (!content) return '';
    
    // Format hashtags
    let formatted = content.replace(/#(\w+)/g, '<span class="text-primary fw-bold">#$1</span>');
    
    // Format mentions
    formatted = formatted.replace(/@(\w+)/g, '<span class="text-success fw-bold">@$1</span>');
    
    return formatted;
  }
  
  toggleComments() {
    this.showComments = !this.showComments;
    console.log('Comments toggled:', this.showComments);
    if (this.showComments) {
      this.loadComments();
    }
  }



  loadComments() {
    this.postService.getComments(this.post.id).subscribe({
      next: (comments) => {
        this.comments = comments.map(comment => this.initializeComment(comment));
      },
      error: (error) => console.error('Error loading comments:', error)
    });
  }
  
  onCommentKeyUp(event: KeyboardEvent) {
    const textarea = event.target as HTMLTextAreaElement;
    const cursorPos = textarea.selectionStart;
    const content = textarea.value;
    
    // Handle arrow keys for suggestion navigation
    if (this.showCommentSuggestions) {
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        this.selectedCommentSuggestionIndex = Math.min(this.selectedCommentSuggestionIndex + 1, this.commentSuggestions.length - 1);
        return;
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault();
        this.selectedCommentSuggestionIndex = Math.max(this.selectedCommentSuggestionIndex - 1, 0);
        return;
      }
      if (event.key === 'Escape') {
        this.hideCommentSuggestions();
        return;
      }
    }
    
    // Check for @ mention
    const beforeCursor = content.substring(0, cursorPos);
    const atIndex = beforeCursor.lastIndexOf('@');
    
    if (atIndex !== -1) {
      const afterAt = beforeCursor.substring(atIndex + 1);
      
      if (!afterAt.includes(' ') && afterAt.length >= 0) {
        this.searchCommentUsers(afterAt);
      } else {
        this.hideCommentSuggestions();
      }
    } else {
      this.hideCommentSuggestions();
    }
  }
  
  onCommentKeyDown(event: KeyboardEvent) {
    if (this.showCommentSuggestions && event.key === 'Enter' && this.selectedCommentSuggestionIndex >= 0) {
      event.preventDefault();
      this.selectCommentUser(this.commentSuggestions[this.selectedCommentSuggestionIndex]);
    }
  }
  
  searchCommentUsers(query: string) {
    this.authService.searchUsers(query).subscribe({
      next: (users: any[]) => {
        this.commentSuggestions = users || [];
        this.showCommentSuggestions = this.commentSuggestions.length > 0;
        this.selectedCommentSuggestionIndex = 0;
      },
      error: () => this.hideCommentSuggestions()
    });
  }
  
  selectCommentUser(user: any) {
    const textarea = this.commentTextarea.nativeElement;
    const content = textarea.value;
    const cursorPos = textarea.selectionStart;
    
    const beforeCursor = content.substring(0, cursorPos);
    const atIndex = beforeCursor.lastIndexOf('@');
    
    if (atIndex !== -1) {
      const newContent = content.substring(0, atIndex) + '@' + user.username + ' ' + content.substring(cursorPos);
      this.newComment = newContent;
      
      setTimeout(() => {
        const newPos = atIndex + user.username.length + 2;
        textarea.setSelectionRange(newPos, newPos);
        textarea.focus();
      });
    }
    
    this.hideCommentSuggestions();
  }
  
  hideCommentSuggestions() {
    this.showCommentSuggestions = false;
    this.commentSuggestions = [];
    this.selectedCommentSuggestionIndex = -1;
  }
  
  addComment() {
    if (this.newComment.trim()) {
      this.postService.addComment(this.post.id, this.newComment).subscribe({
        next: (comment) => {
          this.comments.unshift(this.initializeComment(comment));
          this.newComment = '';
          this.post.commentsCount++;
        },
        error: (error) => console.error('Error adding comment:', error)
      });
    }
  }
  
  initializeComment(comment: any) {
    return {
      ...comment,
      replies: comment.replies || [],
      showReplyForm: false,
      replyText: '',
      showReplySuggestions: false,
      replySuggestions: [],
      selectedReplySuggestionIndex: -1
    };
  }
  
  toggleReply(commentIndex: number) {
    console.log('Toggle reply for comment:', commentIndex);
    const comment = this.comments[commentIndex];
    comment.showReplyForm = !comment.showReplyForm;
    console.log('Reply form shown:', comment.showReplyForm);
    if (comment.showReplyForm) {
      comment.replyText = '';
      comment.replies = comment.replies || [];
      // Focus on reply textarea after a short delay
      setTimeout(() => {
        const replyTextarea = document.querySelector(`textarea[data-reply-index="${commentIndex}"]`) as HTMLTextAreaElement;
        if (replyTextarea) {
          replyTextarea.focus();
        }
      }, 100);
    }
  }
  
  cancelReply(commentIndex: number) {
    const comment = this.comments[commentIndex];
    comment.showReplyForm = false;
    comment.replyText = '';
    this.hideReplySuggestions(commentIndex);
  }
  
  addReply(commentIndex: number) {
    const comment = this.comments[commentIndex];
    if (comment.replyText?.trim()) {
      this.postService.addReply(comment.id, comment.replyText).subscribe({
        next: (reply) => {
          if (!comment.replies) {
            comment.replies = [];
          }
          comment.replies.push(reply);
          comment.replyText = '';
          comment.showReplyForm = false;
          this.hideReplySuggestions(commentIndex);
        },
        error: (error) => console.error('Error adding reply:', error)
      });
    }
  }
  
  onReplyKeyUp(event: KeyboardEvent, commentIndex: number) {
    const comment = this.comments[commentIndex];
    const textarea = event.target as HTMLTextAreaElement;
    const cursorPos = textarea.selectionStart;
    const content = textarea.value;
    
    if (comment.showReplySuggestions) {
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        comment.selectedReplySuggestionIndex = Math.min(comment.selectedReplySuggestionIndex + 1, comment.replySuggestions.length - 1);
        return;
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault();
        comment.selectedReplySuggestionIndex = Math.max(comment.selectedReplySuggestionIndex - 1, 0);
        return;
      }
      if (event.key === 'Escape') {
        this.hideReplySuggestions(commentIndex);
        return;
      }
    }
    
    const beforeCursor = content.substring(0, cursorPos);
    const atIndex = beforeCursor.lastIndexOf('@');
    
    if (atIndex !== -1) {
      const afterAt = beforeCursor.substring(atIndex + 1);
      if (!afterAt.includes(' ') && afterAt.length >= 0) {
        this.searchReplyUsers(afterAt, commentIndex);
      } else {
        this.hideReplySuggestions(commentIndex);
      }
    } else {
      this.hideReplySuggestions(commentIndex);
    }
  }
  
  onReplyKeyDown(event: KeyboardEvent, commentIndex: number) {
    const comment = this.comments[commentIndex];
    if (comment.showReplySuggestions && event.key === 'Enter' && comment.selectedReplySuggestionIndex >= 0) {
      event.preventDefault();
      this.selectReplyUser(comment.replySuggestions[comment.selectedReplySuggestionIndex], commentIndex);
    }
  }
  
  searchReplyUsers(query: string, commentIndex: number) {
    this.authService.searchUsers(query).subscribe({
      next: (users: any[]) => {
        const comment = this.comments[commentIndex];
        comment.replySuggestions = users || [];
        comment.showReplySuggestions = comment.replySuggestions.length > 0;
        comment.selectedReplySuggestionIndex = 0;
      },
      error: () => this.hideReplySuggestions(commentIndex)
    });
  }
  
  selectReplyUser(user: any, commentIndex: number) {
    const comment = this.comments[commentIndex];
    const content = comment.replyText || '';
    const atIndex = content.lastIndexOf('@');
    
    if (atIndex !== -1) {
      // Find the end of the current mention attempt
      let endIndex = content.length;
      for (let i = atIndex + 1; i < content.length; i++) {
        if (content[i] === ' ') {
          endIndex = i;
          break;
        }
      }
      
      const newContent = content.substring(0, atIndex) + '@' + user.username + ' ' + content.substring(endIndex);
      comment.replyText = newContent;
    }
    
    this.hideReplySuggestions(commentIndex);
  }
  
  hideReplySuggestions(commentIndex: number) {
    const comment = this.comments[commentIndex];
    comment.showReplySuggestions = false;
    comment.replySuggestions = [];
    comment.selectedReplySuggestionIndex = -1;
  }
  
  // Edit functionality methods
  canEditPost(): boolean {
    const currentUser = this.authService.getCurrentUser();
    return currentUser && currentUser.username === this.post.author.username;
  }
  
  togglePostOptions() {
    console.log('Post-card: Toggling post options');
    console.log('Post-card: Current state:', this.showPostOptions);
    this.showPostOptions = !this.showPostOptions;
    console.log('Post-card: New state:', this.showPostOptions);
  }
  
  startEdit() {
    console.log('Starting edit mode for post:', this.post.id);
    this.isEditing = true;
    this.editContent = this.post.content;
    this.editVisibility = this.post.visibility || 'PUBLIC';
    console.log('Edit mode activated, content:', this.editContent);
  }
  
  cancelEdit() {
    this.isEditing = false;
    this.editContent = '';
    this.hideEditSuggestions();
  }
  
  saveEdit() {
    if (this.editContent.trim()) {
      const updateData = {
        content: this.editContent,
        visibility: this.editVisibility
      };
      
      this.postService.updatePost(this.post.id, updateData).subscribe({
        next: (updatedPost) => {
          this.post.content = updatedPost.content;
          this.post.visibility = updatedPost.visibility;
          this.isEditing = false;
          this.editContent = '';
          this.hideEditSuggestions();
        },
        error: (error) => console.error('Error updating post:', error)
      });
    }
  }
  
  confirmDelete() {
    this.showPostOptions = false;
    if (confirm('Are you sure you want to delete this post? This action cannot be undone.')) {
      this.deletePost();
    }
  }
  
  deletePost() {
    this.postService.deletePost(this.post.id).subscribe({
      next: () => {
        // Emit event to parent component to remove post from list
        window.location.reload();
      },
      error: (error) => console.error('Error deleting post:', error)
    });
  }
  
  onEditKeyUp(event: KeyboardEvent) {
    const textarea = event.target as HTMLTextAreaElement;
    const cursorPos = textarea.selectionStart;
    const content = textarea.value;
    
    if (this.showEditSuggestions) {
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        this.selectedEditSuggestionIndex = Math.min(this.selectedEditSuggestionIndex + 1, this.editSuggestions.length - 1);
        return;
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault();
        this.selectedEditSuggestionIndex = Math.max(this.selectedEditSuggestionIndex - 1, 0);
        return;
      }
      if (event.key === 'Escape') {
        this.hideEditSuggestions();
        return;
      }
    }
    
    const beforeCursor = content.substring(0, cursorPos);
    const atIndex = beforeCursor.lastIndexOf('@');
    
    if (atIndex !== -1) {
      const afterAt = beforeCursor.substring(atIndex + 1);
      if (!afterAt.includes(' ') && afterAt.length >= 0) {
        this.searchEditUsers(afterAt);
      } else {
        this.hideEditSuggestions();
      }
    } else {
      this.hideEditSuggestions();
    }
  }
  
  onEditKeyDown(event: KeyboardEvent) {
    if (this.showEditSuggestions && event.key === 'Enter' && this.selectedEditSuggestionIndex >= 0) {
      event.preventDefault();
      this.selectEditUser(this.editSuggestions[this.selectedEditSuggestionIndex]);
    }
  }
  
  searchEditUsers(query: string) {
    this.authService.searchUsers(query).subscribe({
      next: (users: any[]) => {
        this.editSuggestions = users || [];
        this.showEditSuggestions = this.editSuggestions.length > 0;
        this.selectedEditSuggestionIndex = 0;
      },
      error: () => this.hideEditSuggestions()
    });
  }
  
  selectEditUser(user: any) {
    const textarea = this.editTextarea.nativeElement;
    const content = textarea.value;
    const cursorPos = textarea.selectionStart;
    
    const beforeCursor = content.substring(0, cursorPos);
    const atIndex = beforeCursor.lastIndexOf('@');
    
    if (atIndex !== -1) {
      const newContent = content.substring(0, atIndex) + '@' + user.username + ' ' + content.substring(cursorPos);
      this.editContent = newContent;
      
      setTimeout(() => {
        const newPos = atIndex + user.username.length + 2;
        textarea.setSelectionRange(newPos, newPos);
        textarea.focus();
      });
    }
    
    this.hideEditSuggestions();
  }
  
  hideEditSuggestions() {
    this.showEditSuggestions = false;
    this.editSuggestions = [];
    this.selectedEditSuggestionIndex = -1;
  }
}