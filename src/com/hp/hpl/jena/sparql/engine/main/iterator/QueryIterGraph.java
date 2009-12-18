/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
import com.hp.hpl.jena.sparql.lib.iterator.NullIterator;
import com.hp.hpl.jena.sparql.lib.iterator.SingletonIterator;


public class QueryIterGraph extends QueryIterRepeatApply
{
    protected OpGraph opGraph ;
    
    public QueryIterGraph(QueryIterator input, OpGraph opGraph, ExecutionContext context)
    {
        super(input, context) ;
        this.opGraph = opGraph ;
    }
    
    @Override
    protected QueryIterator nextStage(Binding outerBinding)
    {
        DatasetGraph ds = getExecContext().getDataset() ;
        Iterator<Node> graphNameNodes = makeSources(ds, outerBinding, opGraph.getNode());
        
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

    protected static Iterator<Node> makeSources(DatasetGraph data, Binding b, Node graphVar)
    {
        // TODO This should done as part of substitution.
        Node n2 = resolve(b, graphVar) ;
        
        if ( n2 != null && ! n2.isURI() )
            // Bloank node or literal possible after resolving
            return new NullIterator<Node>() ;
        
        // n2 is a URI or null.
        
        if ( n2 == null )
            // Do all submodels.
            return data.listGraphNodes() ;
        return new SingletonIterator<Node>(n2) ;
    }
    

    protected static class QueryIterGraphInner extends QueryIter
    {
        protected Binding parentBinding ;
        protected Iterator<Node> graphNames ;
        protected OpGraph opGraph ;
        protected QueryIterator subIter = null ;

        protected QueryIterGraphInner(Binding parent, Iterator<Node> graphNames, OpGraph opGraph, ExecutionContext execCxt)
        {
            super(execCxt) ;
            this.parentBinding = parent ;
            this.graphNames = graphNames ;
            this.opGraph = opGraph ;
        }

        @Override
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

        @Override
        protected Binding moveToNextBinding()
        {
            if ( subIter == null )
                throw new NoSuchElementException(Utils.className(this)+".moveToNextBinding") ;
                
            return subIter.nextBinding() ;
        }

        @Override
        protected void closeIterator()
        {
            if ( subIter != null )
                subIter.close() ;
            subIter = null ;
        }
        
        // This is very like QueryIteratorRepeatApply except its not repeating over bindings
        // There is a tradeoff of generalising QueryIteratorRepeatApply to Objects
        // and hence no type safety. Or duplicating code (generics?)
        
        protected QueryIterator nextIterator()
        {
            if ( ! graphNames.hasNext() )
                return null ;
            Node gn = graphNames.next() ;

//            Graph g = getExecContext().getDataset().getGraph(gn) ;
//            if ( g == null )
//                return null ;
//                
//            // Create a new context with a different active graph
//            ExecutionContext execCxt2 = new ExecutionContext(getExecContext(), g) ;
                
            Binding b = parentBinding ;
            if ( Var.isVar(opGraph.getNode()) ) 
                // Binding the variable (if "GRAPH ?var")
                b = new Binding1(b, Var.alloc(opGraph.getNode()), gn) ;
            
            QueryIterator qIter = buildIterator(b, gn, opGraph, getExecContext()) ; 
            return qIter ;
        }
        

        protected static QueryIterator buildIterator(Binding binding, Node graphNode, OpGraph opGraph, ExecutionContext outerCxt)
        {
            if ( !graphNode.isURI() )
                // e.g. variable bound to a literal or blank node.
                // Alow this?
                throw new ARQInternalErrorException("QueryIterGraphInner.buildIterator") ;
                //return null ;
            
            // TODO Think about avoiding substitution.
            // If the subpattern does not involve the vars from the binding, avoid the substitute.  
            Op op = QC.substitute(opGraph.getSubOp(), binding) ;
            Graph g = outerCxt.getDataset().getGraph(graphNode) ;
            if ( g == null )
                return null ;
            
            ExecutionContext cxt2 = new ExecutionContext(outerCxt, g) ;
            QueryIterator subInput = QueryIterSingleton.create(binding, cxt2) ;
            return QC.execute(op, subInput, cxt2) ;
        }
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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