package org.joinmastodon.core.service;

import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.Block;
import org.joinmastodon.core.repository.BlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BlockService {
    private final BlockRepository blockRepository;

    public BlockService(BlockRepository blockRepository) {
        this.blockRepository = blockRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Block> findByAccountAndTarget(Account account, Account target) {
        return blockRepository.findByAccountAndTargetAccount(account, target);
    }

    @Transactional(readOnly = true)
    public List<Block> findByAccount(Account account) {
        return blockRepository.findByAccount(account);
    }

    @Transactional
    public Block save(Block block) {
        return blockRepository.save(block);
    }

    @Transactional
    public void delete(Block block) {
        blockRepository.delete(block);
    }
}
