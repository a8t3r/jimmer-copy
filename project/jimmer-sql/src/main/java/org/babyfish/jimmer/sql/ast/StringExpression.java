package org.babyfish.jimmer.sql.ast;

public interface StringExpression extends Expression<String> {

    default Predicate like(String pattern) {
        return like(pattern, LikeMode.ANYWHERE);
    }

    Predicate like(String pattern, LikeMode likeMode);

    default Predicate ilike(String pattern) {
        return ilike(pattern, LikeMode.ANYWHERE);
    }

    Predicate ilike(String pattern, LikeMode likeMode);
}