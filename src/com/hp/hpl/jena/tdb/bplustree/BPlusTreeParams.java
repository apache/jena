/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.bplustree;

import static com.hp.hpl.jena.tdb.Const.* ;
import org.slf4j.Logger;

import com.hp.hpl.jena.tdb.base.record.RecordFactory;

/** Configuration for a B+Tree */ 
final
public class BPlusTreeParams
{
    // Global settings
    public static boolean CheckingTree = false ;           // Check on exit of B+Tree modifiying operations
    public static boolean CheckingNode = false ;            // Check within BtreeNode

    public static void checkAll()
    { 
        CheckingTree = true ;
        CheckingNode = true ;
    }
    
    public static boolean DumpTree = false ;                 // Dump the tree during top level logging 
    public static boolean Logging = false ;                  // Turn on/off logging the hard way

    /* The gap is extra space in a node - some books have node size as 2*N 
     * (often for the classic insertion algorithm where it's easier to implement
     * by inserting then splitting).
     */ 
    private static final int Gap            = 0 ;  
    
    public static final int RootParent      = -2 ;
    public static final int NoParent        = -99 ;

    // Per instance settings
    /** Order of the BTree */
    final int order ;
    
    /** Record factory */
    final RecordFactory recordFactory ;

    /** Factory for key-only records */ 
    final RecordFactory keyFactory ;
    
    // ---- Derived constants.

    /** Maximum number of keys */
    final int MaxRec ; 

    /** Maximum number of pointers per block */
    final int MaxPtr  ;

    /** Minimum number of keys per non-leaf block */
    final int MinRec  ;

    /** Minimum number of pointers per block */
    final int MinPtr  ;
    
    /** Index of the split point */
    final int SplitIndex ;

    /** High index of keys array */
    final int HighRec ;
    
    /** High index of pointers array */
    final int HighPtr  ;
 
    /** Space in a block needed for extra information - the count
     * The parent is not stored on-disk because a block is always created by fetching from it's parent. 
     */
    static int BlockHeaderSize = 4 ;
    
    static final boolean logging(Logger log)
    {
        return Logging && log.isDebugEnabled() ;
    }
    
    @Override
    public String toString()
    {
        return String.format("Order=%d : Records [key=%d, value=%d] : records=[%d,%d] : pointers=[%d,%d] : split=%d",
                             order,
                             keyFactory.keyLength() ,
                             recordFactory.valueLength() ,
                             MinRec, MaxRec, 
                             MinPtr, MaxPtr,
                             SplitIndex
                             ) ;
    }

    
    public BPlusTreeParams(int N, int keyLen, int valLen)
    { 
        this(N, new RecordFactory(keyLen, valLen)) ;
    }
    
    public BPlusTreeParams(int N, RecordFactory factory)
    {
        // BTrees of order one aren't strictly BTrees, where the order is >= 2
        // Order 1 => Min size = 0 and max size = 2*N-1 = 1.
        // If there is a gap, then the code may be defensive enough
        // and something will work.  The B+Trees may have empty nodes
        // (i.e. no keys, single child).

        if ( N < 2 )
            throw new IllegalArgumentException("BPTree: illegal order (min 2): "+N);

        order = N ;
        recordFactory = factory ;
        keyFactory = factory.keyFactory() ;

        // Derived constants.
        MaxRec  = 2*N-1 + Gap ;
        MaxPtr  = 2*N + Gap ; 
        MinRec = N-1 ;
        MinPtr  = N ;
        
        SplitIndex = N-1+Gap;
        
        HighPtr  = MaxPtr - 1 ;
        HighRec  = HighPtr-1 ;
    }

    public int getOrder()
    {
        return order ;
    }

    public int getPtrLength()
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
    
    public int getKeyLength()
    {
        return keyFactory.recordLength() ;
    }

    public RecordFactory getKeyFactory()
    {
        return keyFactory ;
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
        int N = (X+1-Gap)/2 ;
        return N ;
    }

    /** return the size of a block */
    public static int calcBlockSize(int bTreeOrder, RecordFactory factory) 
    {
        BPlusTreeParams p = new BPlusTreeParams(bTreeOrder, factory) ;
        int x = p.getMaxRec()*factory.recordLength() + p.getMaxPtr()*SizeOfPointer ;
        x += BlockHeaderSize ;
        return x ;
    }


    public int getMaxRec()
    {
        return MaxRec ;
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
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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