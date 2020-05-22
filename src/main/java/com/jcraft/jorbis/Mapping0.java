/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *  
 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *   
 * Many thanks to 
 *   Monty <monty@xiph.org> and 
 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;

class Mapping0 extends FuncMapping{
  void free_info(Object imap){}


  Object look(DspState vd, InfoMode vm, Object m){
    Info vi=vd.vi;
    LookMapping0 look=new LookMapping0();
    InfoMapping0 info=look.map=(InfoMapping0)m;
    look.mode=vm;

    look.time_look=new Object[info.submaps];
    look.floor_look=new Object[info.submaps];
    look.residue_look=new Object[info.submaps];


    look.time_func=new FuncTime[info.submaps];
    look.floor_func=new FuncFloor[info.submaps];
    look.residue_func=new FuncResidue[info.submaps];

    for(int i=0;i<info.submaps;i++){
      int timenum=info.timesubmap[i];
      int floornum=info.floorsubmap[i];
      int resnum=info.residuesubmap[i];

      look.time_func[i]= FuncTime.time_P[vi.time_type[timenum]];
      look.time_look[i]=look.time_func[i].look(vd,vm,vi.time_param[timenum]);
      look.floor_func[i]=FuncFloor.floor_P[vi.floor_type[floornum]];
      look.floor_look[i]=look.floor_func[i].
                         look(vd,vm,vi.floor_param[floornum]);
      look.residue_func[i]=FuncResidue.residue_P[vi.residue_type[resnum]];
      look.residue_look[i]=look.residue_func[i].
                           look(vd,vm,vi.residue_param[resnum]);
    }

    look.ch=vi.channels;
    return(look);
  }

  // also responsible for range checking
  Object unpack(Info vi, Buffer opb){
    InfoMapping0 info=new InfoMapping0();

    if(opb.read(1)!=0){
      info.submaps=opb.read(4)+1;
    }
    else{
      info.submaps=1;
    }

    if(opb.read(1)!=0){
      info.coupling_steps=opb.read(8)+1;

      for(int i=0;i<info.coupling_steps;i++){
        int testM=info.coupling_mag[i]=opb.read(ilog2(vi.channels));
        int testA=info.coupling_ang[i]=opb.read(ilog2(vi.channels));

        if(testM<0 ||
           testA<0 ||
           testM==testA ||
           testM>=vi.channels ||
           testA>=vi.channels){
          info.free();
          return(null);
        }
      }
    }

    if(opb.read(2)>0){ /* 2,3:reserved */
      info.free();
      return(null);
    }

    if(info.submaps>1){
      for(int i=0;i<vi.channels;i++){
        info.chmuxlist[i]=opb.read(4);
        if(info.chmuxlist[i]>=info.submaps){
          info.free();
          return(null);
	}
      }
    }

    for(int i=0;i<info.submaps;i++){
      info.timesubmap[i]=opb.read(8);
      if(info.timesubmap[i]>=vi.times){
        info.free();
        return(null);
      }
      info.floorsubmap[i]=opb.read(8);
      if(info.floorsubmap[i]>=vi.floors){
        info.free();
        return(null);
      }
      info.residuesubmap[i]=opb.read(8);
      if(info.residuesubmap[i]>=vi.residues){
        info.free();
        return(null);
      }
    }
    return info;
  }

  private static int ilog2(int v){
    int ret=0;
    while(v>1){
      ret++;
      v>>>=1;
    }
    return(ret);
  }
}

class InfoMapping0{
  int   submaps;  // <= 16
  int[] chmuxlist=new int[256];   // up to 256 channels in a Vorbis stream
  
  int[] timesubmap=new int[16];   // [mux]
  int[] floorsubmap=new int[16];  // [mux] submap to floors
  int[] residuesubmap=new int[16];// [mux] submap to residue
  int[] psysubmap=new int[16];    // [mux]; encode only

  int   coupling_steps;
  int[] coupling_mag=new int[256];
  int[] coupling_ang=new int[256];

  void free(){
    chmuxlist = null;
    timesubmap = null;
    floorsubmap = null;
    residuesubmap = null;
    psysubmap = null;

    coupling_mag = null;
    coupling_ang = null;
  }
}

class LookMapping0{
  InfoMode mode;
  InfoMapping0 map;
  Object[] time_look;
  Object[] floor_look;
  Object[] residue_look;

  FuncTime[] time_func;
  FuncFloor[] floor_func; 
  FuncResidue[] residue_func;

  int ch;
}
