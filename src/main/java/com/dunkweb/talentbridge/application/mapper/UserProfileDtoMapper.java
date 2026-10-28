package com.dunkweb.talentbridge.application.mapper;

import com.dunkweb.talentbridge.application.dto.SkillPreferenceResponse;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;
import com.dunkweb.talentbridge.domain.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class UserProfileDtoMapper {

    public UserProfileResponse toResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getProfessionalTitle(),
                profile.getProfessionalSummary(),
                profile.getObjectives(),
                profile.getSkillPreferences().stream()
                        .sorted(Comparator.comparing(skill -> skill.getSkill().toLowerCase()))
                        .map(skill -> new SkillPreferenceResponse(
                                skill.getId(),
                                skill.getSkill(),
                                skill.getWeight(),
                                skill.getCreatedAt()
                        ))
                        .toList(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
