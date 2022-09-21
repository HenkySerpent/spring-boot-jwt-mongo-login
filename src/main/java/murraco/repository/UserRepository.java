package murraco.repository;

import murraco.model.AppUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<AppUser, String> {

    boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);

    void deleteByUsername(String username);

}
