/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.pfunction.PropFuncArg;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/** Property functions (or any OpBGP replacement)
 *  Execution will be per-engine specific
 * @author Andy Seaborne
 */
public class OpPropFunc extends Op0 // implements OpExt???
{
    private String uri ;
    private PropFuncArg args1 ;
    private PropFuncArg args2 ;

    public OpPropFunc(String uri, PropFuncArg args1 , PropFuncArg args2)
    {
        this.uri = uri ;
        this.args1 = args1 ;
        this.args2 = args2 ;
    }
    
    public OpBGP getBGP()
    {
        return null ;
    } 
    
    public Op apply(Transform transform)
    {
        //transform.transform(this) ;
        return null ;
    }

    public Op copy()
    {
        return new OpPropFunc(uri, args1, args2) ;
    }

    // XXX
    public int hashCode()
    {
        return getBGP().hashCode() ;
    }

    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpPropFunc ) ) return false ;
        OpPropFunc procFunc = (OpPropFunc)other ;
        return getBGP().equalTo(procFunc.getBGP(), labelMap) ;
    }

    public void visit(OpVisitor opVisitor)
    {} // { opVisitor.visit(this) ; }

    public String getName()
    {
        return null ;
    }
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