/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import java.util.Collection;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.engine2.op.Quad;
import com.hp.hpl.jena.query.util.FmtUtils;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlRestrict;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.layout2.QuadBlockCompiler2;


public class QueryCompilerTrans extends QuadBlockCompiler2
{
    private TransTable transTable ;
    private String aliasBase ;
    Generator gen = null ;

    public QueryCompilerTrans(SDBRequest request, TransTable transTable)
    {
        this(request, transTable, "Trans");   
    }

    public QueryCompilerTrans(SDBRequest request, TransTable transTable, String aliasBase)
    {
        super(request) ;
        this.transTable = transTable ;   
        this.aliasBase = aliasBase ;
        this.gen = new Gensym(aliasBase) ;
    }
    
    @Override
    protected void addMoreConstants(Collection<Node> constants)
    {
        super.addMoreConstants(constants) ;
        constants.add(transTable.getProperty()) ;
    }
    
    @Override
    public SqlNode compile(Quad quad)
    {
        if ( ! quad.getPredicate().equals(transTable.getProperty()) )
            return super.compile(quad) ;
        
        String alias = gen.next() ;
        SqlExprList conditions = new SqlExprList() ;
        
        SqlTable transTripleTable = transTable.createSqlTable(alias) ;
        
        // TODO stringForQuad
        transTripleTable.addNote("Trans: "+FmtUtils.stringForTriple(quad.getTriple())) ;
        
        //processSlot(request, transTripleTable, conditions, quad.getGraph(), ?????) ;
        processSlot(request, transTripleTable, conditions, quad.getSubject(), transTable.getColLeft()) ; 
        processSlot(request, transTripleTable, conditions, quad.getObject(), transTable.getColRight()) ;
        
        if ( conditions.size() == 0 )
            return transTripleTable ;
        
        return SqlRestrict.restrict(transTripleTable, conditions) ;
    }
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