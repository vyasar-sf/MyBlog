package com.practical.myblog.controller;

import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.service.TagServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagServiceImpl tagServiceImpl;

    public TagController(TagServiceImpl tagServiceImpl) {
        this.tagServiceImpl = tagServiceImpl;
    }

    @GetMapping
    public Page<TagResponseDTO> getTags(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return tagServiceImpl.getAllTags(pageNo, pageSize);
    }

    @GetMapping("/{id}")
    public TagResponseDTO getTag(@PathVariable Long id) {
        return tagServiceImpl.getTag(id);
    }

    @PostMapping
    public List<TagResponseDTO> addTag(@Validated @RequestBody TagRequestDTO tagRequestDTO) {
        return tagServiceImpl.addTag(tagRequestDTO);
    }

    @PatchMapping("/{id}")
    public TagResponseDTO updateTagName(@PathVariable Long id, @Validated @RequestBody TagRequestDTO tagRequestDTO){
        return tagServiceImpl.updateTagName(id, tagRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagServiceImpl.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
