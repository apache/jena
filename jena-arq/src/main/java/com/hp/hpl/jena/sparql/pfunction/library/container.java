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

package com.hp.hpl.jena.sparql.pfunction.library;

import java.util.* ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.iterator.* ;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple ;
import com.hp.hpl.jena.sparql.util.IterLib ;
import com.hp.hpl.jena.sparql.util.graph.GraphContainerUtils ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** container - super class of bag/alt/seq - rdfs:member
 * */ 

public class container extends PFuncSimple
{
    Node typeNode = null ;      // Null means don't check type.
    
    public container() { this.typeNode = null ; }

    protected container(Node typeURI) { this.typeNode = typeURI ; }

    @Override
    public QueryIterator execEvaluated(Binding binding, Node containerNode, Node predicate, Node member, ExecutionContext execCxt)
    {
        QueryIterator qIter1 = execEvaluatedConcrete(binding, containerNode, predicate, member, execCxt) ;
        QueryIterator qIter2 = execEvaluatedCalc(binding, containerNode, predicate, member, execCxt) ;
        QueryIterConcat concat = new QueryIterConcat(execCxt) ;
        concat.add(qIter1) ;
        concat.add(qIter2) ;
        return concat ;
    }
    
    // Ask directly.
    private QueryIterator execEvaluatedConcrete(Binding binding, Node containerNode, Node predicate, Node member,
                                                ExecutionContext execCxt)
    {
        QueryIterator input = QueryIterSingleton.create(binding, execCxt) ;
        Graph graph = execCxt.getActiveGraph() ;
        QueryIterator qIter = new QueryIterTriplePattern(input, new Triple(containerNode, predicate, member), execCxt) ;
        return qIter ;
    }

    // Ask by finding all the rdf:_N + rdf:type  
    private QueryIterator execEvaluatedCalc(Binding binding, Node containerNode, Node predicate, Node member, ExecutionContext execCxt)
    {    
        Graph graph = execCxt.getActiveGraph() ;
        if ( ! containerNode.isVariable() )
        {
            // Container a ground term.
            if ( ! GraphContainerUtils.isContainer(execCxt.getActiveGraph(), containerNode, typeNode) )
                return IterLib.noResults(execCxt) ;
            return oneContainer(binding, containerNode, member, execCxt) ;
        }            
        
        // Container a variable. 
        Collection<Node> c = null ;
        
        if ( member.isVariable() )
            c = findContainers(graph, typeNode) ;
        else
            c = findContainingContainers(graph, typeNode, member) ;
        
        QueryIterConcat cIter = new QueryIterConcat(execCxt) ;
        Var cVar = Var.alloc(containerNode) ;
        for ( Node cn : c )
        {
            //Binding the container node. 
            Binding b = BindingFactory.binding( binding, cVar, cn );
            Node m = member;
            // Special case of ?x rdfs:member ?x
            if ( Var.isVar( member ) && member.equals( cVar ) )
            {
                m = cn;
            }

            cIter.add( oneContainer( b, cn, m, execCxt ) );
        }
        return cIter ;
        //throw new QueryFatalException(Utils.className(this)+": Arg 1 is too hard : "+containerNode) ;
    }
    
    private QueryIterator oneContainer(Binding binding, Node containerNode, Node member, ExecutionContext execCxt)
    {
        // containerNode is a fixed term
        if ( member.isVariable() )
            return members(binding, containerNode,  Var.alloc(member), execCxt) ;
        else
            return verify(binding, containerNode, member, execCxt) ;
    }
    
    private QueryIterator members(Binding binding, Node containerNode, Var memberVar, ExecutionContext execCxt)
    {
        // Not necessarily very efficient
        Collection<Node> x = GraphContainerUtils.containerMembers(execCxt.getActiveGraph(), containerNode, typeNode) ;
        if ( x == null )
            // Wrong type.
            return IterLib.noResults(execCxt) ;

        List<Binding> bindings = new ArrayList<>() ;
        for ( Node n : x )
        {
            Binding b = BindingFactory.binding( binding, memberVar, n );
            bindings.add( b );
        }
        
        // Turn into a QueryIterator of extra bindings.
        return new QueryIterPlainWrapper(bindings.iterator(), execCxt) ;
    }
    
    private QueryIterator verify(Binding binding, Node containerNode, Node member, ExecutionContext execCxt)
    {
        int count = GraphContainerUtils.countContainerMember(execCxt.getActiveGraph(), containerNode, typeNode, member) ;
        return new QueryIterYieldN(count, binding, execCxt) ;
    }
    
    static private Collection<Node> findContainers(Graph graph, Node typeNode)
    {
        Set<Node> acc = new HashSet<>() ;
        if ( typeNode != null )
        {
            findContainers(acc, graph, typeNode) ;
            return acc;
        }
        findContainers(acc, graph, RDF.Bag.asNode()) ;
        findContainers(acc, graph, RDF.Seq.asNode()) ;
        findContainers(acc, graph, RDF.Alt.asNode()) ;
        return acc ;
    }
    
    static private void findContainers(Collection<Node> acc, Graph graph, Node typeNode)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, RDF.type.asNode(), typeNode) ;
        while(iter.hasNext())
        {
            Triple t = iter.next();
            Node containerNode = t.getSubject() ;
            acc.add(containerNode) ;
        }
    }
    
    static private Collection<Node> findContainingContainers(Graph graph, Node typeNode, Node member)
    {
        Collection<Node> acc = new HashSet<>() ;
        // Index off the object
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, member) ;
        while(iter.hasNext())
        {
            Triple t = iter.next();
            Node containerNode = t.getSubject() ;   // Candidate
            if ( GraphContainerUtils.isContainer(graph, containerNode, typeNode) )
                acc.add(containerNode) ;
        }
        return acc ;
    }
}
