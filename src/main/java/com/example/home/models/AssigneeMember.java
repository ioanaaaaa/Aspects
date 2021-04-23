package com.example.home.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Entity
@Table(name = "assignee_members")
@Builder
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class AssigneeMember {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "assignee_id")
    private Long assigneeId;

    @ToString.Exclude
    @ManyToOne()
    @JoinColumn(name = "assignee_id", insertable = false, updatable = false)
    private Assignee assignee;

    private Long userId;
    private Long groupId;

    private boolean active;

}
