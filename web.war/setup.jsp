<%@ page import="java.util.Enumeration" %>
<%!

   /* public void init() throws ServletException {
        System.out.println("init();");
    }

    public void jspDestroy()  {
        System.out.println("destroy()");
    }*/

    int count, total_size;
%>



<%
    // response.setHeader("Cache-Control", "no-cache");

    String num_attrs_str=request.getParameter("num_attrs");
    String size_str=request.getParameter("size");

    int num_attrs=40;
    int size=1000;

    if(num_attrs_str != null)
        num_attrs=Integer.parseInt(num_attrs_str);
    if(size_str != null)
        size=Integer.parseInt(size_str);

    for(int i=0; i < num_attrs; i++) {
        session.setAttribute(String.valueOf(i), new byte[size]);
    }

    count=total_size=0;
    for(Enumeration en=session.getAttributeNames(); en.hasMoreElements();) {
        String attr_name=(String)en.nextElement();
        count++;
        byte[] buf=(byte[])session.getAttribute(attr_name);
        if(buf != null)
            total_size+=buf.length;
    }
%>



<html>

<head>
    <title> Initial setup </title>
</head>

<body bgcolor="white">

Created <%= count%> attributes, total size=<%=total_size%> bytes.<br/>
Session ID is <%= session.getId()%>

</body>
</html>
