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

package com.hp.hpl.jena.tdb.assembler;

import static com.hp.hpl.jena.sparql.util.graph.GraphUtils.getAsStringValue;
import static com.hp.hpl.jena.tdb.assembler.VocabTDB.pNodeIndex;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

public class NodeTableAssembler extends AssemblerBase //implements Assembler
{
    // ???
    /* 
     * [ :location "...." ] 
     * or (TBD)
     * [ :nodeIndex "..." ;
     *   :nodeData "..." ;
     * ]
     */
    

    //private Location location = null ;
    
    //public NodeTableAssembler()                     { this.location = new Location(".") ; }
    //public NodeTableAssembler(Location location)    { this.location = location ; }
    
    public NodeTableAssembler()                     { }
    
    @Override
    public NodeTable open(Assembler a, Resource root, Mode mode)
    {
        String location = getAsStringValue(root, pNodeIndex) ;
//        if ( location != null )
//            return NodeTableFactory.create(IndexBuilder.get(), new Location(location)) ;
//        
//        String nodeIndex = getAsStringValue(root, pNodeIndex) ;
//        String nodeData = getAsStringValue(root, pNodeData) ;
//        
        throw new AssemblerException(root, "Split location index/data file not yet implemented") ; 
    }
}
