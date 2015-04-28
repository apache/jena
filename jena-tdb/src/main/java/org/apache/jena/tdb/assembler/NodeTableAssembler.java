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

package org.apache.jena.tdb.assembler;

import static org.apache.jena.sparql.util.graph.GraphUtils.getAsStringValue ;
import static org.apache.jena.tdb.assembler.VocabTDB.pNodeIndex ;
import org.apache.jena.assembler.Assembler ;
import org.apache.jena.assembler.Mode ;
import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.assembler.exceptions.AssemblerException ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;

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
    
    //public NodeTableAssembler()                     { this.location = Location.create(".") ; }
    //public NodeTableAssembler(Location location)    { this.location = location ; }
    
    public NodeTableAssembler()                     { }
    
    @Override
    public NodeTable open(Assembler a, Resource root, Mode mode)
    {
        String location = getAsStringValue(root, pNodeIndex) ;
//        if ( location != null )
//            return NodeTableFactory.create(IndexBuilder.get(), Location.create(location)) ;
//        
//        String nodeIndex = getAsStringValue(root, pNodeIndex) ;
//        String nodeData = getAsStringValue(root, pNodeData) ;
//        
        throw new AssemblerException(root, "Split location index/data file not yet implemented") ; 
    }
}
