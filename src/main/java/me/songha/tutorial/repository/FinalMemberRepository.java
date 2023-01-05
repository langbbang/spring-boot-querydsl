package me.songha.tutorial.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import me.songha.tutorial.domain.Member;
import me.songha.tutorial.dto.*;
import me.songha.tutorial.repository.support.OrderByNull;
import org.springframework.stereotype.Repository;

import java.util.List;

import static me.songha.tutorial.domain.QMember.member;
import static me.songha.tutorial.domain.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

/**
 * extends 하지 않고 bean 등록된 JPAQueryFactory 를 생성자 주입하는 방법
 */
@Repository
@RequiredArgsConstructor
public class FinalMemberRepository {
    private final JPAQueryFactory queryFactory;

    /**
     * selectFrom()은 select 절 컬럼이 명시되어있지 않아 모든 컬럼을 조회하는 단점이 있다.
     */
    public List<Member> getMembers() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    /** ============================================================================================================ */

    /**
     * JPA를 사용하면 영속성 컨택스트가 Dirty Checking 기능을 지원해주는데
     * 이를 이용하면 엄청나게 많은 양의 데이터에 대해서 업데이트 쿼리가 나갈수도 있다.
     * 이렇게하면 일괄 Update 하는 것보다 확실히 성능이 낮다.
     */
    public void dirtyChecking() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member : result) {
            member.setUsername(member.getUsername() + "+");
        }
    }

    /**
     * 주의해야할 점은 영속성 컨택스트의 1차 캐시 갱신이 안된다.
     * DB의 업데이트 작업만 필요할 경우 사용하는 것이 좋다.
     */
    public void batchUpdate() {
        String test = "test";
        queryFactory
                .update(member)
                .set(member.username, test)
                .execute();
    }

    /** ============================================================================================================ */

    /**
     * BooleanBuilder 를 사용해 where 절을 만드는 방법은 직관적이지 않다.
     */
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }

        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }

    /**
     * BooleanExpression 를 사용하여 where 절을 좀 더 직관적으로 표현할 수 있다.
     */
    public List<MemberTeamDto> searchByWhere(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageBetween(Integer ageLoe, Integer ageGoe) {
        return ageLoe(ageLoe).and(ageGoe(ageGoe));
    }

    /** ============================================================================================================ */

    /**
     * OrderByNull 사용
     */
    public List<Integer> useOrderByNull() {
        return queryFactory
                .select(member.age.sum())
                .from(member)
                .innerJoin(member.team, team)
                .groupBy(member.team)
                .orderBy(OrderByNull.DEFAULT)
                .fetch();
    }

    /** ============================================================================================================ */

    /**
     * select 절에 entity 를 불러올 경우 모든 컬럼을 조회할 수 있으니 주의가 필요하다.
     */
    public List<MemberTeamDto2> entityInSelect(Long teamId) {
        return queryFactory
                .select(new QMemberTeamDto2(
                        member.id,
                        member.username,
                        member.age,
                        member.team))
                .from(member)
                .innerJoin(member.team, team)
                .where(member.team.id.eq(teamId))
                .fetch();
    }

    /** ============================================================================================================ */

    /**
     * teamId는 이미 기존에 매개변수로 받았으니 데이터베이스에서 가져올 필요는 없다.
     * 중복된 컬럼은 가져오지 않도록 해서 성능상에서 약간의 이득을 더 볼 수 있다.
     */
    public List<MemberTeamDto> findSameTeamMember(Long teamId) {
        return queryFactory.select(new QMemberTeamDto(
                        member.id,
                        member.username,
                        member.age,
                        Expressions.asNumber(teamId),
                        team.name)).from(member)
                .innerJoin(member.team, team)
                .where(member.team.id.eq(teamId))
                .fetch();
    }

    /** ============================================================================================================ */

    /**
     * 조인을 명시하지 않고 엔터티에서 다른 엔터티를 조회해서 비교하는 경우 JPA 가 알아서 크로스 조인을 하게 된다.
     * 크로스 조인을 하게 되면 나올 수 있는 데이터가 그냥 조인들보다 많아지기 때문에 성능상에 단점이 있다.
     */
    public List<Member> crossJoin() {
        return queryFactory
                .selectFrom(member)
                .where(member.team.id.gt(member.team.leader.id))
                .fetch();
    }

    /**
     * cross join 을 회피하기 위해 innerjoin 을 명시하였다.
     */
    public List<Member> crossJoinToInnerJoin() {
        return queryFactory
                .selectFrom(member)
                .innerJoin(member.team, team)
                .where(member.team.id.gt(member.team.leader.id))
                .fetch();
    }

    /** ============================================================================================================ */

    /**
     * 값 존재 유무를 확인하고 싶을 땐, count 를 사용하는 것 보단 fetchFirst() 를 사용하는 것이 성능 상 유리하다.
     * count는 전체 행을 조회하기 때문에, 필요한 값이 조회되면 바로 반환하는 limit 1 과 같은 fetchFirst() 를 사용하자.
     */
    public Boolean exist(Long memberId) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(member)
                .where(member.id.eq(memberId))
                .fetchFirst();

        return fetchOne != null;
    }

}
