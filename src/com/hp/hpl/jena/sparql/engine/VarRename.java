/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine;

import java.util.Collection ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprList ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.graph.NodeTransformLib ;

public class VarRename
{
    // See also OpVar, VarFinder and VarLib - needs to be pulled together really.
    // Also need to renaming support for renames where only a
    // certain set are mapped (for (assign (?x ?.0)))
    
    private static final String prefix = ARQConstants.allocVarScopeHiding ;
    
    /** Rename all variables in a pattern, EXCEPT for those named as constant */ 
    public static Op rename(Op op, Collection<Var> constants)
    {
        return NodeTransformLib.transform(new RenameVars(constants, prefix), op) ;
    }

    /** Rename all variables in an expression, EXCEPT for those named as constant */ 
    public static ExprList rename(ExprList exprList, Set<Var> constants)
    {
        NodeTransform renamer = new RenameVars(constants, prefix) ;
        return NodeTransformLib.transform(renamer, exprList) ;
    }
        
    public static Expr rename(Expr expr, Set<Var> constants)
    {
        NodeTransform renamer = new RenameVars(constants, prefix) ;
        return NodeTransformLib.transform(renamer, expr) ;
    }
    
    /** Undo the effect of the rename operation, once or repeatedly.
     * This assumes the op was renamed by VarRename.rename */
    public static Op reverseRename(Op op, boolean repeatedly)
    {
        NodeTransform renamer = new UnrenameVars(prefix, repeatedly) ;
        return NodeTransformLib.transform(renamer, op) ;
    }
    
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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