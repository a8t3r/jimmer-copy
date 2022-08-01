---
sidebar_position: 1
title: Subquery
---

## Typed Subquery

### IN subquery based on single column
```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(
                // highlight-next-line
                book.id().in(
                    q.createSubQuery(AuthorTableEx.class, (sq, author) -> {
                        return sq
                            .where(
                                    author.firstName().eq("Alex")
                            )
                            .select(author.books().id());
                    })
                )
            )
            .select(book);
    })
    .execute();
books.forEach(System.out::println);
```

The final generated SQL is as follows

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where 
    /* highlight-next-line */
    tb_1_.ID in (
        select 
            tb_3_.BOOK_ID 
        from AUTHOR as tb_2_ 
        inner join BOOK_AUTHOR_MAPPING as tb_3_ 
            on tb_2_.ID = tb_3_.AUTHOR_ID 
        where 
            tb_2_.FIRST_NAME = ?
    )

```

### IN subquery based on multiple columns

```java
List<Book> newestBooks = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(
                // highlight-next-line
                Expression.tuple(
                    book.name(),
                    book.edition()
                ).in(
                    q.createSubQuery(BookTable.class, (sq, book2) -> {
                        return sq
                            .groupBy(book2.name())
                            .select(
                                    book2.name(),
                                    book2.edition().max()
                            );
                    })
                )
            )
            .select(book);
    })
    .execute();
newestBooks.forEach(System.out::println);
```

The final generated SQL is as follows

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where 
    /* highlight-next-line */
    (tb_1_.NAME, tb_1_.EDITION) in (
        select 
            tb_2_.NAME, 
            max(tb_2_.EDITION) 
            from BOOK as tb_2_ 
            group by tb_2_.NAME
    )
```

### Treat subqueries as simple value

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(
                // highlight-next-line
                book.price().gt(
                    q.createSubQuery(BookTable.class, (sq, book2) -> {
                        return sq.select(
                            book2
                                .price()
                                .avg()
                                .coalesce(BigDecimal.ZERO)
                        );
                    })
                )
            )
            .select(book);
    })
    .execute();
books.forEach(System.out::println);
```

The final generated SQL is as follows

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where 
    /* highlight-next-line */
    tb_1_.PRICE > (
        select 
            coalesce(avg(tb_2_.PRICE), ?) 
        from BOOK as tb_2_
    )
```

### Using subqueries in select and orderBy clauses

```java
List<Tuple2<BookStore, BigDecimal>> storeAvgPriceTuples = sqlClient
    .createQuery(BookStoreTable.class, (q, store) -> {
        TypedSubQuery<BigDecimal> avgPriceSubQuery =
            q.createSubQuery(BookTable.class, (sq, book) -> {
                return sq.select(
                    book
                        .price()
                        .avg()
                        .coalesce(BigDecimal.ZERO)
                );
            });
        return q
                .orderBy(
                    // highlight-next-line
                    avgPriceSubQuery,
                    OrderMode.DESC
                )
                .select(
                    store,
                    // highlight-next-line
                    avgPriceSubQuery
                );
    })
    .execute();
```

The final generated SQL is as follows

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.WEBSITE, 
    (
        /* highlight-next-line */
        select coalesce(avg(tb_2_.PRICE), ?) 
        from BOOK as tb_2_
    ) 
from BOOK_STORE as tb_1_ 
order by (
    /* highlight-next-line */
    select coalesce(avg(tb_2_.PRICE), ?) 
    from BOOK as tb_2_
) desc
```

### Using the ANY operator

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(
                book.id().eq(
                    q
                        .createSubQuery(
                            AuthorTableEx.class, 
                            (sq, author) -> {
                                return sq
                                    .where(
                                        author.firstName().in(
                                            "Alex", 
                                            "Bill"
                                        )
                                    )
                                    .select(author.books().id());
                                }
                        )
                        // highlight-next-line
                        .any()
                )
            )
            .select(book);
    })
    .execute();
books.forEach(System.out::println);
```

The final generated SQL is as follows

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where tb_1_.ID = 
    /* highlight-next-line */
    any(
        select 
            tb_3_.BOOK_ID 
        from AUTHOR as tb_2_ 
        inner join BOOK_AUTHOR_MAPPING as tb_3_ 
            on tb_2_.ID = tb_3_.AUTHOR_ID 
        where 
            tb_2_.FIRST_NAME in (?, ?)
    )
```

### Using the ALL operator

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(
                book.id().eq(
                    q
                        .createSubQuery(
                            AuthorTableEx.class, 
                            (sq, author) -> {
                                return sq
                                    .where(
                                        author.firstName().in(
                                            "Alex", 
                                            "Bill"
                                        )
                                    )
                                    .select(author.books().id());
                                }
                        )
                        // highlight-next-line
                        .all()
                )
            )
            .select(book);
    })
    .execute();
books.forEach(System.out::println);
```

The final generated SQL is as follows

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where tb_1_.ID = 
    /* highlight-next-line */
    all(
        select 
            tb_3_.BOOK_ID 
        from AUTHOR as tb_2_ 
        inner join BOOK_AUTHOR_MAPPING as tb_3_ 
            on tb_2_.ID = tb_3_.AUTHOR_ID 
        where 
            tb_2_.FIRST_NAME in (?, ?)
    )
```

### Using the EXISTS operator

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(
                q.createSubQuery(
                    AuthorTableEx.class,
                    (sq, author) -> {
                        return sq
                            .where(
                                    book.eq(author.books()),
                                    author.firstName().eq("Alex")
                            )
                            .select(author);
                    }
                )
                // highlight-next-line
                .exists()
            )
            .select(book);
    })
    .execute();
books.forEach(System.out::println);
```

The final generated SQL is as follows

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where 
    /* highlight-next-line */
    exists (
        select 
            1 
        from AUTHOR as tb_2_ 
        inner join BOOK_AUTHOR_MAPPING as tb_3_ 
            on tb_2_.ID = tb_3_.AUTHOR_ID 
        where 
            tb_1_.ID = tb_3_.BOOK_ID 
        and 
            tb_2_.FIRST_NAME = ?
    )
```

:::note
Note that in the final generated SQL, the column selected by the subquery is the constant `1`, not the setting of the Java code.

This is because the `exists` operator only cares whether the subquery matches the data, not which columns the subquery selects. Whatever you select in subquery will be ignored.
:::

## Untyped subquery

The last example in the previous section is the `exists` subquery, whatever you select in subquery will be ignored.

That being the case, why specify the selection columns for the `exists` subquery?

Therefore, jimmer-sql supports untyped sub-query: wild-sub-query, which is different from ordinary sub-query. In the implementation of wild sub-query, the call to method `select` is no longer required, that is, no return type is required.

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(
                // highlight-next-line
                q.createWildSubQuery(
                    AuthorTableEx.class,
                    (sq, author) -> {
                        sq.where(
                                book.eq(author.books()),
                                author.firstName().eq("Alex")
                        );
                        // No need to call select at the end
                    }
                )
                .exists()
            )
            .select(book);
    })
    .execute();
books.forEach(System.out::println);
```

The final generated SQL is unchanged, still is

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where 
    /* highlight-next-line */
    exists (
        select 
            1 
        from AUTHOR as tb_2_ 
        inner join BOOK_AUTHOR_MAPPING as tb_3_ 
            on tb_2_.ID = tb_3_.AUTHOR_ID 
        where 
            tb_1_.ID = tb_3_.BOOK_ID 
        and 
            tb_2_.FIRST_NAME = ?
    )
```

:::note
The only value of wild subqueries is to work with the `exists` operator.
:::