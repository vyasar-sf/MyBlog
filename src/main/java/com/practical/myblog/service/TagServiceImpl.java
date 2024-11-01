package com.practical.myblog.service;

import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.exception.TagValidationException;
import com.practical.myblog.model.Tag;
import com.practical.myblog.repository.TagRepository;
import com.practical.myblog.util.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService{

    private final TagRepository tagRepository;

    @Override
    public Page<TagResponseDTO> getAllTags(int pageNo, int pageSize) {
        log.info("Retrieving all tags with pagination - Page: {}, Size: {}", pageNo, pageSize);
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        return tagRepository.findAll(pageable)
                .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()));
    }

    @Override
    public TagResponseDTO getTag(Long id) {
        log.info("Fetching tag with id: {}", id);
        return tagRepository.findById(id)
                .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()))
                .orElseThrow(() -> new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public List<TagResponseDTO> addTag(TagRequestDTO tagRequestDTO) {
        log.info("Adding tags: {}", tagRequestDTO.getTags());
        return tagRequestDTO.getTags().stream()
                .map(tagName -> {
                    Tag tag = new Tag();
                    tag.setName(tagName);
                    validateTag(tag);
                    Tag savedTag = tagRepository.save(tag);
                    return new TagResponseDTO(savedTag.getId(), savedTag.getName());
                })
                .collect(Collectors.toList());
    }

    @Override
    public TagResponseDTO updateTagName(Long id, TagRequestDTO tagRequestDTO) {
        log.info("Updating tag with id: {}", id);

        if (tagRequestDTO.getTags().size() > 1) {
            log.error("Attempted to update with multiple tags.");
            throw new TagValidationException(ErrorMessages.ONE_TAG_TO_UPDATE);
        }

        if (tagRepository.findByName(tagRequestDTO.getTags().get(0)).isPresent()) {
            log.error("Tag name is not unique: {}", tagRequestDTO.getTags().get(0));
            throw new TagValidationException(ErrorMessages.TAG_NOT_UNIQUE);
        }

        Tag updatedTag = tagRepository.findById(id)
                .map(existingTag -> {
                    existingTag.setName(tagRequestDTO.getTags().get(0));
                    return tagRepository.save(existingTag);
                })
                .orElseThrow(() -> new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_ID + id));

        return new TagResponseDTO(updatedTag.getId(), updatedTag.getName());
    }

    @Override
    public void deleteTag(Long id) {
        log.info("Deleting tag with id: {}", id);
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
            log.info("Tag with id: {} has been deleted.", id);
        } else {
            log.error("Tag not found for deletion with id: {}", id);
            throw new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_ID + id);
        }
    }

    private void validateTag(Tag tag) {
        log.info("Validating tag: {}", tag.getName());
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            log.error("Tag name cannot be empty.");
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }

        if (tagRepository.findByName(tag.getName()).isPresent()) {
            log.error("Tag name is not unique on validateTag: {}", tag.getName());
            throw new TagValidationException(ErrorMessages.TAG_NOT_UNIQUE);
        }
    }
}
