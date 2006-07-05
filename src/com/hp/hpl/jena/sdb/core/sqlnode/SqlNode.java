/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.Annotations;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.query.util.Printable;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;

public interface SqlNode extends Printable, Scope, Annotations
{
    public String         getAliasName() ;
    
    public boolean        isJoin() ;
    public SqlJoin        getJoin() ;
    
    public boolean        isRestrict() ;                // isSelect is confusing
    public SqlRestrict    getRestrict() ;
    
    public boolean        isProject() ;
    public SqlProject     getProject() ;
    
    public boolean        isTable() ;
    public SqlTable       getTable() ;
    
    // Scope
//    public boolean        hasColumnForVar(Var var) ;
//    public SqlColumn      getColumnForVar(Var var) ;       // Get the col for this var - or null. 

    //public SqlColumn      getValColumnForVar(Var var, ValueSpace vSpace) ;
    //public void           setColumnForVar(Var var, SqlColumn) ; 
    
    
    public boolean usesColumn(SqlColumn c) ;  // Does this subtree mentioned a column?
    
    public void visit(SqlNodeVisitor visitor) ;
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