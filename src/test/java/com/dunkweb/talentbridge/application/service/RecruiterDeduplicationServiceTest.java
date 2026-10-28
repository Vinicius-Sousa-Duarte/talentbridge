package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.RecruiterRepository;
import com.dunkweb.talentbridge.domain.valueobject.RecruiterProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruiterDeduplicationServiceTest {

    @Mock
    private RecruiterRepository recruiterRepository;

    @Test
    void shouldDetectDuplicateByLinkedinUrl() {
        RecruiterDeduplicationService service = new RecruiterDeduplicationService(recruiterRepository);
        RecruiterProfile profile = new RecruiterProfile(
                "Ana",
                "Tech Recruiter",
                "Acme",
                "Sao Paulo",
                "https://www.linkedin.com/in/ana",
                "Hiring Java",
                null
        );

            when(recruiterRepository.findByLinkedinUrl("https://www.linkedin.com/in/ana")).thenReturn(Optional.empty());
        when(recruiterRepository.findByNameAndCompany("Ana", "Acme")).thenReturn(Optional.empty());

        Set<String> urlKeys = new HashSet<>();
        Set<String> nameCompanyKeys = new HashSet<>();

        boolean first = service.isDuplicate(profile, urlKeys, nameCompanyKeys);
        boolean second = service.isDuplicate(profile, urlKeys, nameCompanyKeys);

        assertThat(first).isFalse();
        assertThat(second).isTrue();
    }

    @Test
    void shouldDetectDuplicateByNameAndCompanyWhenUrlMissing() {
        RecruiterDeduplicationService service = new RecruiterDeduplicationService(recruiterRepository);
        RecruiterProfile profile = new RecruiterProfile(
                "Bruno",
                "Talent Acquisition",
                "CloudBridge",
                "Lisbon",
                null,
                "Talent Acquisition",
                null
        );

        when(recruiterRepository.findByNameAndCompany("Bruno", "CloudBridge")).thenReturn(Optional.empty());

        Set<String> urlKeys = new HashSet<>();
        Set<String> nameCompanyKeys = new HashSet<>();

        boolean first = service.isDuplicate(profile, urlKeys, nameCompanyKeys);
        boolean second = service.isDuplicate(profile, urlKeys, nameCompanyKeys);

        assertThat(first).isFalse();
        assertThat(second).isTrue();
    }
}
