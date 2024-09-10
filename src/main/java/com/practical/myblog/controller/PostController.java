package com.practical.myblog.controller;

import com.practical.myblog.model.Post;
import com.practical.myblog.service.PostServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostServiceImpl postServiceImpl;

    @GetMapping
    public List<Post> getPosts() {
        return postServiceImpl.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPost(@PathVariable Long id) {
        return postServiceImpl.getPost(id);
    }

    @PostMapping
    public Post addPost(@RequestBody Post post) {
        return postServiceImpl.addPost(post);
    }

    @PutMapping
    public Post updatePost(@RequestBody Post post){
        return postServiceImpl.updatePost(post);
    }

}
