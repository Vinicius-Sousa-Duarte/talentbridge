package com.dunkweb.talentbridge.interfaces.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static com.dunkweb.talentbridge.shared.constants.LogConstants.INTERACTION;
import static com.dunkweb.talentbridge.shared.constants.MdcConstants.REQUEST_ID;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

@RestController
@RequestMapping("/api/interactions")
@Slf4j
public class InteractionController {

    @PostMapping
    public ResponseEntity<Void> createInteraction() {
        log.info("{} Endpoint called endpoint=/api/interactions requestId={}",
                INTERACTION,
                MDC.get(REQUEST_ID));
        throw new ResponseStatusException(NOT_IMPLEMENTED, "Endpoint ainda nao implementado.");
    }

    @GetMapping("/{recruiterId}")
    public ResponseEntity<Void> getInteractionsByRecruiter(@PathVariable UUID recruiterId) {
        log.info("{} Endpoint called endpoint=/api/interactions/{{recruiterId}} requestId={} recruiterId={}",
                INTERACTION,
                MDC.get(REQUEST_ID),
                recruiterId);
        throw new ResponseStatusException(NOT_IMPLEMENTED, "Endpoint ainda nao implementado.");
    }
}
