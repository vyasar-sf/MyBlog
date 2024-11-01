package com.practical.myblog.service;

import com.practical.myblog.dto.PostRequestDTO;
import com.practical.myblog.dto.PostResponseDTO;
import com.practical.myblog.dto.TagResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

public interface PostService {

    /**
     * Gets all posts
     * @param pageNo Page number
     * @param pageSize Page size
     * @return Pages of the posts
     */
    Page<PostResponseDTO> getAllPosts(int pageNo, int pageSize);

    /**
     * Adds a post
     * @param postRequestDTO DTO for post requests
     * @return Added post
     */
    PostResponseDTO addPost(PostRequestDTO postRequestDTO);

    /**
     * Gets post
     * @param id ID of a post
     * @return Post matching the ID
     */
    PostResponseDTO getPost(Long id);

    /**
     * Gets tag relations of post
     * @param id ID of a post
     * @return Set of tags for the post with matching ID
     */
    Set<TagResponseDTO> getTagsOfPost(Long id);

    /**
     * Adds tag relations to post
     * @param id ID of a post
     * @param tagNames List of tags to add to a post
     * @return Post with updated tags, with response entity and post ID in headers
     */
    ResponseEntity<PostResponseDTO> addTagsToPost(Long id, List<String> tagNames);

    /**
     * Removes tag relations from post
     * @param postId ID of a post
     * @param tagNames List of tags to remove from a post
     */
    void removeTagsFromPost(Long postId, List<String> tagNames);

    /**
     * Gets all posts for a tag
     * @param tagName A name of a tag
     * @param pageNo Page number
     * @param pageSize Page size
     * @return Page of all posts matching this tag name
     */
    Page<PostResponseDTO> getAllPostsForTag(String tagName, int pageNo, int pageSize);

    /**
     * Updates post
     * @param postRequestDTO DTO for post requests
     * @return Updated post
     */
    PostResponseDTO updatePost(Long id, PostRequestDTO postRequestDTO);

    /**
     * Deletes post
     * @param id ID of the post
     */
    void deletePost(Long id);

    /**
     * Returns all posts that contains the keyword either in the title or text
     * @param keyword Keyword to look for
     * @param pageNo Page number
     * @param pageSize Page size
     * @return Page of matching posts
     */
    Page<PostResponseDTO> searchByKeyword(String keyword, int pageNo, int pageSize);
}
