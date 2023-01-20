package me.songha.tutorial.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MemberDto {
    private String username;
    private int age;

    /**
     * QueryProjection 를 붙이면 dto 에서도 QClass 를 만든다.
     */
    @QueryProjection
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
