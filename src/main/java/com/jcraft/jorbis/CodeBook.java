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

class CodeBook{
    int dim;            // codebook dimensions (elements per vector)
    int entries;        // codebook entries
    StaticCodeBook c=new StaticCodeBook();

    float[] valuelist; // list of dim*entries actual entry values
    DecodeAux decode_tree;

    void clear() {}


    int init_decode(StaticCodeBook s){
        c=s;
        entries=s.entries;
        dim=s.dim;
        valuelist=s.unquantize();

        decode_tree=make_decode_tree();
        if(decode_tree==null){
            clear();
            return(-1);
        }
        return(0);
    }

    // given a list of word lengths, generate a list of codewords.  Works
    // for length ordered or unordered, always assigns the lowest valued
    // codewords first.  Extended to handle unused entries (length 0)
    static int[] make_words(int[] l, int n){
        int[] marker=new int[33];
        int[] r=new int[n];

        for(int i=0;i<n;i++){
            int length=l[i];
            if(length>0){
                int entry=marker[length];

                // when we claim a node for an entry, we also claim the nodes
                // below it (pruning off the imagined tree that may have dangled
                // from it) as well as blocking the use of any nodes directly
                // above for leaves

                // update ourself
                if(length<32 && (entry>>>length)!=0){
                    // error condition; the lengths must specify an overpopulated tree
                    return(null);
                }
                r[i]=entry;

                // Look to see if the next shorter marker points to the node
                // above. if so, update it and repeat.
                {
                    for(int j=length;j>0;j--){
                        if((marker[j]&1)!=0){
                            // have to jump branches
                            if(j==1)marker[1]++;
                            else marker[j]=marker[j-1]<<1;
                            break; // invariant says next upper marker would already
                            // have been moved if it was on the same path
                        }
                        marker[j]++;
                    }
                }

                // prune the tree; the implicit invariant says all the longer
                // markers were dangling from our just-taken node.  Dangle them
                // from our *new* node.
                for(int j=length+1;j<33;j++){
                    if((marker[j]>>>1) == entry){
                        entry=marker[j];
                        marker[j]=marker[j-1]<<1;
                    }
                    else{
                        break;
                    }
                }
            }
        }

        // bitreverse the words because our bitwise packer/unpacker is LSb
        // endian
        for(int i=0;i<n;i++){
            int temp=0;
            for(int j=0;j<l[i];j++){
                temp<<=1;
                temp|=(r[i]>>>j)&1;
            }
            r[i]=temp;
        }

        return(r);
    }

    // build the decode helper tree from the codewords
    DecodeAux make_decode_tree(){
        int top=0;
        DecodeAux t=new DecodeAux();
        int[] ptr0=t.ptr0=new int[entries*2];
        int[] ptr1=t.ptr1=new int[entries*2];
        int[] codelist=make_words(c.lengthlist, c.entries);

        if(codelist==null)return(null);
        t.aux=entries*2;

        for(int i=0;i<entries;i++){
            if(c.lengthlist[i]>0){
                int ptr=0;
                int j;
                for(j=0;j<c.lengthlist[i]-1;j++){
                    int bit=(codelist[i]>>>j)&1;
                    if(bit==0){
                        if(ptr0[ptr]==0){
                            ptr0[ptr]=++top;
                        }
                        ptr=ptr0[ptr];
                    }
                    else{
                        if(ptr1[ptr]==0){
                            ptr1[ptr]= ++top;
                        }
                        ptr=ptr1[ptr];
                    }
                }

                if(((codelist[i]>>>j)&1)==0){ ptr0[ptr]=-i; }
                else{ ptr1[ptr]=-i; }

            }
        }

        t.tabn = ilog(entries)-4;

        if(t.tabn<5)t.tabn=5;
        int n = 1<<t.tabn;
        t.tab = new int[n];
        t.tabl = new int[n];
        for(int i = 0; i < n; i++){
            int p = 0;
            int j=0;
            for(j = 0; j < t.tabn && (p > 0 || j == 0); j++){
                if ((i&(1<<j))!=0){
                    p = ptr1[p];
                }
                else{
                    p = ptr0[p];
                }
            }
            t.tab[i]=p;  // -code
            t.tabl[i]=j; // length
        }

        return(t);
    }

    private static int ilog(int v){
        int ret=0;
        while(v!=0){
            ret++;
            v>>>=1;
        }
        return(ret);
    }

}

class DecodeAux{
    int[] tab;
    int[] tabl;
    int tabn;

    int[] ptr0;
    int[] ptr1;
    int   aux;        // number of tree entries
}
