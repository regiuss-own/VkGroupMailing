package ru.regiuss.vk.group.mailing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.regiuss.vk.group.mailing.model.Account;
import ru.regiuss.vk.group.mailing.repository.AccountRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<Account> getAll() {
        return accountRepository.findAll();
    }

    @Transactional
    public void save(Account account) {
        accountRepository.save(account);
    }

    @Transactional
    public void delete(Account account) {
        accountRepository.delete(account);
    }

    public Account getById(int accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }
}
