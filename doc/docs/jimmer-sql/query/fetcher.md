---
sidebar_position: 4
title: Object Fetcher
---

:::tip
[Object Fetcher](./fetcher) is a very powerful feature provided by jimmer-sql, comparable to GraphQL.

Even if users do not use any GraphQL-related technology stack, they can obtain object graph query capabilities similar to GraphQL at the SQL query level.
:::

Object fetcher is similar to the following techniques, but more powerful: 

- [EntityGraph of JPA](https://www.baeldung.com/jpa-entity-graph)
- [`Include` of ADO.NET EntityFramework](https://docs.microsoft.com/en-us/dotnet/api/system.data.objects.objectquery-1.include?view=netframework-4.8)
- [`include` of ActiveRecord](https://guides.rubyonrails.org/active_record_querying.html#includes)

While the code to return the entire object in a query is simple, the default object format often doesn't fit well with development needs.

1. Object properties that we don't need are loaded, which is a waste. This is called the <b>over fetch</b> problem.
2. The object properties we need have not been loaded so that the program cannot work normlly, this is called the <b>under fetch</b> problem.

Object fetcher solve this problem nicely, make queries to return objects that are neither over fetched nor under fetched.

## Basic usage

### Fetch scalar property

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                // highlight-next-line
                book.fetch(
                    BookFetcher.$.name()
                )
            );
    })
    .execute();
books.forEach(System.out::println);
```

:::note
The annotation processor will automatically generate a fetcher class for each entity interface, in this case, it is `BookFetcher`.
:::

The generated SQL is as follows:

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME 
from BOOK as tb_1_ 
where tb_1_.EDITION = ?
```

:::note
The Java code does not call the method `id()` in BookFetcher, however, we see that the SQL statement still queries the id property of the object.

The id property is treated specially, is always queried, and is not controlled by the object fetcher.

In fact, there is no method `id() ` in the auto-generated class `BookFetcher` because it is not needed.
:::

The printed result is as follows (the original output is compact, formatted here for readability):

```
{
    "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
    "name":"Learning GraphQL"
}
...omit the 2nd object...,
...omit the 3rd object...,
...omit the 4th object...
```

### Fetch multiple properties

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    // highlight-next-line
                    BookFetcher.$.name().price()
                )
            );
    })
    .execute();
books.forEach(System.out::println);
```

:::note
Object fetchers are immutable objects, and each call to chained method  returns a new object fetcher.

That is, in the above code
1. `BookFetcher.$`
2. `BookFetcher.$.name()`
3. `BookFetcher.$.name().price()`

are three different object fetchers, all of them are immutable.

Object fetcher is immutable object, so you can freely share object fetchers by static variables.
:::

The generated SQL is as follows:

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME,
    tb_1_.PRICE  
from BOOK as tb_1_ 
where tb_1_.EDITION = ?
```

The printed result is as follows (the original output is compact, formatted here for readability):

```
{
    "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
    "name":"Learning GraphQL",
    "price":51.00
}
...omit the 2nd object...,
...omit the 3rd object...,
...omit the 4th object...
```

### allScalarFields

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    // highlight-next-line
                    BookFetcher.$.allScalarFields()
                )
            );
    })
    .execute();
books.forEach(System.out::println);
```

`allScalarFields()` is used to load all scalar fields.

:::info
Since objects in real projects tend to have many properties, `allScalarFields()` is very useful.
:::

The generated SQL is as follows:

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE   
from BOOK as tb_1_ 
where tb_1_.EDITION = ?
```

The printed result is as follows (the original output is compact, formatted here for readability):
```
{
    "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
    "name":"Learning GraphQL",
    "edition":3,
    "price":51.00
}
...omit the 2nd object...,
...omit the 3rd object...,
...omit the 4th object...
```

### Negative property

The properties mentioned above are all positive properties, and properties to be queried are constantly added. Negative properties are the opposite, removing the specified property from the existing object fetcher.
```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    BookFetcher.$
                        .allScalarFields()
                        // highlight-next-line
                        .edition(false)
                )
            );
    })
    .execute();
books.forEach(System.out::println);
```

