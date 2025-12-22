package com.example.revHubBack.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;
    
    private String mediaType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostVisibility visibility = PostVisibility.PUBLIC;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private Integer likesCount = 0;

    @Column(nullable = false)
    private Integer commentsCount = 0;

    @Column(nullable = false)
    private Integer sharesCount = 0;

    @CreationTimestamp
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Like> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Share> shares;
}