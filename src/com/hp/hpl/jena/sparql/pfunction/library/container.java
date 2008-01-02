/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.pfunction.library;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterYieldN;
import com.hp.hpl.jena.sparql.pfunction.PFuncSimple;
import com.hp.hpl.jena.sparql.procedure.ProcLib;
import com.hp.hpl.jena.sparql.util.GraphContainerUtils;

/** container - super class of bag/alt/seq - rdfs:member
 * 
 * 
 * @author Andy Seaborne
 */ 

public class container extends PFuncSimple
{
    Node typeNode = null ;      // Null means don't check type.
    
    public container() { this.typeNode = null ; }

    protected container(Node typeURI) { this.typeNode = typeURI ; }

    public QueryIterator execEvaluated(Binding binding, Node containerNode, Node predicate, Node member, ExecutionContext execCxt)
    {
        Graph graph = execCxt.getActiveGraph() ;
        
        if ( ! containerNode.isVariable() )
        {
            // Container a ground term.
            if ( ! GraphContainerUtils.isContainer(execCxt.getActiveGraph(), containerNode, typeNode) )
                return ProcLib.noResults(execCxt) ;
            return oneContainer(binding, containerNode, member, execCxt) ;
        }            
        
        // Container a variable. 
        Collection c = null ;
        
        if ( member.isVariable() )
            c = findContainers(graph, typeNode) ;
        else
            c = findContainingContainers(graph, typeNode, member) ;
        
        QueryIterConcat cIter = new QueryIterConcat(execCxt) ;
        Var cVar = Var.alloc(containerNode) ;
        for ( Iterator iter = c.iterator() ; iter.hasNext() ; )
        {
            Node cn = (Node)iter.next() ;
            //Binding the container node. 
            Binding b = new Binding1(binding, cVar, cn) ;
            Node m = member ;
            // Special case of ?x rdfs:member ?x
            if ( Var.isVar(member) && member.equals(cVar) )
                m = cn ;
            
            cIter.add(oneContainer(b, cn, m, execCxt)) ;
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
        Collection x = GraphContainerUtils.containerMembers(execCxt.getActiveGraph(), containerNode, typeNode) ;
        if ( x == null )
            // Wrong type.
            return ProcLib.noResults(execCxt) ;

        List bindings = new ArrayList() ;
        for ( Iterator iter = x.iterator() ; iter.hasNext() ; )
        {
            Node n = (Node)iter.next() ;
            Binding b = new Binding1(binding, memberVar, n) ;
            bindings.add(b) ;
        }
        
        // Turn into a QueryIterator of extra bindings.
        return new QueryIterPlainWrapper(bindings.iterator(), execCxt) ;
    }
    
    private QueryIterator verify(Binding binding, Node containerNode, Node member, ExecutionContext execCxt)
    {
        int count = GraphContainerUtils.countContainerMember(execCxt.getActiveGraph(), containerNode, typeNode, member) ;
        return new QueryIterYieldN(count, binding, execCxt) ;
    }
    
    static private Collection findContainers(Graph graph, Node typeNode)
    {
        Set acc = new HashSet() ;
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
    
    static private void findContainers(Collection acc, Graph graph, Node typeNode)
    {
        ExtendedIterator iter = graph.find(Node.ANY, RDF.type.asNode(), typeNode) ;
        while(iter.hasNext())
        {
            Triple t = (Triple)iter.next();
            Node containerNode = t.getSubject() ;
            acc.add(containerNode) ;
        }
    }
    
    static private Collection findContainingContainers(Graph graph, Node typeNode, Node member)
    {
        Collection acc = new HashSet() ; 
        // Index off the object
        ExtendedIterator iter = graph.find(Node.ANY, Node.ANY, member) ;
        while(iter.hasNext())
        {
            Triple t = (Triple)iter.next();
            Node containerNode = t.getSubject() ;   // Candidate
            if ( GraphContainerUtils.isContainer(graph, containerNode, typeNode) )
                acc.add(containerNode) ;
        }
        return acc ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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