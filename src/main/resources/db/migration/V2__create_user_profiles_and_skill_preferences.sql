CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    professional_title VARCHAR(160) NOT NULL,
    professional_summary VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE user_profile_objectives (
    user_profile_id UUID NOT NULL,
    objective VARCHAR(80) NOT NULL,
    CONSTRAINT fk_user_profile_objectives_profile
        FOREIGN KEY (user_profile_id) REFERENCES user_profiles (id)
            ON DELETE CASCADE
);

CREATE TABLE skill_preferences (
    id UUID PRIMARY KEY,
    user_profile_id UUID NOT NULL,
    skill VARCHAR(120) NOT NULL,
    weight INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_skill_preferences_user_profile
        FOREIGN KEY (user_profile_id) REFERENCES user_profiles (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_skill_preferences_skill ON skill_preferences (skill);
CREATE INDEX idx_skill_preferences_weight ON skill_preferences (weight);
