package org.babyfish.jimmer.sql.ast.impl.mutation.save;

import org.babyfish.jimmer.sql.ast.tuple.Tuple2;
import org.babyfish.jimmer.sql.model.TreeNode;
import org.babyfish.jimmer.sql.model.TreeNodeProps;
import org.babyfish.jimmer.sql.model.flat.ProvinceProps;
import org.junit.jupiter.api.Test;

public class MixChildOperationTest extends AbstractChildOperatorTest {

    @Test
    public void testDisconnectTreeExcept() {
        connectAndExpect(
                con -> {
                    return operator(
                            getSqlClient(it -> it.setMaxMutationSubQueryDepth(4)),
                            con,
                            ProvinceProps.COUNTRY.unwrap()
                    ).disconnectExcept(
                            IdPairs.of(
                                    new Tuple2<>("China", 2L),
                                    new Tuple2<>("USA", 5L)
                            )
                    );
                },
                ctx -> {
                    ctx.statement(it -> {
                        it.sql(
                                "update COMPANY set STREET_ID = null " +
                                        "where STREET_ID in (" +
                                        "--->select ID from STREET where CITY_ID in (" +
                                        "--->--->select ID from CITY where PROVINCE_ID in (" +
                                        "--->--->--->select ID " +
                                        "--->--->--->from PROVINCE " +
                                        "--->--->--->where COUNTRY_ID in (?, ?) and (COUNTRY_ID, ID) not in ((?, ?), (?, ?))" +
                                        "--->--->)" +
                                        "--->)" +
                                        ")"
                        );
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from STREET where CITY_ID in (" +
                                        "--->select ID from CITY where PROVINCE_ID in (" +
                                        "--->--->select ID " +
                                        "--->--->from PROVINCE " +
                                        "--->--->where COUNTRY_ID in (?, ?) and (COUNTRY_ID, ID) not in ((?, ?), (?, ?))" +
                                        "--->)" +
                                        ")"
                        );
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from CITY where PROVINCE_ID in (" +
                                        "--->select ID " +
                                        "--->from PROVINCE " +
                                        "--->where COUNTRY_ID in (?, ?) and (COUNTRY_ID, ID) not in ((?, ?), (?, ?))" +
                                        ")"
                        );
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from PROVINCE " +
                                        "where COUNTRY_ID in (?, ?) and (COUNTRY_ID, ID) not in ((?, ?), (?, ?))"
                        );
                    });
                }
        );
    }

    @Test
    public void testDisconnectTreeWithShallowSubQueryDepthExcept() {
        connectAndExpect(
                con -> {
                    return operator(
                            getSqlClient(it -> it.setMaxMutationSubQueryDepth(2)),
                            con,
                            ProvinceProps.COUNTRY.unwrap()
                    ).disconnectExcept(
                            IdPairs.of(
                                    new Tuple2<>("China", 2L),
                                    new Tuple2<>("USA", 5L)
                            )
                    );
                },
                ctx -> {
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.ID " +
                                        "from STREET tb_1_ " +
                                        "where tb_1_.CITY_ID in (" +
                                        "--->select tb_2_.ID " +
                                        "--->from CITY tb_2_ " +
                                        "--->where tb_2_.PROVINCE_ID in (" +
                                        "--->--->select tb_3_.ID " +
                                        "--->--->from PROVINCE tb_3_ " +
                                        "--->--->where " +
                                        "--->--->--->tb_3_.COUNTRY_ID in (?, ?) " +
                                        "--->--->and " +
                                        "--->--->--->(tb_3_.COUNTRY_ID, tb_3_.ID) not in ((?, ?), (?, ?))" +
                                        "--->)" +
                                        ")"
                        );
                        it.variables("China", "USA", "China", 2L, "USA", 5L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "update COMPANY " +
                                        "set STREET_ID = null " +
                                        "where STREET_ID in (" +
                                        "--->select ID " +
                                        "--->from STREET " +
                                        "--->where ID in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                                        ")"
                        );
                        it.variables(
                                1L, 2L, 3L, 4L, 9L, 10L, 11L, 12L,
                                13L, 14L, 15L, 16L, 21L, 22L, 23L, 24L
                        );
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from STREET " +
                                        "where ID in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        );
                        it.variables(
                                1L, 2L, 3L, 4L, 9L, 10L, 11L, 12L,
                                13L, 14L, 15L, 16L, 21L, 22L, 23L, 24L
                        );
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from CITY " +
                                        "where PROVINCE_ID in (" +
                                        "--->select ID " +
                                        "--->from PROVINCE " +
                                        "--->where " +
                                        "--->--->COUNTRY_ID in (?, ?) " +
                                        "--->and " +
                                        "--->--->(COUNTRY_ID, ID) not in ((?, ?), (?, ?))" +
                                        ")"
                        );
                        it.variables("China", "USA", "China", 2L, "USA", 5L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from PROVINCE " +
                                        "where " +
                                        "--->COUNTRY_ID in (?, ?) " +
                                        "and " +
                                        "--->(COUNTRY_ID, ID) not in ((?, ?), (?, ?)" +
                                        ")"
                        );
                        it.variables("China", "USA", "China", 2L, "USA", 5L);
                    });
                }
        );
    }

    @Test
    public void testDisconnectTreeWithShallowestSubQueryDepthExcept() {
        connectAndExpect(
                con -> {
                    return operator(
                            getSqlClient(it -> it.setMaxMutationSubQueryDepth(0)),
                            con,
                            ProvinceProps.COUNTRY.unwrap()
                    ).disconnectExcept(
                            IdPairs.of(
                                    new Tuple2<>("China", 2L),
                                    new Tuple2<>("USA", 5L)
                            )
                    );
                },
                ctx -> {
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.ID " +
                                        "from CITY tb_1_ " +
                                        "where tb_1_.PROVINCE_ID in (" +
                                        "--->select tb_2_.ID " +
                                        "--->from PROVINCE tb_2_ " +
                                        "--->where " +
                                        "--->--->tb_2_.COUNTRY_ID in (?, ?) " +
                                        "--->and " +
                                        "--->--->(tb_2_.COUNTRY_ID, tb_2_.ID) not in ((?, ?), (?, ?))" +
                                        ")"
                        );
                        it.variables("China", "USA", "China", 2L, "USA", 5L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.ID " +
                                        "from STREET tb_1_ " +
                                        "where tb_1_.CITY_ID in (" +
                                        "--->select tb_2_.ID " +
                                        "--->from CITY tb_2_ " +
                                        "--->where tb_2_.ID in (?, ?, ?, ?, ?, ?, ?, ?)" +
                                        ")"
                        );
                        it.variables(1L, 2L, 5L, 6L, 7L, 8L, 11L, 12L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.ID " +
                                        "from COMPANY tb_1_ " +
                                        "where tb_1_.STREET_ID in (" +
                                        "--->select tb_2_.ID " +
                                        "--->from STREET tb_2_ " +
                                        "--->where tb_2_.ID in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                                        ")"
                        );
                        it.variables(
                                1L, 2L, 3L, 4L, 9L, 10L, 11L, 12L,
                                13L, 14L, 15L, 16L, 21L, 22L, 23L, 24L
                        );
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from STREET " +
                                        "where ID in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        );
                        it.variables(
                                1L, 2L, 3L, 4L, 9L, 10L, 11L, 12L,
                                13L, 14L, 15L, 16L, 21L, 22L, 23L, 24L
                        );
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from CITY " +
                                        "where ID in (?, ?, ?, ?, ?, ?, ?, ?)"
                        );
                        it.variables(1L, 2L, 5L, 6L, 7L, 8L, 11L, 12L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from PROVINCE " +
                                        "where " +
                                        "--->COUNTRY_ID in (?, ?) " +
                                        "and " +
                                        "--->(COUNTRY_ID, ID) not in ((?, ?), (?, ?)" +
                                        ")"
                        );
                        it.variables("China", "USA", "China", 2L, "USA", 5L);
                    });
                }
        );
    }

    @Test
    public void testDisconnectRecursiveTreeExcept() {
        connectAndExpect(
                con -> {
                    return operator(
                            getSqlClient(it -> it.setMaxMutationSubQueryDepth(4)),
                            con,
                            TreeNodeProps.PARENT.unwrap()
                    ).disconnectExcept(
                            IdPairs.of(
                                    new Tuple2<>(1L, 2L)
                            )
                    );
                },
                ctx -> {
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.NODE_ID " +
                                        "from TREE_NODE tb_1_ " +
                                        "where tb_1_.PARENT_ID in (" +
                                        "--->select tb_2_.NODE_ID " +
                                        "--->from TREE_NODE tb_2_ " +
                                        "--->where tb_2_.PARENT_ID in (" +
                                        "--->--->select tb_3_.NODE_ID " +
                                        "--->--->from TREE_NODE tb_3_ " +
                                        "--->--->where tb_3_.PARENT_ID in (" +
                                        "--->--->--->select tb_4_.NODE_ID " +
                                        "--->--->--->from TREE_NODE tb_4_ " +
                                        "--->--->--->where tb_4_.PARENT_ID in (" +
                                        "--->--->--->--->select tb_5_.NODE_ID " +
                                        "--->--->--->--->from TREE_NODE tb_5_ " +
                                        "--->--->--->--->where tb_5_.PARENT_ID = ? and tb_5_.NODE_ID <> ?)" +
                                        "--->--->)" +
                                        "--->)" +
                                        ")"
                        );
                        it.variables(1L, 2L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID in (" +
                                        "--->select NODE_ID " +
                                        "--->from TREE_NODE " +
                                        "--->where PARENT_ID in (" +
                                        "--->--->select NODE_ID " +
                                        "--->--->from TREE_NODE " +
                                        "--->--->where PARENT_ID in (" +
                                        "--->--->--->select NODE_ID " +
                                        "--->--->--->from TREE_NODE " +
                                        "--->--->--->where PARENT_ID = ? and NODE_ID <> ?" +
                                        "--->--->)" +
                                        "--->)" +
                                        ")"
                        );
                        it.variables(1L, 2L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID in (" +
                                        "--->select NODE_ID " +
                                        "--->from TREE_NODE " +
                                        "--->where PARENT_ID in (" +
                                        "--->--->select NODE_ID " +
                                        "--->--->from TREE_NODE " +
                                        "--->--->where PARENT_ID = ? and NODE_ID <> ?" +
                                        "--->)" +
                                        ")"
                        );
                        it.variables(1L, 2L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID in (" +
                                        "--->select NODE_ID " +
                                        "--->from TREE_NODE " +
                                        "--->where PARENT_ID = ? and NODE_ID <> ?" +
                                        ")"
                        );
                        it.variables(1L, 2L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID = ? and NODE_ID <> ?"
                        );
                        it.variables(1L, 2L);
                    });
                }
        );
    }

    @Test
    public void testDisconnectRecursiveTreeWithSallowSubQueryExcept() {
        connectAndExpect(
                con -> {
                    return operator(
                            getSqlClient(it -> it.setMaxMutationSubQueryDepth(2)),
                            con,
                            TreeNodeProps.PARENT.unwrap()
                    ).disconnectExcept(
                            IdPairs.of(
                                    new Tuple2<>(1L, 2L)
                            )
                    );
                },
                ctx -> {
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.NODE_ID " +
                                        "from TREE_NODE tb_1_ " +
                                        "where tb_1_.PARENT_ID in (" +
                                        "--->select tb_2_.NODE_ID " +
                                        "--->from TREE_NODE tb_2_ " +
                                        "--->where tb_2_.PARENT_ID in (" +
                                        "--->--->select tb_3_.NODE_ID " +
                                        "--->--->from TREE_NODE tb_3_ " +
                                        "--->--->where tb_3_.PARENT_ID = ? and tb_3_.NODE_ID <> ?" +
                                        "--->)" +
                                        ")"
                        );
                        it.variables(1L, 2L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.NODE_ID " +
                                        "from TREE_NODE tb_1_ " +
                                        "where tb_1_.PARENT_ID in (" +
                                        "--->select tb_2_.NODE_ID " +
                                        "--->from TREE_NODE tb_2_ " +
                                        "--->where tb_2_.PARENT_ID in (" +
                                        "--->--->select tb_3_.NODE_ID " +
                                        "--->--->from TREE_NODE tb_3_ " +
                                        "--->--->where tb_3_.NODE_ID in (?, ?, ?, ?)" +
                                        "--->)" +
                                        ")"
                        );
                        it.variables(11L, 15L, 19L, 22L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID in (" +
                                        "--->select NODE_ID " +
                                        "--->from TREE_NODE " +
                                        "--->where NODE_ID in (?, ?, ?, ?)" +
                                        ")"
                        );
                        it.variables(11L, 15L, 19L, 22L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where NODE_ID in (?, ?, ?, ?)"
                        );
                        it.variables(11L, 15L, 19L, 22L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID in (" +
                                        "--->select NODE_ID " +
                                        "--->from TREE_NODE " +
                                        "--->where PARENT_ID = ? and NODE_ID <> ?" +
                                        ")"
                        );
                        it.variables(1L, 2L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID = ? and NODE_ID <> ?"
                        );
                        it.variables(1L, 2L);
                    });
                }
        );
    }

    @Test
    public void testDisconnectRecursiveTreeWithSallowestSubQueryExcept() {
        connectAndExpect(
                con -> {
                    return operator(
                            getSqlClient(it -> it.setMaxMutationSubQueryDepth(0)),
                            con,
                            TreeNodeProps.PARENT.unwrap()
                    ).disconnectExcept(
                            IdPairs.of(
                                    new Tuple2<>(1L, 2L)
                            )
                    );
                },
                ctx -> {
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.NODE_ID " +
                                        "from TREE_NODE tb_1_ " +
                                        "where tb_1_.PARENT_ID in (" +
                                        "--->select tb_2_.NODE_ID " +
                                        "--->from TREE_NODE tb_2_ " +
                                        "--->where tb_2_.PARENT_ID = ? and tb_2_.NODE_ID <> ?" +
                                        ")"
                        );
                        it.variables(1L, 2L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.NODE_ID " +
                                        "from TREE_NODE tb_1_ " +
                                        "where tb_1_.PARENT_ID in (" +
                                        "--->select tb_2_.NODE_ID " +
                                        "--->from TREE_NODE tb_2_ " +
                                        "--->where tb_2_.NODE_ID in (?, ?)" +
                                        ")"
                        );
                        it.variables(10L, 18L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.NODE_ID " +
                                        "from TREE_NODE tb_1_ " +
                                        "where tb_1_.PARENT_ID in (" +
                                        "--->select tb_2_.NODE_ID " +
                                        "--->from TREE_NODE tb_2_ " +
                                        "--->where tb_2_.NODE_ID in (?, ?, ?, ?)" +
                                        ")"
                        );
                        it.variables(11L, 15L, 19L, 22L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "select tb_1_.NODE_ID " +
                                        "from TREE_NODE tb_1_ " +
                                        "where tb_1_.PARENT_ID in (" +
                                        "--->select tb_2_.NODE_ID " +
                                        "--->from TREE_NODE tb_2_ " +
                                        "--->where tb_2_.NODE_ID in (?, ?, ?, ?, ?, ?, ?, ?, ?)" +
                                        ")"
                        );
                        it.variables(12L, 13L, 14L, 16L, 17L, 20L, 21L, 23L, 24L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where NODE_ID in (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        );
                        it.variables(12L, 13L, 14L, 16L, 17L, 20L, 21L, 23L, 24L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where NODE_ID in (?, ?, ?, ?)"
                        );
                        it.variables(11L, 15L, 19L, 22L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where NODE_ID in (?, ?)"
                        );
                        it.variables(10L, 18L);
                    });
                    ctx.statement(it -> {
                        it.sql(
                                "delete from TREE_NODE " +
                                        "where PARENT_ID = ? and NODE_ID <> ?"
                        );
                        it.variables(1L, 2L);
                    });
                }
        );
    }
}
