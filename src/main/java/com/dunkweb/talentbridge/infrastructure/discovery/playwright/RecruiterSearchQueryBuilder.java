package com.dunkweb.talentbridge.infrastructure.discovery.playwright;

import com.dunkweb.talentbridge.application.port.output.KeywordRepository;
import com.dunkweb.talentbridge.domain.entity.Keyword;
import com.dunkweb.talentbridge.domain.entity.SkillPreference;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.SEARCH;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecruiterSearchQueryBuilder {

    private static final int MAX_SKILLS_FOR_DISCOVERY = 10;

    private final KeywordRepository keywordRepository;

    public List<String> buildQueries(UserProfile profile) {
        return buildQueries(profile, Integer.MAX_VALUE);
    }

    public List<String> buildQueries(UserProfile profile, int maxQueries) {
        if (profile == null || profile.getSkillPreferences() == null || profile.getSkillPreferences().isEmpty()) {
            return List.of();
        }

        List<String> loadedKeywords = keywordRepository.findAll().stream()
                .map(Keyword::getDescription)
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        for (String keyword : loadedKeywords) {
            log.info("{} Keyword loaded keyword={}", SEARCH, keyword);
        }

        if (loadedKeywords.isEmpty()) {
            return List.of();
        }

        List<String> sortedSkills = profile.getSkillPreferences().stream()
                .sorted((left, right) -> Integer.compare(right.getWeight(), left.getWeight()))
                .limit(MAX_SKILLS_FOR_DISCOVERY)
                .map(SkillPreference::getSkill)
                .filter(skill -> skill != null && !skill.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (sortedSkills.isEmpty()) {
            return List.of();
        }

        int safeMaxQueries = Math.max(1, maxQueries);
        LinkedHashSet<String> generatedQueries = new LinkedHashSet<>();
        List<String> orderedQueries = new ArrayList<>();

        for (String keyword : loadedKeywords) {
            for (String skill : sortedSkills) {
                String query = keyword + " " + skill;
                if (generatedQueries.add(query)) {
                    orderedQueries.add(query);
                    log.info("{} Generated query: {}", SEARCH, query);
                }

                if (orderedQueries.size() >= safeMaxQueries) {
                    return orderedQueries;
                }
            }
        }

        return orderedQueries;
    }
}
