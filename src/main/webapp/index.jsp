<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Beautified Hello World</title>
    <!-- Bootstrap Icons CDN (small, no JS needed) -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
    <style>
        /* Modern ve temiz bir font ailesi seçtik */
        body {
            margin: 0;
            padding: 0;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            /* Arka plana yumuşak ve derinlik katan bir gradient (geçiş) ekledik */
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            color: white;
        }

        .container {
            text-align: center;
            background: rgba(255, 255, 255, 0.1); 
            padding: 3rem;
            border-radius: 20px; /*  hmm */
            backdrop-filter: blur(10px);
            box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
            border: 1px border rgba(255, 255, 255, 0.18);
        }

        h2 {
            font-size: 4rem;
            margin: 0;
            letter-spacing: -1px;
            text-shadow: 2px 2px 10px rgba(0,0,0,0.2);
        }

        p {
            opacity: 0.8;
            font-size: 1.2rem;
            margin-top: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h2>To Do List</h2>
        <form id="add-task-form" style="margin-bottom: 2rem; display: flex; gap: 1rem; justify-content: center; align-items: center;">
            <input id="new-task-title" type="text" placeholder="Add a new task..." required style="padding: 0.5rem 1rem; border-radius: 8px; border: none; width: 250px; font-size: 1rem;">
            <button type="submit" style="padding: 0.5rem 1.5rem; border-radius: 8px; border: none; background: #667eea; color: white; font-weight: bold; cursor: pointer;">Add</button>
        </form>
        <ul id="task-list" style="list-style: none; padding: 0; margin: 0; max-width: 400px; margin-left: auto; margin-right: auto;"></ul>
    </div>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        // Helper to get context path dynamically
        function getApiUrl(path) {
            const ctx = window.location.pathname.split('/')[1];
            return `/${ctx}/` + path.replace(/^\//, '');
        }

        async function fetchTasks() {
            try {
                const res = await fetch(getApiUrl('api/tasks'));
                if (!res.ok) throw new Error('Failed to fetch tasks');
                const tasks = await res.json();
                renderTasks(tasks);
            } catch (e) {
                alert('Error loading tasks: ' + e.message);
            }
        }

        function renderTasks(tasks) {
            const list = document.getElementById('task-list');
            list.innerHTML = '';
            tasks.forEach(task => {
                const li = document.createElement('li');
                li.style.display = 'flex';
                li.style.alignItems = 'center';
                li.style.justifyContent = 'space-between';
                li.style.background = 'rgba(255,255,255,0.15)';
                li.style.marginBottom = '0.5rem';
                li.style.padding = '0.75rem 1rem';
                li.style.borderRadius = '10px';

                // Checkbox
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.checked = task.completed;
                checkbox.style.marginRight = '1rem';
                checkbox.onchange = () => updateTask(task.id, task.title, checkbox.checked);

                // Title (editable)
                const title = document.createElement('span');
                title.textContent = task.title;
                title.style.flex = '1';
                title.style.textDecoration = task.completed ? 'line-through' : 'none';
                title.style.opacity = task.completed ? '0.6' : '1';

                // Edit (pen) icon
                const editBtn = document.createElement('button');
                editBtn.innerHTML = '<i class="bi bi-pencil" aria-hidden="true"></i>';
                editBtn.title = 'Edit';
                editBtn.style.background = 'none';
                editBtn.style.border = 'none';
                editBtn.style.cursor = 'pointer';
                editBtn.style.marginLeft = '0.5rem';
                editBtn.onclick = () => {
                    const newTitle = prompt('Update task:', task.title);
                    if (newTitle && newTitle !== task.title) {
                        updateTask(task.id, newTitle, task.completed);
                    }
                };

                // Delete (trash) icon
                const deleteBtn = document.createElement('button');
                deleteBtn.innerHTML = '<i class="bi bi-trash" aria-hidden="true"></i>';
                deleteBtn.title = 'Delete';
                deleteBtn.style.background = 'none';
                deleteBtn.style.border = 'none';
                deleteBtn.style.cursor = 'pointer';
                deleteBtn.style.marginLeft = '0.5rem';
                deleteBtn.onclick = () => deleteTask(task.id);

                li.appendChild(checkbox);
                li.appendChild(title);
                li.appendChild(editBtn);
                li.appendChild(deleteBtn);
                list.appendChild(li);
            });
        }

        async function addTask(title) {
            try {
                const res = await fetch(getApiUrl('api/tasks'), {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: 'title=' + encodeURIComponent(title)
                });
                if (!res.ok) {
                    const text = await res.text();
                    throw new Error('Server error: ' + res.status + ' ' + text);
                }
                fetchTasks();
            } catch (e) {
                alert('Error adding task: ' + e.message);
            }
        }

        async function updateTask(id, title, completed) {
            try {
                // Use path-based id and query params so servlet can read them reliably
                const url = getApiUrl(`api/tasks/${id}?completed=${completed}&title=${encodeURIComponent(title)}`);
                const res = await fetch(url, { method: 'PUT' });
                if (!res.ok) {
                    const text = await res.text();
                    throw new Error('Server error: ' + res.status + ' ' + text);
                }
                fetchTasks();
            } catch (e) {
                alert('Error updating task: ' + e.message);
            }
        }

        async function deleteTask(id) {
            try {
                const url = getApiUrl(`api/tasks/${id}`);
                const res = await fetch(url, { method: 'DELETE' });
                if (!res.ok) {
                    const text = await res.text();
                    throw new Error('Server error: ' + res.status + ' ' + text);
                }
                fetchTasks();
            } catch (e) {
                alert('Error deleting task: ' + e.message);
            }
        }

        document.getElementById('add-task-form').onsubmit = function(e) {
            e.preventDefault();
            const input = document.getElementById('new-task-title');
            const title = input.value.trim();
            if (title) {
                addTask(title);
                input.value = '';
            }
        };

        // Load tasks on page load
        fetchTasks();
    });
    </script>
</body>
</html>
