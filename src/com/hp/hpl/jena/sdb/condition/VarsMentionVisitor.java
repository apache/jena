/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.condition;

import java.util.Collection;

import com.hp.hpl.jena.query.core.Var;

public class VarsMentionVisitor implements SDBConstraintVisitor
{


    private Collection<Var> acc ;

    VarsMentionVisitor(Collection<Var> acc) { this.acc = acc ; }
    
    public void visitC2(C2 c2)
    {
        c2.getLeft().visit(this) ;
        c2.getRight().visit(this) ;
    }

    public void visitC1(C1 c1)
    {
        c1.getConstraint().visit(this) ;
    }

    public void visit(C_Var node)
    {
        acc.add(node.getVar()) ;
    }

    public void visit(C_NodeType node)
    {}

    public void visit(C_Regex regex)  { regex.getConstraint().visit(this) ; }

    public void visit(C_Equals c)     { visitC2(c) ; }

    public void visit(C_IsNotNull c)  { visitC1(c) ; }

    public void visit(C_IsNull c)     { visitC1(c) ; }    

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