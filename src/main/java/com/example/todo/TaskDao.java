package com.example.todo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {
    private static final String URL = "jdbc:sqlite:todo.db";

    public TaskDao() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, completed INTEGER NOT NULL DEFAULT 0, created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public List<Task> list() {
        List<Task> list = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id, title, completed FROM tasks ORDER BY id DESC"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Task(rs.getInt("id"), rs.getString("title"), rs.getInt("completed") != 0));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Task add(String title) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO tasks(title, completed) VALUES(?,0)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Task(keys.getInt(1), title, false);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean updateCompleted(int id, boolean completed) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET completed = ? WHERE id = ?")) {
            ps.setInt(1, completed ? 1 : 0);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(int id) {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
