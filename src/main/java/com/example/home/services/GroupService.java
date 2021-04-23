package com.example.home.services;

import com.example.home.dtos.GroupDto;
import com.example.home.dtos.user.UserDto;
import com.example.home.models.Group;
import com.example.home.models.UserGroup;
import com.example.home.repositories.GroupRepository;
import com.example.home.repositories.UserGroupRepository;
import com.example.home.repositories.UserRepository;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import springfox.documentation.spi.service.contexts.SecurityContext;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserGroupRepository userGroupRepository, UserRepository userRepository){
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Group> getGroups(){
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Group> getGroupsManagedBYCurrentUser(Principal principal){
        //get user
        Long userId = userRepository.findIdByEmail(principal.getName());

        return groupRepository.findByManager(userId);
    }

    @Transactional(readOnly = true)
    public List<Group> getGroupsCreatedByCurrentUser(Principal principal){
        //get user
        Long userId = userRepository.findIdByEmail(principal.getName());

        return groupRepository.findAllByCreatedBy(userId);
    }

    @Transactional
    public void deleteGroupById(Long groupId){
        groupRepository.deleteById(groupId);
    }

    @Transactional(readOnly = true)
    public Set<Group> findGroupsByIds(Set<Long> groupIds){
        return groupRepository.findByIds(groupIds);
    }

    @Transactional
    public void createOrUpdateGroup(GroupDto groupDto, Principal principal) throws NotFoundException {
        Long id = groupDto.getId();
        Group group;
        if(null == id){//insert
            Long userId = userRepository.findIdByEmail(principal.getName());

            group = Group.builder()
                    .name(groupDto.getName())
                    .createdBy(userId)
                    .build();
            group = groupRepository.save(group);
        } else {// update
            Optional<Group> groupOptional = groupRepository.findById(id);
            if(groupOptional.isPresent()){
                group = groupOptional.get().setName(groupDto.getName());
                groupRepository.save(group);
            } else {
                throw new NotFoundException("Group with id:" + id + " does not exist");
            }
        }

        Map<Long, UserGroup> existingUsers = new HashMap<>();
        if(!CollectionUtils.isEmpty(group.getUserGroups())) {
            existingUsers = group.getUserGroups().stream()
                    .collect(toMap(UserGroup::getUserId, userGroup -> userGroup));
        }

        Map<Long, UserDto> newUsers = new HashMap<>();
        if(!CollectionUtils.isEmpty(groupDto.getUserDtoSet())) {
            newUsers = groupDto.getUserDtoSet().stream()
                    .collect(toMap(UserDto::getId, userDto -> userDto));
        }

        if(CollectionUtils.isEmpty(newUsers)){//remove all users from groupOptional
            group.setUserGroups(new HashSet<>());//not sure if this deletes all userGroup
            userGroupRepository.deleteByGroupId(group.getId());
            groupRepository.save(group);
        } else {
            updateUserGroupLinks(group.getId(), existingUsers, newUsers);
        }
    }

    public void updateUserGroupLinks(Long groupId, Map<Long, UserGroup> existingUsers, Map<Long, UserDto> newUsers){
        Set<UserGroup> userGroupsToRemove = new HashSet<>();
        Set<UserGroup> userGroupsToUpdate = new HashSet<>();

        Set<Long> newUserIds = newUsers.keySet();
        for(Map.Entry<Long, UserGroup> userGroupEntry : existingUsers.entrySet()){
            UserGroup userGroup = userGroupEntry.getValue();
            Long userId = userGroup.getUserId();

            boolean exists = newUserIds.stream().anyMatch(newUserId -> newUserId.equals(userId));
            if (Boolean.TRUE.equals(exists)){

                //update manager on existing connection
                if(newUsers.get(userId).isManager() != userGroup.isManager()) {
                    userGroup.setManager(newUsers.get(userId).isManager());
                    userGroupsToUpdate.add(userGroup);
                }

                newUserIds.remove(userId);
                newUsers.remove(userId);
            } else {
                userGroupsToRemove.add(userGroup);
            }
        }

        //delete old connections
        if(!CollectionUtils.isEmpty(userGroupsToRemove)){
            userGroupRepository.deleteAll(userGroupsToRemove);
        }

        //add new connections
        Set<UserGroup> userGroups = newUsers.values().stream().map(newUser -> {
            return UserGroup.builder()
                    .groupId(groupId)
                    .isManager(newUser.isManager())
                    .userId(newUser.getId())
                    .build();
        }).collect(Collectors.toSet());

        //update existing connections
        userGroups.addAll(userGroupsToUpdate);

        if(!CollectionUtils.isEmpty(userGroups)) {
            userGroupRepository.saveAll(userGroups);
        }
    }
}
