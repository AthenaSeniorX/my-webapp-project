package com.example.todo;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "TasksServlet", urlPatterns = {"/api/tasks/*"})
public class TasksServlet extends HttpServlet {
    private TaskDao dao;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
        dao = new TaskDao();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Task> list = dao.list();
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(list));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject body = readJson(req);
        String title = body.has("title") ? body.get("title").getAsString() : null;
        if (title == null || title.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing title");
            return;
        }
        Task created = dao.add(title.trim());
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(gson.toJson(created));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo(); // /{id}
        if (path == null || path.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        int id = Integer.parseInt(path.substring(1));
        JsonObject body = readJson(req);
        boolean completed = body.has("completed") && body.get("completed").getAsBoolean();
        boolean ok = dao.updateCompleted(id, completed);
        if (!ok) resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        else resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || path.length() <= 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        int id = Integer.parseInt(path.substring(1));
        boolean ok = dao.delete(id);
        if (!ok) resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        else resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private JsonObject readJson(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return gson.fromJson(sb.toString(), JsonObject.class);
    }
}
