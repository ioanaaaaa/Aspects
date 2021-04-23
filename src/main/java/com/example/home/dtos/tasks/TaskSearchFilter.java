package com.example.home.dtos.tasks;

import com.example.home.models.Assignee;
import com.example.home.models.AssigneeMember;
import com.example.home.models.Task;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class TaskSearchFilter implements Specification<Task> {
    Set<Long> groupIdsForUser;

    TaskFilterDto taskFilterDto;

    @Override
    public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicatesList = new ArrayList<>();
        Task.Status status = taskFilterDto.getStatus();
        Long groupId = taskFilterDto.getGroupId();
        Long userId = taskFilterDto.getUserId();

        if (null != status) {
            Predicate statusPredicate = criteriaBuilder.equal(root.get("status"), status);
            predicatesList.add(statusPredicate);
        }

        if (null != groupId && null != userId && taskFilterDto.isMatch()) {
            SetJoin<Task, Assignee> taskAssignee = root.joinSet("activeAssignedUsers", JoinType.INNER);
            SetJoin<Assignee, AssigneeMember> assigneeAssigneeMember = taskAssignee.joinSet("activeAssigneeMemberSet", JoinType.INNER);

            Predicate userInGroupPredicate = criteriaBuilder.or(criteriaBuilder.equal(assigneeAssigneeMember.get("groupId"), groupId),
                    criteriaBuilder.equal(assigneeAssigneeMember.get("userId"), userId));
            predicatesList.add(userInGroupPredicate);
        } else if (null != groupId) {
            SetJoin<Task, Assignee> taskAssignee = root.joinSet("activeAssignedUsers", JoinType.INNER);
            SetJoin<Assignee, AssigneeMember> assigneeAssigneeMember = taskAssignee.joinSet("activeAssigneeMemberSet", JoinType.INNER);

            Predicate groupAssigneePredicate = criteriaBuilder.and(
                    criteriaBuilder.equal(assigneeAssigneeMember.get("groupId"), groupId),
                    root.get("claimedBy").isNull());
            predicatesList.add(groupAssigneePredicate);
        } else if (null != userId) {
            SetJoin<Task, Assignee> taskAssignee = root.joinSet("activeAssignedUsers", JoinType.INNER);
            SetJoin<Assignee, AssigneeMember> assigneeAssigneeMember = taskAssignee.joinSet("activeAssigneeMemberSet", JoinType.INNER);

            Predicate userAssigneePredicate = criteriaBuilder.equal(root.get("claimedBy"), userId);

            //or search tasks that are assigned to group that user belongs and that are not claimed
            Predicate orInGroups = null;
            if (!CollectionUtils.isEmpty(groupIdsForUser)) {
                orInGroups = criteriaBuilder.and(assigneeAssigneeMember.get("groupId").in(groupIdsForUser),
                        root.get("claimedBy").isNull());
            }

            if (null != orInGroups) {
                predicatesList.add(criteriaBuilder.or(userAssigneePredicate, orInGroups));
            } else {
                predicatesList.add(userAssigneePredicate);
            }
        }

        criteriaQuery.orderBy(criteriaBuilder.asc(root.get("status")));
        return criteriaBuilder.and(predicatesList.toArray(new Predicate[0]));
    }
}
