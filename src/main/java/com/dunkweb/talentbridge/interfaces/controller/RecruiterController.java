package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.dto.CreateRecruiterRequest;
import com.dunkweb.talentbridge.application.dto.RecalculateRecruiterScoresResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterDiscoveryResponse;
import com.dunkweb.talentbridge.application.dto.RecruiterResponse;
import com.dunkweb.talentbridge.application.port.input.ApproveRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.CreateRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.DeleteRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.DismissRecruiterUseCase;
import com.dunkweb.talentbridge.application.port.input.GetRecruiterByIdUseCase;
import com.dunkweb.talentbridge.application.port.input.ListRecruitersUseCase;
import com.dunkweb.talentbridge.application.port.input.RecalculateRecruiterScoreUseCase;
import com.dunkweb.talentbridge.application.port.input.RegisterConnectionUseCase;
import com.dunkweb.talentbridge.application.port.input.RegisterInviteUseCase;
import com.dunkweb.talentbridge.application.port.input.SearchRecruitersUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.REQUEST;
import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;

@RestController
@RequestMapping("/api/recruiters")
@Slf4j
@RequiredArgsConstructor
public class RecruiterController {

    private final CreateRecruiterUseCase createRecruiterUseCase;
    private final ListRecruitersUseCase listRecruitersUseCase;
    private final GetRecruiterByIdUseCase getRecruiterByIdUseCase;
    private final ApproveRecruiterUseCase approveRecruiterUseCase;
    private final RegisterInviteUseCase registerInviteUseCase;
    private final RegisterConnectionUseCase registerConnectionUseCase;
    private final DismissRecruiterUseCase dismissRecruiterUseCase;
    private final DeleteRecruiterUseCase deleteRecruiterUseCase;
    private final SearchRecruitersUseCase searchRecruitersUseCase;
    private final RecalculateRecruiterScoreUseCase recalculateRecruiterScoreUseCase;

    @PostMapping
    public ResponseEntity<RecruiterResponse> create(@Valid @RequestBody CreateRecruiterRequest request) {
        log.info("{} Create recruiter request received endpoint=/api/recruiters requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        RecruiterResponse response = createRecruiterUseCase.execute(request);
        log.info("{} Create recruiter request completed endpoint=/api/recruiters recruiterId={} status={} requestId={}",
            REQUEST,
            response.id(),
            201,
            MDC.get(REQUEST_ID));
        return ResponseEntity.created(URI.create("/api/recruiters/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RecruiterResponse>> list() {
        log.info("{} List recruiters request received endpoint=/api/recruiters requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        List<RecruiterResponse> response = listRecruitersUseCase.execute();
        log.info("{} List recruiters request completed endpoint=/api/recruiters status={} total={} requestId={}",
            REQUEST,
            200,
            response.size(),
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruiterResponse> getById(@PathVariable UUID id) {
        log.info("{} Get recruiter request received endpoint=/api/recruiters/{{id}} recruiterId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        RecruiterResponse response = getRecruiterByIdUseCase.execute(id);
        log.info("{} Get recruiter request completed endpoint=/api/recruiters/{{id}} recruiterId={} status={} requestId={}",
            REQUEST,
            id,
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<RecruiterResponse> approve(@PathVariable UUID id) {
        log.info("{} Approve recruiter request received endpoint=/api/recruiters/{{id}}/approve recruiterId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        RecruiterResponse response = approveRecruiterUseCase.execute(id);
        log.info("{} Approve recruiter request completed endpoint=/api/recruiters/{{id}}/approve recruiterId={} status={} requestId={}",
            REQUEST,
            id,
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/invite")
    public ResponseEntity<RecruiterResponse> registerInvite(@PathVariable UUID id) {
        log.info("{} Register invite request received endpoint=/api/recruiters/{{id}}/invite recruiterId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        RecruiterResponse response = registerInviteUseCase.execute(id);
        log.info("{} Register invite request completed endpoint=/api/recruiters/{{id}}/invite recruiterId={} status={} requestId={}",
            REQUEST,
            id,
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/connect")
    public ResponseEntity<RecruiterResponse> registerConnection(@PathVariable UUID id) {
        log.info("{} Register connection request received endpoint=/api/recruiters/{{id}}/connect recruiterId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        RecruiterResponse response = registerConnectionUseCase.execute(id);
        log.info("{} Register connection request completed endpoint=/api/recruiters/{{id}}/connect recruiterId={} status={} requestId={}",
            REQUEST,
            id,
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/dismiss")
    public ResponseEntity<RecruiterResponse> dismiss(@PathVariable UUID id) {
        log.info("{} Dismiss recruiter request received endpoint=/api/recruiters/{{id}}/dismiss recruiterId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        RecruiterResponse response = dismissRecruiterUseCase.execute(id);
        log.info("{} Dismiss recruiter request completed endpoint=/api/recruiters/{{id}}/dismiss recruiterId={} status={} requestId={}",
            REQUEST,
            id,
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("{} Delete recruiter request received endpoint=/api/recruiters/{{id}} recruiterId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        deleteRecruiterUseCase.execute(id);
        log.info("{} Delete recruiter request completed endpoint=/api/recruiters/{{id}} recruiterId={} status={} requestId={}",
            REQUEST,
            id,
            204,
            MDC.get(REQUEST_ID));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/discovery")
    public ResponseEntity<List<RecruiterDiscoveryResponse>> discoveryMock() {
        log.info("{} Discovery recruiter request received endpoint=/api/recruiters/discovery requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        List<RecruiterDiscoveryResponse> response = searchRecruitersUseCase.execute();
        log.info("{} Discovery recruiter request completed endpoint=/api/recruiters/discovery status={} total={} requestId={}",
            REQUEST,
            200,
            response.size(),
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate-scores")
    public ResponseEntity<RecalculateRecruiterScoresResponse> recalculateScores() {
        log.info("{} Recalculate scores request received endpoint=/api/recruiters/recalculate-scores requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        RecalculateRecruiterScoresResponse response = recalculateRecruiterScoreUseCase.execute();
        log.info("{} Recalculate scores request completed endpoint=/api/recruiters/recalculate-scores status={} processed={} requestId={}",
            REQUEST,
            200,
            response.recruitersUpdated(),
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }
}
