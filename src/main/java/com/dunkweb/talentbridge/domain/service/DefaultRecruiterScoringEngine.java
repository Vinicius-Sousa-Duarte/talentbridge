package com.dunkweb.talentbridge.domain.service;

import com.dunkweb.talentbridge.domain.entity.SkillPreference;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import com.dunkweb.talentbridge.shared.constants.LogConstants;
import com.dunkweb.talentbridge.shared.util.TextNormalizer;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class DefaultRecruiterScoringEngine implements RecruiterScoringEngine {

    @Override
    public int calculate(UserProfile userProfile, String headline, String description, UUID recruiterId) {
        String recruiterRef = recruiterId == null ? "N/A" : recruiterId.toString();

        if (userProfile == null || userProfile.getSkillPreferences() == null || userProfile.getSkillPreferences().isEmpty()) {
            log.info("{} Final score calculated recruiterId={} finalScore=0 reason=no-active-profile-or-skills",
                    LogConstants.SCORE,
                    recruiterRef);
            return 0;
        }

        String normalizedHeadline = TextNormalizer.normalize(headline);
        String normalizedDescription = TextNormalizer.normalize(description);
        String haystack = (normalizedHeadline + " " + normalizedDescription).trim();

        if (haystack.isBlank()) {
            log.info("{} Final score calculated recruiterId={} finalScore=0 reason=no-headline-or-description",
                    LogConstants.SCORE,
                    recruiterRef);
            return 0;
        }

        int score = 0;
        for (SkillPreference preference : userProfile.getSkillPreferences()) {
            String normalizedSkill = TextNormalizer.normalize(preference.getSkill());
            if (!normalizedSkill.isBlank() && containsSkill(haystack, normalizedSkill)) {
                log.debug("{} Skill matched recruiterId={} skill={} weight={}",
                        LogConstants.SCORE,
                        recruiterRef,
                        normalizedSkill,
                        preference.getWeight());
                score += preference.getWeight();
            } else {
                log.debug("{} Skill not matched recruiterId={} skill={}",
                        LogConstants.SCORE,
                        recruiterRef,
                        normalizedSkill);
            }
        }

        log.info("{} Final score calculated recruiterId={} finalScore={}",
                LogConstants.SCORE,
                recruiterRef,
                score);

        return score;
    }

    private boolean containsSkill(String haystack, String normalizedSkill) {
        Pattern pattern = Pattern.compile("(^|\\W)" + Pattern.quote(normalizedSkill) + "(\\W|$)");
        return pattern.matcher(haystack).find();
    }
}
