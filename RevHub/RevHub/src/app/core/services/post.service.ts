import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

export interface Post {
  id: number;
  content: string;
  imageUrl?: string;
  mediaType?: string;
  visibility?: string;
  author: {
    id: number;
    username: string;
    profilePicture?: string;
  };
  likesCount: number;
  commentsCount: number;
  sharesCount: number;
  createdDate: string;
}

export interface PostRequest {
  content: string;
  imageUrl?: string;
  mediaType?: string;
  visibility?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class PostService {
  private apiUrl = '/api/posts';

  constructor(private http: HttpClient) { }

  getPosts(page: number = 0, size: number = 10, feedType: string = 'universal'): Observable<PageResponse<Post>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('feedType', feedType);
    return this.http.get<PageResponse<Post>>(this.apiUrl, { params });
  }

  getPostById(id: number): Observable<Post> {
    return this.http.get<Post>(`${this.apiUrl}/${id}`);
  }

  createPost(postData: PostRequest): Observable<any> {
    return this.http.post(this.apiUrl, postData, { 
      headers: { 'Content-Type': 'application/json' },
      responseType: 'text'
    }).pipe(
      map(response => {
        try {
          return JSON.parse(response);
        } catch (e) {
          return { success: true, message: response };
        }
      })
    );
  }
  
  createPostWithFile(formData: FormData): Observable<any> {
    return this.http.post(`${this.apiUrl}/upload`, formData, { 
      responseType: 'text'
    }).pipe(
      map(response => {
        try {
          return JSON.parse(response);
        } catch (e) {
          return { success: true, message: response };
        }
      })
    );
  }

  updatePost(id: number, postData: PostRequest): Observable<Post> {
    return this.http.put<Post>(`${this.apiUrl}/${id}`, postData);
  }

  deletePost(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`);
  }

  likePost(id: number): Observable<string> {
    return this.http.post<string>(`${this.apiUrl}/${id}/like`, {});
  }

  unlikePost(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}/like`);
  }

  sharePost(id: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/share`, {});
  }

  toggleLike(postId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${postId}/toggle-like`, {});
  }

  addComment(postId: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${postId}/comments`, { content });
  }

  getComments(postId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${postId}/comments`);
  }

  deleteComment(postId: number, commentId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${postId}/comments/${commentId}`);
  }

  addReply(commentId: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/comments/${commentId}/replies`, { content });
  }
  
  searchPosts(query: string): Observable<Post[]> {
    return this.http.get<Post[]>(`${this.apiUrl}/search?query=${encodeURIComponent(query)}`);
  }
}

export interface Comment {
  id: number;
  content: string;
  author: {
    id: number;
    username: string;
    profilePicture?: string;
  };
  createdDate: string;
  replies?: Comment[];
  parentComment?: Comment;
}