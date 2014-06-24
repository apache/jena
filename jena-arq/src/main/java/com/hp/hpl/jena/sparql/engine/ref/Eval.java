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

package com.hp.hpl.jena.sparql.engine.ref;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Table ;
import com.hp.hpl.jena.sparql.algebra.TableFactory ;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.table.TableEmpty ;
import com.hp.hpl.jena.sparql.algebra.table.TableUnit ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterDistinguishedVars ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterRoot ;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder ;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator ;

// Spit out a few of the longer ops.
public class Eval
{
    public static Table eval(Evaluator evaluator, Op op)
    {
        EvaluatorDispatch ev = new EvaluatorDispatch(evaluator) ;
        op.visit(ev) ;
        Table table = ev.getResult() ;
        return table ;
    }
    
    static Table evalDS(OpDatasetNames opDSN, Evaluator evaluator)
    {
        Node graphNode = opDSN.getGraphNode() ;
        if ( graphNode.isURI() )
        {
            if ( evaluator.getExecContext().getDataset().containsGraph(graphNode) )
            { return new TableUnit() ; } 
            else
                // WRONG
            { return new TableEmpty() ; }
        }

        if ( ! Var.isVar(graphNode) )
            throw new ARQInternalErrorException("OpDatasetNames: Not a URI or variable: "+graphNode) ; 

        DatasetGraph dsg = evaluator.getExecContext().getDataset() ;
        Iterator<Node> iter = dsg.listGraphNodes() ;
        List<Binding> list = new ArrayList<>((int)dsg.size()) ;
        for ( ; iter.hasNext(); )
        {
            Node gn = iter.next();
            Binding b = BindingFactory.binding(Var.alloc(graphNode), gn) ;
            list.add(b) ;
        }

        QueryIterator qIter = new QueryIterPlainWrapper(list.iterator(), evaluator.getExecContext()) ;
        return TableFactory.create(qIter) ;

    }
    
    static Table evalGraph(OpGraph opGraph, Evaluator evaluator)
    {
        ExecutionContext execCxt = evaluator.getExecContext() ;
        
        if ( ! Var.isVar(opGraph.getNode()) )
        {
            Graph graph = execCxt.getDataset().getGraph(opGraph.getNode()) ;
            if ( graph == null )
                // No such name in the dataset
                return new TableEmpty() ;
            ExecutionContext execCxt2 = new ExecutionContext(execCxt, graph) ;
            Evaluator e2 = EvaluatorFactory.create(execCxt2) ;
            return eval(e2, opGraph.getSubOp()) ;
        }
        
        // Graph node is a variable.
        Var gVar = Var.alloc(opGraph.getNode()) ;
        Table current = null ;
        for ( Iterator<Node> iter = execCxt.getDataset().listGraphNodes() ; iter.hasNext() ; )
        {
            Node gn = iter.next();
            Graph graph = execCxt.getDataset().getGraph(gn) ;
            ExecutionContext execCxt2 = new ExecutionContext(execCxt, graph) ;
            Evaluator e2 = EvaluatorFactory.create(execCxt2) ;
            
            Table tableVarURI = TableFactory.create(gVar, gn) ;
            // Evaluate the pattern, join with this graph node possibility.
            // XXX If Var.ANON then no-opt.
            Table patternTable = eval(e2, opGraph.getSubOp()) ;
            Table stepResult = evaluator.join(patternTable, tableVarURI) ;
            
            if ( current == null )
                current = stepResult ;
            else
                current = evaluator.union(current, stepResult) ;
        }
        
        if ( current == null )
            // Nothing to loop over
            return new TableEmpty() ;
        return current ;
    }
    
    static Table evalQuadPattern(OpQuadPattern opQuad, Evaluator evaluator)
    {
        if ( opQuad.isEmpty() )
            return TableFactory.createUnit() ;
        
        ExecutionContext cxt = evaluator.getExecContext() ;
        DatasetGraph ds = cxt.getDataset() ;
        BasicPattern pattern = opQuad.getBasicPattern() ;
        
        if ( ! opQuad.getGraphNode().isVariable() ) 
        {
            if ( ! opQuad.getGraphNode().isURI() )
            { throw new ARQInternalErrorException("Not a URI or variable: "+opQuad.getGraphNode()) ;}
            Graph g = null ;
            
            if ( opQuad.isDefaultGraph() )
                g = ds.getDefaultGraph() ;
            else
                g = ds.getGraph(opQuad.getGraphNode()) ;
            if ( g == null )
                return new TableEmpty() ;
            ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;
            QueryIterator qIter = executeBGP(pattern, QueryIterRoot.create(cxt2), cxt2) ;
            return TableFactory.create(qIter) ;
        }
        else
        {
            // Variable.
            Var gVar = Var.alloc(opQuad.getGraphNode()) ;
            // Or just just devolve to OpGraph and get OpUnion chain of OpJoin
            QueryIterConcat concat = new QueryIterConcat(cxt) ;
            for ( Iterator<Node> graphNodes = cxt.getDataset().listGraphNodes() ; graphNodes.hasNext(); )
            {
                Node gn = graphNodes.next() ;
                //Op tableVarURI = TableFactory.create(gn.getName(), Node.createURI(uri)) ;
                
                Graph g = cxt.getDataset().getGraph(gn) ;
                Binding b = BindingFactory.binding(BindingRoot.create(), gVar, gn) ;
                ExecutionContext cxt2 = new ExecutionContext(cxt, g) ;

                // Eval the pattern, eval the variable, join.
                // Pattern may be non-linear in the variable - do a pure execution.  
                Table t1 = TableFactory.create(gVar, gn) ;
                QueryIterator qIter = executeBGP(pattern, QueryIterRoot.create(cxt2), cxt2) ;
                Table t2 = TableFactory.create(qIter) ;
                Table t3 = evaluator.join(t1, t2) ;
                concat.add(t3.iterator(cxt2)) ;
            }
            return TableFactory.create(concat) ;
        }
    }

    private static QueryIterator executeBGP(BasicPattern pattern, QueryIterator input, ExecutionContext execCxt) {
        if (pattern.isEmpty())
            return input ;

        boolean hideBNodeVars = execCxt.getContext().isTrue(ARQ.hideNonDistiguishedVariables) ;

        StageGenerator gen = StageBuilder.executeInline ;
        QueryIterator qIter = gen.execute(pattern, input, execCxt) ;

        // Remove non-distinguished variables here.
        // Project out only named variables.
        if (hideBNodeVars)
            qIter = new QueryIterDistinguishedVars(qIter, execCxt) ;
        return qIter ;
    }
    
    // This is for an arbitrary quad collection.
    // Downside is that each quad is solved separtely, not in a BGP.
    // This breaks property functions with list arguments
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

}