`edition(false)` takes the parameter false, which is the negative property.

- The properties of `allScalarFields()` means `id + name + edition + price`
- `edition(false)` means `-edition`

Finally, the merged properties are `id + name + price`

:::note
1. For positive properties, `edition()` and `edition(true)` are equivalent.
2. Negative properties are very useful when most properties need to be fetched but a few properties need not be fetched.
:::

The generated SQL is as follows:

```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.PRICE   
from BOOK as tb_1_ 
where tb_1_.EDITION = ?
```

The printed result is as follows (the original output is compact, formatted here for readability):

```
{
    "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
    "name":"Learning GraphQL",
    "price":51.00
}
...omit the 2nd object...,
...omit the 3rd object...,
...omit the 4th object...
```

### allTableFields

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    // highlight-next-line
                    BookFetcher.$.allTableFields()
                )
            );
    })
    .execute();
books.forEach(System.out::println);
```

`allTableFields()` contains all the properties declared by the entity interface, including scalar properties, and many-to-one properties based on foreign keys (of course, the parent object only has id).

:::note
`allTableFields()` returns the default object format, so the following two examples are equivalent

1. 
    ```
    q.select(
        book.fetch(
            BookFetcher.$.allTableFields()
        )
    )
    ```

2. 
    ```
    q.select(
        book
    )
    ```
:::

The generated SQL is as follows:
```sql
select 
    tb_1_.ID, 
    tb_1_.NAME, 
    tb_1_.EDITION, 
    tb_1_.PRICE, 
    tb_1_.STORE_ID 
from BOOK as tb_1_ 
where tb_1_.EDITION = ?
```

The printed result is as follows (the original output is compact, formatted here for readability):
```
{
    "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
    "name":
    "Learning GraphQL",
    "edition":3,
    "price":51.00,
    "store":{"id":"d38c10da-6be8-4924-b9b9-5e81899612a0"}
}
...omit the 2nd object...,
...omit the 3rd object...,
...omit the 4th object...
```

Like the default object format, many-to-one properties based on foreign key are set to the parent object with only the id property.

### Fetch associated object with only id.

We have already explained how to implement this function for foreign key based associations, so let's look at an example of `Book.authors` for many-to-many associations.

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    BookFetcher.$.
                        .allScalarFields()
                        // highlight-next-line
                        .authors()
                )
            );
    })
    .execute();
books.forEach(System.out::println);
```

Here, `authors()` means to fetch many-to-many associations. Note that no parameters are specified, which means that only the id property of the associated object is fetched.

Two SQL statemenets are generated.

1. Query the aggregate root objects: `Book`
    ```sql
    select 
        tb_1_.ID, 
        tb_1_.NAME, 
        tb_1_.EDITION, 
        tb_1_.PRICE, 
        tb_1_.STORE_ID 
    from BOOK as tb_1_ 
    where tb_1_.EDITION = ?
    ```

2. Query all `Author` objects with only id based on association `Book.authors`

    ```sql
    select 
        tb_1_.BOOK_ID, /* batch-map key */
        tb_1_.AUTHOR_ID /* batch-map value */
    from BOOK_AUTHOR_MAPPING as tb_1_ 
        where tb_1_.BOOK_ID in (?, ?, ?, ?)
    ```

This example illustrates:

1. The current query only needs the id of the associated object, and no filter is used (filter is a concept that will be explained later).

    jimmer-sql will optimize this situation by querying only the middle table `BOOK_AUTHOR_MAPPING`, not the target table `AUTHOR`.

