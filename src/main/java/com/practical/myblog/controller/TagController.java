package com.practical.myblog.controller;

import com.practical.myblog.dto.TagRequestDTO;
import com.practical.myblog.dto.TagResponseDTO;
import com.practical.myblog.service.TagServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {
    @Autowired
    private TagServiceImpl tagServiceImpl;

    @GetMapping
    public List<TagResponseDTO> getTags() {
        return tagServiceImpl.getAllTags();
    }

    @GetMapping("/{id}")
    public TagResponseDTO getTag(@PathVariable Long id) {
        return tagServiceImpl.getTag(id);
    }

    @PostMapping
    public List<TagResponseDTO> addTag(@RequestBody TagRequestDTO tagRequestDTO) {
        return tagServiceImpl.addTag(tagRequestDTO);
    }

    @PutMapping("/{id}")
    public TagResponseDTO updateTag(@PathVariable Long id, @RequestBody TagRequestDTO tagRequestDTO){
        return tagServiceImpl.updateTag(id, tagRequestDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagServiceImpl.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
