package com.yourmicroworld.user;

import java.util.List;

public record UserInteractionResponse(
        List<ReceivedCommentResponse> receivedComments
) {
}
