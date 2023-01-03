package me.songha.tutorial.domain;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "team_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    List<Member> members = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "member_id")
    Member leader;

    @Builder
    public Team(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}
