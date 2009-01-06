/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.Utils;

public class OpPath extends Op0
{
    private TriplePath triplePath ;

//    public OpPath(Node start, Path path, Node end)
//    {
//        this.subject = start ;
//        this.path = path ;
//        this.object = object ;
//    }
    
    public OpPath(TriplePath triplePath)
    {
        this.triplePath = triplePath ;
    }
    
    public TriplePath getTriplePath()   { return triplePath ; }

    public String getName()     { return Tags.tagPath ; }

    @Override
    public Op apply(Transform transform)
    { return transform.transform(this) ; }

    @Override
    public Op copy()    { return new OpPath(triplePath) ; }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap isoMap)
    {
        if ( ! (other instanceof OpPath) ) return false ;
        OpPath p = (OpPath)other ;
        return  Utils.triplePathIso(triplePath, p.triplePath, isoMap) ;
    }

    @Override
    public int hashCode()
    {
        return triplePath.hashCode() ;
    }

    public void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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