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

package org.seaborne.jena.engine.access;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.jena.engine.general.OpExecLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** Combine all three access forms */
public final class AccessorGraph extends AccessorBase<Node> implements Accessor<Node> {
    private final Graph graph ;

    public AccessorGraph(Graph graph)
    { this.graph = graph ; }
    
    // ---- AccessData
    @Override
    public Iterator<Tuple<Node>> accessTuples(Tuple<Node> pattern) {
        if ( pattern.size() != 3 )
            throw new ARQInternalErrorException("AccessorGraph.accessTuples : not a 3-tuple: "+pattern) ;
        ExtendedIterator<Triple> iter = graph.find(pattern.get(0), pattern.get(1), pattern.get(2)) ;
        return OpExecLib.convertTriplesToTuples(iter) ;
    }
}

