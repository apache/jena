/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sparql.serializer.SerializationContext;

/** A function in the expression hierarchy.
 *  Everything that is evaluable (i.e. not NodeValue, NodeVar) is a function).
 *  It is useful to distinguish between values, vars and functions.
 *  The exists operations (which take a op, not arguments) are functions of zero arguments.
 */
 
public abstract class ExprFunction extends ExprNode
{
    protected FunctionLabel funcSymbol ;
    protected String opSign ;
    
    protected ExprFunction(String fName)
    {
        funcSymbol = new FunctionLabel(fName) ;
        opSign = null ;
    }

    protected ExprFunction(String fName, String opSign)
    {
        this(fName) ;
        this.opSign = opSign ;
    }

    public abstract Expr getArg(int i) ;
    public abstract int numArgs() ;

    public List<Expr> getArgs()
    {
        List<Expr> x = new ArrayList<Expr>() ;
        for ( int i = 1 ; i <= numArgs() ; i++ )
            x.add(this.getArg(i)) ;
        return x ;        
    }

    @Override
    public boolean isFunction()        { return true ; }
    @Override
    public ExprFunction getFunction()  { return this ; }
    
    @Override
    public int hashCode()
    {
        return funcSymbol.hashCode() ^ numArgs() ;
    }
    
    // A function is equal if:
    // + The name is the same
    // + The arguments are the same (including arity).
    
    @Override
    public boolean equals(Object other)
    {
        if ( this == other ) return true ;

        if ( ! other.getClass().equals(this.getClass()) )
            return false ;
        
        ExprFunction ex = (ExprFunction)other ;
        
        if ( ! funcSymbol.equals(ex.funcSymbol) )
            return false ;
        
        if ( numArgs() != ex.numArgs() )
            return false ;
        
        // Arguments are 1, 2, 3, ...
        for ( int i = 1 ; i <= numArgs() ; i++ )
        {
            Expr a1 = this.getArg(i) ;
            Expr a2 = ex.getArg(i) ;
            if ( ! a1.equals(a2) )
                return false ;
        }
        return true ;
    }
    
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    
    /** Name used for output:
     *    SPARQL format: just the extension functions
     *    Prefix format: the function name, dafaulting to the symbol string
     *  Overrided in ExprFunctionN 
     */
    
    public String getFunctionPrintName(SerializationContext cxt)
    { return funcSymbol.getSymbol() ; }

    /** Name as a simple name or "function" */
    public String getFunctionName(SerializationContext cxt)
    { return funcSymbol.getSymbol() ; }

    public FunctionLabel getFunctionSymbol()
    { return funcSymbol ; }

    public String getFunctionIRI() { return null ; }

    public String getOpName()
    { return opSign ; }
}

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
