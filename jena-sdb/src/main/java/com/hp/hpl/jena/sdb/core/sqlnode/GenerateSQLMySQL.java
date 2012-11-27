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

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import com.hp.hpl.jena.sdb.core.JoinType;

public class GenerateSQLMySQL extends GenerateSQL
{
    @Override
    protected SqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return new GeneratorVisitorMySQL(buff) ;
    }
}

class GeneratorVisitorMySQL extends GenerateSQLVisitor
{
    // STRAIGHT_JOIN stops the optimizer reordering inner join
    // It requires that the left table and right table are kept as left and right,
    // so a sequence of joins can not be reordered. 
    
    static final String InnerJoinOperatorStraight = "STRAIGHT_JOIN" ;
    static final String InnerJoinOperatorDefault = JoinType.INNER.sqlOperator() ;
    
    public GeneratorVisitorMySQL(IndentedWriter out) { super(out) ; }

    @Override
    public void visit(SqlJoinInner join)
    { 
        join = rewrite(join) ;
        visitJoin(join, InnerJoinOperatorDefault) ;
    }   
    
    @Override
    protected void genLimitOffset(SqlSelectBlock sqlSelectBlock)
    {
        if ( sqlSelectBlock.getLength() >= 0 || sqlSelectBlock.getStart() >= 0 )
        {
            // MySQL syntax issue - need LIMIT even if only OFFSET
            long length = sqlSelectBlock.getLength() ;
            if ( length < 0 )
            {
                sqlSelectBlock.addNote("Require large LIMIT") ;
                length = Long.MAX_VALUE ;
            }
            out.println("LIMIT "+length) ;
            if ( sqlSelectBlock.getStart() >= 0 )
                out.println("OFFSET "+sqlSelectBlock.getStart()) ;
        }
    }
}
