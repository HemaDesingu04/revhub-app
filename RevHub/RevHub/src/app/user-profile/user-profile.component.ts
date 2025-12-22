import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProfileService, User } from '../core/services/profile.service';
import { PostService } from '../core/services/post.service';
import { AuthService } from '../core/services/auth.service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {
  user: User | null = null;
  userPosts: any[] = [];
  isLoading = true;
  currentUser: any = null;
  followersCount = 0;
  followingCount = 0;
  followStatus = 'NOT_FOLLOWING';
  canViewPosts = false;
  showComments: { [key: number]: boolean } = {};
  newComment = '';
  activeTab: string = 'posts';
  followers: any[] = [];
  followingList: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private profileService: ProfileService,
    private postService: PostService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.currentUser = this.authService.getCurrentUser();
    this.route.params.subscribe(params => {
      const username = params['username'];
      if (username) {
        this.loadUserProfile(username);
      }
    });
  }

  loadUserProfile(username: string) {
    this.isLoading = true;
    
    this.profileService.getProfile(username).subscribe({
      next: (profile) => {
        this.user = profile;
        this.followersCount = profile.followersCount || 0;
        this.followingCount = profile.followingCount || 0;
        // Load follow status first, then posts
        this.loadFollowStatus(username);
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.isLoading = false;
      }
    });
  }

  loadUserPosts(username: string) {
    // Check if we can view posts (public profile or following private profile)
    this.canViewPosts = !this.user?.isPrivate || this.followStatus === 'ACCEPTED' || this.isOwnProfile();
    
    if (this.canViewPosts) {
      this.profileService.getUserPosts(username).subscribe({
        next: (posts) => {
          this.userPosts = posts;
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Error loading posts:', error);
          this.isLoading = false;
        }
      });
    } else {
      this.isLoading = false;
    }
  }

  loadFollowStatus(username: string) {
    if (this.currentUser && this.currentUser.username !== username) {
      this.profileService.getFollowStatus(username).subscribe({
        next: (response) => {
          this.followStatus = response.status;
          // Load posts after follow status is determined
          this.loadUserPosts(username);
        },
        error: (error) => {
          this.followStatus = 'NOT_FOLLOWING';
          // Load posts even if follow status fails
          this.loadUserPosts(username);
        }
      });
    } else {
      // For own profile, load posts directly
      this.loadUserPosts(username);
    }
  }

  followUser() {
    if (!this.user) return;
    
    this.profileService.followUser(this.user.username).subscribe({
      next: (response) => {
        if (response.message.includes('request sent')) {
          this.followStatus = 'PENDING';
        } else {
          this.followStatus = 'ACCEPTED';
          this.followersCount++;
        }
      },
      error: (error) => {
        console.error('Error following user:', error);
      }
    });
  }

  unfollowUser() {
    if (!this.user) return;
    
    this.profileService.unfollowUser(this.user.username).subscribe({
      next: (response) => {
        this.followStatus = 'NOT_FOLLOWING';
        this.followersCount = Math.max(0, this.followersCount - 1);
      },
      error: (error) => {
        console.error('Error unfollowing user:', error);
      }
    });
  }

  goBack() {
    this.router.navigate(['/dashboard']);
  }

  isOwnProfile(): boolean {
    return this.currentUser && this.user && this.currentUser.username === this.user.username;
  }

  isVideo(url: string): boolean {
    if (!url) return false;
    return url.startsWith('data:video/') || url.includes('.mp4') || url.includes('.webm') || url.includes('.ogg') || url.includes('.mov');
  }

  isImage(url: string): boolean {
    if (!url) return false;
    return url.startsWith('data:image/') || url.includes('.jpg') || url.includes('.jpeg') || url.includes('.png') || url.includes('.gif') || url.includes('.webp');
  }

  likePost(post: any) {
    this.postService.toggleLike(post.id).subscribe({
      next: (response) => {
        post.likesCount = response.likesCount;
        post.isLiked = response.isLiked;
      },
      error: (error) => {
        console.error('Error toggling like:', error);
      }
    });
  }

  commentPost(post: any) {
    this.showComments[post.id] = !this.showComments[post.id];
    if (this.showComments[post.id]) {
      this.postService.getComments(post.id).subscribe({
        next: (comments) => {
          post.commentsList = comments;
        },
        error: (error) => {
          console.error('Error loading comments:', error);
        }
      });
    }
  }

  addComment(post: any) {
    if (this.newComment.trim()) {
      this.postService.addComment(post.id, this.newComment).subscribe({
        next: (response) => {
          if (!post.commentsList) {
            post.commentsList = [];
          }
          post.commentsList.push(response);
          post.commentsCount = post.commentsList.length;
          this.newComment = '';
        },
        error: (error) => {
          console.error('Error adding comment:', error);
        }
      });
    }
  }

  sharePost(post: any) {
    const shareData = {
      title: 'RevHub Post',
      text: `Check out this post by ${post.author.username}: ${post.content}`,
      url: window.location.href
    };

    if (navigator.share) {
      navigator.share(shareData).then(() => {
        this.postService.sharePost(post.id).subscribe({
          next: (response) => {
            post.sharesCount = response.sharesCount;
          },
          error: (error) => {
            console.error('Error updating share count:', error);
          }
        });
      });
    } else {
      const text = `Check out this post by ${post.author.username}: ${post.content}`;
      const whatsappUrl = `https://wa.me/?text=${encodeURIComponent(text)}`;
      window.open(whatsappUrl, '_blank');
      
      this.postService.sharePost(post.id).subscribe({
        next: (response) => {
          post.sharesCount = response.sharesCount;
        },
        error: (error) => {
          console.error('Error updating share count:', error);
        }
      });
    }
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    if (tab === 'followers' && this.followers.length === 0) {
      this.loadFollowers();
    }
    if (tab === 'following' && this.followingList.length === 0) {
      this.loadFollowing();
    }
  }

  getTotalLikes(): number {
    return this.userPosts.reduce((total, post) => total + (post.likesCount || 0), 0);
  }

  getTotalComments(): number {
    return this.userPosts.reduce((total, post) => total + (post.commentsCount || 0), 0);
  }

  getRecentActivity(): any[] {
    const activities: any[] = [];
    
    this.userPosts.slice(0, 3).forEach(post => {
      activities.push({
        icon: 'fa-plus-circle',
        text: 'Posted a new update',
        date: post.createdDate
      });
    });
    
    return activities.sort((a: any, b: any) => new Date(b.date).getTime() - new Date(a.date).getTime()).slice(0, 5);
  }

  loadFollowers() {
    if (!this.user) return;
    this.profileService.getFollowers(this.user.username).subscribe({
      next: (followers) => {
        this.followers = followers;
      },
      error: (error) => {
        console.error('Error loading followers:', error);
        this.followers = [];
      }
    });
  }

  loadFollowing() {
    if (!this.user) return;
    this.profileService.getFollowing(this.user.username).subscribe({
      next: (following) => {
        this.followingList = following;
      },
      error: (error) => {
        console.error('Error loading following:', error);
        this.followingList = [];
      }
    });
  }
}