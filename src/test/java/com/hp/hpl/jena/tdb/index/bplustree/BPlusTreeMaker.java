/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.index.bplustree;

import com.hp.hpl.jena.tdb.base.record.RecordLib;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.RangeIndex;
import com.hp.hpl.jena.tdb.index.RangeIndexMaker;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree;

public class BPlusTreeMaker implements RangeIndexMaker
{
    private int order ;
    private int recordOrder ;
    private boolean trackers ;


    public BPlusTreeMaker(int order, int recordOrder, boolean trackers)
    { 
        this.order = order ; 
        this.recordOrder = recordOrder ;
        this.trackers = trackers ;
    }
    
    @Override
    public Index makeIndex() { return makeRangeIndex() ; }

    @Override
    public RangeIndex makeRangeIndex()
    {
        BPlusTree bpTree = BPlusTree.makeMem(order, recordOrder, RecordLib.TestRecordLength, 0) ;
        if ( trackers )
            bpTree = BPlusTree.addTracking(bpTree) ;
        return bpTree ;
    }

    @Override
    public String getLabel() { return "B+Tree order = "+order ; } 

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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