2. `where tb_1_.BOOK_ID in (?, ?, ?, ?)` is a batch query, because the first SQL statement returns 4 aggregate root objects.

    jimmer-sql uses batch queries to solve the `N+1` problem, just like GraphQL's `DataLoader`.

    When the list of a batch is too long, jimmer-sql will cut it in batches, which will be explained in the [Batch Size section](#batchsize) later.

3. jimmer-sql uses additional SQL to query associated objects instead of using LEFT JOIN in the SQL of the primary data query.

    The purpose of this design is to prevent the JOIN of the collection association from causing duplication of results, because this kind of data duplication has a devastating effect on pagination queries.

The printed result is as follows (the original output is compact, formatted here for readability):

```
{
    "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
    "name":"Learning GraphQL",
    "edition":3,
    "price":51.00,
    "authors":[
        {"id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94"},
        {"id":"fd6bb6cf-336d-416c-8005-1ae11a6694b5"}
    ]
}
...omit the 2nd object...,
...omit the 3rd object...,
...omit the 4th object...
```

### BatchSize

In the above example, we see SQL like this
```sql
select 
    tb_1_.BOOK_ID, 
    tb_1_.AUTHOR_ID 
from BOOK_AUTHOR_MAPPING as tb_1_ 
    where tb_1_.BOOK_ID in (?, ?, ?, ?)
```

Here, the `in` expression implements a batch query, solving the `N+1` problem.

If a batch is too large, it will be divided into several batches according to a setting called `batchSize`, such as

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    BookFetcher.$
                        .authors(
                            AuthorFetcher.$,
                            // highlight-next-line
                            it -> it.batch(2)
                        )
                )
            );
    })
    .execute();
```

:::danger
Here, the batchSize of the association `Book.authors` is set to 2. This configuration will lead to poor performance. This is just for demonstration, please do not set such a small value in actual projects.
:::

This will cause `in(?, ?, ?, ?)` clause to be split into two `in(?, ?)` clauses, and the SQL that fetches the associated objects will be split into two pieces.

1.
    ```sql
    select 
        tb_1_.BOOK_ID, 
        tb_1_.AUTHOR_ID 
    from BOOK_AUTHOR_MAPPING as tb_1_ 
        where tb_1_.BOOK_ID in (?, ?)
    ```

2.
    ```sql
    select 
        tb_1_.BOOK_ID, 
        tb_1_.AUTHOR_ID 
    from BOOK_AUTHOR_MAPPING as tb_1_ 
        where tb_1_.BOOK_ID in (?, ?)
    ```

In actual development, batchSize is not set like this in most cases, but the global configuration in JSqlClient is used.

1. `JSqlClient.getDefaultBatchSize()`: Default batchSize for one-to-one and many-to-one association properties, default value is 128
2. `JSqlClient.getDefaultListBatchSize()`: Default batchSize for one-to-many and many-to-many association properties, default value is 16

When creating a JSqlClient, you can change the global configuration:

```java
JSqlClient sqlClient = JSqlClient
    .newBuilder()
    .setDefaultBatchSize(256)
    .setDefaultListBatchSize(32)
    ....
    build();
```

:::caution
Neither the object fetcher level `batch Size` nor the global level `batch Size` should exceed 1000, because `in(...)` in Oracle database allows up to 1000 values.
:::

### Specify the properties of the associated objects

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    BookFetcher.$.
                        .allScalarFields()
                        .allScalarFields()
                        .store(
                            BookStoreFetcher.$
                                // highlight-next-line
                                .allScalarFields()
                        )
                        .authors(
                            AuthorFetcher.$
                                // highlight-next-line
                                .allScalarFields()
                        )
                )
            );
    })
    .execute();
books.forEach(System.out::println);
```

In this query, we fetch both `Book.store` and `Book.authors`, and further fetch all scalar properties of `BookStore` and `Author`.

Finally, three SQL statements are generated.

1. Query the aggregate root objects: `Book`
    ```sql
    select 
        tb_1_.ID, 
        tb_1_.NAME, 
        tb_1_.EDITION, 
        tb_1_.PRICE, 
        tb_1_.STORE_ID 
    from BOOK as tb_1_ 
    where tb_1_.EDITION = ?
    ```

