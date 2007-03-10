/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.store.TripleTableDesc;

public class SqlStageTripleTable implements SqlStage
{
    private static Log log = LogFactory.getLog(SqlStageTripleTable.class) ;
    
    // TODO Sort out generators
    static protected Generator genTableAlias = Gensym.create(AliasesSql.TriplesTableBase) ;
    private Quad quad ;

    public SqlStageTripleTable(Quad quad)
    {
        this.quad = quad ;
    }

    public SqlNode build(SDBRequest request, SlotCompiler slotCompiler)
    {
        String alias = genTableAlias.next();
        SqlExprList conditions = new SqlExprList() ;
        
        if ( ! quad.getGraph().equals(Quad.defaultGraph) )
        {
            log.fatal("Non-default graph") ;
            throw new SDBException("Non-default graph") ;
        }
        
        SqlTable triples = accessTriplesTable(request, alias) ;
        triples.addNote(FmtUtils.stringForTriple(quad.getTriple(), request.getPrefixMapping())) ;

        TripleTableDesc tripleTableDesc = request.getStore().getTripleTableDesc() ;
        
        if ( false )
            slotCompiler.processSlot(request, triples, conditions, quad.getGraph(),
                                     tripleTableDesc.getGraphColName()) ;
        slotCompiler.processSlot(request, triples, conditions,quad.getSubject(),
                                 tripleTableDesc.getSubjectColName()) ; 
        slotCompiler.processSlot(request, triples, conditions, quad.getPredicate(),
                                 tripleTableDesc.getPredicateColName()) ;
        slotCompiler.processSlot(request, triples, conditions, quad.getObject(),
                                 tripleTableDesc.getObjectColName()) ;
        
        return SqlRestrict.restrict(triples, conditions) ;
    }
    
    private SqlTable accessTriplesTable(SDBRequest request, String alias)
    {
        return new SqlTable(request.getStore().getTripleTableDesc().getTableName(), alias) ;
    }

    @Override
    public String toString() { return "Table: "+quad ; } 
    
    public void output(IndentedWriter out)
    {  out.print(toString()) ; }

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