package vduczz.userservice.domain.repository;

import vduczz.userservice.domain.model.Email;
import vduczz.userservice.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    void save(User user);
}
