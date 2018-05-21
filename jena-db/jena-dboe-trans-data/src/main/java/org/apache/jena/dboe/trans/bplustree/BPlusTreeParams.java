/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.dboe.trans.bplustree;

import static org.apache.jena.dboe.sys.SystemIndex.SizeOfPointer;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.base.file.MetaFile;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.sys.Names;

/** Configuration for a B+Tree */ 
final public class BPlusTreeParams
{
    // Metadata
    //public static final String NS = BPlusTreeParams.class.getName() ;
    public static final String NS                  = Names.keyNSBPlusTree ;
    public static final String ParamOrder          = NS+".order" ;
    public static final String ParamKeyLength      = NS+".keyLength" ;
    public static final String ParamValueLength    = NS+".valueLength" ;
    public static final String ParamBlockSize      = NS+".blockSize" ;

    /* The gap is extra space in a node - some books have node size as 2*N 
     * (often for the classic insertion algorithm where it's easier to implement
     * by inserting then splitting).
     */ 
    private static final int Gap            = 0 ;  
    
    public static final int RootId          = 0 ;
    public static final int RootParent      = -2 ;
    public static final int NoParent        = -99 ;
    // Used when getting a block and we don't need/want/know the parent.
    public static final int UnsetParent     = -98 ;

    // Per instance settings
    
    /** Order of the BTree */
    final int order ;
    
    /** Record factory */
    final RecordFactory recordFactory ;

    /** Factory for key-only records */ 
    final RecordFactory keyFactory ;
    
    // ---- Derived constants.

    /** Maximum number of keys per non-leaf block */
    final int MaxRec ; 

    /** Maximum number of pointers per block per non-leaf block */
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
    
    @Override
    public String toString() {
        return String.format("Order=%d : Records [key=%d, value=%d] : records=[%d,%d] : pointers=[%d,%d] : split=%d",
                             order,
                             keyFactory.keyLength() ,
                             recordFactory.valueLength() ,
                             MinRec, MaxRec, 
                             MinPtr, MaxPtr,
                             SplitIndex
                             ) ;
    }

    public static BPlusTreeParams readMeta(MetaFile mf) {
        try {
            int pOrder = mf.getPropertyAsInteger(ParamOrder) ;
            int pKeyLen = mf.getPropertyAsInteger(ParamKeyLength) ;
            int pRecLen = mf.getPropertyAsInteger(ParamValueLength) ;
            return new BPlusTreeParams(pOrder, pKeyLen, pRecLen) ;
        } catch (NumberFormatException ex)
        {
            Log.error(BPlusTreeParams.class, "Badly formed metadata for B+Tree") ;
            throw new DBOpEnvException("Failed to read metadata") ;
        }
    }
    
    public void addToMetaData(MetaFile mf) {
        mf.setProperty(ParamOrder, order) ;
        mf.setProperty(ParamKeyLength, recordFactory.keyLength()) ;
        mf.setProperty(ParamValueLength, recordFactory.valueLength()) ;
        mf.flush() ;
    }

    public BPlusTreeParams(int order, int keyLen, int valLen) { 
        this(order, new RecordFactory(keyLen, valLen)) ;
    }
    
    public BPlusTreeParams(int order, RecordFactory factory) {
        // BTrees of order one aren't strictly BTrees
        // Order 1 => Min size = 0 and max size = 2*N-1 = 1.
        // If there is a gap, then the code may be defensive enough
        // and something will work.  The B+Trees may have empty nodes
        // (i.e. no keys, single child).

        if ( order < 2 )
            throw new IllegalArgumentException("BPTree: illegal order (min 2): "+order);

        this.order = order ;
        recordFactory = factory ;
        keyFactory = factory.keyFactory() ;

        // Derived constants.
        MaxRec  = 2*order-1 + Gap ;
        MaxPtr  = 2*order + Gap ; 
        MinRec = order-1 ;
        MinPtr  = order ;
        
        SplitIndex = order-1+Gap;
        
        HighPtr  = MaxPtr - 1 ;
        HighRec  = HighPtr-1 ;
    }

    public int getOrder() {
        return order ;
    }

    public int getPtrLength() {
        return SizeOfPointer ;
    }

    public int getRecordLength() {
        return recordFactory.recordLength() ;
    }

    public RecordFactory getRecordFactory(){
        return recordFactory ;
    }

    public int getKeyLength() {
        return keyFactory.recordLength() ;
    }

    public RecordFactory getKeyFactory() {
        return keyFactory ;
    }

    public int getCalcBlockSize() {
        return calcBlockSize(order, recordFactory) ;
    }

    /**
     * Return the best fit for the blocksize and the record length. Knows about
     * block header space.
     */
    public static int calcOrder(int blockSize, RecordFactory factory) {
        return calcOrder(blockSize, factory.recordLength()) ;
    }

    /**
     * Return the best fit for the blocksize and the record length. Knows about
     * block header space.
     */
    public static int calcOrder(int blockSize, int recordLength)    {
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
    public static int calcBlockSize(int bpTreeOrder, RecordFactory factory) {
        BPlusTreeParams p = new BPlusTreeParams(bpTreeOrder, factory) ;
        int x = p.getMaxRec() * factory.recordLength() + p.getMaxPtr() * SizeOfPointer ;
        x += BlockHeaderSize ;
        return x ;
    }

    public int getMaxRec() {
        return MaxRec ;
    }

    public int getMaxPtr() {
        return MaxPtr ;
    }

    public int getMinRec() {
        return MinRec ;
    }

    public int getMinPtr() {
        return MinPtr ;
    }

//    /** return the size of a block */
//    public static int calcBlockSize(int bTreeOrder, int recordLength) {
//        BTreeParams p = new BTreeParams(bTreeOrder, recordLength, 0) ;
//        int x = p.getMaxRec()*recordLength + p.getMaxPtr()*PtrLength ;
//        x += BlockHeaderSize ;
//        return x ;
//    }
}
