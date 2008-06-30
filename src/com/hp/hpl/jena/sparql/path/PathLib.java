/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.*;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterConcat;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunction;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionRegistry;
import com.hp.hpl.jena.sparql.procedure.ProcLib;

public class PathLib
{
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
        Iterator iter = null ;
        Node endNode = null ;
        Graph graph = execCxt.getActiveGraph() ;
        
        if ( Var.isVar(s) && Var.isVar(o) )
            return ungroundedPath(binding, graph, Var.alloc(s), path, Var.alloc(o), execCxt) ;

        if ( Var.isVar(s) )
        {
            // Var subject, concreate obnject - do backwards.
            iter = PathEval.evalReverse(graph, o, path) ;
            endNode = s ;
        } 
        else
        {
            iter = PathEval.eval(graph, s, path) ;
            endNode = o ;
        }
        return _execTriplePath(binding, iter, endNode, execCxt) ;
    }
    
    // Brute force evaluation of a TriplePah where neither subject nor object ar ebound 
    private static QueryIterator ungroundedPath(Binding binding, Graph graph, Var s, Path path, Var o,
                                                ExecutionContext execCxt)
    {
        Iterator iter = allNodes(graph) ;
        QueryIterConcat qIterCat = new QueryIterConcat(execCxt) ;
        
        for ( ; iter.hasNext() ; )
        {
            Node n = (Node)iter.next() ;
            Binding b2 = new Binding1(binding, s, n) ;
            Iterator pathIter = PathEval.eval(graph, n, path) ;
            QueryIterator qIter = _execTriplePath(b2, pathIter, o, execCxt) ;
            qIterCat.add(qIter) ;
        }
        return qIterCat ;
    }

    private static Iterator allNodes(Graph graph)
    {
        Set x = new HashSet(1000) ;
        ExtendedIterator iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = (Triple)iter.next();
            x.add(t.getSubject()) ;
            x.add(t.getObject()) ;
        }
        return x.iterator() ;
    }

    private static QueryIterator _execTriplePath(Binding binding, 
                                                 Iterator iter,
                                                 Node endNode,
                                                 ExecutionContext execCxt)
    {
        List results = new ArrayList() ;

        if (Var.isVar(endNode))
        {
            Var var = Var.alloc(endNode) ;
            // Assign.
            for (; iter.hasNext();)
            {
                Node n = (Node)iter.next() ;
                results.add(new Binding1(binding, var, n)) ;
            }
            return new QueryIterPlainWrapper(results.iterator()) ;
        } else
        {
            // Fixed value - did it match?
            for (; iter.hasNext();)
            {
                Node n = (Node)iter.next() ;
                if (n.sameValueAs(endNode))
                {
                    results.add(binding) ;
                }
            }
            return ProcLib.noResults(execCxt) ;
        }
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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