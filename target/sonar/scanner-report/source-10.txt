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

public class DspState{
  static final float M_PI=3.1415926539f;
  static final int VI_TRANSFORMB=1;
  static final int VI_WINDOWB=1;

  Info vi;
  int modebits;

  float[][] pcm;
  int      pcm_storage;
  int      pcm_current;
  int      pcm_returned;

  int lW;
  int W;
  int centerW;

  long granulepos;
  long sequence;

  float[][][][][] window;                 // block, leadin, leadout, type
  Object[][] transform;
  CodeBook[] fullbooks;
  Object[] mode;

  public DspState(){
    transform=new Object[2][];
    window=new float[2][][][][];
    window[0]=new float[2][][][];
    window[0][0]=new float[2][][];
    window[0][1]=new float[2][][];
    window[0][0][0]=new float[2][];
    window[0][0][1]=new float[2][];
    window[0][1][0]=new float[2][];
    window[0][1][1]=new float[2][];
    window[1]=new float[2][][][];
    window[1][0]=new float[2][][];
    window[1][1]=new float[2][][];
    window[1][0][0]=new float[2][];
    window[1][0][1]=new float[2][];
    window[1][1][0]=new float[2][];
    window[1][1][1]=new float[2][];
  }

  private static int ilog2(int v){
    int ret=0;
    while(v>1){
      ret++;
      v>>>=1;
    }
    return(ret);
  }

  static float[] window(int type, int window, int left, int right){
    float[] ret=new float[window];
    switch (type){
    case 0:
      // The 'vorbis window' (window 0) is sin(sin(x)*sin(x)*2pi)
      {
	int leftbegin=window/4-left/2;
	int rightbegin=window-window/4-right/2;
    
	for(int i=0;i<left;i++){
	  float x=(float)((i+.5)/left*M_PI/2.);
	  x=(float)Math.sin(x);
	  x*=x;
	  x*=M_PI/2.;
	  x=(float)Math.sin(x);
	  ret[i+leftbegin]=x;
	}
      
	for(int i=leftbegin+left;i<rightbegin;i++){
	  ret[i]=1.f;
	}
      
	for(int i=0;i<right;i++){
	  float x=(float)((right-i-.5)/right*M_PI/2.);
	  x=(float)Math.sin(x);
	  x*=x;
	  x*=M_PI/2.;
	  x=(float)Math.sin(x);
	  ret[i+rightbegin]=x;
	}
      }
      break;
    default:
      return(null);
    }
    return(ret);
  }

  // Analysis side code, but directly related to blocking.  Thus it's
  // here and not in analysis.c (which is for analysis transforms only).
  // The init is here because some of it is shared

