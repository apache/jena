/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import org.openjena.atlas.io.IndentedWriter ;
import org.openjena.atlas.io.Printable ;

import com.hp.hpl.jena.sparql.util.Named ;
import com.hp.hpl.jena.tdb.TDBException ;

public enum BlockType implements Printable, Named
{
    // The id should fit into an unsigned byte.
    FREE(-1, "Free"), 
    BTREE_NODE(5, "BTreeNode") ,
    BPTREE_BRANCH(6, "BPlusTreeBranch") ,
    BPTREE_LEAF(7, "BPlusTreeLeaf") ,
    DICTIONARY(10, "Dictionary") ,
    RECORD_BLOCK(99, "RecordBlock"),
    
    // [TxTDB:PATCH-UP]
    UNDEF(-2, "UndefinedBlockType")
    ;

    private final int id ;
    private final String name ;

    BlockType(int id, String name)
    {
        this.id = id ;
        this.name = name ;
        
    }
    
    @Override
    public void output(IndentedWriter out)
    { out.print(getName()) ; }

    final public int id() { return id ; }
    
    @Override
    final public String getName() { return name ; }
    
    @Override public String toString() { return getName() ; }
    
    public static BlockType extract(int x)
    {
        if ( x == BTREE_NODE.id() )         return BTREE_NODE ;
        if ( x == BPTREE_BRANCH.id() )      return BPTREE_BRANCH ;
        if ( x == BPTREE_LEAF.id() )        return BPTREE_LEAF ;
        if ( x == RECORD_BLOCK.id() )       return RECORD_BLOCK ;
        if ( x == DICTIONARY.id() )         return DICTIONARY ;
        throw new TDBException("No known block type for "+x) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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