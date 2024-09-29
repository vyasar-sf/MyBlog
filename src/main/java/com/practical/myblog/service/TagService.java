package com.practical.myblog.service;

import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;

import java.util.List;

public interface TagService {

    /**
     * Gets all tags
     * @return All tags in database
     */
    List<TagResponseDTO> getAllTags();

    /**
     * Adds a tag or a list of tags
     * @param tagRequestDTO DTO for tag requests
     * @return Added tag or a list of tags
     */
    List<TagResponseDTO> addTag(TagRequestDTO tagRequestDTO);

    /**
     * Gets tag
     * @param id ID of a tag
     * @return Tag with matching ID
     */
    TagResponseDTO getTag(Long id);

    /**
     * Updates fields of tag
     * @param id ID of a tag
     * @param tagRequestDTO DTO for tag requests
     * @return Updated tag
     */
    TagResponseDTO updateTagName(Long id, TagRequestDTO tagRequestDTO);

    /**
     * Deletes tag
     * @param id ID of a tag
     */
    void deleteTag(Long id);
}
