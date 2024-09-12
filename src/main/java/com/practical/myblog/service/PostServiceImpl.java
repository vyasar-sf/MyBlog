package com.practical.myblog.service;

import com.practical.myblog.util.ErrorMessages;
import com.practical.myblog.exception.PostValidationException;
import com.practical.myblog.model.Post;
import com.practical.myblog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    // Constructor injection, making the dependency immutable
    @Autowired
    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Post addPost(Post post) {
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new PostValidationException(ErrorMessages.TITLE_CANNOT_BE_EMPTY);
        }
        return postRepository.save(post);
    }

    @Override
    public Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public Post updatePost(Post post) {
        return postRepository.findById(post.getId())
                .map(existingPost -> {
                    existingPost.setTitle(post.getTitle());
                    existingPost.setText(post.getText());
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + post.getId()));
    }

}
