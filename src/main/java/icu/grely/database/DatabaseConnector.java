package icu.grely.database;

import arc.util.Log;
import icu.grely.ranks.UserSave;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import java.sql.*;
import java.util.*;

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

    public static Optional<UserSave> getUserSave(String id) {
        return executeQueryAsync("SELECT * FROM users WHERE id = ?",
                stmt->stmt.setString(1, id),
                UserSave::ResultSetToUserSave);
    }
    public static Optional<UserSave> createOrGetUser(String id) {
        return executeQueryAsync(
                "INSERT INTO users (id, exp) \n" +
                        "VALUES (?, 0)\n" +
                        "ON CONFLICT (id) \n" +
                        "DO UPDATE SET exp = users.exp \n" +
                        "RETURNING *;",
                stmt -> stmt.setString(1, id),
                UserSave::ResultSetToUserSave
        );
    }
    public static Optional<UserSave> createOrUpdateUser(String id, long exp) {
        return executeQueryAsync(
                "INSERT INTO users (id, exp) \n" +
                        "VALUES (?, ?) \n" +
                        "ON CONFLICT (id) \n" +
                        "DO UPDATE SET exp = EXCLUDED.exp \n" +
                        "RETURNING *;",
                stmt -> {
                    stmt.setString(1, id);
                    stmt.setLong(2, exp);
                },
                UserSave::ResultSetToUserSave
        );
    }
    public static List<UserSave> getLeaderboard() {
        return executeQueryList("SELECT * FROM users ORDER BY -id LIMIT 10",
                stmt->{},
                UserSave::ResultSetToUserSave
        );
    }
    public static void loadSQLCommands() {
        registerCommand("sql", "Execute raw SQL", "[query...]", owner.getId().asLong(), (e, args) -> {
            if (args.length == 0) {
                sendReply(e.getMessage(), "А че мне в бд посылать то?");
                return;
            }
            String query = String.join(" ", args);
            if (query.trim().toLowerCase().startsWith("select")) {
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query);
                     ResultSet rs = pstmt.executeQuery()) {

                    List<Map<String, String>> results = new ArrayList<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, String> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getString(i));
                        }
                        results.add(row);
                    }

                    if (results.isEmpty()) {
                        sendReply(e.getMessage(), "БД ничего не сказала...");
                    } else {
                        StringBuilder response = new StringBuilder();
                        for (Map<String, String> row : results) {
                            response.append("{\n");
                            for (Map.Entry<String, String> entry : row.entrySet()) {
                                response.append("  \"").append(entry.getKey()).append("\": \"").append(entry.getValue()).append("\",\n");
                            }
                            response.setLength(response.length() - 2);
                            response.append("\n},\n");
                        }
                        sendReply(e.getMessage(), new StringBuilder().append("```"+response.toString().replace("`", "\\`")+"```"));
                    }

                } catch (SQLException ex) {
                    sendReply(e.getMessage(), "Аа щас взорвется бд: " + ex.getMessage());
                }
            } else {
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        sendReply(e.getMessage(), "Молодец, бд не взорвалась, но и не сказала ничего, молчит гадина.");
                    } else {
                        sendReply(e.getMessage(), "БД ничего не сказала...");
                    }

                } catch (SQLException ex) {
                    sendReply(e.getMessage(), ex.getMessage());
                }
            }
        }).setVisible(false);
    }
}
