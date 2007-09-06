/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingProject;
import com.hp.hpl.jena.sparql.expr.VarExprList;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintUtils;
import com.hp.hpl.jena.sparql.util.Utils;


public class QueryIterProject extends QueryIterConvert
{
    VarExprList projectionVars ;

    public QueryIterProject(QueryIterator input, VarExprList vars, ExecutionContext qCxt)
    {
        super(input, project(vars, qCxt), qCxt) ;
        projectionVars = vars ;
    }

    static QueryIterConvert.Converter project(VarExprList vars, ExecutionContext qCxt)
    {
        if ( vars.getExprs().isEmpty() )
            return new Projection(vars.getVars(), qCxt) ;
        else
            return new ProjectionExpr(vars, qCxt) ;
    }
    
    public VarExprList getProjectionVars()   { return projectionVars ; }

    protected void releaseResources()
    {}

    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(Utils.className(this)) ;
        out.print(" ") ;
        PrintUtils.printList(out, projectionVars.getVars()) ;
    }
    
    static
    class Projection implements QueryIterConvert.Converter
    {
        List projectionVars ;

        Projection(List vars, ExecutionContext qCxt)
        { 
            this.projectionVars = vars ;
        }

        public Binding convert(Binding bind)
        {
            return new BindingProject(projectionVars, bind) ;
        }
    }
    
    static
    class ProjectionExpr implements QueryIterConvert.Converter
    {
        FunctionEnv funcEnv ;
        VarExprList projectionVars ; 

        ProjectionExpr(VarExprList vars, ExecutionContext qCxt)
        { 
            this.projectionVars = vars ;
            funcEnv = qCxt ;
        }

        public Binding convert(Binding bind)
        {
            Binding b = new BindingMap(bind) ;
            for ( Iterator iter = projectionVars.getVars().iterator() ; iter.hasNext(); )
            {
                Var v = (Var)iter.next();
                // Only add those variables that have expressions associated with them
                // The parent, bind, already has bound variables for the non-expressions. 
                if ( ! projectionVars.hasExpr(v) )
                    continue ;
                
                Node n = projectionVars.get(v, bind, funcEnv) ;
                if ( n != null )
                    b.add(v, n) ;
            }
            return new BindingProject(projectionVars.getVars(), b) ;
        }
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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