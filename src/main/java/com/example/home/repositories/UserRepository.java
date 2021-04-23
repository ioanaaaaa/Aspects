package com.example.home.repositories;

import com.example.home.models.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);

    @Query("select u.id from User u where u.email=?1")
    Long findIdByEmail(String email);

    List<User> findTop100ByFullnameContaining(String fullname);

    @Query("select u from User u where u.id in (:userIds)")
    Set<User> findByIds(@Param("userIds") Set<Long> userIds);

    @Query("select u.email from User u where u.id in (:userIds)")
    List<String> findEmailsByIds(@Param("userIds") Set<Long> userIds);

    List<User> findByEmailContainsOrFullnameContains(String searchTerm, String samSearchTerm);

//    List<User> findByEmailOrFullnameContainingIgnoreCase(String searchTerm);

    @Query("select u from User u")
    List<User> findAll();
}
