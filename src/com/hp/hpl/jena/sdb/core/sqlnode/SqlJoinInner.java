/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.JoinType;


public class SqlJoinInner extends SqlJoin
{
    public SqlJoinInner(SqlNode left, SqlNode right)
    { super(JoinType.INNER, left, right) ; }
    
    public SqlJoinInner(SqlNode left, SqlNode right, String alias)
    {
        super(alias, JoinType.INNER, left, right) ;
    }
    
    @Override 
    public boolean   isInnerJoin()        { return true ; }

    @Override 
    public SqlJoinInner   asInnerJoin()         { return this ; }
    
    public void visit(SqlNodeVisitor visitor) { visitor.visit(this) ; }
    
    @Override
    public SqlNode apply(SqlTransform transform, SqlNode left, SqlNode right)
    { return transform.transform(this, left, right) ; }

    @Override
    public SqlNode copy(SqlNode left, SqlNode right)
    {
        SqlJoin j = new SqlJoinInner(left, right, getAliasName()) ;
        j.addConditions(this.getConditions()) ;
        return j ;

    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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