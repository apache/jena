/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import com.hp.hpl.jena.sparql.engine.Renamer ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

/** An expression that is constant (does not depend on evaluating a sub expression).
 */

public abstract class ExprFunction0 extends ExprFunction
{
    protected ExprFunction0(String fName) { this(fName, null) ; }
    
    protected ExprFunction0(String fName, String opSign)
    {
        super(fName, opSign) ;
    }

    @Override
    public Expr getArg(int i)       { return null ; }
    
    @Override
    public int hashCode()           { return getFunctionSymbol().hashCode() ; }

    @Override
    public int numArgs()            { return 0 ; }
    
    // ---- Evaluation
   
    public abstract NodeValue eval(FunctionEnv env)  ;
    
    //@Override
    final public Expr copyNodeTransform(Renamer renamer)
    {
        // Nothing to transform. 
        return copy() ;
    }
    
    public abstract Expr copy() ;
    
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform) { return transform.transform(this) ; }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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