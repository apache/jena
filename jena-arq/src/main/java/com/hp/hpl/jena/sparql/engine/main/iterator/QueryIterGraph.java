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

package com.hp.hpl.jena.sparql.engine.main.iterator;

import java.util.Iterator ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.SingletonIterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterAssignVarValue ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRepeatApply ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSingleton ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterSub ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.util.Utils ;

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
        // Is this closed?
        Iterator<Node> graphNameNodes = makeSources(ds, outerBinding, opGraph.getNode());
        
//        List<Node> x = Iter.toList(graphNameNodes) ;
//        graphNameNodes = x.iterator() ;
//        System.out.println(x) ;
        
        QueryIterator current = new QueryIterGraphInner(
                                               outerBinding, graphNameNodes, 
                                               opGraph, getExecContext()) ;
        return current ;
    }

    private static Node resolve(Binding b, Node n)
    {
        if ( ! n.isVariable() )
            return n ;

        return b.get(Var.alloc(n)) ;
    }

    protected static Iterator<Node> makeSources(DatasetGraph data, Binding b, Node graphVar)
    {
        // TODO This should done as part of substitution.
        Node n2 = resolve(b, graphVar) ;
        
        if ( n2 != null && ! n2.isURI() )
            // Blank node or literal possible after resolving
            return Iter.nullIterator() ;
        
        // n2 is a URI or null.
        
        if ( n2 == null )
            // Do all submodels.
            return data.listGraphNodes() ;
        return new SingletonIterator<>(n2) ;
    }
    

    protected static class QueryIterGraphInner extends QueryIterSub
    {
        protected Binding parentBinding ;
        protected Iterator<Node> graphNames ;
        protected OpGraph opGraph ;

        protected QueryIterGraphInner(Binding parent, Iterator<Node> graphNames, OpGraph opGraph, ExecutionContext execCxt)
        {
            super(null, execCxt) ;
            this.parentBinding = parent ;
            this.graphNames = graphNames ;
            this.opGraph = opGraph ;
        }

        @Override
        protected boolean hasNextBinding()
        {
            for(;;)
            {
                if ( iter == null )
                    iter = nextIterator() ;
                
                if ( iter == null )
                    return false ;
                
                if ( iter.hasNext() )
                    return true ;
                
                iter.close() ;
                iter = nextIterator() ;
                if ( iter == null )
                    return false ;
            }
        }

        @Override
        protected Binding moveToNextBinding()
        {
            if ( iter == null )
                throw new NoSuchElementException(Utils.className(this)+".moveToNextBinding") ;
                
            return iter.nextBinding() ;
        }

        // This code predates ARQ using Java 1.5.

        // This is very like QueryIteratorRepeatApply except its not
        // repeating over bindings, but over graph nodes.
        // There is a tradeoff of generalising QueryIteratorRepeatApply to Objects
        // and hence no type safety. Or duplicating code (generics?)
        
        protected QueryIterator nextIterator()
        {
            if ( ! graphNames.hasNext() )
                return null ;
            Node gn = graphNames.next() ;

            QueryIterator qIter = buildIterator(parentBinding, gn, opGraph, getExecContext()) ;
            if ( qIter == null )
                // Know to be nothing (e.g. graph does not exist). 
                return null ;
            
            if ( Var.isVar(opGraph.getNode()) )
            {
                // This is the join of the graph node variable to the sub-pattern solution.
                // Do after the subpattern so that the variable is not visible to the
                // subpattern.
                Var v = Var.alloc(opGraph.getNode()) ;
                qIter = new QueryIterAssignVarValue(qIter, v, gn, getExecContext()) ;
            }
            
            return qIter ;
        }
        
        // Create the iterator - or return null if there can't be any results.
        protected static QueryIterator buildIterator(Binding binding, Node graphNode, OpGraph opGraph, ExecutionContext outerCxt)
        {
            if ( !graphNode.isURI() && !graphNode.isBlank() )
                // e.g. variable bound to a literal or blank node.
                throw new ARQInternalErrorException("QueryIterGraphInner.buildIterator: Not a URI or balnk node: "+graphNode) ;
            
            // Think about avoiding substitution.
            // If the subpattern does not involve the vars from the binding, avoid the substitute.  
            Op op = QC.substitute(opGraph.getSubOp(), binding) ;
            
            // We can't just use DatasetGraph.getGraph because it may "auto-create" graphs.
            // Use the containsGraph function.
            
            boolean syntheticGraph = ( Quad.isDefaultGraph(graphNode) || Quad.isUnionGraph(graphNode) ) ;
            if ( ! syntheticGraph && ! outerCxt.getDataset().containsGraph(graphNode) )
                return null ;

            Graph g = outerCxt.getDataset().getGraph(graphNode) ;
            // And the contains was true??!!!!!!
            if ( g == null )
                return null ;
                //throw new ARQInternalErrorException(".containsGraph was true but .getGraph is null") ;
            
            ExecutionContext cxt2 = new ExecutionContext(outerCxt, g) ;
            QueryIterator subInput = QueryIterSingleton.create(binding, cxt2) ;
            return QC.execute(op, subInput, cxt2) ;
        }

        @Override
        protected void requestSubCancel()
        {}

        @Override
        protected void closeSubIterator()
        {}
    }
}
