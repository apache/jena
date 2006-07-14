/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.condition;

import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Regex;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented;

public class SqlExprGenerator
{
    public static SqlExpr compile(Scope scope, SDBConstraint c)
    {
        Generator g = new Generator(scope) ; 
        c.visit(g) ;
        return g.getResult() ;
    }
    
    static class Generator implements SDBConstraintVisitor
    {
    
        private Scope scope ;
        private SqlExpr result = null ; 
        
        public Generator(Scope scope) { this.scope = scope ; }
        public SqlExpr getResult()           { return result ; }
        

        public void visit(C_Regex regex)
        {
            SqlExpr sub = compile(scope, regex.getConstraint()) ;
            result = new S_Regex(sub,
                                 regex.getPattern(),
                                 regex.isCaseInsensitive() ? "i": null) ;
            
        }
        
        public void visit(C_Equals c)
        {
            result = new S_Equal(compile(scope, c.getLeft()),
                                 compile(scope, c.getRight())) ;
        }
        
        public void visit(C_Var node) { result = scope.getColumnForVar(node.getVar()) ; }

        public void visit(C_NodeType node) { throw new SDBNotImplemented("C_NodeType") ; } 

        public void visit(C_IsNotNull c)   { throw new SDBNotImplemented("C_IsNotNull") ; }

        public void visit(C_IsNull c)      { throw new SDBNotImplemented("C_IsNull") ; }
        
        public void visit(C_Constant constant)
        {
            result = new SqlConstant(constant.getValue()) ;
        }
        
        
 
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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