2. Query the associated `BookStore` objects
    ```sql
    select 
        tb_1_.ID, 
        tb_1_.NAME, 
        tb_1_.WEBSITE 
    from BOOK_STORE as tb_1_ 
    where 
        tb_1_.ID in (?, ?)
    /* 
     * There are 4 main objects, and there are
     * 2 foreign keys after deduplication 
     */
    ```

3. Query the associated `Author` objects
    ```sql
    select 
        
        /* batch-map key */
        tb_1_.BOOK_ID, 

        /* batch-map value */
        tb_1_.AUTHOR_ID, 
        tb_3_.FIRST_NAME, 
        tb_3_.LAST_NAME, 
        tb_3_.GENDER

    from BOOK_AUTHOR_MAPPING as tb_1_ 
    inner join AUTHOR as tb_3_ 
        on tb_1_.AUTHOR_ID = tb_3_.ID 
    where 
        tb_1_.BOOK_ID in (?, ?, ?, ?)
    ```

The printed result is as follows (the original output is compact, formatted here for readability):

```
{
    "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
    "name":"Learning GraphQL",
    "edition":3,
    "price":51.00,
    "store":{
        "id":"d38c10da-6be8-4924-b9b9-5e81899612a0",
        "name":"O'REILLY",
        "website":null
    },
    "authors":[
        {
            "id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94",
            "firstName":"Alex",
            "lastName":"Banks",
            "gender":"MALE"
        },{
            "id":"fd6bb6cf-336d-416c-8005-1ae11a6694b5",
            "firstName":"Eve",
            "lastName":"Procello",
            "gender":"MALE"
        }
    ]
},
...omit the 2nd object...,
...omit the 3rd object...,
...omit the 4th object...
```

:::info
From this example, we can see that the object fetcher is a tree, so there is no limit to the fetching depth of the associated objects.
:::

## Association-level pagination

For collection association property, `limit(limit, offset)` can be specified when fetch property, this is pagination at the association level

:::caution
Associative-level pagination and batch loading cannot coexist, therefore, associative-level pagination will inevitably lead to the `N+1` problem, please use this feature with caution!

If associative-level pagination is used, `batchSize` must be specified as 1, otherwise it will cause an exception. The purpose of this design is to make it clear to developers and code reviewers that the current code has `N+1` performance risks.
:::

```java
List<Book> books = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(
                book.fetch(
                    BookFetcher.$
                        .allScalarFields()
                        .authors(
                            AuthorFetcher.$.allScalarFields(),
                            // highlight-next-line
                            it -> it.batch(1).limit(10, 90)
                        )
                )
            );
    })
    .execute();
```

- Because the associated pagination cannot solve the `N + 1` problem, more SQL is generated
- Due to different pagination ways in different databases, in order to simplify the discussion, it is assumed that the dialect uses `H2Dialect`

1. Query the aggregate root objects: `Book`
    ```sql
    select 
        tb_1_.ID, 
        tb_1_.NAME, 
        tb_1_.EDITION, 
        tb_1_.PRICE 
    from BOOK as tb_1_ 
    where tb_1_.EDITION = ?
    ```

2. Pagination query for the assciated object referenced by the property `Book.authors` of 1st `Book` object
    ```sql
    select 
        tb_1_.AUTHOR_ID, 
        tb_3_.FIRST_NAME, 
        tb_3_.LAST_NAME, 
        tb_3_.GENDER 
    from BOOK_AUTHOR_MAPPING as tb_1_ 
    inner join AUTHOR as tb_3_ 
        on tb_1_.AUTHOR_ID = tb_3_.ID 
    where tb_1_.BOOK_ID = ?
    /* highlight-next-line */ 
    limit ? offset ?
    ```

3. Pagination query for the assciated object referenced by the property `Book.authors` of 2nd `Book` object

    Same as above, slightly

4. Pagination query for the assciated object referenced by the property `Book.authors` of 3rd `Book` object

    Same as above, slightly

5. Pagination query for the assciated object referenced by the property `Book.authors` of 4th `Book` object

    Same as above, slightly

## Filter

