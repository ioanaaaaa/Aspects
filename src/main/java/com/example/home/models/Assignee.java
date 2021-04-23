package com.example.home.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "assignees")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Assignee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "task_id")
    private Long taskId;

    @ToString.Exclude
    @ManyToOne()
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    @ToString.Exclude
    @OneToMany(mappedBy = "assignee")
    private Set<AssigneeMember> assigneeMemberSet;

    @ToString.Exclude
    @Where(clause = "active=true")
    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private Set<AssigneeMember> activeAssigneeMemberSet;

    private boolean active;

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
