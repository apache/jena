/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.base.block;

import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.Named;
import com.hp.hpl.jena.sparql.util.Printable;
import com.hp.hpl.jena.tdb.TDBException;

public enum BlockType implements Printable, Named
{
    // The id should fit into an unsigned byte.
    BTREE_NODE
    {
        @Override public String getName()   { return "BTreeNode" ; }
        @Override public int id()           { return 5 ; }
    } ,
    
    BPTREE_BRANCH
    {
        @Override public String getName()   { return "BPlusTreeBranch" ; }
        @Override public int id()           { return 6 ; }
    } ,

    BPTREE_LEAF
    {
        @Override public String getName()   { return "BPlusTreeLeaf" ; }
        @Override public int id()           { return 7 ; }
    } ,

    DICTIONARY
    {
        @Override public String getName()   { return "Dictionary" ; }
        @Override public int id()           { return 10 ; }
    } ,
    
    RECORD_BLOCK
    {
        @Override public String getName()   { return "RecordBlock" ; }
        @Override public int id()           { return 99 ; }
    }
    ;

    public void output(IndentedWriter out)
    { out.print(getName()) ; }

    abstract public int id() ;
    abstract public String getName() ;
    
    @Override public String toString() { return getName() ; }
    
    public static BlockType extract(int x)
    {
        if ( x == BTREE_NODE.id() )       return BTREE_NODE ;
        if ( x == BPTREE_BRANCH.id() )      return BPTREE_BRANCH ;
        if ( x == BPTREE_LEAF.id() )        return BPTREE_LEAF ;
        if ( x == RECORD_BLOCK.id() )       return RECORD_BLOCK ;
        if ( x == DICTIONARY.id() )         return DICTIONARY ;
        throw new TDBException("No known block type for "+x) ;
    }
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