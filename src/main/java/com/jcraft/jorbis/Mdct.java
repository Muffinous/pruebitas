
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

class Mdct{

    int n;
    int log2n;

    float[] trig;
    int[] bitrev;

    float scale;

    void init(int n){
        bitrev=new int[n/4];
        trig=new float[n+n/4];

        log2n=(int)Math.rint(Math.log(n)/Math.log(2));
        this.n=n;


        int AE=0;
        int AO=1;
        int BE=AE+n/2;
        int BO=BE+1;
        int CE=BE+n/2;
        int CO=CE+1;
        // trig lookups...
        for(int i=0;i<n/4;i++){
            trig[AE+i*2]=(float)Math.cos((Math.PI/n)*(4*i));
            trig[AO+i*2]=(float)-Math.sin((Math.PI/n)*(4*i));
            trig[BE+i*2]=(float)Math.cos((Math.PI/(2*n))*(2*i+1));
            trig[BO+i*2]=(float)Math.sin((Math.PI/(2*n))*(2*i+1));
        }
        for(int i=0;i<n/8;i++){
            trig[CE+i*2]=(float)Math.cos((Math.PI/n)*(4*i+2));
            trig[CO+i*2]=(float)-Math.sin((Math.PI/n)*(4*i+2));
        }

        {
            int mask=(1<<(log2n-1))-1;
            int msb=1<<(log2n-2);
            for(int i=0;i<n/8;i++){
                int acc=0;
                for(int j=0;msb>>>j!=0;j++)
                    if(((msb>>>j)&i)!=0)acc|=1<<j;
                bitrev[i*2]=((~acc)&mask);
                bitrev[i*2+1]=acc;
            }
        }
        scale=4.f/n;
    }

}