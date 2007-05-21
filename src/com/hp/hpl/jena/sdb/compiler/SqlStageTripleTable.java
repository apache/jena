/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.compiler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class SqlStageTripleTable implements SqlStage
{
    private static Log log = LogFactory.getLog(SqlStageTripleTable.class) ;
    private Quad quad ;

    public SqlStageTripleTable(Quad quad)
    {
        this.quad = quad ;
    }

    public SqlNode build(SDBRequest request, SlotCompiler slotCompiler)
    {
        SqlExprList conditions = new SqlExprList() ;
        boolean defaultGraph = quad.getGraph().equals(Quad.defaultGraph) ;
        
        // CHECKING
        if ( ! defaultGraph )
        {
            log.fatal("Non-default graph") ;
            throw new SDBException("Non-default graph") ;
        }
        
        // The default graph table may be a specialized quad table,
        // or it may be the same as the quad table with a known name.
        
        TableDescQuads tableDesc = null ;
        String alias = null ;
        
        if ( defaultGraph )
        {
            tableDesc = request.getStore().getTripleTableDesc() ;
            alias = request.genId(AliasesSql.TriplesTableBase) ;
        }
        else
        {
            tableDesc = request.getStore().getQuadTableDesc() ;
            alias = request.genId(AliasesSql.QuadTableBase) ;
        }
        
        SqlTable table = new SqlTable(tableDesc.getTableName(), alias) ;
        table.addNote(FmtUtils.stringForTriple(quad.getTriple(), request.getPrefixMapping())) ;

        if ( tableDesc.getGraphColName() != null )
            slotCompiler.processSlot(request, table, conditions, quad.getGraph(),
                                     tableDesc.getGraphColName()) ;
        slotCompiler.processSlot(request, table, conditions, quad.getSubject(),
                                 tableDesc.getSubjectColName()) ; 
        slotCompiler.processSlot(request, table, conditions, quad.getPredicate(),
                                 tableDesc.getPredicateColName()) ;
        slotCompiler.processSlot(request, table, conditions, quad.getObject(),
                                 tableDesc.getObjectColName()) ;
        
        return SqlRestrict.restrict(table, conditions) ;
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