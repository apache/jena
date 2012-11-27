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

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.store.SQLGenerator;
import org.apache.jena.atlas.io.IndentedLineBuffer;

public class GenerateSQL implements SQLGenerator 
{
    public static boolean forceOldGenerator = false ; 
   
    public static String toSQL(SDBRequest request, SqlNode sqlNode)
    { return new GenerateSQL().generateSQL(request, sqlNode) ; }
    
    public static String toPartSQL(SDBRequest request, SqlNode sqlNode)
    { return new GenerateSQL().generatePartSQL(sqlNode) ; }
    
    /** Generate an SQL statement for the node - force the outer level to be a SELECT */
    @Override
    public String generateSQL(SDBRequest request, SqlNode sqlNode)
    {
//        if ( forceOldGenerator )
//            return GenerateSQL_Old.toSQL(sqlNode) ;
        // Top must be a project to cause the SELECT to be written
        sqlNode = ensureProject(request, sqlNode) ;
        return generatePartSQL(sqlNode) ;
    }
    
    /** Generate an SQL string for the node - which may no tbe legal SQL (e.g. no outer SELECT).*/  
    public String generatePartSQL(SqlNode sqlNode)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        
        // Step one - rewrite the SQL node tree to have SelectBlocks, not the various SqlNodes
        // that contribute to a SELECT statement.
        
        // XXX Temp - the nodes this tranforms should not be generated now 
        //sqlNode = SqlTransformer.transform(sqlNode, new TransformSelectBlock()) ;

        // Step two - turn the SqlNode tree, with SqlSelectBlocks in it,
        // in an SQL string.
        SqlNodeVisitor v = makeVisitor(buff) ;
        sqlNode.visit(v) ;
        return buff.asString() ;
    }
    
    protected SqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return new GenerateSQLVisitor(buff) ;
    }
    
    public static SqlNode ensureProject(SDBRequest request, SqlNode sqlNode)
    {
        if ( ! sqlNode.isSelectBlock() )
            sqlNode = SqlSelectBlock.project(request, sqlNode) ;
        return sqlNode ;
    }
}
