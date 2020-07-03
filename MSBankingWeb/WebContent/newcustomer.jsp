<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Banking Web UI</title>
<link rel="stylesheet" type="text/css" href="Banking.css">
</head>
<body>
<div class="appTitle">
<a href=<% out.print(request.getContextPath()); %>><button class="appTitleButton" type="button" id="nav_home">
<img src="bank.png" alt="Banking Web UI" style="width:280px;height:40px;">
</button></a>
</div>
<h1>New Customer</h1>
<p>Fill out the information for the new Customer below...</p>
<form action="customers" method="post">
  <table border="1">
    <tr><th>Name</th><td><input type="text" name="name" size="20"></td></tr>
    <tr><th>Address</th><td><input type="text" name="address" size="40"></td></tr>
  </table>
  <button type="submit" name="button" value="Cancel">Cancel</button>
  <button type="submit" name="button" value="Submit">Submit</button>
</form>
</body>
</html>