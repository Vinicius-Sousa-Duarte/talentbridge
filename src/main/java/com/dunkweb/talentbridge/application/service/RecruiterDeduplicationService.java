package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecruiterDeduplicationService {

    private final RecruiterRepository recruiterRepository;

    public boolean isDuplicate(
            RecruiterProfile profile,
            Set<String> sessionLinkedinUrls,
            Set<String> sessionNameCompanyKeys
    ) {
        String linkedinUrlKey = normalizeUrl(profile.linkedinUrl());
        if (linkedinUrlKey != null) {
            if (sessionLinkedinUrls.contains(linkedinUrlKey)
                    || recruiterRepository.findByLinkedinUrl(linkedinUrlKey).isPresent()) {
                return true;
            }
            sessionLinkedinUrls.add(linkedinUrlKey);
        }

        String nameCompanyKey = normalizeNameCompany(profile.name(), profile.company());
        if (nameCompanyKey != null) {
            if (sessionNameCompanyKeys.contains(nameCompanyKey)
                    || recruiterRepository.findByNameAndCompany(profile.name(), profile.company()).isPresent()) {
                return true;
            }
            sessionNameCompanyKeys.add(nameCompanyKey);
        }

        return false;
    }

    private String normalizeUrl(String linkedinUrl) {
        if (linkedinUrl == null || linkedinUrl.isBlank()) {
            return null;
        }

        String normalized = linkedinUrl.trim().toLowerCase();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String normalizeNameCompany(String name, String company) {
        if (name == null || name.isBlank() || company == null || company.isBlank()) {
            return null;
        }
        return name.trim().toLowerCase() + "|" + company.trim().toLowerCase();
    }
}
