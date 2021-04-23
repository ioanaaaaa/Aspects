package com.example.home.services;

import com.example.home.dtos.GroupDto;
import com.example.home.dtos.MemberDto;
import com.example.home.dtos.tasks.CreateTaskDto;
import com.example.home.dtos.tasks.TaskFilterDto;
import com.example.home.dtos.tasks.TaskModelDto;
import com.example.home.dtos.tasks.TaskSearchFilter;
import com.example.home.dtos.user.UserDto;
import com.example.home.models.*;
import com.example.home.repositories.AssigneeMemberRepository;
import com.example.home.repositories.AssigneeRepository;
import com.example.home.repositories.TaskRepository;
import com.example.home.services.observable.ManagerObserver;
import com.example.home.services.observable.TaskObservable;
import com.example.home.services.observable.TaskObservableModel;
import com.example.home.services.states.CreatedState;
import com.example.home.services.states.InProgressState;
import javassist.NotFoundException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserGroupService userGroupService;
    private final AssigneeRepository assigneeRepository;
    private final AssigneeMemberRepository assigneeMemberRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final EmailService emailService;
    private final Map<Long, TaskObservable> taskObservableMap;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserGroupService userGroupService, AssigneeRepository assigneeRepository, AssigneeMemberRepository assigneeMemberRepository, UserService userService, GroupService groupService, EmailService emailService) {
        this.taskRepository = taskRepository;
        this.userGroupService = userGroupService;
        this.assigneeRepository = assigneeRepository;
        this.assigneeMemberRepository = assigneeMemberRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.emailService = emailService;
        this.taskObservableMap = new HashMap<>();
    }

    /**
     * Get open and completed tasks for members of the groups that are managed by principle
     *
     * @param principal
     * @return
     */
    @Transactional(readOnly = true)
    public List<TaskModelDto> getMyTeamsTasks(Principal principal) {
        //get user
        User user = userService.getByEmail(principal.getName());

        Set<Long> groupIds = userGroupService.getGroupIdsManagedByPrinciple(user.getId());

        if (CollectionUtils.isEmpty(groupIds))
            return null;

        List<Task> tasks = taskRepository.findByActiveAssignedUsers_activeAssigneeMemberSet_groupIdIn(groupIds);

        List<TaskModelDto> taskModelDtos = new ArrayList<>();
        for (Task task : tasks) {
            TaskModelDto taskModelDto = convertToModel(task);

            taskModelDtos.add(taskModelDto);
        }

        return taskModelDtos;

    }

    /**
     * Deletes the specified task only if the current user is manager
     *
     * @param id
     */
    @Transactional
    public void deleteTask(Long id, Principal principal) throws IllegalAccessException, NotFoundException {
        if (null == id) {
            throw new IllegalAccessException("The task with id" + id + " does not exist!");
        }

        Optional<Task> taskOptional = taskRepository.findById(id);
        if (!taskOptional.isPresent()) {
            throw new NotFoundException("The task with id " + id + " was not found!");
        }

        //check it the principal is manager in order to delete the task
        if (Boolean.FALSE.equals(checkForManager(taskOptional.get(), principal.getName()))) {
            throw new IllegalAccessException("You can not delete this task because you are not a manager");
        }

        Set<Assignee> assignees = taskOptional.get().getAssignedUsers();
        Set<Long> assigneeIds = assignees.stream().map(Assignee::getId).collect(toSet());
        Set<Long> assigneeMembers = assignees.stream().map(Assignee::getAssigneeMemberSet)
                .flatMap(list -> list.stream())
                .map(AssigneeMember::getId)
                .collect(Collectors.toSet());

        assigneeMemberRepository.deleteAllByIdIn(assigneeMembers);
        assigneeRepository.deleteAllByIdIn(assigneeIds);
        taskRepository.delete(taskOptional.get());

    }

    //la reassign task trebuie sa setez si claim by cu null;
