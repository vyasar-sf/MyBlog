package com.practical.myblog.controller;

import com.practical.myblog.dto.PostRequestDTO;
import com.practical.myblog.dto.PostResponseDTO;
import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.service.PostServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostServiceImpl postServiceImpl;

    public PostController(PostServiceImpl postServiceImpl) {
        this.postServiceImpl = postServiceImpl;
    }

    @GetMapping
    public Page<PostResponseDTO> getPosts(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return postServiceImpl.getAllPosts(pageNo, pageSize);
    }

    @GetMapping("/{id}")
    public PostResponseDTO getPost(@PathVariable Long id) {
        return postServiceImpl.getPost(id);
    }

    @PostMapping
    public PostResponseDTO addPost(@Validated @RequestBody PostRequestDTO postRequestDTO) {
        return postServiceImpl.addPost(postRequestDTO);
    }

    @PutMapping("/{id}")
    public PostResponseDTO updatePost(@PathVariable Long id, @Validated @RequestBody PostRequestDTO postRequestDTO){
        return postServiceImpl.updatePost(id, postRequestDTO);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postServiceImpl.deletePost(postId);
        // The request was successfully processed, but there is no content to send back
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tags/{postId}")
    public Set<TagResponseDTO> getTagsOfPost(@PathVariable Long postId) {
        return postServiceImpl.getTagsOfPost(postId);
    }

    @PostMapping("/{postId}/tags")
    public ResponseEntity<PostResponseDTO> addTagsToPost(@PathVariable Long postId, @Validated @RequestBody TagRequestDTO tagRequestDTO) {
        return postServiceImpl.addTagsToPost(postId, tagRequestDTO.getTags());
    }

    @DeleteMapping("/{postId}/tags")
    public ResponseEntity<Void> removeTagsFromPost(@PathVariable Long postId, @Validated @RequestBody TagRequestDTO tagRequestDTO) {
        postServiceImpl.removeTagsFromPost(postId, tagRequestDTO.getTags());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tag")
    public Page<PostResponseDTO> getAllPostsForTag(
            @RequestParam("tagName") String tagName,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return postServiceImpl.getAllPostsForTag(tagName, pageNo, pageSize);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostResponseDTO>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<PostResponseDTO> posts = postServiceImpl.searchByKeyword(keyword, pageNo, pageSize);
        return ResponseEntity.ok(posts);
    }
}
