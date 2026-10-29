package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.discovery.DiscoveryProvider;
import com.dunkweb.talentbridge.application.discovery.DiscoveryProviderRegistry;
import com.dunkweb.talentbridge.application.dto.CreateManualCandidateRequest;
import com.dunkweb.talentbridge.application.dto.DiscoveryProviderHealthItemResponse;
import com.dunkweb.talentbridge.application.dto.DiscoveryProviderHealthResponse;
import com.dunkweb.talentbridge.application.dto.DiscoveryProviderResponse;
import com.dunkweb.talentbridge.application.port.input.ImportCsvDiscoveryUseCase;
import com.dunkweb.talentbridge.application.port.input.ImportManualDiscoveryUseCase;
import com.dunkweb.talentbridge.domain.enums.DiscoverySource;
import com.dunkweb.talentbridge.domain.exception.ValidationException;
import com.dunkweb.talentbridge.domain.valueobject.discovery.DiscoveryResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.DISCOVERY;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.PROVIDER;
import static com.dunkweb.talentbridge.shared.constants.LogConstants.REQUEST;
import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
@Slf4j
public class DiscoveryController {

    private final ImportCsvDiscoveryUseCase importCsvDiscoveryUseCase;
        private final ImportManualDiscoveryUseCase importManualDiscoveryUseCase;
    private final DiscoveryProviderRegistry discoveryProviderRegistry;

    @PostMapping
    public ResponseEntity<Void> discover() {
        log.info("{} Discovery endpoint called endpoint=/api/discovery requestId={}",
                DISCOVERY,
                MDC.get(REQUEST_ID));
        throw new ResponseStatusException(NOT_IMPLEMENTED, "Endpoint ainda nao implementado.");
    }

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiscoveryResult> importCsv(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "desiredProfiles", required = false) Integer desiredProfiles
    ) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Arquivo CSV e obrigatorio.");
        }

        String correlationId = MDC.get(REQUEST_ID);
        log.info("{} CSV discovery request received endpoint=/api/discovery/csv fileName={} desiredProfiles={} requestId={}",
                REQUEST,
                file.getOriginalFilename(),
                desiredProfiles,
                correlationId);

        try {
            String csvContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            DiscoveryResult result = importCsvDiscoveryUseCase.execute(csvContent, desiredProfiles, correlationId);
            log.info("{} CSV discovery completed endpoint=/api/discovery/csv requestId={} jobId={} providers={}",
                    DISCOVERY,
                    correlationId,
                    result.jobId(),
                    result.providerResults().stream().map(p -> p.providerName()).toList());
            return ResponseEntity.ok(result);
        } catch (IOException exception) {
            throw new ValidationException("Falha ao ler arquivo CSV enviado.");
        }
    }

    @PostMapping("/manual")
    public ResponseEntity<DiscoveryResult> discoverManual(@Valid @RequestBody CreateManualCandidateRequest request) {
        String requestId = MDC.get(REQUEST_ID);
        log.info("{} Manual discovery request received endpoint=/api/discovery/manual requestId={} provider={} candidateName={}",
                REQUEST,
                requestId,
                DiscoverySource.MANUAL.name(),
                request.name());

        DiscoveryResult result = importManualDiscoveryUseCase.execute(request, requestId);

        log.info("{} Manual discovery completed endpoint=/api/discovery/manual requestId={} jobId={} provider={} candidateName={}",
                DISCOVERY,
                requestId,
                result.jobId(),
                DiscoverySource.MANUAL.name(),
                request.name());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/jobs")
    public ResponseEntity<Void> listJobs() {
        log.info("{} Discovery jobs list endpoint called endpoint=/api/discovery/jobs requestId={}",
                DISCOVERY,
                MDC.get(REQUEST_ID));
        throw new ResponseStatusException(NOT_IMPLEMENTED, "Endpoint ainda nao implementado.");
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<Void> getJobById(@PathVariable UUID id) {
        log.info("{} Discovery job details endpoint called endpoint=/api/discovery/jobs/{{id}} requestId={} jobId={}",
                DISCOVERY,
                MDC.get(REQUEST_ID),
                id);
        throw new ResponseStatusException(NOT_IMPLEMENTED, "Endpoint ainda nao implementado.");
    }

    @GetMapping("/providers")
    public ResponseEntity<List<DiscoveryProviderResponse>> listProviders() {
        String requestId = MDC.get(REQUEST_ID);
        Set<DiscoverySource> enabledSources = discoveryProviderRegistry.listProviders().stream()
                .map(DiscoveryProvider::source)
                .collect(java.util.stream.Collectors.toSet());

        List<DiscoveryProviderResponse> response = List.of(
                DiscoverySource.CSV,
                DiscoverySource.MANUAL,
                DiscoverySource.GOOGLE,
                DiscoverySource.BING,
                DiscoverySource.LINKEDIN,
                DiscoverySource.CHROME_EXTENSION
        ).stream().map(source -> new DiscoveryProviderResponse(source.name(), enabledSources.contains(source))).toList();

        log.info("{} Provider list requested endpoint=/api/discovery/providers requestId={} enabledProviders={}",
                PROVIDER,
                requestId,
                response.stream().filter(DiscoveryProviderResponse::enabled).map(DiscoveryProviderResponse::name).toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/providers/health")
    public ResponseEntity<DiscoveryProviderHealthResponse> providersHealth() {
        String requestId = MDC.get(REQUEST_ID);
        List<DiscoveryProviderHealthItemResponse> providers = discoveryProviderRegistry.listProviders().stream()
                .map(provider -> new DiscoveryProviderHealthItemResponse(
                        provider.source().name(),
                        provider.health() ? "UP" : "DOWN"
                ))
                .toList();

        log.info("{} Provider health requested endpoint=/api/discovery/providers/health requestId={} providers={}",
                PROVIDER,
                requestId,
                providers.stream().map(item -> item.provider() + ":" + item.status()).toList());

        return ResponseEntity.ok(new DiscoveryProviderHealthResponse(providers));
    }
}
