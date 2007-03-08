/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.compiler.QuadBlockCompilerTriple;
import com.hp.hpl.jena.sdb.compiler.SlotCompiler;
import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.Generator;
import com.hp.hpl.jena.sdb.core.Gensym;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.store.NodeTableDesc;
import com.hp.hpl.jena.sdb.store.TripleTableDesc;
import com.hp.hpl.jena.sparql.core.Var;

public class QuadBlockCompiler2 extends QuadBlockCompilerTriple
{
    private static Log log = LogFactory.getLog(QuadBlockCompiler2.class) ;
    
    private Generator genNodeResultAlias = Gensym.create(AliasesSql.NodesResultAliasBase) ;

    // Slot stuff.
    List<Node> constants = new ArrayList<Node>() ;
    List<Var>  vars = new ArrayList<Var>() ;
    // --
    
    protected TripleTableDesc tripleTableDesc ;
    protected NodeTableDesc   nodeTableDesc ;
    
    public QuadBlockCompiler2(SDBRequest request, SlotCompiler slotCompiler)
    { 
        super(request, slotCompiler) ;
        tripleTableDesc = request.getStore().getTripleTableDesc() ;
        nodeTableDesc = request.getStore().getNodeTableDesc() ;
    }


    // Maybe needed, later, for value-based testing
    
//    private SqlNode addRestrictions(SDBRequest request,
//                                    SqlNode sqlNode,
//                                    List<SDBConstraint> constraints)
//    {
//        if ( constraints.size() == 0 )
//            return sqlNode ;
//
//        // Add all value columns
//        for ( SDBConstraint c : constraints )
//        {
//            @SuppressWarnings("unchecked")
//            Set<Var> vars = c.getExpr().getVarsMentioned() ;
//            for ( Var v : vars )
//            {
//                // For Variables used in this SQL constraint, make sure the value is available.  
//                ScopeEntry e = sqlNode.getIdScope().findScopeForVar(v) ;  // tripleTableCol
//                
//                if ( e == null )
//                {
//                    // Not in scope.
//                    log.info("Var not in scope for value of expression: "+v) ;
//                    continue ;
//                }
//                SqlColumn tripleTableCol = e.getColumn() ;    
//
//                // Value table column
//                SqlTable nTable =   new SqlTable(nodeTableDesc.getTableName(), genNodeResultAlias.next()) ;
//                SqlColumn colId =   new SqlColumn(nTable, nodeTableDesc.getIdColName()) ;
//                SqlColumn colLex =  new SqlColumn(nTable, nodeTableDesc.getLexColName()) ;
////                SqlColumn colType = new SqlColumn(nTable, nodeTableDesc.getTypeColName()) ;
//
//                nTable.setValueColumnForVar(v, colLex) ;        // ASSUME lexical/string form needed 
//                nTable.setIdColumnForVar(v, colId) ;            // Id scope => join
//                sqlNode = QC.innerJoin(request, sqlNode, nTable) ;
//            }
//
//            SqlExpr sqlExpr = c.compile(sqlNode.getNodeScope()) ;
//            sqlNode = SqlRestrict.restrict(sqlNode, sqlExpr) ;
//        }
//        return sqlNode ;
//    }

    @Override
    final protected SqlTable accessTriplesTable(String alias)
    {
        return new SqlTable(tripleTableDesc.getTableName(), alias) ;
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