package com.dunkweb.talentbridge.application.port.output;

import java.util.Optional;

public interface AiScoringGateway {

    Optional<Integer> suggestScore(String headline, String description);
}
