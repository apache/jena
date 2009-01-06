/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.binding;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.expr.*;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.sparql.util.NodeUtils;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.SortCondition;

public class BindingComparator implements java.util.Comparator<Binding>
{
    List<SortCondition> conditions ;
    private FunctionEnv env ;
    
    public BindingComparator(List<SortCondition> conditions, ExecutionContext execCxt)
    {
        this.conditions = conditions ;
        env = execCxt ;
    }
    
    public BindingComparator(List<SortCondition> _conditions)
    {
        conditions = _conditions ;
        this.env = new FunctionEnvBase();
    }

    // Compare bindings by iterating.
    // Node comparsion is:
    //  Compare by 

    public int compare(Binding bind1, Binding bind2)
    {
        for ( Iterator<SortCondition> iter = conditions.iterator() ; iter.hasNext() ; )
        {
            SortCondition sc = iter.next() ;
            if ( sc.expression == null )
                throw new QueryExecException("Broken sort condition") ;

            NodeValue nv1 = null ;
            NodeValue nv2 = null ;
            
            try { nv1 = sc.expression.eval(bind1, env) ; }
            catch (VariableNotBoundException ex) {}
            catch (ExprEvalException ex)
            { ALog.warn(this, ex.getMessage()) ; }
            
            try { nv2 = sc.expression.eval(bind2, env) ; }
            catch (VariableNotBoundException ex) {}
            catch (ExprEvalException ex)
            { ALog.warn(this, ex.getMessage()) ; }
            
            Node n1 = NodeValue.toNode(nv1) ;
            Node n2 = NodeValue.toNode(nv2) ;
            int x = compareNodes(nv1, nv2, sc.direction) ;
            if ( x != Expr.CMP_EQUAL )
                return x ;
        }
        // Same by the SortConditions - now do any extra tests to make sure they are unique.
        return compareBindingsSyntactic(bind1, bind2) ;
        //return 0 ;
    }
    
    private static int compareNodes(NodeValue nv1, NodeValue nv2, int direction)
    {
        int x = compareNodesRaw(nv1, nv2) ;
        if ( direction == Query.ORDER_DESCENDING )
            x = -x ;
        return x ;
    }
    
    public static int compareNodesRaw(NodeValue nv1, NodeValue nv2)
    {
        // Absent nodes sort to the start
        if ( nv1 == null )
            return nv2 == null ? Expr.CMP_EQUAL : Expr.CMP_LESS ;

        if ( nv2 == null )
            return Expr.CMP_GREATER ;
        
        // Compare - always getting a result.
        return NodeValue.compareAlways(nv1, nv2) ;
    }
    

    public static int compareBindingsSyntactic(Binding bind1, Binding bind2)
    {
        int x = 0 ;
        for ( Iterator<Var> iter = bind1.vars() ; iter.hasNext() ; )
        {
            Var v = iter.next() ;
            Node n1 = bind1.get(v) ;
            Node n2 = bind2.get(v) ;
            x = NodeUtils.compareRDFTerms(n1, n2) ; 
            if  ( x != 0 )
                return x ;
        }
        return x ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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