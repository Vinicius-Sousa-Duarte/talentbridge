package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;

import java.util.List;

public interface LinkedInPageClient {

    void openSearch(String query);

    List<RecruiterProfile> extractRecruitersFromCurrentPage();

    boolean goToNextPage();
}
