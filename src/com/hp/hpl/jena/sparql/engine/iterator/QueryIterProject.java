/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingProject;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrintUtils;
import com.hp.hpl.jena.sparql.util.Utils;


public class QueryIterProject extends QueryIterConvert
{
    List projectionVars ;

    private QueryIterProject(QueryIterator input, List vars, ExecutionContext qCxt)
    {
        this(input, vars, null, qCxt) ;
    }
    
    public QueryIterProject(QueryIterator input, List vars, Map exprs, ExecutionContext qCxt)
    {
        super(input, new Projection(vars, exprs, qCxt), qCxt) ;
        Var.checkVarList(vars) ;
        checkExprs(vars, exprs) ;
        projectionVars = vars ;
    }

    public List getProjectionVars()   { return projectionVars ; }

    private void checkExprs(List vars, Map exprs)
    {
        if ( exprs == null )
            return ;
//        // Initialise to all varables not used as an expression name.
//        Set scope = new HashSet(vars) ;
//        scope.removeAll(exprs.keySet()) ;
//
//        // For each expression
//        for ( Iterator iter = exprs.keySet().iterator() ; iter.hasNext() ; )
//        {
//            Var v = (Var)iter.next();
//            Expr ex = (Expr)exprs.get(v) ;
//            Set eVars = ex.getVarsMentioned() ;
//            if ( ! scope.containsAll(eVars) )
//            {
//                for ( Iterator iter2 = eVars.iterator() ; iter2.hasNext() ; )
//                {
//                    Var ev2 = (Var)iter2.next();
//                    if ( !scope.contains(ev2) )
//                        throw new QueryException("Expression involves a variable ("+ev2+") not yet mentioned: "+ex) ;
//                }
//                // Should not happen
//                throw new QueryException("Expression involves a variable not yet mentioned: "+ex) ;
//            }
//            // Add in the variable of the expression 
//            scope.add(v) ;
//        }
    }

    protected void releaseResources()
    {}

    protected void details(IndentedWriter out, SerializationContext sCxt)
    {
        out.print(Utils.className(this)) ;
        out.print(" ") ;
        PrintUtils.printList(out, projectionVars) ;
    }
    
    static
    class Projection implements QueryIterConvert.Converter
    {
        FunctionEnv funcEnv ;
        List projectionVars ; 
        Map exprs ;

        Projection(List vars, Map exprs, ExecutionContext qCxt)
        { 
            this.projectionVars = vars ;
            this.exprs = exprs ;
            funcEnv = qCxt ;
        }

        public Binding convert(Binding bind)
        {
            if ( exprs == null || exprs.size() == 0 )
                return new BindingProject(projectionVars, bind) ;
            // Create a new binding that wraps the undelying one and adds the expressions.
            Binding b = new BindingMap(bind) ;
            for ( Iterator iter = exprs.keySet().iterator() ; iter.hasNext() ; )
            {
                Var v = (Var)iter.next();
                Expr expr = (Expr)exprs.get(v) ;
                try {
                    Node n = expr.eval(b, funcEnv).asNode() ;
                    b.add(v, n) ;
                } catch (ExprEvalException ex)
                //{ ALog.warn(this, "Eval failure "+expr+": "+ex.getMessage()) ; }
                { }
            }
            return new BindingProject(projectionVars, b) ;
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