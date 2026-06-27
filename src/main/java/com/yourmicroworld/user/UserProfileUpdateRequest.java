package com.yourmicroworld.user;

import jakarta.validation.constraints.Size;

public record UserProfileUpdateRequest(
        @Size(max = 1000) String bio
) {
}
