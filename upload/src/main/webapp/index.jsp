<%--
  Created by IntelliJ IDEA.
  User: Stas
  Date: 01.10.2018
  Time: 16:22
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>File Upload</title>
</head>
<body>

    <h1>File Upload</h1>
    <form method="post" action="UploadServlet" enctype="multipart/form-data">
        Select file to upload: <input type="file" name="file" size="60" accept="text/xml"/><br />
        <br /> <input type="submit" value="Upload" />
    </form>

</body>
</html>
