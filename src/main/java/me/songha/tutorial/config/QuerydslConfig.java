package me.songha.tutorial.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;

/**
 * repository 에서 extends or implements 를 지양하기 위해 JPAQueryFactory 를 bean 등록해준다.
 */
@Configuration
@RequiredArgsConstructor
public class QuerydslConfig {
    private final EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }
}
