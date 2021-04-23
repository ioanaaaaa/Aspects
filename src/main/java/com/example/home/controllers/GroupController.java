package com.example.home.controllers;

import com.example.home.dtos.GroupDto;
import com.example.home.services.GroupService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Create or edit group
     * @param groupDto
     * @param principal
     * @return
     */
    @PostMapping("/add-edit")
    public ResponseEntity createOrUpdateGroup(@RequestBody GroupDto groupDto, Principal principal) throws NotFoundException {
        groupService.createOrUpdateGroup(groupDto, principal);

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Get all existing groups created by all users.
     * @return
     */
    @GetMapping("")
    public List<GroupDto> getGroups(){
        return GroupDto.toDtos(groupService.getGroups());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteGroup(@PathVariable("id") Long id){
        groupService.deleteGroupById(id);

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Get groups created by current user
     * @param principal
     * @return
     */
    @GetMapping("/current-user")
    public List<GroupDto> getGroupsCreatedByCurrentUser(Principal principal){
        return  GroupDto.toDtos(groupService.getGroupsCreatedByCurrentUser(principal));
    }

    /**
     * Get groups managed by current user.
     * Return also the user members.
     *
     * @param principal
     * @return GroupDto
     */
    @GetMapping("/my-teams")
    public List<GroupDto> getGroupsManagedByCurrentUser(Principal principal){
        return  GroupDto.toDtos(groupService.getGroupsManagedBYCurrentUser(principal));
    }
}
