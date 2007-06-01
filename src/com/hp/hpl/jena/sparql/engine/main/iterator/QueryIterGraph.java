/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.main.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpSubstitute;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIter;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.iterator.NullIterator;
import com.hp.hpl.jena.util.iterator.SingletonIterator;


public class QueryIterGraph extends QueryIterRepeatApply
{
    OpGraph opGraph ;
    
    public QueryIterGraph(QueryIterator input, OpGraph opGraph, ExecutionContext context)
    {
        super(input, context) ;
        this.opGraph = opGraph ;
    }
    
    protected QueryIterator nextStage(Binding outerBinding)
    {
        DatasetGraph ds = getExecContext().getDataset() ;
        Iterator graphNameNodes = makeSources(ds, outerBinding, opGraph.getNode());
        
        QueryIterator current = new QueryIterGraphInner(
                                               outerBinding, graphNameNodes, 
                                               opGraph, getExecContext()) ;
        return current ;
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

    // Iterator<Node>
    private static Iterator makeSources(DatasetGraph data, Binding b, Node graphVar)
    {
        Node n2 = resolve(b, graphVar) ;
        
        if ( n2 != null && ! n2.isURI() )
            // Bloank node or literal possible after resolving
            return new NullIterator() ;
        
        // n2 is a URI or null.
        
        if ( n2 == null )
            // Do all submodels.
            return data.listGraphNodes() ;
        return new SingletonIterator(n2) ;
    }
    

    private static class QueryIterGraphInner extends QueryIter
    {
        private Binding parentBinding ;
        private Iterator graphNames ;       // Names as Nodes
        private OpGraph opGraph ;
        private QueryIterator subIter = null ;

        public QueryIterGraphInner(Binding parent, Iterator graphNames, OpGraph opGraph, ExecutionContext execCxt)
        {
            super(execCxt) ;
            this.parentBinding = parent ;
            this.graphNames = graphNames ;
            this.opGraph = opGraph ;
        }

        protected boolean hasNextBinding()
        {
            for(;;)
            {
                if ( subIter == null )
                    subIter = nextIterator() ;
                
                if ( subIter == null )
                    return false ;
                
                if ( subIter.hasNext() )
                    return true ;
                
                subIter.close() ;
                subIter = nextIterator() ;
                if ( subIter == null )
                    return false ;
            }
        }

        protected Binding moveToNextBinding()
        {
            if ( subIter == null )
                throw new NoSuchElementException(Utils.className(this)+".moveToNextBinding") ;
                
            return subIter.nextBinding() ;
        }

        protected void closeIterator()
        {
            if ( subIter != null )
                subIter.close() ;
            subIter = null ;
        }
        
        // This is very like QueryIteratorRepeatApply except its not repeating over bindings
        // There is a tradeoff of generalising QueryIteratorRepeatApply to Objects
        // and hence no type safety. Or duplicating code (generics?)
        
        private QueryIterator nextIterator()
        {
            if ( ! graphNames.hasNext() )
                return null ;
            Node gn = (Node)graphNames.next() ;

            Graph g = getExecContext().getDataset().getGraph(gn) ;
            if ( g == null )
                return null ;
                
            // Create a new context with a different active graph
            ExecutionContext execCxt2 = new ExecutionContext(getExecContext(), g) ;
                
            Binding b = parentBinding ;
            if ( Var.isVar(opGraph.getNode()) ) 
                // Binding the variable (if "GRAPH ?var")
                b = new Binding1(b, Var.alloc(opGraph.getNode()), gn) ;
            
            QueryIterator qIter = buildIterator(b, gn, opGraph, execCxt2) ; 
            return qIter ;
        }
        

        private static QueryIterator buildIterator(Binding binding, Node graphNode, OpGraph opGraph, ExecutionContext cxt)
        {
            if ( !graphNode.isURI() )
                // e.g. variable bound to a literal or blank node.
                // Alow this?
                throw new ARQInternalErrorException("QueryIterGraphInner.buildIterator") ;
                //return null ;
            
            // TODO Think about avoiding substitution.
            // If the subpattern does not involve the vars from the binding, avoid the substitute.  
            Op op = OpSubstitute.substitute(opGraph.getSubOp(), binding) ;
            Graph g = cxt.getDataset().getGraph(graphNode) ;
            if ( g == null )
                return null ;
            
            ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
            QueryIterator subInput = new QueryIterSingleton(binding, cxt) ;
            return QC.compile(op, subInput, cxt2) ;
        }
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