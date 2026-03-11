package vduczz.userservice.domain.service;

import vduczz.userservice.domain.repository.UserRepository;

public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
