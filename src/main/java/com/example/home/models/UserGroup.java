package com.example.home.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "user_group")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "user_id", insertable = true)
    private Long userId;

    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @ManyToOne
    private User user;

    @NotNull
    @Column(name = "group_id")
    private Long groupId;

    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    @ManyToOne
    private Group group;

    private boolean isManager;

    @Override
    public String toString() {
        return "UserGroup{" +
                "id=" + id +
                ", userId=" + userId +
                ", groupId=" + groupId +
                ", isManager=" + isManager +
                '}';
    }
}
