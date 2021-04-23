package com.example.home.controllers;

import com.example.home.dtos.tasks.CreateTaskDto;
import com.example.home.dtos.tasks.TaskFilterDto;
import com.example.home.dtos.tasks.TaskModelDto;
import com.example.home.services.TaskService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/add-edit")
    public ResponseEntity createOrEditTask(Principal principal, @RequestBody CreateTaskDto taskDto) {
        taskService.createOrEditTask(taskDto, principal);
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping("")
    public List<TaskModelDto> getAllTasks() {
        return taskService.getAllTasks();
    }

    /**
     * Get open tasks for current user.
     * returns also the tasks that are assigned to groups to which the user belongs.
     *
     * @param principal
     * @return Set<TaskModelDto>
     */
    @GetMapping("/open/current-user")
    public List<TaskModelDto> getOpenTasksForCurrentUser(Principal principal) {
        return taskService.getOpenTasksForCurrentUser(principal);
    }

    /**
     * Get completed tasks for current user.
     *
     * @param principal
     * @return Set<TaskModelDto>
     */
    @GetMapping("/completed/current-user")
    public List<TaskModelDto> getCompletedTasksForCurrentUser(Principal principal) {
        return taskService.getCompletedTasksForCurrentUser(principal);
    }

    /**
     * Claim task by current user.
     *
     * @param principal
     * @param taskId
     * @return
     */
    @PostMapping("/claim/{id}")
    public ResponseEntity claimTask(Principal principal, @PathVariable("id") Long taskId) {
        taskService.claimTask(principal, taskId);

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Deletes the specified task only if the current user is manager
     *
     * @param taskId
     * @param principal
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity deleteTaskById(@PathVariable("id") Long taskId, Principal principal) throws NotFoundException, IllegalAccessException {
        taskService.deleteTask(taskId, principal);
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Submit the task only if current user claimed it
     *
     * @param principal
     */
    @PostMapping("/submit/{id}")
    public ResponseEntity submitTask(Principal principal, @PathVariable("id") Long taskId) throws NotFoundException, IllegalAccessException {
        taskService.submitTask(principal, taskId);

        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Get open and completed tasks for members of the groups that are managed by principle
     *
     * @param principal
     * @return
     */
    @GetMapping("/my-teams")
    public List<TaskModelDto> getMyTeamsTasks(Principal principal) {
        return taskService.getMyTeamsTasks(principal);

    }

    /**
     * Filter tasks by group, user, status, and any combination of this 3 attributes
     *
     * @param taskFilterDto
     * @return
     */
    @PostMapping("/filter")
    public List<TaskModelDto> filterTasks(@RequestBody TaskFilterDto taskFilterDto) {
        return taskService.searchTasks(taskFilterDto);
    }

}
