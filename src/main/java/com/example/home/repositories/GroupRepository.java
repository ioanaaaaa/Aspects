package com.example.home.repositories;

import com.example.home.models.Group;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long> {
    @Query("select distinct g from Group g left join fetch UserGroup ug on (g.id = ug.groupId) ")
    List<Group> findAll();

    @Query("select g from Group g where g.id in (:groupIds)")
    Set<Group> findByIds(@Param("groupIds") Set<Long> groupIds);

    List<Group> findAllByCreatedBy(Long userId);

    @Query(value = "select g from Group g inner join  g.userGroups ug " +
            "where ug.groupId in ( select g2.id from Group as g2 inner join g2.userGroups ug2 " +
            "where ug2.userId =:userId and ug.isManager = true) ")
    List<Group> findByManager(@Param("userId") Long userId);

}
