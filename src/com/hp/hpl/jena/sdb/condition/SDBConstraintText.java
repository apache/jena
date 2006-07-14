/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.condition;

import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.sql.SQLUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SDBConstraintText implements SDBConstraintVisitor
{
    static private Log log = LogFactory.getLog(SDBConstraintText.class) ;
    private IndentedWriter out ;
    
    public SDBConstraintText(IndentedWriter out) { this.out = out ; }
    
    public void visitC2(C2 c2)
    {
        // Prefix notation
        out.print("(") ;
        out.print(c2.getLabel()) ;
        out.print(" ") ;
        c2.getLeft().visit(this) ;
        out.print(" ") ;
        c2.getRight().visit(this) ;
        out.print(")") ;
        
//        out.print("( ") ;
//        c2.getLeft().visit(this) ;
//        out.print(" ") ;
//        out.print(c2.getLabel()) ;
//        out.print(" ") ;
//        c2.getRight().visit(this) ;
//        out.print(")") ;
    }

    public void visit(C_Var node)
    {
        out.print("?"+node.getVar().getName()) ;
    }

    
    public void visit(SDBConstraint c)
    {
        LogFactory.getLog(c.getClass()).warn("Not implemented") ;
    }

    public void visitC1(C1 c1)
    {
        out.print("( ") ;
        out.print(c1.getLabel()) ;
        out.print(" ") ;
        c1.getConstraint() ; 
        out.print(")") ;
    }

    public void visit(C_IsNull c)     { visitC1(c) ; }
    public void visit(C_IsNotNull c)  { visitC1(c) ; }

    public void visit(C_NodeType node) { out.print("C_NodeType") ; }

    public void visit(C_Regex regex)
    {
        
        out.print(String.format("regex(%s, %s",
                  regex.getConstraint().toString(),
                  SQLUtils.quote(regex.getPattern()))) ;
        if ( regex.isCaseInsensitive() )
            out.print(", \"i\"") ;
        out.print(")") ;
    }

    public void visit(C_Equals c) { visitC2(c) ; } 
    
    public void visit(C_Constant c)
    {
        out.print(SQLUtils.quote(c.getValue())) ;
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