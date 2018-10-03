<%--
  Created by IntelliJ IDEA.
  User: Stas
  Date: 01.10.2018
  Time: 16:23
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Upload</title>
</head>
<body>
<h2>${requestScope.message}</h2>

<table border="1" cellpadding="8" cellspacing="0">
    <tr>
        <td>
            Name
        </td>
        <td>
            Email
        </td>
        <td>
            Flag
        </td>
    </tr>
    <c:forEach items="${users}" var="user">
        <jsp:useBean id="user" scope="page" type="ru.javaops.masterjava.xml.schema.User"/>
        <tr>
            <td>${user.value}</td>
            <td>${user.email}</td>
            <td>${user.flag}</td>
        </tr>

    </c:forEach>
</table>

</body>
</html>
