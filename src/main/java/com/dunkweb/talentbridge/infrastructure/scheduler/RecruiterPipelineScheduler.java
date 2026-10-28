package com.dunkweb.talentbridge.infrastructure.scheduler;

import com.dunkweb.talentbridge.shared.constants.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecruiterPipelineScheduler {

    @Scheduled(fixedDelay = 1_800_000)
    public void heartbeat() {
        long startedAt = System.nanoTime();
        log.info("{} Job started jobName=recruiter-pipeline-heartbeat", LogConstants.SCHEDULER);

        int processed = 0;

        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} Job finished jobName=recruiter-pipeline-heartbeat processedCount={} durationMs={}",
                LogConstants.SCHEDULER,
                processed,
                durationMs);
    }
}
