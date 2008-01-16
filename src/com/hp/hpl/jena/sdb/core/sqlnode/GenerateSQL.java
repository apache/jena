/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.store.SQLGenerator;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;

public class GenerateSQL implements SQLGenerator 
{
    public static boolean forceOldGenerator = false ; 
   
    public static String toSQL(SqlNode sqlNode)
    { return new GenerateSQL().generateSQL(sqlNode) ; }
    
    public static String toPartSQL(SqlNode sqlNode)
    { return new GenerateSQL().generatePartSQL(sqlNode) ; }
    
    /** Generate an SQL statement for the node - force the outer level to be a SELECT */
    public String generateSQL(SqlNode sqlNode)
    {
//        if ( forceOldGenerator )
//            return GenerateSQL_Old.toSQL(sqlNode) ;
        // Top must be a project to cause the SELECT to be written
        sqlNode = ensureProject(sqlNode) ;
        return generatePartSQL(sqlNode) ;
    }
    
    /** Generate an SQL string for the node - which may no tbe legal SQL (e.g. no outer SELECT).*/  
    public String generatePartSQL(SqlNode sqlNode)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        
        // Step one - rewrite the SQL node tree to have SelectBlocks, not the various SqlNodes
        // that contribute to a SELECT statement.
        
        // XXX Temp - the nodes tis tranforms should not be generated 
        sqlNode = SqlTransformer.transform(sqlNode, new TransformSelectBlock()) ;

        // Step two - turn the SqlNode tree, with SqlSelectBlocks in it,
        // in an SQL string.
        SqlNodeVisitor v = makeVisitor(buff) ;
        sqlNode.visit(v) ;
        return buff.asString() ;
    }
    
    protected SqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return new GenerateSQLVisitor(buff.getIndentedWriter()) ;
    }
    
    public static SqlNode ensureProject(SqlNode sqlNode)
    {
        if ( ! sqlNode.isProject() )
            sqlNode = SqlProject.project(sqlNode) ;
            
        return sqlNode ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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