//    public void reassignTask(){
//
//    }

    @Transactional
    public void submitTask(Principal principal, Long taskId) throws IllegalAccessException, NotFoundException {
        if (null == taskId) {
            throw new IllegalAccessException("You must provide a task id in order to submit the task!");
        }

        //get user
        User user = userService.getByEmail(principal.getName());

        Optional<Task> optionalTask = taskRepository.findByTaskIdEagerlyActive(taskId);
        if (!optionalTask.isPresent()) {
            throw new NotFoundException("The task with id " + taskId + " was not found!");
        }

        Task task = optionalTask.get();
        if (Task.Status.COMPLETED.name().equalsIgnoreCase(task.getStatus().name())) {
            throw new IllegalAccessException("The task was completed!");
        }

        if (null == task.getClaimedBy() || null == task.getClaimedByUser()) {
            throw new IllegalAccessException("The task was not claimed!");
        }

        if (!user.getId().equals(task.getClaimedBy())) {
            throw new IllegalAccessException("Tou did not claimed this task!");
        }

        task.setStatus(Task.Status.COMPLETED);
        if(null != task.getState()) {
            task.getState().next(task);
        }

        taskRepository.save(task);
    }

    @SneakyThrows
    @Transactional
    public void claimTask(Principal principal, Long taskId) {
        if (null == taskId) {
            throw new IllegalAccessException("You must provide a task id in order to claim the task!");
        }

        //get user
        User user = userService.getByEmail(principal.getName());

        Optional<Task> optionalTask = taskRepository.findByTaskIdEagerlyActive(taskId);
        if (!optionalTask.isPresent()) {
            throw new NotFoundException("The task with id " + taskId + " was not found!");
        }

        Task task = optionalTask.get();
        if (Task.Status.COMPLETED.name().equalsIgnoreCase(task.getStatus().name())) {
            throw new IllegalAccessException("The task was completed!");
        }

        if (null != task.getClaimedBy() || null != task.getClaimedByUser()) {
            throw new IllegalAccessException("The task was already claimed!");
        }

        task.setClaimedBy(user.getId());
        task.setStatus(Task.Status.IN_PROGRESS);
        if(null != task.getState()) {
            task.getState().next(task);
        }

        Set<Assignee> taskAssignees = task.getActiveAssignedUsers();
        if (taskAssignees.size() > 1) {
            throw new IllegalAccessException("The task has more than 1 assignee!");
        }

        AssigneeMember foundAssigneeMember = null;
        for (AssigneeMember assigneeMember : taskAssignees.iterator().next().getActiveAssigneeMemberSet()) {
            //we can have use A as assignee and group that contains user A so we deactivate the group
            if (user.getId().equals(assigneeMember.getUserId()) && null == foundAssigneeMember) {
                foundAssigneeMember = assigneeMember;
            } else if (null != foundAssigneeMember || !user.getId().equals(assigneeMember.getUserId())) {
                assigneeMember.setActive(false);
                continue;
            }

            if (null != assigneeMember.getGroupId() && null != foundAssigneeMember) {
                Set<Long> userIds = userGroupService.getUserIdsForGroup(assigneeMember.getGroupId());
                if (userIds.contains(user.getId())) {
                    foundAssigneeMember = assigneeMember;
                } else {
                    assigneeMember.setActive(false);
                }
            }
        }

        assigneeMemberRepository.saveAll(taskAssignees.iterator().next().getActiveAssigneeMemberSet());
        taskRepository.save(task);

        //send notification with task updated to managers
        sendTaskUpdatedToManagers(user, taskId, task.getTitle());
    }

    public void sendTaskUpdatedToManagers(User user, Long taskId, String title){
        TaskObservableModel taskObservableModel = new TaskObservableModel(TaskObservableModel.Action.CLAIM, user, taskId, title);

        TaskObservable taskObservable = taskObservableMap.get(taskId);
        if(null != taskObservable) {
            taskObservable.setTaskObservableModel(taskObservableModel);
        }
    }

    @Transactional
    public List<TaskModelDto> getAllTasks() {
        Set<Task> tasks = taskRepository.findAll();

        List<TaskModelDto> taskModelDtos = new ArrayList<>();
        for (Task task : tasks) {
            TaskModelDto taskModelDto = convertToModel(task);

            taskModelDtos.add(taskModelDto);
        }

        return taskModelDtos;
    }

    /**
     * Get open tasks for current user.
     * returns also the tasks that are assigned to groups to which the user belongs.
     *
     * @param principal
     * @return Set<TaskModelDto>
     */
    @Transactional
    public List<TaskModelDto> getOpenTasksForCurrentUser(Principal principal) {
        //get user
        User user = userService.getByEmail(principal.getName());
        //get groups for current user
        Set<Long> groupIds = userGroupService.getGroupsIdsForUser(user.getId());

        Set<Task> tasks = new HashSet<>();
        if(CollectionUtils.isEmpty(groupIds)){
            tasks = taskRepository.findAllActiveForUser(user.getId());
        } else {
            tasks = taskRepository.findAllActiveForUser(user.getId(), groupIds);
        }
        Set<Task> claimedTasks = taskRepository.findAllTaskClaimed(user.getId());
        tasks.addAll(claimedTasks);


        List<TaskModelDto> taskModelDtos = new ArrayList<>();
        for (Task task : tasks) {
            TaskModelDto taskModelDto = convertToModel(task);

            taskModelDtos.add(taskModelDto);
        }

        return taskModelDtos;
    }

    /**
     * Get completed tasks for current user.
     *
     * @param principal
     * @return Set<TaskModelDto>
     */
    @Transactional
    public List<TaskModelDto> getCompletedTasksForCurrentUser(Principal principal) {
        //get user
        User user = userService.getByEmail(principal.getName());

        Set<Task> tasks = taskRepository.findAllCompletedForUser(user.getId());
        Set<Task> tasksClaimed = taskRepository.findAllTaskClaimedCompleted(user.getId());
        tasks.addAll(tasksClaimed);

        List<TaskModelDto> taskModelDtos = new ArrayList<>();
        for (Task task : tasks) {
            TaskModelDto taskModelDto = convertToModel(task);

            taskModelDtos.add(taskModelDto);
        }

        return taskModelDtos;
    }

    @Transactional(readOnly = true)
    public TaskModelDto convertToModel(Task task) {
        TaskModelDto taskModelDto = new TaskModelDto(task);

        //if the task was not claimed yet then get all the assignees
        if (null == task.getClaimedBy()) {
            Set<AssigneeMember> assigneeMembers = task.getActiveAssignedUsers().iterator().next().getActiveAssigneeMemberSet();
            Set<Long> userIds = assigneeMembers.stream().map(AssigneeMember::getUserId).collect(Collectors.toSet());
            Set<Long> groupIds = assigneeMembers.stream().map(AssigneeMember::getGroupId).collect(Collectors.toSet());

            if (!CollectionUtils.isEmpty(userIds)) {
                Map<User, Boolean> users = new HashMap<>();
                userService.findUsersByIds(userIds).forEach(user -> {
                    users.put(user, false);
                });

                taskModelDto.setUsers(UserDto.toDtos(users));
            }

            if (!CollectionUtils.isEmpty(groupIds)) {
                List<Group> groups = new ArrayList<>(groupService.findGroupsByIds(groupIds));

                taskModelDto.setGroups(GroupDto.toDtos(groups));
            }
        }

        return taskModelDto;
    }

    @SneakyThrows
    @Transactional
    public void createOrEditTask(CreateTaskDto taskDto, Principal principal) {
        if (null == taskDto.getId()) {// create
            if (CollectionUtils.isEmpty(taskDto.getGroups()) && CollectionUtils.isEmpty(taskDto.getUsers())) {
                throw new IllegalAccessException("You do not have users or groups assigned to this task!");
            }

            Task task = new Task();
            task.setContent(taskDto.getContent());
            task.setTitle(taskDto.getTitle());
            task.setCategory(taskDto.getCategory());

            //autoclaim task if has only one user assigned
            if (CollectionUtils.isEmpty(taskDto.getGroups()) && !CollectionUtils.isEmpty(taskDto.getUsers()) && taskDto.getUsers().size() == 1) {
                task.setClaimedBy(taskDto.getUsers().iterator().next().getId());
                task.setState(new InProgressState());
                task.setStatus(Task.Status.IN_PROGRESS);
            } else {
                task.setStatus(Task.Status.CREATED);
                task.setState(new CreatedState());
            }

            task = taskRepository.save(task);

            this.addTaskAssignees(task.getId(), taskDto.getUsers(), taskDto.getGroups());

            //add observers
            if(!CollectionUtils.isEmpty(taskDto.getGroups())) {
                Set<Long> groupIds = taskDto.getGroups().stream().map(MemberDto::getId).collect(toSet());
                addObservers(groupIds, task);
            }

            //send email notification
            this.sendEmailNotifications( taskDto.getUsers(), taskDto.getGroups(), task);

        } else {// edit task by manager
            Optional<Task> optionalTask = taskRepository.findByTaskIdEagerlyActive(taskDto.getId());
            if (!optionalTask.isPresent()) {
                throw new NotFoundException("A task with id:" + taskDto.getId() + " does not exist!");
            }

            Task task = optionalTask.get();
            String managerEmail = principal.getName();

            //check it the principal is manager in order to edit the task
            if (Boolean.FALSE.equals(checkForManager(task, managerEmail))) {
                throw new IllegalAccessException("You can not edit this task because you are not a manager");
            }

            task.setCategory(taskDto.getCategory());
            task.setContent(taskDto.getContent());
            task.setTitle(taskDto.getTitle());

            taskRepository.save(task);

            //remove old assignees
            assigneeMemberRepository.deleteAll(task.getAssignedUsers().iterator().next().getAssigneeMemberSet());
            assigneeRepository.delete(task.getAssignedUsers().iterator().next());

            //add new assignees
            this.addTaskAssignees(task.getId(), taskDto.getUsers(), taskDto.getGroups());
        }
    }

    public void addObservers(Set<Long> groupIds, Task task){
        Set<String> managerEmails = getManagersForGroupsAndUserAssignedToTask(groupIds);

        TaskObservable observable = new TaskObservable();
        managerEmails.forEach(email -> {
            ManagerObserver observer = new ManagerObserver(emailService);
            observer.setEmail(email);
            observable.addObserver(observer);
        });

        if(!CollectionUtils.isEmpty(managerEmails)) {
            taskObservableMap.put(task.getId(), observable);
        }

    }


    public void sendEmailNotifications(Set<MemberDto> users, Set<MemberDto> groups, Task task){
        Set<Long> userIds = new HashSet<>();

        if(!CollectionUtils.isEmpty(users)){
            userIds = users.stream().map(MemberDto::getId).collect(toSet());
        }

        if(!CollectionUtils.isEmpty(groups)) {
            Set<Long> userIdsFromGroups = userGroupService.getUserIdsForGroupIds(groups.stream().map(MemberDto::getId).collect(toSet()));
            userIds.addAll(userIdsFromGroups);
        }

        if(!CollectionUtils.isEmpty(userIds)) {
            List<String> emails = userService.findUsersEmailsByIds(userIds);

            emails.forEach(email -> emailService.sendAssignmentTaskEmail(email, task));
        }
    }

    public Set<AssigneeMember> getActiveAssignees(Task task) {
        Assignee taskAssignee = null;
        if(CollectionUtils.isEmpty(task.getActiveAssignedUsers())) {
            Optional<Task> task2 = taskRepository.findByTaskIdEagerlyActive(task.getId());
            if(task2.isPresent()){
                taskAssignee = task.getActiveAssignedUsers().iterator().next();
            }
        } else {
            taskAssignee = task.getActiveAssignedUsers().iterator().next();
        }
        return taskAssignee.getActiveAssigneeMemberSet();
    }

    /**
     * Check if the given user email is manager for given groups
     * @param task
     * @param principalEmail
     * @return
     */
    private boolean checkForManager(Task task, String principalEmail) {
        Set<AssigneeMember> assigneeMembers = getActiveAssignees(task);
        Set<Long> groupIds = assigneeMembers.stream().map(AssigneeMember::getGroupId).collect(toSet());
        if (!CollectionUtils.isEmpty(groupIds)) {
            return userGroupService.checkForManager(groupIds, principalEmail);
        }

        return false;
    }


    /**
     //     * Gets the manager emails for groups that the user that claimed the tasks belong and that specified groups were assigned to task
     //     */
    private Set<String> getManagersForGroupsAndUserAssignedToTask(Set<Long> groupIds) {
        Set<String> managersEmails = new HashSet<>();
        if (!CollectionUtils.isEmpty(groupIds)) {
            managersEmails = userGroupService.getManagersForUserThatBelongToGroups(groupIds);
        }

        return managersEmails;
    }

    @Transactional
    public void addTaskAssignees(Long taskId, Set<MemberDto> users, Set<MemberDto> groups) {
        //create assignee
        Assignee assignee = new Assignee();
        assignee.setTaskId(taskId);
        assignee.setActive(true);

        assignee = assigneeRepository.save(assignee);

        //create assignee members
        Set<AssigneeMember> assigneeMembers = new HashSet<>();
        if (!CollectionUtils.isEmpty(users)) {
            for (MemberDto userMember : users) {
                assigneeMembers.add(AssigneeMember.builder()
                        .active(true)
                        .userId(userMember.getId())
                        .assigneeId(assignee.getId())
                        .build());
            }
        }

        if (!CollectionUtils.isEmpty(groups)) {
            for (MemberDto groupMember : groups) {
                assigneeMembers.add(AssigneeMember.builder()
                        .active(true)
                        .groupId(groupMember.getId())
                        .assigneeId(assignee.getId())
                        .build());
            }
        }

        assigneeMemberRepository.saveAll(assigneeMembers);
    }

    /**
     * Filter tasks by group, user, status, and any combination of this 3 attributes
     *
     * @param taskFilterDto
     * @return
     */
    public List<TaskModelDto> searchTasks(TaskFilterDto taskFilterDto) {
        boolean isAMatch = false;
        if (null != taskFilterDto.getGroupId() && null != taskFilterDto.getUserId()) {
            isAMatch = userGroupService.checkIfUserBelongsToGroup(taskFilterDto.getUserId(), taskFilterDto.getGroupId());
        }

        Set<Long> groupIdsForUSer = new HashSet<>();
        if (null != taskFilterDto.getUserId()) {
            groupIdsForUSer = userGroupService.getGroupsIdsForUser(taskFilterDto.getUserId());
        }

        taskFilterDto.setMatch(isAMatch);

        TaskSearchFilter taskSearchFilter = new TaskSearchFilter();
        taskSearchFilter.setTaskFilterDto(taskFilterDto);
        taskSearchFilter.setGroupIdsForUser(groupIdsForUSer);

        List<TaskModelDto> taskModelDtos = new ArrayList<>();
        for (Task task : taskRepository.findAll(taskSearchFilter)) {
            TaskModelDto taskModelDto = convertToModel(task);

            taskModelDtos.add(taskModelDto);
        }

        return taskModelDtos;
    }

}
