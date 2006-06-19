/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.condition;

import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.sdb.core.CompileContext;

import org.apache.commons.logging.LogFactory;

public class SQLCondition implements SDBConstraintVisitor
{
    private IndentedWriter out ;
    private CompileContext cxt ;
    
    public SQLCondition(IndentedWriter out, CompileContext cxt)
    { 
        this.out = out ;
        this.cxt = cxt ;
    }
    
    public void visit(C2 c2)
    {
        out.print("( ") ;
        c2.getLeft().visit(this) ;
        out.print(") ") ;
        out.print(c2.getLabel()) ;
        out.print(" ( ") ;
        c2.getRight().visit(this) ;
        out.print(")") ;
    }

    public void visit(C_Node node)
    {
        //out.print(cxt.getAlias(node.getVar()).asString()) ;
    }

    
    public void visit(SDBConstraint c)
    {
        LogFactory.getLog(c.getClass()).warn("Not implemented") ;
    }

    public void visit(C1 c1) {}

    public void visit(C_IsNull c)     { LogFactory.getLog(c.getClass()).warn("Not implemented/IsNull") ; }
    public void visit(C_IsNotNull c)  { LogFactory.getLog(c.getClass()).warn("Not implemented/IsNotNull") ; }

    public void visit(C_NodeType node)
    {}
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