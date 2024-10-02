package com.practical.myblog.service;

import com.practical.myblog.dto.PostRequestDTO;
import com.practical.myblog.dto.PostResponseDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.exception.PostValidationException;
import com.practical.myblog.exception.TagValidationException;
import com.practical.myblog.model.Post;
import com.practical.myblog.model.Tag;
import com.practical.myblog.repository.PostRepository;
import com.practical.myblog.repository.TagRepository;
import com.practical.myblog.util.ErrorMessages;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    // Constructor injection, making the dependency immutable
    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> new PostResponseDTO(post.getId(), post.getTitle(), post.getText()))
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO getPost(Long id) {
        return postRepository.findById(id)
                .map(post -> new PostResponseDTO(post.getId(), post.getTitle(), post.getText()))
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public PostResponseDTO addPost(PostRequestDTO postRequestDTO) {
        isTitleEmpty(postRequestDTO);

        Post post = new Post();
        post.setText(postRequestDTO.getText());
        post.setTitle(postRequestDTO.getTitle());

        Post savedPost = postRepository.save(post);
        return new PostResponseDTO(savedPost.getId(), savedPost.getTitle(), savedPost.getText());
    }

    @Override
    public Set<TagResponseDTO> getTagsOfPost(Long id) {
        if (postRepository.findById(id).isEmpty()) {
            throw new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id);
        }

         return postRepository.tagsByPost(id).stream()
                 .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()))
                 .collect(Collectors.toSet());
    }

    @Override
    public ResponseEntity<PostResponseDTO> addTagsToPost(Long id, List<String> tagNames) {
        if (tagNames.isEmpty()) {
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id));

        Set<Tag> tags = retrieveTags(id, tagNames, ErrorMessages.TAG_ALREADY_EXISTS);
        post.getTags().addAll(tags);

        Post savedPost = postRepository.save(post);
        var postResponseDTO = new PostResponseDTO(savedPost.getId(), savedPost.getTitle(), savedPost.getText());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Post-ID", String.valueOf(postResponseDTO.getId()));
        return ResponseEntity.ok().headers(headers).body(postResponseDTO);
    }


    @Override
    public void removeTagsFromPost(Long postId, List<String> tagNames) {
        if (tagNames.isEmpty()) {
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + postId));

        Set<Tag> tagsToRemove = retrieveTags(postId, tagNames, ErrorMessages.TAG_NOT_EXISTS_IN_POST);

        Set<Tag> tagsInPost = postRepository.tagsByPost(postId);
        if (tagsInPost.isEmpty()) {
            throw new PostValidationException(ErrorMessages.NO_TAGS_IN_POST);
        }

        // Iterate through the tags to remove
        for (Tag tagToRemove : tagsToRemove) {
            if (!tagsInPost.contains(tagToRemove)) {
                throw new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_NAME + tagToRemove.getName());
            }
            tagsInPost.remove(tagToRemove);
        }

        post.setTags(tagsInPost);
        postRepository.save(post);
    }

    @Override
    public List<PostResponseDTO> getAllPostsForTag(String tagName) {
        isTagEmpty(tagName);

        List<Post> matchingPosts = postRepository.findAllPostsByTagName(tagName);
        if (matchingPosts.isEmpty()) {
            throw new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_NAME + tagName);
        }

        return matchingPosts.stream()
                .map(post -> new PostResponseDTO(post.getId(), post.getTitle(), post.getText()))
                .collect(Collectors.toList());
    }

    @Override
    public PostResponseDTO updatePost(Long id, PostRequestDTO postRequestDTO) {
        isTitleEmpty(postRequestDTO);

        Post updatedPost = postRepository.findById(id)
                .map(existingPost -> {
                    existingPost.setTitle(postRequestDTO.getTitle());
                    existingPost.setText(postRequestDTO.getText());
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id));

        return new PostResponseDTO(id, updatedPost.getTitle(), updatedPost.getText());
    }

    @Override
    public void deletePost(Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        } else {
            throw new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id);
        }
    }
    

    private @NotNull Set<Tag> retrieveTags(Long postId, List<String> tagNames, String errorMessage) {
        return tagNames.stream()
                .peek(this::isTagEmpty)  // Validate each tag without modifying the stream
                .map(tagName -> {
                    // Check if the tag already exists for the post
                    if (postRepository.existsTagForPost(postId, tagName)) {
                        throw new TagValidationException(tagName + errorMessage);
                    }
                    return tagRepository.findByName(tagName)
                            .orElseThrow(() -> new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_NAME + tagName));
                }).collect(Collectors.toSet());
    }


    private static void isTitleEmpty(@NotNull PostRequestDTO postRequestDTO) {
        if (postRequestDTO.getTitle() == null || postRequestDTO.getTitle().trim().isEmpty()) {
            throw new PostValidationException(ErrorMessages.POST_TITLE_CANNOT_BE_EMPTY);
        }
    }

    private void isTagEmpty(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }
    }

}
