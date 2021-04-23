package com.example.home.repositories;

import com.example.home.models.AssigneeMember;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface AssigneeMemberRepository extends CrudRepository<AssigneeMember, Long> {

    @Modifying
    @Query("delete from AssigneeMember am where am.id in (?1) ")
    void deleteAllByIdIn(Set<Long> ids);
}
