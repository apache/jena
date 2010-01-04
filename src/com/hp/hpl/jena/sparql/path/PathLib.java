/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpPath;
import com.hp.hpl.jena.sparql.algebra.op.OpSequence;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.PathBlock;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.sparql.util.IterLib;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

public class PathLib
{
    /** Convert any paths of exactly one predicate to a triple pattern */ 
    public static Op pathToTriples(PathBlock pattern)
    {
        //Step 2 : gather into OpBGP(BasicPatterns) or OpPath
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
            //@Override
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
            // Could fake with P_Link, of BGP execution.
            throw new ARQInternalErrorException("Attempt to execute a TriplePath which is a plain Triple") ;
        
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
        s = Var.lookup(binding, s) ;
        o = Var.lookup(binding, o) ;
        Iterator<Node> iter = null ;
        Node endNode = null ;
        Graph graph = execCxt.getActiveGraph() ;
        
        if ( Var.isVar(s) && Var.isVar(o) )
            return ungroundedPath(binding, graph, Var.alloc(s), path, Var.alloc(o), execCxt) ;

        if ( ! Var.isVar(s) && ! Var.isVar(o) )
            return groundedPath(binding, graph, s, path, o, execCxt) ;
        
        if ( Var.isVar(s) )
        {
            // Var subject, concreate object - do backwards.
            iter = PathEval.evalInverse(graph, o, path) ;
            endNode = s ;
        } 
        else
        {
            iter = PathEval.eval(graph, s, path) ;
            endNode = o ;
        }
        return _execTriplePath(binding, iter, endNode, execCxt) ;
    }
    
    private static QueryIterator _execTriplePath(Binding binding, 
                                                 Iterator<Node> iter,
                                                 Node endNode,
                                                 ExecutionContext execCxt)
    {
        List<Binding> results = new ArrayList<Binding>() ;
        
        if (! Var.isVar(endNode))
            throw new ARQInternalErrorException("Non-variable endnode in _execTriplePath") ;
        
        Var var = Var.alloc(endNode) ;
        // Assign.
        for (; iter.hasNext();)
        {
            Node n = iter.next() ;
            results.add(new Binding1(binding, var, n)) ;
        }
        return new QueryIterPlainWrapper(results.iterator()) ;
    }

    // Subject and object are nodes.
    private static QueryIterator groundedPath(Binding binding, Graph graph, Node subject, Path path, Node object,
                                              ExecutionContext execCxt)
    {
        Iterator<Node> iter = PathEval.eval(graph, subject, path) ;
        for ( ; iter.hasNext() ; )
        {
            Node n = iter.next() ;
            if ( n.sameValueAs(object) )
                return IterLib.result(binding, execCxt) ;        
        }
        return IterLib.noResults(execCxt) ;
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
            Binding b2 = new Binding1(binding, sVar, n) ;
            Iterator<Node> pathIter = PathEval.eval(graph, n, path) ;
            QueryIterator qIter = _execTriplePath(b2, pathIter, oVar, execCxt) ;
            qIterCat.add(qIter) ;
        }
        return qIterCat ;
    }

 

    public static TriplePath substitute(TriplePath triplePath, Binding binding)
    {
        if ( triplePath.isTriple() )
            return new TriplePath(BindingUtils.substituteIntoTriple(triplePath.asTriple(), binding)) ;
  
        Node s = Var.lookup(binding, triplePath.getSubject()) ;
        Node o = Var.lookup(binding, triplePath.getObject()) ;
        return new TriplePath(s, triplePath.getPath(), o) ;
    }
    
    
    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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