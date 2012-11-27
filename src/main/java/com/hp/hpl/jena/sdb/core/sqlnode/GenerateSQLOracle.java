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

public class GenerateSQLOracle extends GenerateSQL
{
    @Override
    protected SqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return new GeneratorVisitorOracle(buff) ;
    }
}

class GeneratorVisitorOracle extends GenerateSQLVisitor
{
    public GeneratorVisitorOracle(IndentedWriter out)
    { super(out) ; }

    // No "AS" in Oracle
    @Override
    protected String aliasToken()
    {
        return " " ;
    }
    
    // No "true" in Oracle
    @Override
    protected String leftJoinNoConditionsString()
    { return "1=1" ; }

    // TODO
    // It looks like the LIMIT only case can be done with SELECT * from T WHERE ROWNUM <= 10
    // but OFFSET/LIMIT needs SELECT * FROM (SELECT ... FROM ...) WHERE ROWNUM BETWEEN 3 AND 200
    
//    @Override
//    protected void genLimitOffset(SqlSelectBlock sqlSelectBlock)
//    { super.genLimitOffset(sqlSelectBlock) ; }
}
