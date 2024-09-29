package com.practical.myblog.service;

import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.exception.TagValidationException;
import com.practical.myblog.model.Tag;
import com.practical.myblog.repository.TagRepository;
import com.practical.myblog.util.ErrorMessages;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService{

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    public List<TagResponseDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public TagResponseDTO getTag(Long id) {
        return tagRepository.findById(id)
                .map(tag -> new TagResponseDTO(tag.getId(), tag.getName()))
                .orElseThrow(() -> new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_ID + id));
    }

    @Override
    public List<TagResponseDTO> addTag(TagRequestDTO tagRequestDTO) {

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
        if (tagRequestDTO.getTags().size() > 1) {
            throw new TagValidationException(ErrorMessages.ONE_TAG_TO_UPDATE);
        }

        if (tagRepository.findByName(tagRequestDTO.getTags().get(0)).isPresent()) {
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
        if (tagRepository.existsById(id)) {
            tagRepository.deleteById(id);
        } else {
            throw new TagValidationException(ErrorMessages.TAG_NOT_FOUND_WITH_ID + id);
        }
    }

    private void validateTag(Tag tag) {
        if (tag.getName() == null || tag.getName().trim().isEmpty()) {
            throw new TagValidationException(ErrorMessages.TAG_NAME_CANNOT_BE_EMPTY);
        }

        if (tagRepository.findByName(tag.getName()).isPresent()) {
            throw new TagValidationException(ErrorMessages.TAG_NOT_UNIQUE);
        }
    }
}
