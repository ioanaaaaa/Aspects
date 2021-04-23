package com.example.home.homemanagement;

import com.example.home.dtos.GroupDto;
import com.example.home.dtos.user.UserDto;
import com.example.home.models.Group;
import com.example.home.models.UserGroup;
import com.example.home.repositories.GroupRepository;
import com.example.home.repositories.UserGroupRepository;
import com.example.home.repositories.UserRepository;
import com.example.home.services.GroupService;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GroupServiceTest {
    @InjectMocks
    private GroupService groupService;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Principal principal;

    @Captor
    private ArgumentCaptor<Set<UserGroup>> userGroupEntityArgumentCaptor;

    @Captor
    private ArgumentCaptor<Set<UserGroup>> saveUserGroupEntityArgumentCaptor;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        groupService = new GroupService(groupRepository, userGroupRepository, userRepository);
    }

    @Test
    public void remove_all_members() throws NotFoundException {
        Group group = buildGroup();
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        GroupDto groupDto = GroupDto.builder()
                .userDtoSet(null)
                .id(1L)
                .name("Name")
                .build();
        groupService.createOrUpdateGroup(groupDto, null);

        Assert.assertEquals(new HashSet<>(), group.getUserGroups());
    }

    @Test
    public void replace_member() throws NotFoundException {
        Group group = buildGroup();
        Set<UserGroup> oldUserGroup = group.getUserGroups();
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        GroupDto groupDto = GroupDto.builder()
                .userDtoSet(List.of(buildUserDto(false, 5L)))
                .id(1L)
                .name("Name")
                .build();
        groupService.createOrUpdateGroup(groupDto, null);

        Mockito.verify(this.userGroupRepository,
                Mockito.times(1)).deleteAll(userGroupEntityArgumentCaptor.capture());
        Set<UserGroup> userGroupDeleted = userGroupEntityArgumentCaptor.getValue();

        Mockito.verify(this.userGroupRepository,
                Mockito.times(1)).saveAll(saveUserGroupEntityArgumentCaptor.capture());
        Set<UserGroup> userGroupAdded = saveUserGroupEntityArgumentCaptor.getValue();

        Assert.assertEquals(1, userGroupDeleted.size());
        Assert.assertEquals(oldUserGroup, userGroupDeleted);
        Assert.assertEquals(Long.valueOf(5), userGroupAdded.stream().findAny().get().getUserId());
    }

    @Test
    public void add_member() throws NotFoundException {
        Group group = new Group();

        when(principal.getName()).thenReturn("email");
        when(groupRepository.save(any())).thenReturn(group);
        when(userRepository.findIdByEmail(any())).thenReturn(5L);


        GroupDto groupDto = GroupDto.builder()
                .userDtoSet(List.of(buildUserDto(false, 5L)))
                .name("Name")
                .build();
        groupService.createOrUpdateGroup(groupDto, principal);

        Mockito.verify(this.userGroupRepository,
                Mockito.times(1)).saveAll(saveUserGroupEntityArgumentCaptor.capture());
        Set<UserGroup> userGroupAdded = saveUserGroupEntityArgumentCaptor.getValue();

        Assert.assertEquals(1, userGroupAdded.size());
    }

    @Test
    public void update_manager() throws NotFoundException {
        Group group = buildGroup();
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        GroupDto groupDto = GroupDto.builder()
                .id(1L)
                .userDtoSet(List.of(buildUserDto(false, 5L)))
                .name("Name")
                .build();
        groupService.createOrUpdateGroup(groupDto, null);

        Mockito.verify(this.userGroupRepository,
                Mockito.times(1)).saveAll(saveUserGroupEntityArgumentCaptor.capture());
        UserGroup userGroup = saveUserGroupEntityArgumentCaptor.getValue().iterator().next();

        Assert.assertEquals(false, userGroup.isManager());
    }

    public Group buildGroup(){
        Set<UserGroup> userGroupSet = new HashSet<>();
        userGroupSet.add(buildUserGroup());
        return  Group.builder()
                .id(1L)
                .name("group1")
                .userGroups(userGroupSet)
                .build();
    }

    public UserGroup buildUserGroup(){
        return UserGroup.builder()
                .userId(Long.valueOf("2"))
                .groupId(Long.valueOf(1))
                .isManager(true)
                .build();
    }

    public UserDto buildUserDto(boolean isManager, Long id){
        return UserDto.userDtoBuilder()
                .isManager(isManager)
                .id(id)
                .build();
    }
}
