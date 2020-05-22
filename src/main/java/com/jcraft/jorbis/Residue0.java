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

class Residue0 extends FuncResidue{
  Object unpack(Info vi, Buffer opb){
    int acc=0;
    InfoResidue0 info=new InfoResidue0();

    info.begin=opb.read(24);
    info.end=opb.read(24);
    info.grouping=opb.read(24)+1;
    info.partitions=opb.read(6)+1;
    info.groupbook=opb.read(8);

    for(int j=0;j<info.partitions;j++){
      int cascade=opb.read(3);
      if(opb.read(1)!=0){
        cascade|=(opb.read(5)<<3);
      }
      info.secondstages[j] = cascade;
      acc += icount(cascade);
    }

    for (int j = 0; j < acc; j++){
      info.booklist[j]=opb.read(8);
    }

    if(info.groupbook>=vi.books){
      free_info(info);
      return(null);
    }

    for(int j=0;j<acc;j++){
      if(info.booklist[j]>=vi.books){
	free_info(info);
	return(null);
      }
    }
    return(info);
  }

  Object look(DspState vd, InfoMode vm, Object vr){
    InfoResidue0 info=(InfoResidue0)vr;
    LookResidue0 look=new LookResidue0();
    int acc=0;
    int dim;
    int maxstage=0;
    look.info=info;
    look.map=vm.mapping;

    look.parts=info.partitions;
    look.fullbooks=vd.fullbooks;
    look.phrasebook=vd.fullbooks[info.groupbook];

    dim=look.phrasebook.dim;

    look.partbooks=new int[look.parts][];

    for(int j=0;j<look.parts;j++){
      int stages=ilog(info.secondstages[j]);
      if(stages!=0){
        if(stages>maxstage)maxstage=stages;
        look.partbooks[j]=new int[stages];
        for(int k=0; k<stages; k++){
          if((info.secondstages[j]&(1<<k))!=0){
            look.partbooks[j][k]=info.booklist[acc++];
	      }
	    }
      }
    }

    look.partvals=(int)Math.rint(Math.pow(look.parts,dim));
    look.stages=maxstage;
    look.decodemap=new int[look.partvals][];
    for(int j=0;j<look.partvals;j++){
      int val=j;
      int mult=look.partvals/look.parts;
      look.decodemap[j]=new int[dim];

      for(int k=0;k<dim;k++){
        int deco=val/mult;
        val-=deco*mult;
        mult/=look.parts;
        look.decodemap[j][k]=deco;
      }
    }
    return(look);
  }
  void free_info(Object i){}

  private static int ilog(int v){
    int ret=0;
    while(v!=0){
      ret++;
      v>>>=1;
    }
    return(ret);
  }
  private static int icount(int v){
    int ret=0;
   while(v!=0){
      ret+=(v&1);
      v>>>=1;
    }
    return(ret);
  }
}

class LookResidue0 {
  InfoResidue0 info;
  int map;
  
  int parts;
  int stages;
  CodeBook[] fullbooks;
  CodeBook   phrasebook;
  int[][] partbooks;

  int partvals;
  int[][] decodemap;
}

class InfoResidue0{
  // block-partitioned VQ coded straight residue
  int begin;
  int end;

  // first stage (lossless partitioning)
  int grouping;                   // group n vectors per partition
  int partitions;                 // possible codebooks for a partition
  int groupbook;                  // huffbook for partitioning
  int[] secondstages=new int[64]; // expanded out to pointers in lookup
  int[] booklist=new int[256];    // list of second stage books

}
