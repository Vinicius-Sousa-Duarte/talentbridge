package com.dunkweb.talentbridge.interfaces.controller;

import com.dunkweb.talentbridge.application.dto.CreateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UpdateUserProfileRequest;
import com.dunkweb.talentbridge.application.dto.UserProfileResponse;
import com.dunkweb.talentbridge.application.port.input.CreateUserProfileUseCase;
import com.dunkweb.talentbridge.application.port.input.DeleteUserProfileUseCase;
import com.dunkweb.talentbridge.application.port.input.GetUserProfileUseCase;
import com.dunkweb.talentbridge.application.port.input.UpdateUserProfileUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.REQUEST;
import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;

@RestController
@RequestMapping("/api/profile")
@Slf4j
@RequiredArgsConstructor
public class UserProfileController {

    private final CreateUserProfileUseCase createUserProfileUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final DeleteUserProfileUseCase deleteUserProfileUseCase;

    @PostMapping
    public ResponseEntity<UserProfileResponse> create(@Valid @RequestBody CreateUserProfileRequest request) {
        log.info("{} Create profile request received endpoint=/api/profile requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        UserProfileResponse response = createUserProfileUseCase.execute(request);
        log.info("{} Create profile request completed endpoint=/api/profile profileId={} status={} requestId={}",
            REQUEST,
            response.id(),
            201,
            MDC.get(REQUEST_ID));
        return ResponseEntity.created(URI.create("/api/profile")).body(response);
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> get() {
        log.info("{} Get profile request received endpoint=/api/profile requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        UserProfileResponse response = getUserProfileUseCase.execute();
        log.info("{} Get profile request completed endpoint=/api/profile profileId={} status={} requestId={}",
            REQUEST,
            response.id(),
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> update(@Valid @RequestBody UpdateUserProfileRequest request) {
        log.info("{} Update profile request received endpoint=/api/profile requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        UserProfileResponse response = updateUserProfileUseCase.execute(request);
        log.info("{} Update profile request completed endpoint=/api/profile profileId={} status={} requestId={}",
            REQUEST,
            response.id(),
            200,
            MDC.get(REQUEST_ID));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete() {
        log.info("{} Delete profile request received endpoint=/api/profile requestId={}",
            REQUEST,
            MDC.get(REQUEST_ID));
        deleteUserProfileUseCase.execute();
        log.info("{} Delete profile request completed endpoint=/api/profile status={} requestId={}",
            REQUEST,
            204,
            MDC.get(REQUEST_ID));
        return ResponseEntity.noContent().build();
    }
}
