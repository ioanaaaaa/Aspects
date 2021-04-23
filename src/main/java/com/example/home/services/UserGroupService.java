package com.example.home.services;

import com.example.home.repositories.UserGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;

    @Autowired
    public UserGroupService(UserGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }

    @Transactional(readOnly = true)
    public boolean checkForManager(Set<Long> groupIds, String principalEmail) {
        String email = userGroupRepository.findManagerByGroupIds(groupIds, principalEmail);
        return StringUtils.isEmpty(email) ? false : true;
    }

//    @Transactional(readOnly = true)
//    public Set<String> getManagersForUserThatBelongToGroups(Set<Long> groupIds, Long userThatClaimedTask) {
//        Set<Long> remainingGroupIds = userGroupRepository.selectGroupsToWhichUserBelongs(groupIds, userThatClaimedTask);
//        Set<String> managerEmails = userGroupRepository.getManagersEmailsForGroups(remainingGroupIds);
//        return managerEmails;
//    }

    @Transactional(readOnly = true)
    public Set<String> getManagersForUserThatBelongToGroups(Set<Long> groupIds) {
//        Set<Long> remainingGroupIds = userGroupRepository.selectGroupsToWhichUserBelongs(groupIds, userThatClaimedTask);
        Set<String> managerEmails = userGroupRepository.getManagersEmailsForGroups(groupIds);
        return managerEmails;
    }

    public Set<Long> getGroupIdsManagedByPrinciple(Long userId) {
        return userGroupRepository.findGroupIdsManagedByPrinciple(userId);
    }

    public boolean checkIfUserBelongsToGroup(Long userId, Long groupId) {
        return userGroupRepository.findByUserIdAndGroupId(userId, groupId).isPresent() ? true : false;
    }

    public Set<Long> getGroupsIdsForUser(Long userId) {
        return userGroupRepository.findGroupIdsByUserId(userId);
    }

    public Set<Long> getUserIdsForGroup(Long groupId) {
        return userGroupRepository.findUserIdsByGroupId(groupId);
    }

    public Set<Long> getUserIdsForGroupIds(Set<Long> groupIds) {
        return userGroupRepository.findUserIdsByGroupIds(groupIds);
    }
}
