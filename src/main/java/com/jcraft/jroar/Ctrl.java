/* -*-mode:java; c-basic-offset:2; -*- */
/* JRoar -- pure Java streaming server for Ogg
 *
 * Copyright (C) 2001,2002 ymnk, JCraft,Inc.
 *
 * Written by: 2001,2002 ymnk<ymnk@jcraft.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jroar;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

class Ctrl extends Page{
  String tdd = "</td>";
  String nbsp = "&nbsp;&nbsp;";
  String td = "<td>&nbsp;</td>";
  String pass = "passwd:&nbsp";
  String table2 = "</table>";
  String width = "<hr width=80%>";
  String select = "</select>";
  String form = "</form>";
  String align = "<td align=left>";
  String tr = "</tr>";
  String optionval = "<OPTION VALUE=";
  String passlen = "<input type=password name=passwd value='' length=8>";
  String table = "<table cellpadding=3 cellspacing=0 border=0>";
  static void register(){
    register("/ctrl.html", Ctrl.class.getName());
  }
  private static int count = 0;

  public void kick(MySocket s, Hashtable<?, ?> vars, Vector<?> httpheader) throws IOException{
    count++;
    s.println( "HTTP/1.0 200 OK" );
    s.println( "Content-Type: text/html" );
    s.println( "" ) ;
    s.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">");
    s.println("<HTML><HEAD>");
    s.println("<TITLE>JRoar "+JRoar.version+" at "+HttpServer.myURL+"/</TITLE>");
    s.println("</HEAD><BODY>");
    s.println( "<h1>JRoar "+JRoar.version+" at "+HttpServer.myURL+"/</h1>" );

    Enumeration<?> keys=Source.sources.keys();
    if (keys.hasMoreElements()) {
      s.println("Mount points.<br>");
    }
    else {
      s.println("There is no mount point.<br>");
    }

    s.println(table);
    while (keys.hasMoreElements()) {
      String mountpoint=((String)(keys.nextElement()));
      Source source=Source.getSource(mountpoint); if(source==null) continue;
      String source_name = source.source;

      s.println("<tr>");

      s.println("<td align=left nowrap>");
      s.print("<a href="+ogg2m3u(mountpoint)+">"+mountpoint);

      if (source instanceof UDPSource) {
         UDPSource foo=(UDPSource)source;
         s.print("(UDP:"+foo.b.port+")");
      }

      s.print("</a>");
      s.print("&nbsp;("+source.getListeners()+","+source.getConnections()+")");
      s.println(tdd);

      s.println("<td nowrap> &lt;--- </td>");

      if(source instanceof Proxy){
        s.println(align);
        s.print("<a href="+source_name+">"+source_name+"</a>");
        s.println(tdd);
      }
      else if(source instanceof UDPSource){
        UDPSource foo=(UDPSource)source;
        s.println(align);
        s.print(foo.b.srcmpoint);
        s.println(tdd);
      }
      else{
        s.println(align+source_name+tdd);
      }
      s.println(tr);
      Object[] proxies=source.getProxies();

      if(proxies != null){
        for(int i=0; i<proxies.length; i++){
          String foo=(String)(proxies[i]);
          s.println("<tr>");
          s.println(td);
          s.println("<td nowrap>---&gt</td>");
          String host=getHost(foo);
          if(host==null){
            s.println("<td><a href="+ogg2m3u(foo)+">"+foo+"</a></td>");
	  }
          else{
            s.println("<td><a href="+ogg2m3u(foo)+">"+foo.substring(host.length()-1)+"</a>&nbsp;at&nbsp;<a href="+host+">"+host+"</a></td>");
	  }
          s.println(tr);
	}
      }

    }
    s.println(table2);

    s.println(width);
    s.println("<font size=+1>Mount</font>");
    s.println(table);
    s.println("<form method=post action=/mount>");
    s.println("<tr><td>");
    s.print("mountpoint:&nbsp;");
    s.print("<input type=text name=mountpoint value='/' size=10 maxlength=32>");
    s.print(nbsp);
    s.print("source:&nbsp;");
    s.print("<input type=text name=source value='http://' size=40 maxlength=100>");

    s.print(nbsp);
    s.print("<select name=livestream>");
    s.print("  <option value='true' selected>LiveStream</option>");
    s.print("  <option value='false'>PlayList</option>");
    s.print(select);

    s.print(nbsp);
    s.print("limit:&nbsp;");
    s.print("<input type=text name=limit value='' size=3 maxlength=3>");

    s.println("</td></tr>");

    s.println("<tr><td>");
    s.print(pass);
    s.print(passlen);

    s.print(nbsp);
    s.print("<input type=submit name=Mount value=Mount>");
    s.println("</td></tr>");
    s.print(form);
    s.print(table2);

    s.print("<p>");

    synchronized(Client.clients){

    keys=Source.sources.keys();
    if(keys.hasMoreElements()){
    s.println(width);
    s.println("<font size=+1>Drop</font>");
    s.println(table);
    s.println("<form method=post action=/drop>");

    s.print("<select name=mpoint size=1>");
    while (keys.hasMoreElements()){
      String mpoint=((String)(keys.nextElement()));
      s.println(optionval+mpoint+">"+mpoint);
    }
    s.print(select);

    s.print(nbsp);
    s.print(pass);
    s.print(passlen);

    s.print(nbsp);
    s.print("<input type=submit name=Drop value=Drop>");
    s.print(form);
    s.print(table2);
    }

    keys=Source.sources.keys();
    if(keys.hasMoreElements()){
    s.println(width);
    s.println("<font size=+1>Shout</font>");

    s.println(table);
    for(int i=0; i<Client.clients.size(); i++){
      Client c=(Client.clients.elementAt(i));

      if (c instanceof ShoutClient) {
        ShoutClient sc=(ShoutClient)c;
        s.println("<tr>");

        s.println(align);
        s.print(sc.srcmpoint);
        s.println(tdd);

        s.println("<td nowrap>---&gt</td>");

        s.println(align);
        s.print("<a href=http://"+sc.dsthost+":"+sc.dstport+sc.dstmpoint+">http://"+sc.dsthost+":"+sc.dstport+sc.dstmpoint+"</a>");
        s.println(tdd);

        s.println(tr);
      }
    }
    s.println(table2);

    s.println(table);
    s.println("<form method=post action=/shout>");

    s.print("<select name=srcmpoint size=1>");
    while (keys.hasMoreElements()) {
      String mpoint = ((String)(keys.nextElement()));
      if (Source.sources.get(mpoint) instanceof UDPSource)
        continue;
      s.println(optionval+mpoint+">"+mpoint);
    }
    s.print(select);

    s.println(" ---&gt ");

    s.print("<input type=text name=dst value='ice://' size=20 maxlength=50>");
    s.print(nbsp);
    s.print("ice-passwd:&nbsp;");
  s.print("<input type=password name=ice-passwd value='' size=8 maxlength=8>");
    s.print("<br>");

    s.print(nbsp);
    s.print(pass);
    s.print(passlen);

    s.print(nbsp);
    s.print("<input type=submit name=Shout value=Shout>");
    s.print(form);
    s.print(table2);
    }

    keys=Source.sources.keys();
    if(keys.hasMoreElements()){
    s.println(width);
    s.println("<font size=+1>UDP Broadcast</font>");
    s.println(table);
    s.println("<form method=post action=/udp>");

    s.println("<tr>");

    s.println("<td>");
    s.print("<select name=srcmpoint size=1>");

    while (keys.hasMoreElements()){
      String mpoint=((String)(keys.nextElement()));
      Source source=Source.getSource(mpoint);

      if(source==null || source instanceof UDPSource)
        continue;
      s.println(optionval+mpoint+">"+mpoint);
    }

    s.print(select);
    s.println(tdd);

    s.println("<td nowrap>---&gt</td>");

    s.println("<td>");
    s.print("port:&nbsp; ");
    s.print("<input type=text name=port value='' size=4 maxlength=4>");
    s.print("&nbsp;&nbsp;broadcast address:&nbsp; ");
    s.print("<input type=text name=baddress value='' size=15 maxlength=20>");
    s.println(tdd);
    s.println(tr);

    s.println("<tr>");
    s.println(td);
    s.println(td);

    s.println("<td>");
    s.print("mountpoint:&nbsp; ");
    s.print("<input type=text name=dstmpoint value='/' size=20 maxlength=50>");

    s.print(nbsp);
    s.print(pass);
    s.print(passlen);

    s.print(nbsp);
    s.print("<input type=submit name=Broadcast value=Broadcast>");
    s.println(tdd);
    s.println(tr);

    s.print(form);
    s.print(table2);
    }
    }

    s.println(width);

    s.println("<table width=100%><tr>");
    s.println("<td align=\"right\"><small><i>"+count+"</i></small></td>");
    s.println("</tr></table>");

    s.println("</BODY></HTML>");
    s.flush();
    s.close();

  }

  private String ogg2m3u(String ogg){
    if(!ogg.endsWith(".ogg") && !ogg.endsWith(".spx")) return ogg;
    byte[] foo=ogg.getBytes();
    foo[foo.length-1]='u';foo[foo.length-2]='3';foo[foo.length-3]='m';
    return new String(foo);
  }

  private static final String _http="http://";
  private String getHost(String url){
    if (!url.startsWith(_http))
      return null;
    int foo=url.substring(_http.length()).indexOf('/');
    if(foo!=-1){
      return url.substring(0, _http.length()+foo+1);
    }
    return null;
  }

}
