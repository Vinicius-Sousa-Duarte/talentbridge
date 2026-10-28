package com.dunkweb.talentbridge.application.service;

import com.dunkweb.talentbridge.application.port.output.DiscoveryJobRepository;
import com.dunkweb.talentbridge.domain.enums.DiscoveryJobStatus;
import com.dunkweb.talentbridge.domain.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class SingleDiscoveryJobGuard {

    private final DiscoveryJobRepository discoveryJobRepository;
    private final ReentrantLock lock = new ReentrantLock(true);

    public SingleDiscoveryJobGuard(DiscoveryJobRepository discoveryJobRepository) {
        this.discoveryJobRepository = discoveryJobRepository;
    }

    public <T> T runWithLock(Supplier<T> operation) {
        lock.lock();
        try {
            return operation.get();
        } finally {
            lock.unlock();
        }
    }

    public void ensureNoRunningJob() {
        if (discoveryJobRepository.existsByStatus(DiscoveryJobStatus.RUNNING)) {
            throw new BusinessException("Já existe um DiscoveryJob em execução.");
        }
    }
}
