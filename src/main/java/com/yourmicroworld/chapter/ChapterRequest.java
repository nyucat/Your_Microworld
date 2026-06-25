package com.yourmicroworld.chapter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ChapterRequest(@NotBlank @Size(max = 200) String title, @NotBlank @Size(max = 100000) String content) { }
