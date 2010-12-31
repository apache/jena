/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.modify.request;

import java.util.List ;

import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.syntax.Element ;

public class UpdateModify extends UpdateWithUsing
{
    private final QuadAcc deletePattern ;
    private final QuadAcc insertPattern ;
    private boolean hasInsert = false ;
    private boolean hasDelete = false ;
    private Element wherePattern ;
    
    public UpdateModify() 
    { 
        this.deletePattern = new QuadAcc() ;
        this.insertPattern = new QuadAcc() ;
        this.wherePattern = null ;
    }
    
    public void setElement(Element element)
    {
        this.wherePattern = element ; 
    }
    
    public QuadAcc getDeleteAcc()
    {
        return deletePattern ;
    }

    public QuadAcc getInsertAcc()
    {
        return insertPattern ;
    }

    public List<Quad> getDeleteQuads()
    {
        return deletePattern.getQuads() ;
    }

    public List<Quad> getInsertQuads()
    {
        return insertPattern.getQuads() ;
    }

    /** Explicit flag to indicate a INSERT clause was seen, even if it had no quads */  
    public void setHasInsertClause(boolean flag)
    {
        hasInsert = flag ;
    }
    
    /** Explicit flag to indicate a DELETE clause was seen, even if it had no quads */  
    public void setHasDeleteClause(boolean flag)
    {
        hasDelete = flag ;
    }

    public boolean hasInsertClause()
    {
        if ( hasInsert ) return true ;
        return insertPattern.getQuads().size() > 0 ;
    }
    
    public boolean hasDeleteClause()
    {
        if ( hasDelete) return true ;
        return deletePattern.getQuads().size() > 0 ;
    }
    
    public Element getWherePattern()
    {
        return wherePattern ;
    }

    @Override
    public void visit(UpdateVisitor visitor)
    { visitor.visit(this) ; }
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