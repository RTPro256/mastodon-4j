package org.joinmastodon.core.service;

import java.util.Optional;
import org.joinmastodon.core.entity.Application;
import org.joinmastodon.core.repository.ApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Application> findById(Long id) {
        return applicationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Application> findByClientId(String clientId) {
        return applicationRepository.findByClientId(clientId);
    }

    @Transactional
    public Application save(Application application) {
        return applicationRepository.save(application);
    }

    @Transactional
    public void delete(Application application) {
        applicationRepository.delete(application);
    }
}
