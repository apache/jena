/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.util.alg.Action;
import com.hp.hpl.jena.sdb.util.alg.Filter;
import com.hp.hpl.jena.sdb.util.alg.Transform;

public class ScopeEntry
{
    Var var;
    SqlColumn column;
    ScopeStatus status ;
    
    public static Filter<ScopeEntry> OptionalFilter = new Filter<ScopeEntry>()
    {
        public boolean accept(ScopeEntry item)
        { return item.getStatus() == ScopeStatus.OPTIONAL ; }
    } ;
    
    public static Transform<ScopeEntry, Var> ToVar = new Transform<ScopeEntry, Var>()
    {
        public Var convert(ScopeEntry item)
        { return item.getVar() ; } 
    } ;
    
    public static Action<ScopeEntry> SetOpt = new Action<ScopeEntry>()
    {
        public void apply(ScopeEntry item)
        { item.setStatus(ScopeStatus.OPTIONAL) ; } 
    } ;

    
    public ScopeEntry(Var var, SqlColumn column)
    { this(var, column, ScopeStatus.FIXED) ; }

    private ScopeEntry(Var var, SqlColumn column, ScopeStatus status)
    {
        this.var = var ; 
        this.column = column ;
        this.status = status ;
    }

    public SqlColumn getColumn()
    {
        return column ;
    }

    public ScopeStatus getStatus()
    {
        return status ;
    }

    public void setStatus(ScopeStatus newStatus)
    {
        status = newStatus ;
    }

    public boolean isOptional()     { return hasStatus(ScopeStatus.OPTIONAL) ; }
    public boolean isFixed()        { return hasStatus(ScopeStatus.FIXED) ; }
    
    public boolean hasStatus(ScopeStatus testStatus2)
    { return status == testStatus2 ; }
    
    public Var getVar()
    {
        return var ;
    }
    
    @Override
    public String toString() { return "("+var+", "+column+"/"+status.name()+")" ; }
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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