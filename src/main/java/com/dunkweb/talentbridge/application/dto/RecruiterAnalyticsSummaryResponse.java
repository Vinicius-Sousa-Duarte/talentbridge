package com.dunkweb.talentbridge.application.dto;

public record RecruiterAnalyticsSummaryResponse(
        long totalRecruiters,
        double averageScore,
        int highestScore,
        int lowestScore,
        long pendingReview,
        long approved,
        long inviteSent,
        long connected,
        long dismissed
) {
}
