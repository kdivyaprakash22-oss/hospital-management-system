package com.hospital;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@WebServlet("/app/*")
public class HospitalServlet extends HttpServlet {
  private static final Map<String, Module> MODULES = new LinkedHashMap<>();
  static {
    add("patients", "Patients", "patients", new String[][]{{"full_name","Full name","text"},{"date_of_birth","Date of birth","date"},{"gender","Gender","select:Male,Female,Other"},{"phone","Phone","text"},{"email","Email","email"},{"address","Address","text"},{"status","Status","select:ACTIVE,DISCHARGED"}}, "id,full_name,date_of_birth,gender,phone,email,address,status,created_at");
    add("departments", "Departments", "departments", new String[][]{{"name","Department name","text"},{"description","Description","text"}}, "id,name,description,created_at");
    add("doctors", "Doctors", "doctors", new String[][]{{"full_name","Full name","text"},{"department_id","Department ID (optional)","number"},{"specialization","Specialization","text"},{"phone","Phone","text"},{"email","Email","email"}}, "id,full_name,department_id,specialization,phone,email,created_at");
    add("nurses", "Nurses", "nurses", new String[][]{{"full_name","Full name","text"},{"department_id","Department ID (optional)","number"},{"phone","Phone","text"},{"email","Email","email"}}, "id,full_name,department_id,phone,email,created_at");
    add("rooms", "Rooms / Beds", "rooms", new String[][]{{"room_number","Room number","text"},{"room_type","Room type","select:General,Private,ICU"},{"daily_charge","Daily charge","number"},{"active","Available for use","select:true,false"}}, "id,room_number,room_type,daily_charge,active");
    add("medicines", "Medicines", "medicines", new String[][]{{"name","Medicine name","text"},{"quantity","Quantity","number"},{"unit_price","Unit price","number"},{"expiry_date","Expiry date","date"}}, "id,name,quantity,unit_price,expiry_date,updated_at");
    add("admissions", "Admissions", "admissions", new String[][]{{"patient_id","Patient ID","number"},{"room_id","Room ID","number"},{"doctor_id","Doctor ID (optional)","number"},{"admission_date","Admission date","date"}}, "id,patient_id,room_id,doctor_id,admission_date,discharge_date,status");
    add("bills", "Bills", "bills", new String[][]{{"patient_id","Patient ID","number"},{"admission_id","Admission ID (optional)","number"},{"description","Description","text"},{"amount","Amount","number"},{"payment_status","Payment status","select:UNPAID,PAID,WAIVED"}}, "id,patient_id,admission_id,description,amount,payment_status,created_at");
    add("attendance", "Staff attendance", "attendance", new String[][]{{"staff_type","Staff type","select:DOCTOR,NURSE"},{"staff_id","Staff ID","number"},{"attendance_date","Date","date"},{"status","Status","select:PRESENT,ABSENT,LEAVE"}}, "id,staff_type,staff_id,attendance_date,status,created_at");
  }
  private static void add(String key,String title,String table,String[][] fields,String columns){ MODULES.put(key,new Module(key,title,table,fields,columns.split(","))); }
  public static final class Module { public final String key,title,table; public final String[][] fields; public final String[] columns; Module(String k,String t,String tb,String[][] f,String[] c){key=k;title=t;table=tb;fields=f;columns=c;} }

