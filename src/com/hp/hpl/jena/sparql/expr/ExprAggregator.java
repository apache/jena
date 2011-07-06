/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import static org.openjena.atlas.lib.Lib.equal ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.util.Utils ;

/** Group aggregation functions calculated a value during grouping and
 *  placed in the output binding.  This class is relationship of 
 *  an aggregation expression and that variable.  Evaluation returns
 *  the variable's bound value. 
 */

public class ExprAggregator extends ExprNode
{
    protected Aggregator aggregator ;
    protected Var var ;
    protected ExprVar exprVar = null ;
    
    public ExprAggregator(Var v, Aggregator agg)          { _setVar(v) ; aggregator = agg ; }
    public Var getVar()                                 { return var ; }
    
    public void setVar(Var v)
    {
        if (this.var != null) 
            throw new ARQInternalErrorException(Utils.className(this)+ ": Attempt to set variable to " + v + " when already set as " + this.var) ;
        if (v == null) 
            throw new ARQInternalErrorException(Utils.className(this)+ ": Attempt to set variable to null") ;
        _setVar(v) ;
    }

    private void _setVar(Var v)
    {
        this.var = v ;
        this.exprVar = new ExprVar(var) ;
    }

    public Aggregator getAggregator()   { return aggregator ; }
    
    @Override
    public int hashCode()
    { 
        int x = aggregator.hashCode() ;
        if ( var != null )
            x ^= var.hashCode() ;
        return x ;
    }
    
    @Override
    public boolean equals(Object other) 
    {
        if ( this == other ) return true ;
        if ( ! ( other instanceof ExprAggregator ) )
            return false ;
        ExprAggregator agg = (ExprAggregator)other ;
        if ( ! equal(var, agg.var) )
            return false ;
        return equal(aggregator, agg.aggregator) ;
    }

    // Ensure no confusion - in an old design, an ExprAggregator was a subclass of ExprVar. 
    @Override
    public ExprVar getExprVar()
    { throw new ARQInternalErrorException() ; }
    
    @Override
    public Var asVar()
    { throw new ARQInternalErrorException() ; }
    
    public ExprVar getAggVar() { return exprVar ; }
    
    // As an expression suitable for outputting the calculation. 
    //@Override
    public String asSparqlExpr()        
    { return aggregator.toString() ; }
    
    @Override
    public ExprAggregator copySubstitute(Binding binding, boolean foldConstants)
    {
        Var v = var ;
        Aggregator agg = aggregator ;
        return new ExprAggregator(v, agg) ;
    }
    
    @Override
    public ExprAggregator applyNodeTransform(NodeTransform transform)
    {
        // Can't rewrite this to a non-variable.
        Node node = transform.convert(var) ;
        if ( ! Var.isVar(node) )
        {
            Log.warn(this, "Attempt to convert an aggregation variable to a non-variable: ignored") ;
            node = var ;
        }
        
        Var v = (Var)node ;
        Aggregator agg = aggregator.copyTransform(transform) ;
        return new ExprAggregator(Var.alloc(node), agg) ;
    }
    
    // DEBUGGING
    @Override
    public String toString()
    { return "(AGG "+
                (var==null?"<>":"?"+var.getVarName())+
                " "+aggregator.toString()+")"; }
    
    public Expr copy(Var v)  { return new ExprAggregator(v, aggregator.copy(aggregator.getExpr())) ; }
    
    @Override
    public NodeValue eval(Binding binding, FunctionEnv env)
    {
       return ExprVar.eval(var, binding, env) ;
    }
    
    public Expr apply(ExprTransform transform)  { return transform.transform(this) ; }
    
    public void visit(ExprVisitor visitor)
    { visitor.visit(this) ; }
    
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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