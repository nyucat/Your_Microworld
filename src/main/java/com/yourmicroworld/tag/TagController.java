package com.yourmicroworld.tag;

import com.yourmicroworld.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping
    public ApiResponse<List<String>> list() {
        return ApiResponse.ok(
                tagRepository.findAllByOrderByNameAsc().stream().map(Tag::getName).toList()
        );
    }
}
