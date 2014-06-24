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

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpPath ;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.PathBlock ;
import com.hp.hpl.jena.sparql.core.TriplePath ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ExecutionContext ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterYieldN ;
import com.hp.hpl.jena.sparql.mgt.Explain ;
import com.hp.hpl.jena.sparql.path.eval.PathEval ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory ;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;

public class PathLib
{
    /** Convert any paths of exactly one predicate to a triple pattern */ 
    public static Op pathToTriples(PathBlock pattern)
    {
        BasicPattern bp = null ;
        Op op = null ;

        for ( TriplePath tp : pattern )
        {
            if ( tp.isTriple() )
            {
                if ( bp == null )
                    bp = new BasicPattern() ;
                bp.add(tp.asTriple()) ;
                continue ;
            }
            // Path form.
            op = flush(bp, op) ;
            bp = null ;

            OpPath opPath2 = new OpPath(tp) ;
            op = OpSequence.create(op, opPath2) ;
            continue ;
        }

        // End.  Finish off any outstanding BGP.
        op = flush(bp, op) ;
        return op ;
    }
    
    static private Op flush(BasicPattern bp, Op op)
    {
        if ( bp == null || bp.isEmpty() )
            return op ;
        
        OpBGP opBGP = new OpBGP(bp) ;
        op = OpSequence.create(op, opBGP) ;
        return op ;
    }
    
    /** Install a path as a property function in the global property function registry */
    public static void install(String uri, Path path)
    { install(uri, path, PropertyFunctionRegistry.get()) ; }

    /** Install a path as a property function in a given registry */
    public static void install(String uri, final Path path, PropertyFunctionRegistry registry)
    {
        PropertyFunctionFactory pathPropFuncFactory = new PropertyFunctionFactory()
        {
            @Override
            public PropertyFunction create(String uri)
            {
                return new PathPropertyFunction(path) ;
            }
        }; 
        
        registry.put(uri, pathPropFuncFactory) ;
    }

    public static QueryIterator execTriplePath(Binding binding, TriplePath triplePath, ExecutionContext execCxt)
    {
        if ( triplePath.isTriple() )
        {
            // Fake it.  This happens only for API constructed situations. 
            Path path = new P_Link(triplePath.getPredicate()) ;
            triplePath = new TriplePath(triplePath.getSubject(),
                                        path,
                                        triplePath.getObject()) ;
        }
        
        return execTriplePath(binding, 
                              triplePath.getSubject(),
                              triplePath.getPath(),
                              triplePath.getObject(),
                              execCxt) ;
    }
    
    public static QueryIterator execTriplePath(Binding binding, 
                                               Node s, Path path, Node o,
                                               ExecutionContext execCxt)
    {
        Explain.explain(s, path, o, execCxt.getContext()) ;
        
        s = Var.lookup(binding, s) ;
        o = Var.lookup(binding, o) ;
        Iterator<Node> iter = null ;
        Node endNode = null ;
        Graph graph = execCxt.getActiveGraph() ;
        
        if ( Var.isVar(s) && Var.isVar(o) )
        {
            if ( s.equals(o) )
                return ungroundedPathSameVar(binding, graph, Var.alloc(s), path, execCxt) ;
            else
                return ungroundedPath(binding, graph, Var.alloc(s), path, Var.alloc(o), execCxt) ;
        }

        if ( ! Var.isVar(s) && ! Var.isVar(o) )
            return groundedPath(binding, graph, s, path, o, execCxt) ;
        
        if ( Var.isVar(s) )
        {
            // Var subject, concrete object - do backwards.
            iter = PathEval.evalReverse(graph, o, path, execCxt.getContext()) ;
            endNode = s ;
        } 
        else
        {
            iter = PathEval.eval(graph, s, path, execCxt.getContext()) ;
            endNode = o ;
        }
        return _execTriplePath(binding, iter, endNode, execCxt) ;
    }
    
    private static QueryIterator _execTriplePath(Binding binding, 
                                                 Iterator<Node> iter,
                                                 Node endNode,
                                                 ExecutionContext execCxt)
    {
        List<Binding> results = new ArrayList<>() ;
        
        if (! Var.isVar(endNode))
            throw new ARQInternalErrorException("Non-variable endnode in _execTriplePath") ;
        
        Var var = Var.alloc(endNode) ;
        // Assign.
        for (; iter.hasNext();)
        {
            Node n = iter.next() ;
            results.add(BindingFactory.binding(binding, var, n)) ;
        }
        return new QueryIterPlainWrapper(results.iterator(), execCxt) ;
    }

    // Subject and object are nodes.
    private static QueryIterator groundedPath(Binding binding, Graph graph, Node subject, Path path, Node object,
                                              ExecutionContext execCxt)
    {
        Iterator<Node> iter = PathEval.eval(graph, subject, path, execCxt.getContext()) ;
        // Now count the number of matches.
        
        int count = 0 ;
        for ( ; iter.hasNext() ; )
        {
            Node n = iter.next() ;
            if ( n.sameValueAs(object) )
                count++ ;
        }
        
        return new QueryIterYieldN(count, binding, execCxt) ;
    }

    // Brute force evaluation of a TriplePath where neither subject nor object are bound 
    private static QueryIterator ungroundedPath(Binding binding, Graph graph, Var sVar, Path path, Var oVar,
                                                ExecutionContext execCxt)
    {
        Iterator<Node> iter = GraphUtils.allNodes(graph) ;
        QueryIterConcat qIterCat = new QueryIterConcat(execCxt) ;
        
        for ( ; iter.hasNext() ; )
        {
            Node n = iter.next() ;
            Binding b2 = BindingFactory.binding(binding, sVar, n) ;
            Iterator<Node> pathIter = PathEval.eval(graph, n, path, execCxt.getContext()) ;
            QueryIterator qIter = _execTriplePath(b2, pathIter, oVar, execCxt) ;
            qIterCat.add(qIter) ;
        }
        return qIterCat ;
    }
    
    private static QueryIterator ungroundedPathSameVar(Binding binding, Graph graph, Var var, Path path, ExecutionContext execCxt)
    {
        // Try each end, grounded  
        // Slightly more efficient would be to add a per-engine to do this.
        Iterator<Node> iter = GraphUtils.allNodes(graph) ;
        QueryIterConcat qIterCat = new QueryIterConcat(execCxt) ;
        
        for ( ; iter.hasNext() ; )
        {
            Node n = iter.next() ;
            Binding b2 = BindingFactory.binding(binding, var, n) ;
            int x = existsPath(graph, n, path, n, execCxt) ;
            if ( x > 0 )
            {
                QueryIterator qIter = new QueryIterYieldN(x, b2, execCxt) ;
                qIterCat.add(qIter) ;
            }
        }
        return qIterCat ; 
    }
    
    private static int existsPath(Graph graph, Node subject, Path path, final Node object, ExecutionContext execCxt)
    {
        if ( ! subject.isConcrete() || !object.isConcrete() )
            throw new ARQInternalErrorException("Non concrete node for existsPath evaluation") ;
        Iterator<Node> iter = PathEval.eval(graph, subject, path, execCxt.getContext()) ;
        Filter<Node> filter = new Filter<Node>() { @Override public boolean accept(Node node) { return Lib.equal(node,  object) ; } } ; 
        // See if we got to the node we're interested in finishing at.
        iter = Iter.filter(iter, filter) ;
        long x = Iter.count(iter) ; 
        return (int)x ;
    }
}
