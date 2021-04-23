package com.example.home.homemanagement;

import com.example.home.dtos.GroupDto;
import com.example.home.dtos.MemberDto;
import com.example.home.dtos.tasks.CreateTaskDto;
import com.example.home.dtos.user.UserDto;
import com.example.home.models.*;
import com.example.home.repositories.*;
import com.example.home.services.GroupService;
import com.example.home.services.TaskService;
import com.example.home.services.UserGroupService;
import com.example.home.services.UserService;
import com.example.home.services.observable.TaskObservableModel;
import javassist.NotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.*;

import java.security.Principal;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AllTests {
    @InjectMocks
    private TaskService taskService;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private AssigneeRepository assigneeRepository;
    @Mock
    private AssigneeMemberRepository assigneeMemberRepository;
    @InjectMocks
    private UserGroupService userGroupService;
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupService groupService;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private Principal principal;

    @Captor
    private ArgumentCaptor<Set<UserGroup>> userGroupEntityArgumentCaptor;

    @Captor
    private ArgumentCaptor<Set<UserGroup>> saveUserGroupEntityArgumentCaptor;

    @Captor
    private ArgumentCaptor<Task> taskArgumentCaptor;
    @Captor
    private ArgumentCaptor<Set<AssigneeMember>> assigneeMemberArgumentCaptor;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        groupService = new GroupService(groupRepository, userGroupRepository, userRepository);
        userService = new UserService(null, userRepository, null);
        taskService = new TaskService(taskRepository, userGroupService, assigneeRepository, assigneeMemberRepository, userService, null, null);
        userGroupService = new UserGroupService(userGroupRepository);
        userService = new UserService(null, userRepository, null);
        taskService = new TaskService(taskRepository, userGroupService, assigneeRepository, assigneeMemberRepository, userService, null, null);

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


//    @Before
//    public void initMocks() {
//        MockitoAnnotations.initMocks(this);
//        userService = new UserService(null, userRepository, null);
//        taskService = new TaskService(taskRepository, userGroupService, assigneeRepository, assigneeMemberRepository, userService, null, null);
//    }

    /**
     * Task has user A and group B (that has  user A as a member) as assignees
     */
    @Test
    public void claim_task_with_user_and_group(){
        Map<Long, TaskObservableModel> map = new HashMap<>();
        map.put(1L, new TaskObservableModel());

        when(principal.getName()).thenReturn("email");
        when(userRepository.findByEmail(any())).thenReturn(User.builder()
                .id(2L)
                .build());

        when(taskRepository.findByTaskIdEagerlyActive(any())).thenReturn(buildTask());

        taskService.claimTask(principal, 1L);

        Mockito.verify(this.assigneeMemberRepository,
                Mockito.times(1)).saveAll(assigneeMemberArgumentCaptor.capture());
        Set<AssigneeMember> assigneeMembers = assigneeMemberArgumentCaptor.getValue();

        Assert.assertEquals(2, assigneeMembers.size());

        //check if user assigneeMember is still active
        Assert.assertEquals(Long.valueOf(2L), assigneeMembers.stream()
                .filter(AssigneeMember::isActive)
                .map(AssigneeMember::getUserId).collect(toList()).get(0));

        //check if group assignee member is deactivated
        assigneeMembers.remove(assigneeMembers.stream()
                .filter(AssigneeMember::isActive)
                .map(AssigneeMember::getUserId).collect(toList()).get(0));
        Assert.assertFalse(assigneeMembers.iterator().next().isActive());

        Mockito.verify(this.taskRepository,
                Mockito.times(1)).save(taskArgumentCaptor.capture());
        Task task = taskArgumentCaptor.getValue();

        //check for claimed by principal
        Assert.assertEquals(Long.valueOf(2L), task.getClaimedBy());
    }

    public Optional<Task> buildTask(){
        Set<AssigneeMember> assigneeMemberSet = new HashSet<>();
        assigneeMemberSet.add(buildAssigneeMember(2L, null, null));
        assigneeMemberSet.add(buildAssigneeMember(null, 3L, null));

        return Optional.of(Task.builder()
                .id(1L)
                .activeAssignedUsers(Set.of(Assignee.builder()
                        .activeAssigneeMemberSet(assigneeMemberSet)
                        .build()))
                .status(Task.Status.IN_PROGRESS)
                .build()
        );
    }

    public AssigneeMember buildAssigneeMember(Long userId, Long groupId, Long assigneeId){
        return AssigneeMember.builder()
                .userId(userId)
                .groupId(groupId)
                .active(true)
                .assigneeId(assigneeId)
                .build();
    }

    @Test
    public void submit_completed_task() throws NotFoundException, IllegalAccessException {
        Task task = new Task().setClaimedBy(1L).setId(2L).setStatus(Task.Status.COMPLETED);

        when(principal.getName()).thenReturn("email");
        when(userRepository.findByEmail(any())).thenReturn(User.builder().id(1L).build());
        when(taskRepository.findByTaskIdEagerlyActive(any())).thenReturn(Optional.ofNullable(task));

        exceptionRule.expect(IllegalAccessException.class);
        exceptionRule.expectMessage("The task was completed!");

        taskService.submitTask(principal, task.getId());

    }

    @Test
    public void submit_unclaimed_task() throws NotFoundException, IllegalAccessException {
        Task task = new Task().setId(2L).setStatus(Task.Status.IN_PROGRESS);

        when(principal.getName()).thenReturn("email");
        when(userRepository.findByEmail(any())).thenReturn(User.builder().id(1L).build());
        when(taskRepository.findByTaskIdEagerlyActive(any())).thenReturn(Optional.ofNullable(task));

        exceptionRule.expect(IllegalAccessException.class);
        exceptionRule.expectMessage("The task was not claimed!");

        taskService.submitTask(principal, task.getId());

    }

    @Test
    public void submit_wrong_task() throws NotFoundException, IllegalAccessException {
        Task task = new Task().setClaimedBy(3L).setClaimedByUser(new User()).setId(2L).setStatus(Task.Status.IN_PROGRESS);

        when(principal.getName()).thenReturn("email");
        when(userRepository.findByEmail(any())).thenReturn(User.builder().id(1L).build());
        when(taskRepository.findByTaskIdEagerlyActive(any())).thenReturn(Optional.ofNullable(task));

        exceptionRule.expect(IllegalAccessException.class);
        exceptionRule.expectMessage("Tou did not claimed this task!");

        taskService.submitTask(principal, task.getId());

    }

    @Test
    public void submit_task() throws NotFoundException, IllegalAccessException {
        Task task = new Task().setClaimedBy(1L).setClaimedByUser(new User()).setId(2L).setStatus(Task.Status.IN_PROGRESS);

        when(principal.getName()).thenReturn("email");
        when(userRepository.findByEmail(any())).thenReturn(User.builder().id(1L).build());
        when(taskRepository.findByTaskIdEagerlyActive(any())).thenReturn(Optional.ofNullable(task));

        taskService.submitTask(principal, task.getId());

        Mockito.verify(this.taskRepository,
                Mockito.times(1)).save(taskArgumentCaptor.capture());
        Task taskResulted = taskArgumentCaptor.getValue();

        Assert.assertEquals(Task.Status.COMPLETED, taskResulted.getStatus());
    }

    @Test
    public void create_task_with_autoclaim(){
        CreateTaskDto taskDto = buildTask(null, null, Set.of(new MemberDto(2L)));

        when(principal.getName()).thenReturn("email");
        when(userRepository.findByEmail(any())).thenReturn(User.builder().id(1L).build());
        when(taskRepository.save(any())).thenReturn(buildTaskEntity());
        when(assigneeRepository.save(any())).thenReturn(Assignee.builder()
                .id(3L)
                .build());

        taskService.createOrEditTask(taskDto, principal);

        Mockito.verify(this.taskRepository,
                Mockito.times(1)).save(taskArgumentCaptor.capture());
        Task task = taskArgumentCaptor.getValue();

        Assert.assertEquals(Long.valueOf(2L), task.getClaimedBy());

    }

    @Test(expected = IllegalAccessException.class)
    public void create_task_with_no_assignees(){
        CreateTaskDto taskDto = buildTask(null, null, null);

        when(principal.getName()).thenReturn("email");

        taskService.createOrEditTask(taskDto, principal);
    }

    @Test
    public void create_task(){
        CreateTaskDto taskDto = buildTask(null, Set.of(new MemberDto(2L)), null);

        when(userGroupService.getManagersForUserThatBelongToGroups(any())).thenReturn(new HashSet<>());
        when(principal.getName()).thenReturn("email");
        when(taskRepository.save(any())).thenReturn(buildTaskEntity());
        when(assigneeRepository.save(any())).thenReturn(Assignee.builder()
                .id(3L)
                .build());

        taskService.createOrEditTask(taskDto, principal);

        Mockito.verify(this.taskRepository,
                Mockito.times(1)).save(taskArgumentCaptor.capture());
        Task task = taskArgumentCaptor.getValue();

        Mockito.verify(this.assigneeMemberRepository,
                Mockito.times(1)).saveAll(assigneeMemberArgumentCaptor.capture());
        AssigneeMember assigneeMember = assigneeMemberArgumentCaptor.getValue().iterator().next();

        Assert.assertEquals(Task.Status.CREATED, task.getStatus());
        Assert.assertEquals(taskDto.getGroups().iterator().next().getId(), assigneeMember.getGroupId());

    }

    @Test(expected = IllegalAccessException.class)
    public void update_task_with_no_manager(){
        CreateTaskDto taskDto = buildTask(1L, Set.of(new MemberDto(2L)), null);

        when(principal.getName()).thenReturn("email");
        when(taskRepository.findByTaskIdEagerlyActive(any()))
                .thenReturn(Optional.of(new Task()
                        .setActiveAssignedUsers(Set.of(new Assignee()
                                .setActiveAssigneeMemberSet(Set.of(new AssigneeMember().setGroupId(5L)))))));

        taskService.createOrEditTask(taskDto, principal);

        Mockito.verify(this.taskRepository,
                Mockito.times(1)).save(taskArgumentCaptor.capture());
        Task task = taskArgumentCaptor.getValue();

        Assert.assertEquals(taskDto.getCategory(), task.getCategory());
        Assert.assertEquals(Task.Status.IN_PROGRESS, task.getStatus());
        Assert.assertEquals(taskDto.getTitle(), task.getTitle());
        Assert.assertEquals(taskDto.getContent(), task.getContent());
    }

    public CreateTaskDto buildTask(Long id, Set<MemberDto> groups, Set<MemberDto> users){
        return CreateTaskDto.createTaskDtoBuilder()
                .category(Task.Category.Cleaning)
                .title("title")
                .content("content")
                .users(users)
                .groups(groups)
                .id(id)
                .build();
    }

    public Task buildTaskEntity(){
        return Task.builder()
                .id(1L)
                .category(Task.Category.Cleaning)
                .title("title")
                .content("content")
                .status(Task.Status.IN_PROGRESS)
                .build();
    }
}
