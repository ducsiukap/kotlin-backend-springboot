package vduczz.userservice.infrastructure.config.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vduczz.userservice.domain.repository.AccountRepository;
import vduczz.userservice.domain.service.AccountDomainService;

@Configuration
public class DomainConfig {
    @Bean
    public AccountDomainService accountDomainService(AccountRepository accountRepository) {
        return new AccountDomainService(accountRepository);
    }
}
