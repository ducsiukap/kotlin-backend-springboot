package vduczz.userservice.domain.service;

import vduczz.userservice.domain.exception.auth.UniqueEmailException;
import vduczz.userservice.domain.model.Account;
import vduczz.userservice.domain.model.Email;
import vduczz.userservice.domain.model.Password;
import vduczz.userservice.domain.repository.AccountRepository;

// DomainService: chỉ quan tâm nghiệp vụ thuần túy
//  + nhận dữ liệu vào và tạo entity
//  + không sửa DB! //không quan tâm cái Entity đó sau này bị đem đi lưu DB
//      nhiê vụ của ApplicationService

public class AccountDomainService {
    private final AccountRepository accountRepository;

    public AccountDomainService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String name, Email email, Password password) {
        if (accountRepository.existsByEmail(email)) {
            throw new UniqueEmailException(email.email());
        }

        return Account.create(name, email, password);
    }
}
