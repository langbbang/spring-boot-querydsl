package me.songha.tutorial.repository.support;

import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;

/**
 * ** ORDER BY NULL 기능 제공 **
 * MySQL 에서는 GROUP BY 쿼리 사용 시 filesort 라는 것을 생성하며 성능 저하의 원인이 된다고한다.
 * 그래서 ORDER BY NULL 를 조건으로 주는 것인데,
 * Querydsl 에서는 별도로 해당 구문을 지원해주지 않기 때문에 커스텀한 클래스이다.
 */
public class OrderByNull extends OrderSpecifier {

    public static final OrderByNull DEFAULT = new OrderByNull();

    private OrderByNull(){
        super(Order.ASC, NullExpression.DEFAULT, NullHandling.Default);
    }
}
