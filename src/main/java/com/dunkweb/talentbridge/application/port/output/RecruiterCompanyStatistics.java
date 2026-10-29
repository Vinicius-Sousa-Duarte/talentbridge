package com.dunkweb.talentbridge.application.port.output;

public record RecruiterCompanyStatistics(
        String company,
        long recruiterCount,
        double averageScore
) {
}
