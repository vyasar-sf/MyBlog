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
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.mapper.orm.Search;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final EntityManager entityManager;

    @Override
    public Page<PostResponseDTO> getAllPosts(int pageNo, int pageSize) {
        log.info("Retrieving all posts with pagination - Page: {}, Size: {}", pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return postRepository.findAll(pageable)
                .map(post -> modelMapper.map(post, PostResponseDTO.class));
    }

    @Override
    public PostResponseDTO getPost(Long id) {
        log.info("Retrieving post with id: {}", id);
        return postRepository.findById(id)
                .map(post -> modelMapper.map(post, PostResponseDTO.class))
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public PostResponseDTO addPost(PostRequestDTO postRequestDTO) {
        isTitleEmpty(postRequestDTO);

        Post post = modelMapper.map(postRequestDTO, Post.class);
        log.info("Adding post with title: {}", postRequestDTO.getTitle());
        Post savedPost = postRepository.save(post);
        log.info("Post added with id: {}", savedPost.getId());

        return modelMapper.map(savedPost, PostResponseDTO.class);
    }

    @Override
    public Set<TagResponseDTO> getTagsOfPost(Long id) {
        log.info("Retrieving tags for post id: {}", id);
        if (postRepository.findById(id).isEmpty()) {
            log.error("Post not found with id: {}", id);
            throw new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id);
        }

         return postRepository.tagsByPost(id).stream()
                 .map(tag -> modelMapper.map(tag, TagResponseDTO.class))
                 .collect(Collectors.toSet());
    }

    @Override
    public ResponseEntity<PostResponseDTO> addTagsToPost(Long id, List<String> tagNames) {
        if (tagNames.isEmpty()) {
            log.error("Tag names cannot be empty");
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }

        log.info("Adding tags to post id: {} with tags: {}", id, tagNames);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id));

        Set<Tag> tags = retrieveTags(id, tagNames, ErrorMessages.TAG_ALREADY_EXISTS);
        post.getTags().addAll(tags);

        Post savedPost = postRepository.save(post);
        log.info("Tags added to post id: {}", savedPost.getId());

        var postResponseDTO = new PostResponseDTO(savedPost.getId(), savedPost.getTitle(), savedPost.getText(), post.getImageUrl(), post.getVideoUrl());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Post-ID", String.valueOf(postResponseDTO.getId()));
        return ResponseEntity.ok().headers(headers).body(postResponseDTO);
    }


    @Override
    public void removeTagsFromPost(Long postId, List<String> tagNames) {
        if (tagNames.isEmpty()) {
            log.error("Tag names cannot be empty on removeTagsFromPost");
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }

        log.info("Removing tags from post id: {} with tags: {}", postId, tagNames);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + postId));

        Set<Tag> tagsToRemove = retrieveTags(postId, tagNames, ErrorMessages.TAG_NOT_EXISTS_IN_POST);

        Set<Tag> tagsInPost = postRepository.tagsByPost(postId);
        if (tagsInPost.isEmpty()) {
            log.error("No tags found in post id: {}", postId);
            throw new PostValidationException(ErrorMessages.NO_TAGS_IN_POST);
        }

        // Iterate through the tags to remove
        for (Tag tagToRemove : tagsToRemove) {
            if (!tagsInPost.contains(tagToRemove)) {
                log.error("Tag not found in post: {}", tagToRemove.getName());
                throw new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_NAME + tagToRemove.getName());
            }
            tagsInPost.remove(tagToRemove);
        }

        post.setTags(tagsInPost);
        postRepository.save(post);
    }

    @Override
    public Page<PostResponseDTO> getAllPostsForTag(String tagName, int pageNo, int pageSize) {
        isTagEmpty(tagName);

        log.info("Retrieving posts for tag: {}", tagName);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var matchingPosts = postRepository.findAllPostsByTagName(tagName, pageable)
                .orElseThrow(() -> new PostValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_NAME + tagName));

        return matchingPosts.map(post -> modelMapper.map(post, PostResponseDTO.class));
    }

    @Override
    public PostResponseDTO updatePost(Long id, PostRequestDTO postRequestDTO) {
        isTitleEmpty(postRequestDTO);

        log.info("Updating post with id: {}", id);
        Post updatedPost = postRepository.findById(id)
                .map(existingPost -> {
                    existingPost.setTitle(postRequestDTO.getTitle());
                    existingPost.setText(postRequestDTO.getText());
                    return postRepository.save(existingPost);
                })
                .orElseThrow(() -> new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id));

        log.info("Post updated with id: {}", updatedPost.getId());
        return new PostResponseDTO(id, updatedPost.getTitle(), updatedPost.getText(), updatedPost.getImageUrl(), updatedPost.getVideoUrl());
    }

    @Override
    public void deletePost(Long id) {
        log.info("Deleting post with id: {}", id);
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
            log.info("Post deleted with id: {}", id);
        } else {
            log.error("Post not found with id on deletePost: {}", id);
            throw new PostValidationException(ErrorMessages.POST_NOT_FOUND_WITH_ID + id);
        }
    }

    private @NotNull Set<Tag> retrieveTags(Long postId, List<String> tagNames, String errorMessage) {
        return tagNames.stream()
                .peek(this::isTagEmpty)  // Validate each tag without modifying the stream
                .map(tagName -> {
                    // Check if the tag already exists for the post
                    if (postRepository.existsTagForPost(postId, tagName)) {
                        log.error("Tag already exists for post: {}", tagName);
                        throw new TagValidationException(tagName + errorMessage);
                    }
                    return tagRepository.findByName(tagName)
                            .orElseThrow(() -> new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_NAME + tagName));
                }).collect(Collectors.toSet());
    }

    @Override
    public Page<PostResponseDTO> searchByKeyword(String keyword, int pageNo, int pageSize) {
        log.info("Searching for posts with keyword: '{}', page: {}, size: {}", keyword, pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        List<Post> result = Search.session(entityManager)
                .search(Post.class)
                .where(f -> f.bool()
                        .should(f.match().fields("title", "text").matching(keyword))
                )
                .fetchAllHits();

        if (result.isEmpty()) {
            log.warn("No posts found for keyword: '{}'", keyword);
            throw new PostValidationException(ErrorMessages.POST_NOT_FOUND_FOR_KEYWORD + keyword);
        }

        List<PostResponseDTO> postResponseDTOs = result.stream()
                .map(post -> modelMapper.map(post, PostResponseDTO.class))
                .toList();

        log.info("Found {} posts for keyword: '{}'", postResponseDTOs.size(), keyword);
        return new PageImpl<>(postResponseDTOs, pageable, result.size());
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void rebuildIndexOnStartup() throws InterruptedException {
        Search.session(entityManager).massIndexer().startAndWait();
    }


    private static void isTitleEmpty(@NotNull PostRequestDTO postRequestDTO) {
        if (postRequestDTO.getTitle() == null || postRequestDTO.getTitle().trim().isEmpty()) {
            log.error("Post title cannot be empty");
            throw new PostValidationException(ErrorMessages.POST_TITLE_CANNOT_BE_EMPTY);
        }
    }

    private void isTagEmpty(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            log.error("Tag name cannot be empty");
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }
    }
}
