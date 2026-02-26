package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.SQLiteDataSource;
import java.util.ArrayList;
import java.util.List;
import com.example.model.Task;

public class TaskDao {
    /** Data access object for tasks backed by an embedded SQLite database. */
    // Keep driver loading once per class
    static {
        try {
            try {
                Class.forName("org.sqlite.JDBC");
                System.out.println("[TaskDao] org.sqlite.JDBC driver loaded");
            } catch (ClassNotFoundException cnfe) {
                System.err.println("[TaskDao] Class.forName failed for org.sqlite.JDBC: " + cnfe.getMessage());
            }
            try {
                org.sqlite.JDBC driver = new org.sqlite.JDBC();
                java.sql.DriverManager.registerDriver(driver);
                System.out.println("[TaskDao] org.sqlite.JDBC driver instance registered");
            } catch (Exception t) {
                System.err.println("[TaskDao] Failed to register org.sqlite.JDBC driver instance: " + t.getMessage());
                t.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("[TaskDao] Failed to load org.sqlite.JDBC driver");
            e.printStackTrace();
        }
    }

    private final SQLiteDataSource ds;
    private final String dbFilePath;

    /**
     * Default constructor kept for backward compatibility. Uses the project-local path.
     */
    public TaskDao() {
        this("/Users/apple/my-webapp-project/todo.db");
    }

    /**
     * Create a TaskDao that uses the given file path for the SQLite database.
     * The caller should supply an absolute path (usually from servlet context).
     */
    public TaskDao(String dbFilePath) {
        this.dbFilePath = dbFilePath;
        String url = "jdbc:sqlite:" + dbFilePath;
        ds = new SQLiteDataSource();
        ds.setUrl(url);
        System.out.println("[TaskDao] SQLiteDataSource configured with URL: " + url);

        // Ensure the table exists
        try (Connection conn = ds.getConnection()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks (" +
                                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                    "title TEXT NOT NULL, " +
                                    "completed BOOLEAN NOT NULL DEFAULT 0)";
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (SQLException e) {
            System.err.println("[TaskDao] Failed to create or open DB at: " + dbFilePath);
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all tasks from the database.
     *
     * @return a list of all Task objects in the database
     */
    public List<Task> getAllTasks() {
        System.out.println("[TaskDao] getAllTasks called. DB path: " + dbFilePath);
        List<Task> tasks = new ArrayList<>();
        try (Connection conn = ds.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id, title, completed FROM tasks")) {
            while (rs.next()) {
                Task t = new Task(rs.getInt("id"), rs.getString("title"), rs.getBoolean("completed"));
                System.out.println("[TaskDao] fetched: id=" + t.getId() + ", title=" + t.getTitle() + ", completed=" + t.isCompleted());
                tasks.add(t);
            }
        } catch (SQLException e) {
            System.err.println("[TaskDao] getAllTasks failed for DB: " + dbFilePath);
            e.printStackTrace();
        }
        System.out.println("[TaskDao] getAllTasks returning " + tasks.size() + " tasks.");
        return tasks;
    }

    public Task addTask(String title) {
        System.out.println("[TaskDao] addTask called with title: " + title + ". DB path: " + dbFilePath);
        String sql = "INSERT INTO tasks (title) VALUES (?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, title);
            int affected = pstmt.executeUpdate();
            System.out.println("[TaskDao] Rows inserted: " + affected);
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    System.out.println("[TaskDao] Inserted task id: " + id);
                    return new Task(id, title, false);
                }
            }
        } catch (SQLException e) {
            System.err.println("[TaskDao] addTask failed for DB: " + dbFilePath + ", title=" + title);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Update only the completed status for backward compatibility.
     */
    public void updateTask(int id, boolean completed) {
        updateTask(id, null, completed);
    }

    /**
     * Update a task's title and completion status.
     */
    public void updateTask(int id, String title, boolean completed) {
        String sql;
        boolean updateTitle = title != null && !title.isEmpty();
        if (updateTitle) {
            sql = "UPDATE tasks SET title = ?, completed = ? WHERE id = ?";
        } else {
            sql = "UPDATE tasks SET completed = ? WHERE id = ?";
        }
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (updateTitle) {
                pstmt.setString(1, title);
                pstmt.setBoolean(2, completed);
                pstmt.setInt(3, id);
            } else {
                pstmt.setBoolean(1, completed);
                pstmt.setInt(2, id);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TaskDao] updateTask failed for DB: " + dbFilePath + ", id=" + id + ", title=" + title);
            e.printStackTrace();
        }
    }

    public void deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[TaskDao] deleteTask failed for DB: " + dbFilePath + ", id=" + id);
            e.printStackTrace();
        }
    }
}