When fetching association properties, you can specify filter to specify filter conditions for association objects.

Here, for comparison, we let the query select two columns, both of type `Book`.

- Apply filter to the association `Book.autors` of the objects of first column.
- No filter to the association `Book.autors` of the objects of second column.

```java
List<Tuple2<Book, Book>> tuples = sqlClient
    .createQuery(BookTable.class, (q, book) -> {
        return q
            .where(book.edition().eq(3))
            .select(

                // First column
                book.fetch(
                    BookFetcher.$
                        .allScalarFields()
                        .authors(
                            AuthorFetcher.$
                                .allScalarFields(),

                            // Use filter here
                            // highlight-next-line
                            it -> it.filter(args -> {
                                args.where(
                                    args.getTable()
                                        .firstName().ilike("a")
                                );
                            })
                        )
                ),

                // Second column
                book.fetch(
                    BookFetcher.$
                        .allScalarFields()
                        .authors(
                            AuthorFetcher.$
                                    .allScalarFields()

                            // No filter here
                        )
                )
            );
    })
    .execute();
tuples.forEach(System.out::println);
```

Three SQL statements are generated.

1. Select the aggregate root, a tuple list, each Tuple consists of two `Book` objects.

    ```sql
    select

        /* For tuple._1 */
        tb_1_.ID, 
        tb_1_.NAME, 
        tb_1_.EDITION, 
        tb_1_.PRICE, 

        /* For tuple._2 */
        tb_1_.ID, 
        tb_1_.NAME, 
        tb_1_.EDITION, 
        tb_1_.PRICE 

    from BOOK as tb_1_ 
    where tb_1_.EDITION = ?
    ```

2. Query the association property ` Book.authors` for the 4 `Book` objects in column 1, using a filter.
    ```sql
    select 
        
        tb_1_.BOOK_ID, 

        tb_1_.AUTHOR_ID, 
        tb_3_.FIRST_NAME, 
        tb_3_.LAST_NAME, 
        tb_3_.GENDER 
    from BOOK_AUTHOR_MAPPING as tb_1_ 
    inner join AUTHOR as tb_3_ 
        on tb_1_.AUTHOR_ID = tb_3_.ID 
    where 
        tb_1_.BOOK_ID in (?, ?, ?, ?) 
    and 
        /* Use filter here */
        /* highlight-next-line */
        lower(tb_3_.FIRST_NAME) like ?
    ```

3. Query the association property ` Book.authors` for the 4 `Book` objects in column 2, without filter.
    ```sql
    select 
        
        tb_1_.BOOK_ID, 

        tb_1_.AUTHOR_ID, 
        tb_3_.FIRST_NAME, 
        tb_3_.LAST_NAME, 
        tb_3_.GENDER 
    from BOOK_AUTHOR_MAPPING as tb_1_ 
    inner join AUTHOR as tb_3_ 
        on tb_1_.AUTHOR_ID = tb_3_.ID 
    where 
        tb_1_.BOOK_ID in (?, ?, ?, ?) 
    /* No filter here */
    ```

The printed result is as follows (the original output is compact, formatted here for readability):
```
Tuple2{
    _1={
        "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
        "name":"Learning GraphQL",
        "edition":3,
        "price":51.00,
        "authors":[
            {
                "id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94",
                "firstName":"Alex",
                "lastName":"Banks",
                "gender":"MALE"
            }
        ]
    }, 
    _2={
        "id":"64873631-5d82-4bae-8eb8-72dd955bfc56",
        "name":"Learning GraphQL",
        "edition":3,
        "price":51.00,
        "authors":[
            {
                "id":"1e93da94-af84-44f4-82d1-d8a9fd52ea94",
                "firstName":"Alex",
                "lastName":"Banks",
                "gender":"MALE"
            },{
                "id":"fd6bb6cf-336d-416c-8005-1ae11a6694b5",
                "firstName":"Eve",
                "lastName":"Procello",
                "gender":"MALE"
            }
        ]
    }
}
```

