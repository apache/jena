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

package com.hp.hpl.jena.tdb.base.block;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;

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
