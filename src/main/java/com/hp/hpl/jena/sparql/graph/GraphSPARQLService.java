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

package com.hp.hpl.jena.sparql.graph;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** This class provides the Jena Graph interface to a remote SPARQL endpoint.
 *  Efficiency not guaranteed. */

public class GraphSPARQLService extends GraphBase implements Graph
{
    private static Logger log = LoggerFactory.getLogger(GraphSPARQLService.class) ;

    private String serviceURI ;
    private String graphIRI = null ;
    
    // Remote default graph
    public GraphSPARQLService(String serviceURI)
    {  
        this.serviceURI = serviceURI ;
        this.graphIRI = null ;
    }
    
    // Remote named graph
    public GraphSPARQLService(String serviceURI, String graphIRI)
    {  
        this.serviceURI = serviceURI ;
        this.graphIRI = graphIRI ;
    }

//    @Override
//    public Capabilities getCapabilities()
//    { 
//    	if (capabilities == null)
//            capabilities = new AllCapabilities()
//        	  { @Override public boolean handlesLiteralTyping() { return false; } };
//        return capabilities;
//    }
    
//    @SuppressWarnings("null")
//    @Override
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
    {
        Node s = m.getMatchSubject() ;
        Var sVar = null ;
        if ( s == null )
        {
            sVar = Var.alloc("s") ;
            s = sVar ;
        }
        
        Node p = m.getMatchPredicate() ;
        Var pVar = null ;
        if ( p == null )
        {
            pVar = Var.alloc("p") ;
            p = pVar ;
        }
        
        Node o = m.getMatchObject() ;
        Var oVar = null ;
        if ( o == null )
        {
            oVar = Var.alloc("o") ;
            o = oVar ;
        }
        
        Triple triple = new Triple(s, p ,o) ;
        
        // Evaluate as an algebra expression
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(triple) ;
        Op op = new OpBGP(pattern) ;
        
//        // Make remote execution object. 
//        System.err.println("GraphSPARQLService.graphBaseFind: Unimplemented : remote service execution") ;
//        //Plan plan = factory.create(op, getDataset(), BindingRoot.create(), null) ;
//
//        QueryIterator qIter = plan.iterator() ;
//        List<Triple> triples = new ArrayList<Triple>() ;
//        
//        
//        for (; qIter.hasNext() ; )
//        {
//            Binding b = qIter.nextBinding() ;
//            Node sResult = s ;
//            Node pResult = p ;
//            Node oResult = o ;
//            if ( sVar != null )
//                sResult = b.get(sVar) ;
//            if ( pVar != null )
//                pResult = b.get(pVar) ;
//            if ( oVar != null )
//                oResult = b.get(oVar) ;
//            Triple resultTriple = new Triple(sResult, pResult, oResult) ;
//            if ( log.isDebugEnabled() )
//                log.debug("  "+resultTriple) ;
//            triples.add(resultTriple) ;
//        }
//        qIter.close() ;
//        return WrappedIterator.createNoRemove(triples.iterator()) ;
        return null ;
    }

}