:::note
Filters can not only filter associated objects, but also sort associated objects. The principle is similar, and this article does not demonstrate.
:::

:::caution

For an associated property that satisfies both of the following conditions
1. is many-to-one association
2. is non-null

Applying a filter results in an exception
:::

## Recursively query self-associated properties

There is a common scenario: self-association.

From a database perspective, a self-association means that a table's foreign key refers to itself; from an object model's perspective, a self-association means a tree.

The difficulty of self-association is that the depth of the object cannot be controlled. In theory, it can be infinitely deep. For this, jimmer-sql provides good support.

### Model and data preparation

Define entity interface

```java
@Entity
public interface TreeNode {

    @Id
    @Column(name = "NODE_ID", nullable = false)
    long id();

    String name();

    @ManyToOne
    TreeNode parent();

    @OneToMany(mappedBy = "parent")
    List<TreeNode> childNodes();
}
```

Prepare the database
```sql
create table tree_node(
    node_id bigint not null,
    name varchar(20) not null,
    parent_id bigint
);
alter table tree_node
    add constraint pk_tree_node
        primary key(node_id);
alter table tree_node
    add constraint uq_tree_node
        unique(parent_id, name);
alter table tree_node
    add constraint fk_tree_node__parent
        foreign key(parent_id)
            references tree_node(node_id);

insert into tree_node(
    node_id, name, parent_id
) values
    (1, 'Home', null),
        (2, 'Food', 1),
            (3, 'Drinks', 2),
                (4, 'Coca Cola', 3),
                (5, 'Fanta', 3),
            (6, 'Bread', 2),
                (7, 'Baguette', 6),
                (8, 'Ciabatta', 6),
        (9, 'Clothing', 1),
            (10, 'Woman', 9),
                (11, 'Casual wear', 10),
                    (12, 'Dress', 11),
                    (13, 'Miniskirt', 11),
                    (14, 'Jeans', 11),
                (15, 'Formal wear', 10),
                    (16, 'Suit', 15),
                    (17, 'Shirt', 15),
            (18, 'Man', 9),
                (19, 'Casual wear', 18),
                    (20, 'Jacket', 19),
                    (21, 'Jeans', 19),
                (22, 'Formal wear', 18),
                    (23, 'Suit', 22),
                    (24, 'Shirt', 22)
;
```

### Limited depth

Top down, fetch two layers

```java
List<TreeNode> treeNodes = sqlClient
    .createQuery(TreeNodeTable.class, (q, node) -> {
        q.where(node.parent().isNull());
        return q.select(
            node.fetch(
                TreeNodeFetcher.$
                    .name()
                    .childNodes(
                        TreeNodeFetcher.$.name(),
                        // highlight-next-line
                        it -> it.depth(2)
                    )
            )
        );
    })
    .execute();
treeNodes.forEach(System.out::println);
```

Three SQL statements are generated.

1. Get the root nodes (layer 0)

    ```sql
    select 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID is null
    ```

2. Fetch layer 1

    ```sql
    select 
        
        tb_1_.PARENT_ID,

        tb_1_.NODE_ID, 
        tb_1_.NAME

    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?)
    ```

3. Fetch layer 2

    ```sql
    select 
        tb_1_.PARENT_ID, 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?, ?)
    ```

The printed result is as follows (the original output is compact, formatted here for readability):
```json
{
    "id":1,
    "name":"Home",
    "childNodes":[
        {
            "id":9,
            "name":"Clothing",
            "childNodes":[
                {"id":18,"name":"Man"},
                {"id":10,"name":"Woman"}
            ]
        },{
            "id":2,
            "name":"Food",
            "childNodes":[
                {"id":6,"name":"Bread"},
                {"id":3,"name":"Drinks"}
            ]
        }
    ]
}
```
### Infinite recursion

Top-down, fetch infinite layers

