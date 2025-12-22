import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostService, Post } from '../../core/services/post.service';
import { PostCardComponent } from './post-card/post-card.component';

@Component({
  selector: 'app-feed',
  standalone: true,
  imports: [CommonModule, PostCardComponent],
  templateUrl: './feed.component.html',
  styleUrl: './feed.component.css'
})
export class FeedComponent implements OnInit {
  posts: Post[] = [];
  isLoading = true;
  currentPage = 0;
  totalPages = 0;
  activeFeedType = 'universal';

  constructor(private postService: PostService) {}

  ngOnInit() {
    this.loadPosts();
  }

  loadPosts() {
    this.isLoading = true;
    this.postService.getPosts(this.currentPage, 10, this.activeFeedType).subscribe({
      next: (response) => {
        this.posts = response.content;
        this.totalPages = response.totalPages;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
      }
    });
  }
  
  switchFeed(feedType: string) {
    this.activeFeedType = feedType;
    this.currentPage = 0;
    this.posts = [];
    this.loadPosts();
  }

  likePost(post: Post) {
    this.postService.likePost(post.id).subscribe({
      next: () => {
        this.loadPosts();
      },
      error: (error) => {
      }
    });
  }

  sharePost(post: Post) {
    this.postService.sharePost(post.id).subscribe({
      next: () => {
        this.loadPosts();
      },
      error: (error) => {
      }
    });
  }

  addComment(post: Post, content: string) {
    if (content.trim()) {
      this.postService.addComment(post.id, content).subscribe({
        next: () => {
          this.loadPosts();
        },
        error: (error) => {
        }
      });
    }
  }

  loadMore() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.postService.getPosts(this.currentPage, 10, this.activeFeedType).subscribe({
        next: (response) => {
          this.posts = [...this.posts, ...response.content];
        },
        error: (error) => {
        }
      });
    }
  }
}