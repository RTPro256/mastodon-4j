package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Mute;
import org.joinmastodon.core.repository.MuteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MuteService {
    private final MuteRepository muteRepository;

    public MuteService(MuteRepository muteRepository) {
        this.muteRepository = muteRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Mute> findByAccountAndTarget(Account account, Account target) {
        return muteRepository.findByAccountAndTargetAccount(account, target);
    }

    @Transactional(readOnly = true)
    public List<Mute> findByAccount(Account account) {
        return muteRepository.findByAccount(account);
    }

    @Transactional
    public Mute save(Mute mute) {
        return muteRepository.save(mute);
    }

    @Transactional
    public void delete(Mute mute) {
        muteRepository.delete(mute);
    }
}
