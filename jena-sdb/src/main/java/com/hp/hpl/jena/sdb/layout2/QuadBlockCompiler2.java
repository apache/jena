/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sdb.layout2;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.compiler.QuadBlockCompilerMain;
import com.hp.hpl.jena.sdb.compiler.SlotCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;


// Only here to think about addRestrictions later.
public class QuadBlockCompiler2 extends QuadBlockCompilerMain
{
    //private static Logger log = LoggerFactory.getLogger(QuadBlockCompiler2.class) ;

    public QuadBlockCompiler2(SDBRequest request, SlotCompiler slotCompiler)
    { 
        super(request, slotCompiler) ;
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
}
