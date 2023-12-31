package com.hn.api.diary.repository;

import com.hn.api.diary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByNick(String nick);
    Optional<User> findByEmailOrNick(String email, String nick);

}