```java
List<TreeNode> treeNodes = sqlClient
    .createQuery(TreeNodeTable.class, (q, node) -> {
        q.where(node.parent().isNull());
        return q.select(
            node.fetch(
                TreeNodeFetcher.$
                    .name()
                    .childNodes(
                        TreeNodeFetcher.$.name(),
                        // highlight-next-line
                        it -> it.recursive()
                    )
            )
        );
    })
    .execute();
treeNodes.forEach(System.out::println);
```

:::note
In the above code, `it.recursive()` can also be written as `it.depth(Integer.MAX_VALUE)`, the two are completely equivalent
:::

Six SQL statements are generated.

1. Get the root nodes (layer 0)

    ```sql
    select 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID is null
    ```

2. Fetcher layer 1

    ```sql
    select 
        
        tb_1_.PARENT_ID,

        tb_1_.NODE_ID, 
        tb_1_.NAME

    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?)
    ```

3. Fetcher layer 2

    ```sql
    select 
        tb_1_.PARENT_ID, 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?, ?)
    ```

4. Fetcher layer 3

    ```sql
    select 
        tb_1_.PARENT_ID, 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?, ?, ?, ?)
    ```

5. Fetcher layer 4

    ```sql
    select 
        tb_1_.PARENT_ID, 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?, ?, ?, ?, ?, ?, ?, ?)
    ```

6. Fetcher layer 5

    ```sql
    select 
        tb_1_.PARENT_ID, 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?, ?, ?, ?, ?, ?, ?, ?, ?)
    ```
The printed result is as follows (the original output is compact, formatted here for readability):

```json
{
    "id":1,"name":
    "Home","childNodes":[
        {
            "id":9,
            "name":"Clothing",
            "childNodes":[
                {
                    "id":18,
                    "name":"Man",
                    "childNodes":[
                        {
                            "id":19,
                            "name":"Casual wear",
                            "childNodes":[
                                {"id":20,"name":"Jacket","childNodes":[]},
                                {"id":21,"name":"Jeans","childNodes":[]}
                            ]
                        },{
                            "id":22,
                            "name":"Formal wear",
                            "childNodes":[
                                {"id":24,"name":"Shirt","childNodes":[]},
                                {"id":23,"name":"Suit","childNodes":[]}
                            ]
                        }
                    ]
                },{
                    "id":10,
                    "name":"Woman",
                    "childNodes":[
                        {
                            "id":11,
                            "name":"Casual wear",
                            "childNodes":[
                                {"id":12,"name":"Dress","childNodes":[]},
                                {"id":14,"name":"Jeans","childNodes":[]},
                                {"id":13,"name":"Miniskirt","childNodes":[]}
                            ]
                        },{
                            "id":15,
                            "name":"Formal wear",
                            "childNodes":[
                                {"id":17,"name":"Shirt","childNodes":[]},
                                {"id":16,"name":"Suit","childNodes":[]}
                            ]
                        }
                    ]
                }
            ]
        },{
            "id":2,
            "name":"Food",
            "childNodes":[
                {
                    "id":6,
                    "name":"Bread",
                    "childNodes":[
                        {"id":7,"name":"Baguette","childNodes":[]},
                        {"id":8,"name":"Ciabatta","childNodes":[]}
                    ]
                },{
                    "id":3,
                    "name":"Drinks",
                    "childNodes":[
                        {"id":4,"name":"Coca Cola","childNodes":[]},
                        {"id":5,"name":"Fanta","childNodes":[]}
                    ]
                }
            ]
        }
    ]
}
```

### Developer controls whether each node is recursive

From top to bottom, fetch infinite layers. However, for nodes with the name "Clothing", skip recursion

```java
List<TreeNode> treeNodes = sqlClient
    .createQuery(TreeNodeTable.class, (q, node) -> {
        q.where(node.parent().isNull());
        return q.select(
            node.fetch(
                TreeNodeFetcher.$
                    .name()
                    .childNodes(
                        TreeNodeFetcher.$.name(),
                        // highlight-next-line
                        it -> it.recursive(args ->
                            !args.getEntity().name().equals("Clothing")
                        )
                    )
            )
        );
    })
    .execute();
treeNodes.forEach(System.out::println);
```

