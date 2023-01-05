package me.songha.tutorial.dto;

import com.querydsl.core.annotations.QueryProjection;
import me.songha.tutorial.domain.Team;
import lombok.Data;

@Data
public class MemberTeamDto2 {
    private Long memberId;
    private String username;
    private int age;
    private Team team;

    @QueryProjection
    public MemberTeamDto2(Long memberId, String username, int age, Team team) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.team = team;
    }

    @QueryProjection
    public MemberTeamDto2(Long memberId, String username, int age, Long teamId) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.team = Team.builder().id(teamId).build();
    }
}
