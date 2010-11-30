/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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

/** This class provides the Jena Graph interface to a remove SPARQL endpoint.
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

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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