In the above code, the parameter of `it.recursive(args)` is a lambda expression, and its parameter `args` is an object that provides two methods

1. `args.getEntity()`: The current node object.
2. `args.getDepth()`: current node depth. It is 0 for nodes obtained directly through the primary query, and it increases as the recursion goes deeper.
3. User-determined return value: a boolean variable that determines whether to recursively fetch the current node.

The meaning of the above code is that, except for the `Clothing` node, the rest of the sections will be recursively fetched.

Five SQL statements are generated.

1. Get the root nodes (layer 0)

    ```sql
    select 
        tb_1_.NODE_ID, 
        tb_1_.NAME 
    from TREE_NODE as tb_1_ 
    where tb_1_.PARENT_ID is null
    ```

2. Fetch layer 1
    ```sql
    select 

        tb_1_.PARENT_ID, 
        
        tb_1_.NODE_ID, 
        tb_1_.NAME 

    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?)
    ```

3. Fetch layer 2
    ```sql
    select 

        tb_1_.PARENT_ID, 
        
        tb_1_.NODE_ID, 
        tb_1_.NAME 

    from TREE_NODE as tb_1_ 
    where 
        /* 
         * Home node has two child nodes:
         *      "Food" and "Clothing",
         * 
         * However, "Clothing is excluded by user,
         * so `in(?)` is used here, not `in(?, ?)`
         */
        tb_1_.PARENT_ID in (?)
    ```
4. Fetch layer 3
    ```sql
    select 

        tb_1_.PARENT_ID, 
        
        tb_1_.NODE_ID, 
        tb_1_.NAME 

    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?, ?)
    ```
5. Fetch layer 4
    ```sql
    select 

        tb_1_.PARENT_ID, 
        
        tb_1_.NODE_ID, 
        tb_1_.NAME 

    from TREE_NODE as tb_1_ 
    where 
        tb_1_.PARENT_ID in (?, ?, ?, ?)
    ```

The printed result is as follows (the original output is compact, formatted here for readability):

```json
{
    "id":1,
    "name":"Home",
    "childNodes":[
        // highlight-next-line
        {"id":9,"name":"Clothing"},
        {
            "id":2,
            "name":"Food",
            "childNodes":[
                {
                    "id":6,
                    "name":"Bread",
                    "childNodes":[
                        {"id":7,"name":"Baguette","childNodes":[]},
                        {"id":8,"name":"Ciabatta","childNodes":[]}
                    ]
                },{
                    "id":3,
                    "name":"Drinks",
                    "childNodes":[
                        {"id":4,"name":"Coca Cola","childNodes":[]},
                        {"id":5,"name":"Fanta","childNodes":[]}
                    ]
                }
            ]
        }
    ]
}
```

## Compare with GraphQL

GraphQL is a specification, and there are no restrictions about how to  implement it, they are not comparable.

However, if only discuss using of RDBMS to implement GraphQL, they are not comparable. The comparison is as follows

||Object Fetcher|GraphQL|
|--|--|--|
|Utilize short-lived caches in the query life cycle to prevent repeated data loading|Supported|Supported|
|Solve `N + 1` problem with batch loading|Supported|Supported|
|Parallel execution of different batch loading tasks|Not supported|Supported|
|Add a configuration or parameter to association to filter the associated objects|Supported|Supported|
|Recursively query self-associated properties|Supported|Not supported|

They have different design purposes, different emphases, and different usage scenarios.

1. Object fetcher: All data fetching within a query are executed based on the same database connection. Even if the current database transaction has not been committed, the latest data can still be fetched based on the current database connection.

2. GraphQL: Only fetch the submitted data, but can use multiple different database connections, so that different batch loading task can be executed concurrently.

:::info
In fact, aside from the object fetcher discussed in this article, jimmer-sql has great support for GraphQL as well. In order to accelerate the development efficiency of Spring GraphQL, special API is provided. Please see [Support for Spring GraphQL](../spring-graphql) to learn more.
:::