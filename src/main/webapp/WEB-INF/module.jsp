<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Records</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/style.css">
</head>
<body>
<header class="topbar">
    <a class="brand" href="<%= request.getContextPath() %>/app/">✚ HospitalCare</a>
    <span>Management Portal</span>
    <a class="logout" href="<%= request.getContextPath() %>/app/logout">Sign out</a>
</header>

<main class="container">
    <a class="back" href="<%= request.getContextPath() %>/app/">← Dashboard</a>

    <%
        Object mo = request.getAttribute("module");
        String title = (String) mo.getClass().getField("title").get(mo);
        String key = (String) mo.getClass().getField("key").get(mo);
        String[][] fields = (String[][]) mo.getClass().getField("fields").get(mo);
    %>

    <div class="page-heading">
        <div>
            <p class="eyebrow">RECORD MANAGEMENT</p>
            <h1><%= title %></h1>
            <p class="muted">Add records and review recent entries.</p>
        </div>
    </div>

    <%
        String flash = (String) session.getAttribute("flash");
        if (flash != null) {
    %>
        <div class="alert"><%= flash %></div>
    <%
            session.removeAttribute("flash");
        }
    %>

    <section class="panel">
        <h2>Add record</h2>
        <form class="record-form" method="post" action="<%= request.getContextPath() %>/app/<%= key %>">
            <input type="hidden" name="module" value="<%= key %>">
            <input type="hidden" name="action" value="add">
            <% for (String[] f : fields) { %>
                <label>
                    <%= f[1] %>
                    <% if (f[2].startsWith("select:")) {
                        String[] opts = f[2].substring(7).split(",");
                    %>
                        <select name="<%= f[0] %>">
                            <option value="">Choose…</option>
                            <% for (String op : opts) { %>
                                <option value="<%= op %>"><%= op %></option>
                            <% } %>
                        </select>
                    <% } else { %>
                        <input
                            name="<%= f[0] %>"
                            type="<%= f[2] %>"
                            <%= Set.of("full_name", "name", "room_number", "amount", "patient_id", "room_id", "admission_date", "attendance_date").contains(f[0]) ? "required" : "" %>
                            <%= f[2].equals("number") ? "step=\"any\" min=\"0\"" : "" %>
                        >
                    <% } %>
                </label>
            <% } %>
            <button class="primary" type="submit">Save record</button>
        </form>
    </section>

    <section class="panel">
        <h2>Recent records</h2>
        <div class="table-wrap">
            <table>
                <thead>
                    <tr>
                        <%
                            List<Map<String, Object>> rows =
                                (List<Map<String, Object>>) request.getAttribute("rows");
                            if (!rows.isEmpty()) {
                                for (String col : rows.get(0).keySet()) {
                        %>
                            <th><%= col.replace('_', ' ').toUpperCase() %></th>
                        <%
                                }
                            } else {
                        %>
                            <th>Records</th>
                        <% } %>
                        <% if (key.equals("admissions") || key.equals("bills")) { %>
                            <th>Action</th>
                        <% } %>
                    </tr>
                </thead>
                <tbody>
                    <% for (Map<String, Object> row : rows) { %>
                        <tr>
                            <% for (Object val : row.values()) { %>
                                <td><%= val %></td>
                            <% } %>
                            <% if (key.equals("admissions") || key.equals("bills")) {
                                Object id = row.get("id");
                                Object status = row.get("status");
                                Object pay = row.get("payment_status");
                            %>
                                <td>
                                    <form method="post" action="<%= request.getContextPath() %>/app/<%= key %>">
                                        <input type="hidden" name="module" value="<%= key %>">
                                        <input type="hidden" name="id" value="<%= id %>">
                                        <% if (key.equals("admissions") && "ADMITTED".equals(String.valueOf(status))) { %>
                                            <input type="hidden" name="action" value="discharge">
                                            <button class="small" type="submit">Discharge</button>
                                        <% } else if (key.equals("bills") && "UNPAID".equals(String.valueOf(pay))) { %>
                                            <input type="hidden" name="action" value="pay">
                                            <button class="small" type="submit">Mark paid</button>
                                        <% } else { %>
                                            —
                                        <% } %>
                                    </form>
                                </td>
                            <% } %>
                        </tr>
                    <% } %>
                    <% if (rows.isEmpty()) { %>
                        <tr>
                            <td colspan="12" class="empty">No records yet. Use the form above to add one.</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </section>

    <p class="footnote">Only the latest 200 records are shown. Use fictional data in this demo.</p>
</main>
</body>
</html>
