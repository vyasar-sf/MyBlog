package com.practical.myblog.service;

import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;

import java.util.List;

public interface TagService {

    /**
     *
     * @return All tags in database
     */
    List<TagResponseDTO> getAllTags();

    /**
     *
     * @param tagRequestDTO DTO for tag requests
     * @return Added tag
     */
    List<TagResponseDTO> addTag(TagRequestDTO tagRequestDTO);

    /**
     *
     * @param id ID of a tag
     * @return Tag with matching ID
     */
    TagResponseDTO getTag(Long id);

    /**
     *
     * @param id ID of a tag
     * @param tagRequestDTO DTO for tag requests
     * @return Updated tag
     */
    TagResponseDTO updateTag(Long id, TagRequestDTO tagRequestDTO);

    /**
     *
     * @param id ID of a tag
     */
    void deleteTag(Long id);
}
