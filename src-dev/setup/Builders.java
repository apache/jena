/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package setup;

import org.openjena.atlas.lib.ColumnMap ;

import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;

public class Builders
{

//    interface DatasetGraphBuilder {
//        DatasetGraphTDB build(Location location) ;
//    }
//    
//    interface TableBuilder {
//        // (TupleIndex[] indexes, NodeTable nodeTable)
//        TripleTable buildTripleTable(Location location) ;
//        QuadTable buildQuadTable(Location location) ;
//    }
//    
    interface NodeTupleTableBuilder {
        NodeTupleTable buildNodeTupleTable(int N, TupleIndex[] indexes, NodeTable nodeTable) ;
    }
    
    interface NodeTableBuilder {
        NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, int sizeNode2NodeIdCache, int sizeNodeId2NodeCache) ;
    }

    interface TupleIndexBuilder {
        TupleIndex buildTupleIndex(FileSet fileSet, ColumnMap colMap) ;
    }
    
    interface RangeIndexBuilder {
        RangeIndex buildRangeIndex(FileSet fileSet, RecordFactory recordfactory) ;
    }
    
    interface IndexBuilder {
        Index buildIndex(FileSet fileSet, RecordFactory recordfactory) ;
    }
    
    interface BlockMgrBuilder {
        // ?? FileSet fileSet
        BlockMgr buildBlockMgr(Location location, String name) ;
    }
    
    interface ObjectFileBuilder {
        ObjectFile buildObjectFile(FileSet fileSet, String ext) ;
    }
}

/*
 * (c) Copyright 2011 Epimorphics Ltd.
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