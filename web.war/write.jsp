<%!
    int size=0;
    String id;
%>



<%
    // response.setHeader("Cache-Control", "no-cache");
    id=request.getParameter("id");
    if(id == null)
        id="1";
    size=1000;
    String size_str=request.getParameter("size");
    if(size_str != null)
        size=Integer.parseInt(size_str);

    byte[] buf=new byte[size];
    session.setAttribute(id, buf);
%>



<html>

<head>
    <title> Initial setup </title>
</head>

<body bgcolor="white">

Set data for key <%=id%>: <%=size%> bytes

</body>
</html>
