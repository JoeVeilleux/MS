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
<h1>What would you like to do?</h1>
<p>Select one of the buttons below to perform the indicated action:</p>
<div class="actions">
<a href="passengers"><button type="button" id="action_passengers">Manage Passengers</button></a>
<a href="drivers"><button type="button" id="action_drivers">Manage Drivers</button></a>
<a href="rides"><button type="button" id="action_rides">Manage Rides</button></a>
</div>
</body>
</html>
