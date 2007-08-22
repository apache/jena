/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.gen;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.sdb.core.sqlnode.SqlJoin;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sparql.expr.ExprList;

/** A unit that generates an SQL SELECT Statement.
 *  The SQL generation process is a pass over the SqlNdoe structure to generate SelectBlocks,
 *  then to generate the SQL strings.
 * 
 * @author Andy Seaborne
 * @version $Id$
 */

public class SqlSelectBlock
{
    // Mapping of names
    // projection
    // Joins (inner and left)
    // Restriction.
    // Group
    // aggregrate
    // order
    // limit/offset
    
    //Scope scope = new ScopeBase() ; 
    List<String> outputCols = new ArrayList<String>() ;
    
    public List<String> getCols()       { return null ; }
    public List<SqlTable> getTables()   { return null ; }
    public SqlJoin getJoin()            { return null ; }
    public ExprList exprs()             { return null ; } 
    
    private long limit = -1 ;
    private long offset = -1 ;
    
    public long getLimit()              { return limit ; }
    public void setLimit(long limit)    { this.limit = limit ; }
    public long getOffset()             { return offset ; }
    public void setOffset(long offset)  { this.offset = offset ; }
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