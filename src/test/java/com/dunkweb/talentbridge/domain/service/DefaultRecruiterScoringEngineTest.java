package com.dunkweb.talentbridge.domain.service;

import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.domain.enums.CareerObjective;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRecruiterScoringEngineTest {

    private final DefaultRecruiterScoringEngine scoringEngine = new DefaultRecruiterScoringEngine();

    @Test
    void shouldReturnZeroWhenProfileHasNoSkills() {
        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of()
        );

        int score = scoringEngine.calculate(profile, "Hiring Java engineers", "AWS and backend");

        assertThat(score).isZero();
    }

    @Test
    void shouldReturnWeightWhenSkillIsFound() {
        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );

        int score = scoringEngine.calculate(profile, "Hiring Java engineers", null);

        assertThat(score).isEqualTo(10);
    }

    @Test
    void shouldReturnZeroWhenSkillIsNotFound() {
        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Kafka", 8))
        );

        int score = scoringEngine.calculate(profile, "Hiring Java engineers", null);

        assertThat(score).isZero();
    }

    @Test
    void shouldSumWeightsWhenMultipleSkillsAreFound() {
        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(
                        new UserProfile.SkillInput("Java", 10),
                        new UserProfile.SkillInput("AWS", 10),
                        new UserProfile.SkillInput("Spring Boot", 8)
                )
        );

        int score = scoringEngine.calculate(profile, "Hiring Java AWS backend engineers", "Spring Boot team");

        assertThat(score).isEqualTo(28);
    }

    @Test
    void shouldReturnZeroWhenRecruiterHasNoHeadlineAndNoDescription() {
        UserProfile profile = UserProfile.create(
                "Senior Backend Developer",
                "Java and AWS specialist",
                Set.of(CareerObjective.SOLUTIONS_ARCHITECT),
                List.of(new UserProfile.SkillInput("Java", 10))
        );

        int score = scoringEngine.calculate(profile, null, null);

        assertThat(score).isZero();
    }
}
