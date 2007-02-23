/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.engine1.iterator;

import java.util.* ;

import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.*;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.engine1.PlanElement;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.util.iterator.SingletonIterator;
import com.hp.hpl.jena.graph.* ;

/** Process one aggregate source.
 * 
 * @author Andy Seaborne
 * @version $Id: QueryIterNamedGraph.java,v 1.8 2007/02/06 17:06:03 andy_seaborne Exp $
 */

public class QueryIterNamedGraph extends QueryIterPlanRepeatApply
{
    Node sourceNode ;
    PlanElement subPattern ;
   
    public QueryIterNamedGraph(QueryIterator input,
                               Node _sourceNode,
                               ExecutionContext context,
                               PlanElement subComp)  
    {
        super(input, context) ;
        sourceNode = _sourceNode ;
        subPattern = subComp ;
    }
    
    //@Override
    protected QueryIterator nextStage(Binding outerBinding)
    {     
        DatasetGraph ds = getExecContext().getDataset() ;
        Iterator graphURIs = makeSources(ds, outerBinding, sourceNode);
        QueryIterator current = new QueryIterNamedGraphInner(
                                               outerBinding, sourceNode, graphURIs, 
                                               subPattern, getExecContext()) ;
        return current ;
    }
    
    
    private static Iterator makeSources(DatasetGraph data, Binding b, Node graphVar)
    {
        Node n2 = resolve(b, graphVar) ;
        
        if ( n2 != null && ! n2.isURI() )
        {
            return new NullIterator() ;
            //throw new QueryExecException("GRAPH is not a URI: "+n2) ;
        }
        
        // n2 is a URI or null.
        
        if ( n2 == null )
            // Do all submodels.
            return data.listNames() ;
        return new SingletonIterator(n2.getURI()) ;
    }

    private static Node resolve(Binding b, Node n)
    {
        if ( n == null )
            return n ;
        
        if ( ! n.isVariable() )
            return n ;
        
        if (  b == null )
            return null ;
        
        return b.get(Var.alloc(n)) ;
    }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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