  int init(Info vi){
    this.vi=vi;
    modebits=ilog2(vi.modes);

    transform[0]=new Object[VI_TRANSFORMB];
    transform[1]=new Object[VI_TRANSFORMB];

    // MDCT is tranform 0

    transform[0][0]=new Mdct();
    transform[1][0]=new Mdct();
    ((Mdct)transform[0][0]).init(vi.blocksizes[0]);
    ((Mdct)transform[1][0]).init(vi.blocksizes[1]);

    window[0][0][0]=new float[VI_WINDOWB][];
    window[0][0][1]=window[0][0][0];
    window[0][1][0]=window[0][0][0];
    window[0][1][1]=window[0][0][0];
    window[1][0][0]=new float[VI_WINDOWB][];
    window[1][0][1]=new float[VI_WINDOWB][];
    window[1][1][0]=new float[VI_WINDOWB][];
    window[1][1][1]=new float[VI_WINDOWB][];

    for(int i=0;i<VI_WINDOWB;i++){
      window[0][0][0][i]=
	window(i,vi.blocksizes[0],vi.blocksizes[0]/2,vi.blocksizes[0]/2);
      window[1][0][0][i]=
	window(i,vi.blocksizes[1],vi.blocksizes[0]/2,vi.blocksizes[0]/2);
      window[1][0][1][i]=
	window(i,vi.blocksizes[1],vi.blocksizes[0]/2,vi.blocksizes[1]/2);
      window[1][1][0][i]=
	window(i,vi.blocksizes[1],vi.blocksizes[1]/2,vi.blocksizes[0]/2);
      window[1][1][1][i]=
	window(i,vi.blocksizes[1],vi.blocksizes[1]/2,vi.blocksizes[1]/2);
    }

//    if(encp){ // encode/decode differ here
//      // finish the codebooks
//      fullbooks=new CodeBook[vi.books];
//      for(int i=0;i<vi.books;i++){
//	fullbooks[i]=new CodeBook();
//	fullbooks[i].init_encode(vi.book_param[i]);
//      }
//      analysisp=1;
//    }
//    else{
      // finish the codebooks
      fullbooks=new CodeBook[vi.books];
      for(int i=0;i<vi.books;i++){
	fullbooks[i]=new CodeBook();
	fullbooks[i].init_decode(vi.book_param[i]);
      }
//    }

    // initialize the storage vectors to a decent size greater than the
    // minimum
  
    pcm_storage=8192; // we'll assume later that we have
                        // a minimum of twice the blocksize of
			// accumulated samples in analysis
    pcm=new float[vi.channels][];
    //pcmret=new float[vi.channels][];
    {
      for(int i=0;i<vi.channels;i++){
	pcm[i]=new float[pcm_storage];
      }
    }

    // all 1 (large block) or 0 (small block)
    // explicitly set for the sake of clarity
    lW=0; // previous window size
    W=0;  // current window size

    // all vector indexes; multiples of samples_per_envelope_step
    centerW=vi.blocksizes[1]/2;

    pcm_current=centerW;

    // initialize all the mapping/backend lookups
    mode=new Object[vi.modes];
    for(int i=0;i<vi.modes;i++){
      int mapnum=vi.mode_param[i].mapping;
      int maptype=vi.map_type[mapnum];
      mode[i]=FuncMapping.mapping_P[maptype].look(this,vi.mode_param[i], 
						  vi.map_param[mapnum]);
    }
    return(0);
  }

  DspState(Info vi){
    this();
    init(vi);
    // Adjust centerW to allow an easier mechanism for determining output
    pcm_returned=centerW;
    centerW-= vi.blocksizes[W]/4+vi.blocksizes[lW]/4;
    granulepos=-1;
    sequence=-1;
  }

  // Unike in analysis, the window is only partially applied for each
  // block.  The time domain envelope is not yet handled at the point of
  // calling (as it relies on the previous block).

  public void clear(){
/*
    if(window[0][0][0]!=0){
      for(i=0;i<VI_WINDOWB;i++)
	if(v->window[0][0][0][i])free(v->window[0][0][0][i]);
      free(v->window[0][0][0]);

      for(j=0;j<2;j++)
	for(k=0;k<2;k++){
	  for(i=0;i<VI_WINDOWB;i++)
	    if(v->window[1][j][k][i])free(v->window[1][j][k][i]);
	  free(v->window[1][j][k]);
	}
    }
    
    if(v->pcm){
      for(i=0;i<vi->channels;i++)
	if(v->pcm[i])free(v->pcm[i]);
      free(v->pcm);
      if(v->pcmret)free(v->pcmret);
    }
    if(v->multipliers)free(v->multipliers);

    _ve_envelope_clear(&v->ve);
    if(v->transform[0]){
      mdct_clear(v->transform[0][0]);
      free(v->transform[0][0]);
      free(v->transform[0]);
    }
    if(v->transform[1]){
      mdct_clear(v->transform[1][0]);
      free(v->transform[1][0]);
      free(v->transform[1]);
    }

    // free mode lookups; these are actually vorbis_look_mapping structs
    if(vi){
      for(i=0;i<vi->modes;i++){
	int mapnum=vi->mode_param[i]->mapping;
	int maptype=vi->map_type[mapnum];
	_mapping_P[maptype]->free_look(v->mode[i]);
      }
      // free codebooks
      for(i=0;i<vi->books;i++)
	vorbis_book_clear(v->fullbooks+i);
    }

    if(v->mode)free(v->mode);    
    if(v->fullbooks)free(v->fullbooks);

    // free header, header1, header2
    if(v->header)free(v->header);
    if(v->header1)free(v->header1);
    if(v->header2)free(v->header2);

    memset(v,0,sizeof(vorbis_dsp_state));
  }
*/
}
}
