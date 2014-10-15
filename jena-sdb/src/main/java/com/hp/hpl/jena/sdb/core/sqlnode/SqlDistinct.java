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

import com.hp.hpl.jena.sdb.shared.SDBInternalError;

// Not used - may be removed
/*public*/ class SqlDistinct extends SqlNodeBase1
{
    private static SqlNode distinct(SqlNode sqlNode)
    {
        return null ;
        //return SqlSelectBlock.distinct(sqlNode) ;
        
//        SqlDistinct p = null ;
//        if ( sqlNode.isDistinct() )
//            p = sqlNode.asDistinct() ;
//        else
//            p = new SqlDistinct(sqlNode) ;
//        return p ;
    }
    
    private SqlDistinct(SqlNode sqlNode)
    {
        super(null, sqlNode) ;
    }
    
    @Override
    public boolean isDistinct() { return true ; }
    @Override
    public SqlDistinct asDistinct() { return this ; }
    
    //@Override
    @Override
    public void visit(SqlNodeVisitor visitor)
    { throw new SDBInternalError("SqlDistinct.visit") ; /*visitor.visit(this) ;*/ }
    
    @Override
    public SqlNode apply(SqlTransform transform, SqlNode subNode)
    { throw new SDBInternalError("SqlDistinct.transform") ; /* return transform.transform(this, subNode) ; */ }

    @Override
    public SqlNode copy(SqlNode subNode)
    {
        return new SqlDistinct(subNode) ;
    }
}
