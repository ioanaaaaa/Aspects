package com.example.home.models;

import com.example.home.models.converters.JsonStateConverter;
import com.example.home.services.states.TaskState;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@Entity
@Table(name = "tasks")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Task {
    public enum Status{
        IN_PROGRESS, COMPLETED, CREATED
    }
    public enum Category{
        Shopping, Cleaning
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String content;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "claimed_by")
    private Long claimedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claimed_by", insertable = false, updatable = false)
    private User claimedByUser;

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = true)
    private Set<Assignee> assignedUsers = new HashSet<>();

    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = true)
    @Where(clause = "active=true")
    private Set<Assignee> activeAssignedUsers = new HashSet<>();

    //    @Enumerated(EnumType.STRING)
//    @Enumerated(EnumType.STRING)
    @Convert(converter = JsonStateConverter.class)
    private TaskState state;

    @Override
    public int hashCode() {
        return id.hashCode();
    }

//    @PrePersist
//    public void setState(){
//        if(null == claimedBy){
//            this.status = new CreatedState().getStatus();
//        }
//    }




}
