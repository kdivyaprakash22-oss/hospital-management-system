package com.hospital;
import javax.servlet.annotation.WebServlet;import javax.servlet.http.*;import java.io.IOException;
@WebServlet("/login") public class LoginServlet extends HttpServlet {
 protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException{
  req.setCharacterEncoding("UTF-8");String expectedUser=System.getenv().getOrDefault("APP_USERNAME","admin");String expectedPass=System.getenv("APP_PASSWORD");
  String user=req.getParameter("username"),pass=req.getParameter("password");
  if(expectedPass!=null&&!expectedPass.isBlank()&&expectedUser.equals(user)&&expectedPass.equals(pass)){req.getSession(true).setAttribute("loggedIn",true);req.getSession().setAttribute("username",user);resp.sendRedirect(req.getContextPath()+"/app/");}
  else {req.getSession().setAttribute("loginError","Login failed. Check your username and password.");resp.sendRedirect(req.getContextPath()+"/login.jsp");}
 }
}
