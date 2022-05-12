package org.babyfish.jimmer.sql.example;

import org.babyfish.jimmer.sql.SqlClient;
import org.babyfish.jimmer.sql.dialect.DefaultDialect;
import org.babyfish.jimmer.sql.example.model.Gender;
import org.babyfish.jimmer.sql.runtime.DefaultExecutor;
import org.babyfish.jimmer.sql.runtime.Executor;
import org.babyfish.jimmer.sql.runtime.ScalarProvider;
import org.babyfish.jimmer.sql.runtime.SqlFunction;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

public class AppContext {

    private AppContext() {}

    public static final SqlClient SQL_CLIENT =
            SqlClient.newBuilder()
                    .setDialect(DefaultDialect.INSTANCE)
                    .setExecutor(
                            new Executor() {

                                @Override
                                public <R> R execute(
                                        Connection con,
                                        String sql,
                                        List<Object> variables,
                                        SqlFunction<PreparedStatement, R> block
                                ) {
                                    System.err.println("jdbc sql: " + sql);
                                    System.err.println("jdbc parameters: " + variables);
                                    return DefaultExecutor.INSTANCE.execute(con, sql, variables, block);
                                }
                            }
                    )
                    .addScalarProvider(
                            ScalarProvider.enumProviderByString(Gender.class, it -> {
                                it.map(Gender.MALE, "M");
                                it.map(Gender.FEMALE, "F");
                            })
                    )
                    .build();

    public static <R> R jdbc(Function<Connection, R> block) {
        try (Connection con = DriverManager.getConnection("jdbc:h2:~/example")) {
            return block.apply(con);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to execute jdbc action", ex);
        }
    }

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException ex) {
           throw new RuntimeException("Cannot load jdbc driver for H2 database", ex);
        }

        InputStream stream = AppContext.class
                .getClassLoader()
                .getResourceAsStream("database.sql");
        if (stream == null) {
            throw new RuntimeException("Cannot load database.sql");
        }

        try (Reader reader = new InputStreamReader(stream)) {
            StringBuilder builder = new StringBuilder();
            char[] buf = new char[1024];
            while (true) {
                int len = reader.read(buf);
                if (len == -1) {
                    break;
                }
                builder.append(buf, 0, len);
            }
            jdbc(con -> {
                try {
                    return con.createStatement().executeUpdate(builder.toString());
                } catch (SQLException ex) {
                    throw new RuntimeException("Cannot init database", ex);
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException("Cannot read database initialization sql", ex);
        }
    }
}