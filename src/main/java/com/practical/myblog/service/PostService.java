package com.practical.myblog.service;

import com.practical.myblog.model.Post;
import com.practical.myblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post addPost(Post post) {
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        return postRepository.save(post);
    }

    public Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post not found with id: " + id));
    }


    public Post updatePost(Post post) {
        return postRepository.findById(post.getId())
                .map(existingPost -> {
                    existingPost.setTitle(post.getTitle());
                    existingPost.setText(post.getText());
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new NoSuchElementException("Post not found with id: " + post.getId()));
    }

}
