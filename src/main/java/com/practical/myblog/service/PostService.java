package com.practical.myblog.service;

import com.practical.myblog.model.Post;

import java.util.List;

public interface PostService {

    List<Post> getAllPosts();

    Post addPost(Post post);

    Post getPost(Long id);

    Post updatePost(Post post);
}
