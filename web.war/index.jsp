<%@ page import="java.util.Enumeration"%>
<%@ page import="java.net.InetAddress" %>

<%!
    String hostname="n/a", user_name="n/a";
    String nodeName = System.getProperty("jboss.jvmRoute");
%>

<%
    //    response.setHeader("Cache-Control", "no-cache");

    response.setHeader("X-ClusterNode", nodeName);

    // Touch the session to ensure cookie is sent
    session.getId();

    String key=request.getParameter("key"), val=request.getParameter("value");
    if(key != null) {
        if(val == null || val.trim().length() == 0) {
            System.out.println("removing " + key);
            session.removeAttribute(key);
        }
        else {
            System.out.println("adding " + key + "=" + val);
            session.setAttribute(key, val);
        }
    }
%>

<%
    try {
        hostname=InetAddress.getLocalHost().getHostName();
    }
    catch(Throwable t) {
    }
    try {
        user_name=System.getProperty("user.name", "n/a");
    }
    catch(Throwable t) {}
%>

<html>

<head>
    <title> Session information</title>
</head>

<body bgcolor="white">
<hr>

<br/>

<%!
int number_of_attrs=0, total_size=0;
%>

<%
    number_of_attrs=total_size=0;
    for(Enumeration en=session.getAttributeNames(); en.hasMoreElements();) {
        String attr_name=(String)en.nextElement();
        number_of_attrs++;
        byte[] buf=(byte[])session.getAttribute(attr_name);
        if(buf != null)
            total_size+=buf.length;
    }
%>

<font size=5> Session information (user=<%=user_name%>, host=<%=hostname%>):<br/><br/>
    ID: <%= session.getId()%><br/>
    Created: <%= new java.util.Date(session.getCreationTime())%><br/>
    Last accessed: <%= new java.util.Date(session.getLastAccessedTime())%><br/>
    Attributes: <b><%= number_of_attrs%></b><br/>
    Total size: <b><%= total_size%> bytes</b><br/>
</font>

<br/>

<%
    ServletContext ctx=session.getServletContext();
    Integer hits=(Integer)ctx.getAttribute("hits");
    if(hits == null) {
        hits=new Integer(0);
        ctx.setAttribute("hits", hits);
    }
    ctx.setAttribute("hits", new Integer(hits.intValue() +1));
%>

<%=hits%> hits
</body>
</html>
