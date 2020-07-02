<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Ridesharing Web UI</title>
<link rel="stylesheet" type="text/css" href="Rideshare.css">
</head>
<body>
<div class="appTitle">
<a href=<% out.print(request.getContextPath()); %>><button class="appTitleButton" type="button" id="nav_home">
<img src="limo.png" alt="Ridesharing Web UI" style="width:180px;height:60px;">
</button></a>
</div>
<h1>New Passenger</h1>
<p>Fill out the information for the new Passenger below...</p>
<form action="passengers" method="post">
  <table border="1">
    <tr><th>Name</th><td><input type="text" name="name" size="20"></td></tr>
    <tr><th>Address</th><td><input type="text" name="address" size="40"></td></tr>
  </table>
  <button type="submit" name="button" value="Cancel">Cancel</button>
  <button type="submit" name="button" value="Submit">Submit</button>
</form>
</body>
</html>