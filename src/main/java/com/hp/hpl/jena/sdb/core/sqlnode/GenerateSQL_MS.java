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

import java.util.List;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;

public class GenerateSQL_MS extends GenerateSQL
{
    @Override
    protected SqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return new GeneratorVisitorMSSQL(buff) ;
    }
}

class GeneratorVisitorMSSQL extends GenerateSQLVisitor
{
    public GeneratorVisitorMSSQL(IndentedWriter out)
    { super(out) ; }
    
    @Override
    protected String leftJoinNoConditionsString()
    { return "1=1" ; }

    @Override
    protected void genLimitOffset(SqlSelectBlock sqlSelectBlock)
    {
        // Microsoft SQL Server does not support LIMIT or OFFSET.
        // Instead, if LIMIT or OFFSET are necessary, we generate a
        // query of this form:
        //
        // SELECT col1, col2 FROM (
        //    SELECT ROW_NUMBER() OVER (ORDER BY (SELECT 1)) AS __row_number,
        //    col1, col2
        //    FROM table ...
        // ) AS q
        // WHERE __row_number BETWEEN 5 AND 10
        // ORDER BY __row_number
}

    @Override
    protected void genPrefix(SqlSelectBlock sqlSelectBlock)
    {
        long length = sqlSelectBlock.getLength() ;
        long start = sqlSelectBlock.getStart() ;
        if( length >= 0 || start >= 0 ) {
            out.print( "SELECT " ) ;
            printColumnAliases(sqlSelectBlock.getCols()) ;
            out.println( " FROM (" ) ;
            out.incIndent();
        }
    }

    @Override
    protected void genColumnPrefix(SqlSelectBlock sqlSelectBlock)
    {
        long length = sqlSelectBlock.getLength() ;
        long start = sqlSelectBlock.getStart() ;
        if( length >= 0 || start >= 0 ) {
            out.print( " ROW_NUMBER() OVER (ORDER BY (SELECT 1)) AS " +
                "__row_number, ");
        }
    }

    @Override
    protected void genSuffix(SqlSelectBlock sqlSelectBlock)
    {
        long length = sqlSelectBlock.getLength() ;
        long start = sqlSelectBlock.getStart() ;
        if( length >= 0 || start >= 0 ) {
            out.decIndent();
            out.println( ") AS q") ;
            out.print( "WHERE " ) ;
            if ( length >= 0 && start >= 0 ) {
                out.println ( "__row_number BETWEEN " +(start+1)+" AND "+
                    (start+length)) ;
            } else if ( length >= 0 ) {
                out.println ( "__row_number <= "+length ) ;
            } else {
                out.println ( "__row_number >= "+(start+1) ) ;
            }
            out.println( "ORDER BY __row_number" ) ;
        }
    }

    protected void printColumnAliases(List<ColAlias> cols)
    {
        String sep = "" ;
        if ( cols.size() == 0 )
        {
            // Can happen - e.g. query with no variables.
            //log.info("No SELECT columns") ;
            out.print("1") ;
        }

        for ( ColAlias c : cols )
        {
            out.print(sep) ;
            out.print(c.getAlias().getColumnName());
            sep = ", " ;
        }
    }
}
