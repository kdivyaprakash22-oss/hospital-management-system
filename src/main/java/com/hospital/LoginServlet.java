package com.hospital;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
  @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp)throws IOException {
    req.setCharacterEncoding("UTF-8");
    if (!WebUtil.validCsrfToken(req)) {
      req.getSession().setAttribute("loginError","Your form expired. Please try again.");
      resp.sendRedirect(req.getContextPath()+"/login.jsp");
      return;
    }
    String expectedUser=System.getenv().getOrDefault("APP_USERNAME","admin");
    String expectedPass=System.getenv("APP_PASSWORD");
    String user=req.getParameter("username"),pass=req.getParameter("password");
    if(expectedPass!=null&&!expectedPass.isBlank()&&expectedUser.equals(user)&&pass!=null&&expectedPass.equals(pass)){
      HttpSession old=req.getSession(false);
      if(old!=null) old.invalidate();
      HttpSession session=req.getSession(true);
      session.setAttribute("loggedIn",true);
      session.setAttribute("username",user);
      WebUtil.csrfToken(req);
      resp.sendRedirect(req.getContextPath()+"/app/");
    } else {
      req.getSession().setAttribute("loginError","Login failed. Check your username and password.");
      resp.sendRedirect(req.getContextPath()+"/login.jsp");
    }
  }
}
