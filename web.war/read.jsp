<%!
    byte[] buf=null;
    int length=0;
    String id;
%>



<%
    // response.setHeader("Cache-Control", "no-cache");
    id=request.getParameter("id");
    if(id == null)
        id="1";

    buf=(byte[])session.getAttribute(id);
    length=buf != null? buf.length : 0;
%>



<html>

<head>
    <title> Initial setup </title>
</head>

<body bgcolor="white">

Read data for key <%=id%>: <%=length%> bytes

</body>
</html>
