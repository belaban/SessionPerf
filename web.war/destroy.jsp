
<%!
    String session_id=null;
    String nodeName = System.getProperty("jboss.jvmRoute");
%>

<%
    session_id=session.getId();
    response.setHeader("X-ClusterNode", nodeName);
%>



<html>

<head>
    <title> Destroying the HTTP session </title>
</head>

<body bgcolor="white">

<%
    session.invalidate();
%>

Invalidated session <%= session_id %>

</body>
</html>
