/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.btree;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.*;

import com.hp.hpl.jena.tdb.base.record.RecordFactory;


/** Configuration for a BTree */ 
final
public class BTreeParams
{
    // Global settings
    public static boolean CheckingBTree = false ;           // Check on exit of btree modifiying operations
    public static boolean CheckingNode = false ;            // Check within BtreeNode

    public static boolean DumpTree = false ;                // Dump the tree during top level logging 
    public static boolean Logging = true ;                  // Turn on/off logging the hard way

    /* The gap is extra space in a node - some books have node size as 2*N 
     * (often for the classic insertion algorithm where it' easier to implement
     * by inserting then splitting).
     */ 
    private static final int Gap            = 0 ;  
    //private static final int PtrLength      = 4 ;
    
    public static final int RootParent      = -2 ;
    public static final int NoParent        = -99 ;

    // Per instance settings
    /** Order of the BTree */
    final int order ;
    
    /** Record factory */
    final RecordFactory recordFactory ; 
//    /** Size of record entry */
//    final int recordLength ;
    
    // ---- Derived constants.
    /** Maximum number of keys per non-leaf block */
    final int MaxRecNonLeaf ; 

    /** Leaves have no pointers - maximum number of keys per leaf block */
    final int MaxRecLeaf ; 

    /** Maximum number of pointers per block */
    final int MaxPtr  ;

    /** Minimum number of keys per non-leaf block */
    final int MinRec  ;

    /** Minimum number of pointers per block */
    final int MinPtr  ;
    
    /** Index of the split point */
    final int SplitIndex ;
    /** High index of keys array (non-leaf) */
    final int HighRecNonLeaf ;
    /** High index of keys array - leaf*/
    final int HighRecLeaf ;
    /** High index of pointers array */
    final int HighPtr  ;
 
    /** Space in a block needed for extra information - the count
     * The parent is not stored on-disk because a block is always created by fetching from it's parent. 
     */
    static int BlockHeaderSize = 4 ;
    
    @Override
    public String toString()
    {
        return String.format("Order=%d : Record factory=%s : records=[%d,%d/%d] : pointers=[%d,%d] : split=%d",
                             order,
                             recordFactory,
                             MinRec, MaxRecNonLeaf, MaxRecLeaf,
                             MinPtr, MaxPtr,
                             SplitIndex
                             ) ;
    }

    
    public BTreeParams(int order, int keyLen, int valLen)
    { 
        this(order, new RecordFactory(keyLen, valLen)) ;
    }
    
    public BTreeParams(int order, RecordFactory factory)
    {
        // BTrees of order one aren't strictly BTrees, where the order is >= 2
        // Order 1 => Min size = 0 and max size = 2*N-1 = 1.
        // If there is a gap, then the code is defensive enough
        // and something will work.  The BTree may have empty nodes
        // (i.e. no keys, single child).

        if ( order < 2 )
            throw new IllegalArgumentException("BTree: illegal order (min 2): "+order);

        this.order = order ;
        recordFactory = factory ;

        // Derived constants.
        MaxRecNonLeaf = 2*order-1 + Gap ;
        MaxPtr  = 2*order + Gap ; 
        MinRec = order-1 ;
        MinPtr  = order ;
        
        SplitIndex = order-1+Gap;
        
        HighRecNonLeaf = MaxRecNonLeaf - 1 ;
        HighPtr  = MaxPtr - 1 ;
        
        // XXX Temporary - same as above.
        MaxRecLeaf = MaxRecNonLeaf ;
        HighRecLeaf = HighRecNonLeaf ;
    }

    public int getOrder()
    {
        return order ;
    }

    public static int getPtrLength()
    {
        return SizeOfPointer ;
    }

    public int getRecordLength()
    {
        return recordFactory.recordLength() ;
    }


    public RecordFactory getRecordFactory()
    {
        return recordFactory ;
    }

    public int getBlockSize()
    {
        // Min size.
        return calcBlockSize(order, recordFactory) ;
    }
    
    /** Return the best fit for the blocksize and the record length.
     * Knows about block header space. 
     */ 
    public static int calcOrder(int blockSize, RecordFactory factory) 
    {
        return calcOrder(blockSize, factory.recordLength()) ; 
    }
    
    /** Return the best fit for the blocksize and the record length.
     * Knows about block header space. 
     */ 
    public static int calcOrder(int blockSize, int recordLength) 
    {
        // Length = X*recordLength+(X+1)*PtrLength
        // => X = (Length-PtrLength)/(recordLength+PtrLength)
        // BTree order N
        // MaxRec = 2N-1+Gap = X
        // N = (X+1-Gap)/2
        blockSize -= BlockHeaderSize ;
        
        int X = (blockSize-recordLength)/(recordLength+SizeOfPointer) ;
        int order = (X+1-Gap)/2 ;
        return order ;
    }

    /** return the size of a block */
    public static int calcBlockSize(int bTreeOrder, RecordFactory factory) 
    {
        BTreeParams p = new BTreeParams(bTreeOrder, factory) ;
        int x = p.getMaxRecNonLeaf()*factory.recordLength() + p.getMaxPtr()*SizeOfPointer ;
        x += BlockHeaderSize ;
        return x ;
    }


    public int getMaxRecNonLeaf()
    {
        return MaxRecNonLeaf ;
    }


    public int getMaxRecLeaf()
    {
        return MaxRecLeaf ;
    }


    public int getMaxPtr()
    {
        return MaxPtr ;
    }


    public int getMinRec()
    {
        return MinRec;
    }

    public int getMinPtr()
    {
        return MinPtr ;
    }

//    /** return the size of a block */
//    public static int calcBlockSize(int bTreeOrder, int recordLength)
//    {
//        BTreeParams p = new BTreeParams(bTreeOrder, recordLength, 0) ;
//        int x = p.getMaxRec()*recordLength + p.getMaxPtr()*PtrLength ;
//        x += BlockHeaderSize ;
//        return x ;
//    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */