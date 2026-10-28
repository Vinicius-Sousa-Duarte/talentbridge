package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecruiterSearchQueryBuilderTest {

    @Mock
    private KeywordRepository keywordRepository;

    @Test
    void shouldReturnEmptyQueriesWhenProfileHasNoSkills() {
        RecruiterSearchQueryBuilder builder = new RecruiterSearchQueryBuilder(keywordRepository);

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
            List.of()
        );

        List<String> queries = builder.buildQueries(profile);

        assertThat(queries).isEmpty();
    }

    @Test
    void shouldReturnEmptyQueriesWhenNoKeywordsArePersisted() {
        RecruiterSearchQueryBuilder builder = new RecruiterSearchQueryBuilder(keywordRepository);

        when(keywordRepository.findAll()).thenReturn(List.of());

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );

        List<String> queries = builder.buildQueries(profile);

        assertThat(queries).isEmpty();
    }

    @Test
    void shouldGenerateQueriesWhenProfileHasSkills() {
        RecruiterSearchQueryBuilder builder = new RecruiterSearchQueryBuilder(keywordRepository);

        when(keywordRepository.findAll()).thenReturn(List.of(
                Keyword.create("Tech Recruiter")
        ));

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(
                        new UserProfile.SkillInput("Java", 10),
                        new UserProfile.SkillInput("AWS", 8)
                )
        );

        List<String> queries = builder.buildQueries(profile);

        assertThat(queries).containsExactly(
                "Tech Recruiter Java",
                "Tech Recruiter AWS"
        );
    }

    @Test
    void shouldGenerateCombinedQueriesFromSkillsAndKeywords() {
        RecruiterSearchQueryBuilder builder = new RecruiterSearchQueryBuilder(keywordRepository);

        when(keywordRepository.findAll()).thenReturn(List.of(
                Keyword.create("Tech Recruiter"),
                Keyword.create("Talent Acquisition")
        ));

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(
                        new UserProfile.SkillInput("Java", 10),
                        new UserProfile.SkillInput("AWS", 9),
                        new UserProfile.SkillInput("Spring Boot", 8)
                )
        );

        List<String> queries = builder.buildQueries(profile);

        assertThat(queries).contains(
                "Tech Recruiter Java",
                "Tech Recruiter AWS",
                "Tech Recruiter Spring Boot",
                "Talent Acquisition Java",
                "Talent Acquisition AWS"
        );
    }

    @Test
    void shouldRespectMaximumQueryGenerationLimit() {
        RecruiterSearchQueryBuilder builder = new RecruiterSearchQueryBuilder(keywordRepository);

        when(keywordRepository.findAll()).thenReturn(List.of(
                Keyword.create("Tech Recruiter"),
                Keyword.create("Technical Recruiter")
        ));

        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and cloud specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(
                        new UserProfile.SkillInput("Java", 10),
                        new UserProfile.SkillInput("AWS", 9),
                        new UserProfile.SkillInput("Spring Boot", 8)
                )
        );

        List<String> queries = builder.buildQueries(profile, 4);

        assertThat(queries).hasSize(4);
        assertThat(queries).containsExactly(
                "Tech Recruiter Java",
                "Tech Recruiter AWS",
                "Tech Recruiter Spring Boot",
                "Technical Recruiter Java"
        );
    }
}