  @Override protected void doGet(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
    if (!authorized(req,resp)) return;
    String path=req.getPathInfo(); String key=(path==null||path.equals("/"))?"dashboard":path.substring(1);
    if (key.equals("logout")) { resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Sign out must use POST."); return; }
    if(key.equals("dashboard")){req.setAttribute("modules",MODULES.values());req.getRequestDispatcher("/WEB-INF/dashboard.jsp").forward(req,resp);return;}
    Module m=MODULES.get(key); if(m==null){resp.sendError(404,"Unknown module");return;}
    try(Connection c=Db.get(); Statement s=c.createStatement(); ResultSet rs=s.executeQuery("SELECT "+String.join(",",m.columns)+" FROM "+m.table+" ORDER BY id DESC LIMIT 200")){
      List<Map<String,Object>> rows=new ArrayList<>(); ResultSetMetaData md=rs.getMetaData(); while(rs.next()){Map<String,Object> row=new LinkedHashMap<>(); for(int i=1;i<=md.getColumnCount();i++){Object v=rs.getObject(i);row.put(md.getColumnLabel(i),v==null?"":v);} rows.add(row);}
      req.setAttribute("module",m);req.setAttribute("rows",rows);req.getRequestDispatcher("/WEB-INF/module.jsp").forward(req,resp);
    }catch(SQLException e){getServletContext().log("Database query failed for module "+key,e);throw new ServletException("Database query failed. Check the database configuration and schema.");}
  }

  @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws ServletException,IOException {
    req.setCharacterEncoding("UTF-8");
    if (!authorized(req,resp)) return;
    if (!WebUtil.validCsrfToken(req)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired form token. Refresh the page and try again."); return; }
    String path=req.getPathInfo();
    if ("/logout".equals(path)) {
      HttpSession session=req.getSession(false);
      if(session!=null) session.invalidate();
      resp.sendRedirect(req.getContextPath()+"/login.jsp");
      return;
    }
    String key=req.getParameter("module"); Module m=MODULES.get(key); if(m==null){resp.sendError(400,"Unknown module");return;}
    String action=req.getParameter("action");
    try(Connection c=Db.get()){
      if("add".equals(action)){
        List<String> names=new ArrayList<>();List<String> vals=new ArrayList<>();List<Object> args=new ArrayList<>();
        for(String[] f:m.fields){String value=req.getParameter(f[0]);if(value==null||value.trim().isEmpty())continue;if(f[2].startsWith("select:")&&!Arrays.asList(f[2].substring(7).split(",")).contains(value.trim()))throw new IllegalArgumentException("Invalid value for "+f[1]+".");names.add(f[0]);vals.add("?");args.add(convert(f[0],value));}
        if(names.isEmpty())throw new IllegalArgumentException("Enter at least one field.");
        if(key.equals("bills")) validateBillAdmission(c, req);
        String sql="INSERT INTO "+m.table+" ("+String.join(",",names)+") VALUES ("+String.join(",",vals)+")";
        if(key.equals("admissions")) c.setAutoCommit(false);
        try(PreparedStatement ps=c.prepareStatement(sql)){for(int i=0;i<args.size();i++)ps.setObject(i+1,args.get(i));ps.executeUpdate();}
        if(key.equals("admissions")){
          try(PreparedStatement ps=c.prepareStatement("UPDATE patients SET status='ACTIVE' WHERE id=?")){
            ps.setLong(1,Long.parseLong(req.getParameter("patient_id")));ps.executeUpdate();
          }
          c.commit();
        }
        req.getSession().setAttribute("flash","Record added successfully.");
      } else if("discharge".equals(action) && key.equals("admissions")){
        int id=Integer.parseInt(req.getParameter("id"));
        c.setAutoCommit(false);
        try {
          int changed;
          try(PreparedStatement ps=c.prepareStatement("UPDATE admissions SET status='DISCHARGED', discharge_date=CURRENT_DATE WHERE id=? AND status='ADMITTED'")){
            ps.setInt(1,id); changed=ps.executeUpdate();
          }
          if(changed==1){
            try(PreparedStatement ps=c.prepareStatement("UPDATE patients p SET status=CASE WHEN EXISTS (SELECT 1 FROM admissions a WHERE a.patient_id=p.id AND a.status='ADMITTED') THEN 'ACTIVE' ELSE 'DISCHARGED' END WHERE p.id=(SELECT patient_id FROM admissions WHERE id=?)")){
              ps.setInt(1,id);ps.executeUpdate();
            }
          }
          c.commit();
          req.getSession().setAttribute("flash",changed==1?"Admission discharged.":"No active admission found for that ID.");
        } catch(Exception e) {
          try { c.rollback(); } catch(SQLException rollbackError) { e.addSuppressed(rollbackError); }
          throw e;
        }
      } else if("pay".equals(action) && key.equals("bills")){
        try(PreparedStatement ps=c.prepareStatement("UPDATE bills SET payment_status='PAID' WHERE id=? AND payment_status='UNPAID'")){ps.setInt(1,Integer.parseInt(req.getParameter("id")));int n=ps.executeUpdate();req.getSession().setAttribute("flash",n==1?"Bill marked paid.":"Unpaid bill not found.");}
      } else throw new IllegalArgumentException("Unsupported action.");
      resp.sendRedirect(req.getContextPath()+"/app/"+key);
    }catch(Exception e){getServletContext().log("Operation failed for module "+key,e);req.getSession().setAttribute("flash","Operation failed. Check the entered values and related record IDs.");resp.sendRedirect(req.getContextPath()+"/app/"+key);}
  }

  private void validateBillAdmission(Connection c,HttpServletRequest req)throws SQLException {
    String admissionValue=req.getParameter("admission_id");
    String patientValue=req.getParameter("patient_id");
    if(admissionValue==null||admissionValue.isBlank()) return;
    if(patientValue==null||patientValue.isBlank()) throw new IllegalArgumentException("A patient ID is required when linking a bill to an admission.");
    long admissionId=Long.parseLong(admissionValue.trim());
    long patientId=Long.parseLong(patientValue.trim());
    try(PreparedStatement ps=c.prepareStatement("SELECT patient_id FROM admissions WHERE id=?")){
      ps.setLong(1,admissionId);
      try(ResultSet rs=ps.executeQuery()){
        if(!rs.next()) throw new IllegalArgumentException("The selected admission does not exist.");
        if(rs.getLong(1)!=patientId) throw new IllegalArgumentException("The bill patient must match the selected admission's patient.");
      }
    }
  }

  private Object convert(String field,String value){
    String v=value.trim();
    if(Set.of("department_id","patient_id","room_id","doctor_id","admission_id","staff_id").contains(field))return Long.valueOf(v);
    if(field.equals("quantity"))return Integer.valueOf(v);
    if(Set.of("daily_charge","unit_price","amount").contains(field))return new BigDecimal(v);
    if(Set.of("date_of_birth","expiry_date","admission_date","attendance_date").contains(field))return java.sql.Date.valueOf(v);
    return v;
  }

  private boolean authorized(HttpServletRequest req,HttpServletResponse resp)throws IOException{
    HttpSession s=req.getSession(); if(Boolean.TRUE.equals(s.getAttribute("loggedIn")))return true;
    String pass=System.getenv("APP_PASSWORD");
    if(pass==null||pass.isBlank()){resp.sendError(503,"APP_PASSWORD is not configured. Set APP_USERNAME and APP_PASSWORD in hosting environment variables.");return false;}
    resp.sendRedirect(req.getContextPath()+"/login.jsp");return false;
  }
}
