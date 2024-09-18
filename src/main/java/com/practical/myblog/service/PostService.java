package com.practical.myblog.service;

import com.practical.myblog.dto.PostRequestDTO;
import com.practical.myblog.dto.PostResponseDTO;
import com.practical.myblog.dto.TagResponseDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

public interface PostService {

    /**
     *
     * @return List of posts
     */
    List<PostResponseDTO> getAllPosts();

    /**
     *
     * @param postRequestDTO DTO for post requests
     * @return Added post
     */
    PostResponseDTO addPost(PostRequestDTO postRequestDTO);

    /**
     *
     * @param id ID of a post
     * @return Post matching the ID
     */
    PostResponseDTO getPost(Long id);

    /**
     *
     * @param id ID of a post
     * @return Set of tags for the post with matching ID
     */
    Set<TagResponseDTO> getTagsOfPost(Long id);

    /**
     *
     * @param id ID of a post
     * @param tagNames List of tags to add to a post
     * @return Post with updated tags, with response entity and post ID in headers
     */
    ResponseEntity<PostResponseDTO> addTagsToPost(Long id, List<String> tagNames);

    /**
     *
     * @param postId ID of a post
     * @param tagNames List of tags to remove from a post
     */
    void removeTagsFromPost(Long postId, List<String> tagNames);

    /**
     *
     * @param tagName A name of a tag
     * @return List of all posts matching this tag name
     */
    List<PostResponseDTO> getAllPostsForTag(String tagName);

    /**
     *
     * @param postRequestDTO DTO for post requests
     * @return Updated post
     */
    PostResponseDTO updatePost(Long id, PostRequestDTO postRequestDTO);

    /**
     *
     * @param id ID of the post
     */
    void deletePost(Long id);
}
