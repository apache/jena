/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.engine.tdb;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.jena.engine.Slot ;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;

/** Access to a specific index with prefix (cols to skip) and a variable. */
public class IndexAccess
{
    // Accessor<NodeId>??
    private final Tuple<Slot<NodeId>> pattern ;
    private final TupleIndex index ;
    private final int prefixLen ;
    private final Var var ;

    public IndexAccess(Tuple<Slot<NodeId>> pattern, TupleIndex index, int prefixLen, Var var)
    {
        this.pattern = pattern ;
        this.index = index ;
        this.prefixLen = prefixLen ;
        this.var = var ;
    }

    public int getPrefixLen()       { return prefixLen ; }

    public TupleIndex getIndex()    { return index ; } 

    public Var getVar()             { return var ; }
    
    public Tuple<Slot<NodeId>> getPattern()             { return pattern ; }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder() ;
        builder.append("[")
               .append(pattern)
               .append(" - ")
               .append(index.getName())
               .append("/")
               .append(index.getName().substring(0, prefixLen))
               .append("->")
               .append(var)
               .append("]") ;
        return builder.toString() ;
    }
}
