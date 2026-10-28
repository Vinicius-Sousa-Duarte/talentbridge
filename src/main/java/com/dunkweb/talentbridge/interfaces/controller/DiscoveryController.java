package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.dto.DiscoveryJobResponse;
import com.dunkweb.talentbridge.application.dto.ExecuteRecruiterDiscoveryRequest;
import com.dunkweb.talentbridge.application.port.input.ExecuteRecruiterDiscoveryUseCase;
import com.dunkweb.talentbridge.application.port.input.GetDiscoveryJobByIdUseCase;
import com.dunkweb.talentbridge.application.port.input.ListDiscoveryJobsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.REQUEST;
import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/discovery")
public class DiscoveryController {

    private final ExecuteRecruiterDiscoveryUseCase executeRecruiterDiscoveryUseCase;
    private final ListDiscoveryJobsUseCase listDiscoveryJobsUseCase;
    private final GetDiscoveryJobByIdUseCase getDiscoveryJobByIdUseCase;

    @PostMapping
    public ResponseEntity<DiscoveryJobResponse> execute(@Valid @RequestBody ExecuteRecruiterDiscoveryRequest request) {
        log.info("{} Execute discovery request received endpoint=/api/discovery requestId={} desiredProfiles={}",
                REQUEST,
                MDC.get(REQUEST_ID),
                request.desiredProfiles());

        DiscoveryJobResponse response = executeRecruiterDiscoveryUseCase.execute(request);

        log.info("{} Execute discovery request completed endpoint=/api/discovery requestId={} status={} jobId={}",
                REQUEST,
                MDC.get(REQUEST_ID),
                200,
                response.jobId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<DiscoveryJobResponse>> listJobs() {
        log.info("{} List discovery jobs request received endpoint=/api/discovery/jobs requestId={}",
                REQUEST,
                MDC.get(REQUEST_ID));

        List<DiscoveryJobResponse> response = listDiscoveryJobsUseCase.execute();

        log.info("{} List discovery jobs request completed endpoint=/api/discovery/jobs requestId={} status={} total={}",
                REQUEST,
                MDC.get(REQUEST_ID),
                200,
                response.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<DiscoveryJobResponse> getJob(@PathVariable("id") UUID id) {
        log.info("{} Get discovery job request received endpoint=/api/discovery/jobs/{{id}} requestId={} jobId={}",
                REQUEST,
                MDC.get(REQUEST_ID),
                id);

        DiscoveryJobResponse response = getDiscoveryJobByIdUseCase.execute(id);

        log.info("{} Get discovery job request completed endpoint=/api/discovery/jobs/{{id}} requestId={} status={} jobId={}",
                REQUEST,
                MDC.get(REQUEST_ID),
                200,
                response.jobId());

        return ResponseEntity.ok(response);
    }
}
