package com.cooksync_server.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cooksync_server.dtos.request.CreateTagRequest;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.services.TagService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TagService tagService;

    public AdminController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/tags/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable String id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PostMapping("/tags")
    public ResponseEntity<Tag> createTag(@RequestBody CreateTagRequest request) {
        Tag tag = Tag.builder().name(request.getName()).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.createTag(tag));
    }

    @DeleteMapping("/tags/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable String id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
