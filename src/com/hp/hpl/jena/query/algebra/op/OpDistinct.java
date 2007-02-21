/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.query.algebra.op;

import java.util.List;

import com.hp.hpl.jena.query.algebra.Op;
import com.hp.hpl.jena.query.algebra.OpVisitor;
import com.hp.hpl.jena.query.algebra.Transform;
import com.hp.hpl.jena.query.core.ARQNotImplemented;
import com.hp.hpl.jena.query.engine.ref.Evaluator;
import com.hp.hpl.jena.query.engine.ref.Table;
import com.hp.hpl.jena.query.util.Utils;

public class OpDistinct extends OpModifier
{
    private List vars ;
    
    public OpDistinct(Op subOp, List vars)
    { 
        super(subOp) ;
        this.vars = vars ;
    }
    
    public List getVars() { return vars ; }
    
    public Table eval_1(Table table, Evaluator evaluator)
    { return evaluator.distinct(table, vars) ; }

//    public Op apply(Transform transform, Op subOp)
//    { return transform.transform(this, subOp) ; }

    public String getName()                 { return "Distinct" ; }

    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    public Op copy(Op subOp)                { return new OpDistinct(subOp, vars) ; }

    public Op apply(Transform transform, Op subOp)
    { throw new ARQNotImplemented(Utils.className(this)+".apply") ; } 
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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