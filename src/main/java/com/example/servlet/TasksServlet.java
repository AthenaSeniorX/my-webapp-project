package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.example.dao.TaskDao;
import com.example.model.Task;

/** Servlet exposing basic CRUD API for Tasks. */
@WebServlet("/api/tasks/*")
public class TasksServlet extends HttpServlet {
    private transient TaskDao taskDao;
    private transient final Gson gson = new Gson();
    private static final String APPLICATION_JSON = "application/json";

    @Override
    public void init() throws ServletException {
        super.init();
        // Prefer storing DB inside WEB-INF so the container user can write to it
        String dbRelPath = "/WEB-INF/todo.db";
        String dbFilePath = getServletContext().getRealPath(dbRelPath);
        if (dbFilePath == null) {
            // getRealPath may return null in some container setups (exploded war not used)
            dbFilePath = System.getProperty("java.io.tmpdir") + "/todo.db";
            System.err.println("[TasksServlet] getRealPath returned null; using temp file: " + dbFilePath);
        }
        System.out.println("[TasksServlet] Initializing TaskDao with DB file: " + dbFilePath);
        taskDao = new TaskDao(dbFilePath);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Task> tasks = taskDao.getAllTasks();
        resp.setContentType(APPLICATION_JSON);
        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(tasks));
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String title = req.getParameter("title");
        if (title != null && !title.isEmpty()) {
            Task newTask = taskDao.addTask(title);
            if (newTask != null) {
                resp.setContentType(APPLICATION_JSON);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(newTask));
                out.flush();
            } else {
                // DB insert failed; return 500 and log for easier debugging
                System.err.println("[TasksServlet] Failed to create task for title='" + title + "' - TaskDao returned null");
                resp.setContentType(APPLICATION_JSON);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                PrintWriter out = resp.getWriter();
                out.print("{\"error\":\"Failed to create task on server\"}");
                out.flush();
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            int id = Integer.parseInt(pathInfo.substring(1));
            String title = req.getParameter("title");
            boolean completed = Boolean.parseBoolean(req.getParameter("completed"));
            if (title == null) title = ""; // avoid NPE in DAO
            taskDao.updateTask(id, title, completed);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            int id = Integer.parseInt(pathInfo.substring(1));
            taskDao.deleteTask(id);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
