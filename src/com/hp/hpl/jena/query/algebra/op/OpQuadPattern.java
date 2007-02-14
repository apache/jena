/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.algebra.op;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.algebra.Algebra;
import com.hp.hpl.jena.query.algebra.Evaluator;
import com.hp.hpl.jena.query.algebra.Table;
import com.hp.hpl.jena.query.core.*;
import com.hp.hpl.jena.query.engine.ExecutionContext;
import com.hp.hpl.jena.query.engine.QueryIterator;
import com.hp.hpl.jena.query.engine.binding.Binding;
import com.hp.hpl.jena.query.engine.binding.Binding1;
import com.hp.hpl.jena.query.engine.binding.BindingRoot;
import com.hp.hpl.jena.query.engine.engine1.PlanElement;
import com.hp.hpl.jena.query.engine.engine1.plan.PlanTriplesBlock;
import com.hp.hpl.jena.query.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.query.engine.iterator.QueryIterSingleton;
import com.hp.hpl.jena.query.engine.ref.TableFactory;
import com.hp.hpl.jena.query.engine.ref.table.TableEmpty;
import com.hp.hpl.jena.query.syntax.ElementTriplesBlock;

public class OpQuadPattern extends Op0
{
    Node graphNode ;
    BasicPattern triples ;
    List quads = null ;
    
    // A QuadPattern is a block of quads with the same graph arg.
    // i.e. a BasicGraphPattern.  This gets the blank node coping right.
    
    // Quads are for a specific quad store.
    
    // Later, we may introduce OpQuadBlock for this and OpQuadPattern becomes
    // a sequence of such blocks.
    
    public OpQuadPattern(Node quadNode, BasicPattern triples)
    { 
        this.graphNode = quadNode ;
        this.triples = triples ;
    }
    
    public List getQuads()
    {
        if ( quads == null )
        {
            quads = new ArrayList() ;
            for (Iterator iter = triples.iterator() ; iter.hasNext() ; )
            {
                Triple t = (Triple)iter.next() ;
                quads.add(new Quad(graphNode, t)) ;
            } 
        }
        return quads ; 
    }
    
    public Node getGraphNode() { return graphNode ; } 
    
//    // Alternative
//    public OpQuadPattern(List quads)
//    { 
//        this.quads = quads ;
//    }
    
    public Table eval(Evaluator evaluator)
    {
        if ( getQuads().size() == 0 )
            return TableFactory.createUnit() ;
        
        ExecutionContext cxt = evaluator.getExecContext() ;
        DatasetGraph ds = cxt.getDataset() ;
        
        ElementTriplesBlock bgp = new ElementTriplesBlock() ;
        bgp.getTriples().addAll(triples) ;
        
        if ( ! graphNode.isVariable() ) 
        {
            if ( ! graphNode.isURI() )
            { throw new ARQInternalErrorException("Not a URI or variable: "+graphNode) ;}
            Graph g = null ;
            
            if ( graphNode.equals(Quad.defaultGraph) )
                g = ds.getDefaultGraph() ;
            else
                g = ds.getNamedGraph(graphNode.getURI()) ;
            if ( g == null )
                return new TableEmpty() ;
            ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
            // This enable PropertyFunctions.
            // need to separate it out
            // And use in opBGP.
            PlanElement planElt = PlanTriplesBlock.make(cxt2.getContext(), bgp) ;
            return TableFactory.
                    create(planElt.build(Algebra.makeRoot(cxt2), cxt2)) ;
        }
        else
        {
            // Variable.
            Var gVar = Var.alloc(graphNode) ;
            // Or just just devolve to OpGraph and get OpUnion chain of OpJoin
            QueryIterConcat concat = new QueryIterConcat(cxt) ;
            for ( Iterator graphURIs = cxt.getDataset().listNames() ; graphURIs.hasNext(); )
            {
                String uri = (String)graphURIs.next() ;
                //Op tableVarURI = TableFactory.create(gn.getName(), Node.createURI(uri)) ;
                
                Graph g = cxt.getDataset().getNamedGraph(uri) ;
                Binding b = new Binding1(BindingRoot.create(), gVar, Node.createURI(uri)) ;
                QueryIterator qIter1 = new QueryIterSingleton(b, cxt) ;
                ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
                PlanElement planElt = PlanTriplesBlock.make(cxt2.getContext(), bgp) ;
                QueryIterator qIter = planElt.build(qIter1, cxt2) ;
                concat.add(qIter) ;
            }
            return TableFactory.create(concat) ;
        }
    }

