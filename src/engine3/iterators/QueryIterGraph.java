/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package engine3.iterators;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine.Binding;
import com.hp.hpl.jena.query.engine.Binding1;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.query.engine1.iterator.QueryIterConcat;
import com.hp.hpl.jena.query.engine2.OpSubstitute;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.engine2.op.OpGraph;

import engine3.QC;

public class QueryIterGraph extends QueryIterStream
{
    OpGraph opGraph ;
    
    public QueryIterGraph(QueryIterator input, OpGraph opGraph, ExecutionContext context)
    {
        super(input, context) ;
        this.opGraph = opGraph ;
    }
    
    protected QueryIterator nextStage(Binding binding)
    {
        Node graphURI = resolve(binding, opGraph.getNode()) ;
        if ( graphURI == null )
            return nextStageMultiple(binding) ;
        
        return nextStageSingle(binding, graphURI) ;
    }
        
    private QueryIterator nextStageSingle(Binding binding, Node graphURI)
    {
        if ( !graphURI.isURI() )
            return null ;
        ExecutionContext cxt = getExecContext() ;
        Op op = OpSubstitute.substitute(binding, opGraph.getSubOp()) ;
        Graph g = cxt.getDataset().getNamedGraph(graphURI.getURI()) ;
        if ( g == null )
            return null ;
        
        ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
        QueryIterator subInput = new QueryIterSingleton(binding, cxt) ;
        return QC.compile(op, subInput, cxt2) ;
    }
    
    private QueryIterator nextStageMultiple(Binding binding)
    {
        ExecutionContext cxt = getExecContext() ;
        Var graphVar = Var.alloc(opGraph.getNode()) ;
        QueryIterConcat q = new QueryIterConcat(cxt) ;
        for ( Iterator iter = cxt.getDataset().listNames() ; iter.hasNext() ; )
        {
            String uri =  (String)iter.next() ;
            Node gn = Node.createURI(uri) ;
            Binding b = new Binding1(binding, graphVar, gn) ;
            QueryIterator gIter = nextStageSingle(b, gn) ;
            if ( gIter != null )
                q.add(gIter) ;
        }
        return q ;
    }

    private static Node resolve(Binding b, Node n)
    {
//        if (  b == null )
//            return null ;
//
//        if ( n == null )
//            return n ;
        
        if ( ! n.isVariable() )
            return n ;

        return b.get(Var.alloc(n)) ;
    }

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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