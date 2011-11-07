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

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.List ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.util.iterator.ClosableIterator ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

public class QueryIterTriplePattern extends QueryIterRepeatApply
{
    private final Triple pattern ;
    
    public QueryIterTriplePattern( QueryIterator input,
                                   Triple pattern , 
                                   ExecutionContext cxt)
    {
        super(input, cxt) ;
        this.pattern = pattern ;
    }

    @Override
    protected QueryIterator nextStage(Binding binding)
    {
        return new TripleMapper(binding, pattern, getExecContext()) ;
    }
    
    static int countMapper = 0 ; 
    static class TripleMapper extends QueryIter
    {
        private Node s ;
        private Node p ;
        private Node o ;
        private Binding binding ;
        private ClosableIterator<Triple> graphIter ;
        private Binding slot = null ;
        private boolean finished = false ;
        private volatile boolean cancelled = false ;

        TripleMapper(Binding binding, Triple pattern, ExecutionContext cxt)
        {
            super(cxt) ;
            this.s = substitute(pattern.getSubject(), binding) ;
            this.p = substitute(pattern.getPredicate(), binding) ;
            this.o = substitute(pattern.getObject(), binding) ;
            this.binding = binding ;
            Node s2 = tripleNode(s) ;
            Node p2 = tripleNode(p) ;
            Node o2 = tripleNode(o) ;
            Graph graph = cxt.getActiveGraph() ;
            
            ExtendedIterator<Triple> iter = graph.find(s2, p2, o2) ;
            
            if ( false )
            {
                // Materialize the results now. Debugging only.
                List<Triple> x = iter.toList() ;
                this.graphIter = WrappedIterator.create(x.iterator()) ;
                iter.close();
            }
            else
                // Stream.
                this.graphIter = iter ;
        }

        private static Node tripleNode(Node node)
        {
            if ( node.isVariable() )
                return Node.ANY ;
            return node ;
        }

        private static Node substitute(Node node, Binding binding)
        {
            if ( Var.isVar(node) )
            {
                Node x = binding.get(Var.alloc(node)) ;
                if ( x != null )
                    return x ;
            }
            return node ;
        }

        private Binding mapper(Triple r)
        {
            BindingMap results = BindingFactory.create(binding) ;

            if ( ! insert(s, r.getSubject(), results) )
                return null ; 
            if ( ! insert(p, r.getPredicate(), results) )
                return null ;
            if ( ! insert(o, r.getObject(), results) )
                return null ;
            return results ;
        }

        private static boolean insert(Node inputNode, Node outputNode, BindingMap results)
        {
            if ( ! Var.isVar(inputNode) )
                return true ;
            
            Var v = Var.alloc(inputNode) ;
            Node x = results.get(v) ;
            if ( x != null )
                return outputNode.equals(x) ;
            
            results.add(v, outputNode) ;
            return true ;
        }
        
        @Override
        protected boolean hasNextBinding()
        {
            if ( finished ) return false ;
            if ( slot != null ) return true ;
            if ( cancelled )
            {
                graphIter.close() ;
                finished = true ;
                return false ;
            }

            while(graphIter.hasNext() && slot == null )
            {
                Triple t = graphIter.next() ;
                slot = mapper(t) ;
            }
            if ( slot == null )
                finished = true ;
            return slot != null ;
        }

        @Override
        protected Binding moveToNextBinding()
        {
            if ( ! hasNextBinding() ) 
                throw new ARQInternalErrorException() ;
            Binding r = slot ;
            slot = null ;
            return r ;
        }

        @Override
        protected void closeIterator()
        {
            if ( graphIter != null )
                NiceIterator.close(graphIter) ;
            graphIter = null ;
        }
        
        @Override
        protected void requestCancel()
        {
            // The QueryIteratorBase machinary will do the real work.
            cancelled = true ;
        }
    }
}