    // This is for an arbitrary quad collection.
    // Downside is that each quad is solved separtely, not in a BGP.
    // This break property functions with list arguments
    // and extension to entailment regimes which as DL.
    
//    public Table evalAny(Evaluator evaluator)
//    {
//        if ( getQuads().size() == 0 )
//            return TableFactory.createUnit().eval(evaluator) ;
//        
//        ExecutionContext cxt = evaluator.getExecContext() ;
//        DatasetGraph ds = cxt.getDataset() ;
//        //Table working = TableFactory.createUnit().eval(evaluator) ;
//        Table working = null ; 
//        for ( Iterator iter = quads.iterator() ; iter.hasNext() ; )
//        {
//            Quad quad = (Quad)iter.next() ;
//            Table nextTable = oneStep(quad, cxt)  ;
//            if ( working != null )
//                working = evaluator.join(working, nextTable) ;
//            else
//                working = nextTable ;
//        }
//        return working ;
//    }
//
//    private Table oneStep(Quad quad, ExecutionContext cxt)
//    {
//        Node gn = quad.getGraph() ;
//        DatasetGraph ds = cxt.getDataset() ;
//        if ( ! gn.isVariable() ) 
//        {
//            if ( ! gn.isURI() )
//                throw new ARQInternalErrorException("Not a URI or variable: "+gn) ;
//            
//            Graph g = gn.equals(AlgebraCompilerQuad.defaultGraph) ?
//                          ds.getDefaultGraph() : 
//                          ds.getNamedGraph(gn.getURI()) ;
//            
//            if ( g == null )
//                return new TableEmpty() ;
//
//            ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
//            // A BGP of one triple ensure property functions are processed.
//            // QueryIterator qIter = new QueryIterTriplePattern(OpBGP.makeRoot(cxt2), quad.getTriple(), cxt2) ;
//            // return TableFactory.create(qIter) ;
//
//            // Alt : gather adjacent quads with the same graph node.
//            // Then blank node projection can be done.
//            
//            // Create a BGP of one to sort property functions out.
//            // ** Projecting blank nodes must be off.
//            ElementBasicGraphPattern bgp = new ElementBasicGraphPattern() ;
//            bgp.addTriple(quad.getTriple()) ;
//            PlanElement planElt = PlanBasicGraphPattern.make(cxt.getContext(), bgp) ;
//            return TableFactory.
//                    create(planElt.build(Algebra.makeRoot(cxt2), cxt2)) ;
//        }
//        else
//        {
//            // Or just just devolve to OpGraph and get OpUnion chain of OpJoin
//            QueryIterConcat concat = new QueryIterConcat(cxt) ;
//            for ( Iterator graphURIs = cxt.getDataset().listNames() ; graphURIs.hasNext(); )
//            {
//                String uri = (String)graphURIs.next() ;
//                //Op tableVarURI = TableFactory.create(gn.getName(), Node.createURI(uri)) ;
//                
//                Graph g = cxt.getDataset().getNamedGraph(uri) ;
//                Binding b = new Binding1(BindingRoot.create(), gn.getName(), Node.createURI(uri)) ;
//                QueryIterator qIter1 = new QueryIterSingleton(b, cxt) ;
//                ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
//                QueryIterator qIter = new QueryIterTriplePattern(qIter1, quad.getTriple(), cxt2) ;
//                concat.add(qIter) ;
//            }
//            return TableFactory.create(concat) ;
//        }
//        
//    }
    
    public String getName()                 { return "QuadPattern" ; }
    public Op apply(Transform transform)    { return transform.transform(this) ; } 
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    public Op copy()                        { return new OpQuadPattern(graphNode, triples) ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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