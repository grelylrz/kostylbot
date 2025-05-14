package icu.grely.database;

import arc.util.Log;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static icu.grely.Vars.*;
import static icu.grely.bot.SendUtils.sendReply;
import static icu.grely.bot.commands.CommandsHandler.registerCommand;

public class DatabaseConnector {
    private static final DataSource dataSource = createDataSource();
    private static DataSource createDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(JDBC_URL);
        ds.setUser(DB_USER);
        ds.setPassword(DB_PASSWORD);
        return ds;
    }
    private static <T> Optional<T> executeQueryAsync(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.apply(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            Log.err("Database query failed: " + sql, e);
            return Optional.empty();
        }
    }
    private static boolean executeUpdate(String sql, ThrowingConsumer<PreparedStatement> parameterSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);
            int updated = pstmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            Log.err("Database update failed: " + sql, e);
            return false;
        }
    }
    private static <T> List<T> executeQueryList(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            Log.err("Database query failed: " + sql, e);
        }
        return results;
    }
    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }

    @FunctionalInterface
    private interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    public static void loadSQLCommands() {
        registerCommand("sql", "Execute raw SQL", "[query...]", owner.getId().asLong(), (e, args) -> {
            if (args.length == 0) {
                sendReply(e.getMessage(), "А че мне в бд посылать то?");
                return;
            }
            String query = String.join(" ", args);
            if (query.trim().toLowerCase().startsWith("select")) {
                List<String> results = DatabaseConnector.executeQueryList(
                        query,
                        pstmt -> {},
                        rs -> {
                            try {
                                return rs.getString(1);
                            } catch (SQLException ex) {
                                Log.err("Error fetching result", ex);
                                return "";
                            }
                        }
                );

                if (results.isEmpty()) {
                    sendReply(e.getMessage(), "No results found.");
                } else {
                    sendReply(e.getMessage(), "Query Results:\n" + String.join("\n", results));
                }
            } else {
                boolean success = DatabaseConnector.executeUpdate(query, pstmt -> {});
                if (success) {
                    sendReply(e.getMessage(), "БД ничего не сказала.");
                } else {
                    sendReply(e.getMessage(), "БД тарахтит, ты что то не так сделал, сейчас взорвется еще!");
                }
            }
        }).setVisible(false);
    }
}
