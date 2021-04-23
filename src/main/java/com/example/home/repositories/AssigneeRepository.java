package com.example.home.repositories;

import com.example.home.models.Assignee;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface AssigneeRepository extends CrudRepository<Assignee, Long> {

    Assignee findByTaskId(Long taskId);

    @Modifying
    @Query("delete from Assignee a where a.id in (?1) ")
    void deleteAllByIdIn(Set<Long> ids);
}
