package org.joinmastodon.web.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.joinmastodon.core.entity.Account;
import org.joinmastodon.core.entity.ListAccount;
import org.joinmastodon.core.entity.ListEntity;
import org.joinmastodon.core.service.AccountService;
import org.joinmastodon.core.service.ListAccountService;
import org.joinmastodon.core.service.ListService;
import org.joinmastodon.web.api.dto.AccountDto;
import org.joinmastodon.web.api.dto.ListDto;
import org.joinmastodon.web.api.dto.request.ListCreateRequest;
import org.joinmastodon.web.api.dto.request.ListUpdateRequest;
import org.joinmastodon.web.auth.AuthenticatedPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiVersion.V1 + "/lists")
public class ListController {
    private final ListService listService;
    private final ListAccountService listAccountService;
    private final AccountService accountService;

    public ListController(ListService listService, ListAccountService listAccountService, AccountService accountService) {
        this.listService = listService;
        this.listAccountService = listAccountService;
        this.accountService = accountService;
    }

    @GetMapping
    public List<ListDto> getLists() {
        Account account = requireAccount();
        return listService.findByAccount(account)
                .stream()
                .map(ApiMapper::toListDto)
                .toList();
    }

    @GetMapping("/{id}")
    public ListDto getList(@PathVariable("id") String id) {
        Account account = requireAccount();
        ListEntity list = listService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        ensureOwner(list, account);
        return ApiMapper.toListDto(list);
    }

    @PostMapping
    public ListDto create(@Valid @RequestBody ListCreateRequest request) {
        Account account = requireAccount();
        ListEntity list = new ListEntity();
        list.setAccount(account);
        list.setTitle(request.title());
        return ApiMapper.toListDto(listService.save(list));
    }

    @PutMapping("/{id}")
    public ListDto update(@PathVariable("id") String id, @Valid @RequestBody ListUpdateRequest request) {
        Account account = requireAccount();
        ListEntity list = listService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        ensureOwner(list, account);
        if (request.title() != null && !request.title().isBlank()) {
            list.setTitle(request.title());
        }
        return ApiMapper.toListDto(listService.save(list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        Account account = requireAccount();
        ListEntity list = listService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        ensureOwner(list, account);
        listService.delete(list);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/accounts")
    public List<AccountDto> getListAccounts(@PathVariable("id") String id) {
        Account account = requireAccount();
        ListEntity list = listService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        ensureOwner(list, account);
        return listAccountService.findByList(list).stream()
                .map(ListAccount::getAccount)
                .map(ApiMapper::toAccountDto)
                .toList();
    }

    @PostMapping("/{id}/accounts")
    public ResponseEntity<Void> addAccounts(
            @PathVariable("id") String id,
            @RequestParam("account_ids") List<String> accountIds) {
        Account account = requireAccount();
        ListEntity list = listService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        ensureOwner(list, account);
        for (String accountId : accountIds) {
            Account target = accountService.findById(parseId(accountId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
            boolean exists = listAccountService.findByList(list).stream()
                    .anyMatch(entry -> entry.getAccount().getId().equals(target.getId()));
            if (!exists) {
                ListAccount entry = new ListAccount();
                entry.setList(list);
                entry.setAccount(target);
                listAccountService.save(entry);
            }
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/accounts")
    public ResponseEntity<Void> removeAccounts(
            @PathVariable("id") String id,
            @RequestParam("account_ids") List<String> accountIds) {
        Account account = requireAccount();
        ListEntity list = listService.findById(parseId(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "List not found"));
        ensureOwner(list, account);
        for (ListAccount entry : listAccountService.findByList(list)) {
            if (accountIds.contains(entry.getAccount().getId().toString())) {
                listAccountService.delete(entry);
            }
        }
        return ResponseEntity.ok().build();
    }

    private void ensureOwner(ListEntity list, Account account) {
        if (!list.getAccount().getId().equals(account.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    private long parseId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }
    }

    private Account requireAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedPrincipal principal) {
            if (principal.accountId() != null) {
                return accountService.findById(principal.accountId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
