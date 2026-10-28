package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.dto.CreateKeywordRequest;
import com.dunkweb.talentbridge.application.dto.KeywordResponse;
import com.dunkweb.talentbridge.application.port.input.CreateKeywordUseCase;
import com.dunkweb.talentbridge.application.port.input.DeleteKeywordUseCase;
import com.dunkweb.talentbridge.application.port.input.GetKeywordByIdUseCase;
import com.dunkweb.talentbridge.application.port.input.ListKeywordsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.REQUEST;
import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;

@RestController
@RequestMapping("/api/keywords")
@Slf4j
@RequiredArgsConstructor
public class KeywordController {

    private final CreateKeywordUseCase createKeywordUseCase;
    private final ListKeywordsUseCase listKeywordsUseCase;
    private final GetKeywordByIdUseCase getKeywordByIdUseCase;
    private final DeleteKeywordUseCase deleteKeywordUseCase;

    @PostMapping
    public ResponseEntity<KeywordResponse> create(@Valid @RequestBody CreateKeywordRequest request) {
        log.info("{} Create keyword request received endpoint=/api/keywords requestId={}", REQUEST, MDC.get(REQUEST_ID));
        KeywordResponse response = createKeywordUseCase.execute(request);
        log.info("{} Create keyword request completed endpoint=/api/keywords keywordId={} status={} requestId={}",
                REQUEST,
                response.id(),
                201,
                MDC.get(REQUEST_ID));
        return ResponseEntity.created(URI.create("/api/keywords/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<KeywordResponse>> list() {
        log.info("{} List keywords request received endpoint=/api/keywords requestId={}", REQUEST, MDC.get(REQUEST_ID));
        List<KeywordResponse> response = listKeywordsUseCase.execute();
        log.info("{} List keywords request completed endpoint=/api/keywords status={} total={} requestId={}",
                REQUEST,
                200,
                response.size(),
                MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KeywordResponse> getById(@PathVariable UUID id) {
        log.info("{} Get keyword request received endpoint=/api/keywords/{{id}} keywordId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        KeywordResponse response = getKeywordByIdUseCase.execute(id);
        log.info("{} Get keyword request completed endpoint=/api/keywords/{{id}} keywordId={} status={} requestId={}",
            REQUEST,
            id,
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        log.info("{} Delete keyword request received endpoint=/api/keywords/{{id}} keywordId={} requestId={}",
            REQUEST,
            id,
            MDC.get(REQUEST_ID));
        deleteKeywordUseCase.execute(id);
        log.info("{} Delete keyword request completed endpoint=/api/keywords/{{id}} keywordId={} status={} requestId={}",
            REQUEST,
            id,
            204,
            MDC.get(REQUEST_ID));
        return ResponseEntity.noContent().build();
    }
}
