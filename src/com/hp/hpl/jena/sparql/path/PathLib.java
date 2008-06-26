/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.path;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.Binding1;
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
        if (Var.isVar(s)) s = binding.get(Var.alloc(s)) ;
        if (Var.isVar(o)) o = binding.get(Var.alloc(o)) ;

        Iterator iter = PathEval.eval(execCxt.getActiveGraph(), s, path) ;
        List results = new ArrayList() ;

        if (Var.isVar(o))
        {
            Var var = Var.alloc(o) ;
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
                if (n.sameValueAs(o))
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