package misc;/* JRoar -- pure Java streaming server for Ogg
 *
 * Copyright (C) 2001, 2002 ymnk, JCraft,Inc.
 *
 * Written by: 2001, 2002 ymnk<ymnk@jcraft.com>
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

/*
  USAGE: java com.jcraft.jroar.JRoar -page /JOrbisPlayer JOrbisPlayer
  Then try http://myaddress:8000/JOrbisPlayer?play=/test.ogg
 */

import java.io.*;
import java.util.*;

import com.jcraft.jroar.*;

public class JOrbisPlayer extends UserPage{
  static{
    JRoar.store("/JOrbisPlayer.jar", 
                "http://www.jcraft.com/jorbis/player/JOrbisPlayer-current.jar");
  }
  @Override
  public void kick(MySocket s, Hashtable vars, Vector httpheader) throws IOException{
    String play=(String)vars.get("play");

    String url="http://www.jcraft.com/jorbis/player/JOrbisPlayer.php?codebase="+JRoar.getMyURL()+"/&jar=JOrbisPlayer.jar&play="+play;

    s.println( "HTTP/1.1 302 Found" );
    s.println( "Location: "+url);
    s.println( "Connection: close" );
    s.println( "Content-Type: text/html" );
    s.println( "" ) ;

    s.flush();
    s.close();
  }

}
