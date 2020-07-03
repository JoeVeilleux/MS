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
<h1>What would you like to do?</h1>
<p>Select one of the buttons below to perform the indicated action:</p>
<div class="actions">
<a href="customers"><button type="button" id="action_customers">Manage Customers</button></a>
<a href="accounts"><button type="button" id="action_accounts">Manage Accounts</button></a>
</div>
</body>
</html>
