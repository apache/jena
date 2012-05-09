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


public class SqlNodeWalker 
{
    public SqlNodeVisitor visitor ;
    
    public SqlNodeWalker(SqlNodeVisitor visitor) { this.visitor = visitor ; }

    public static void walk(SqlNode node, SqlNodeVisitor visitor)
    {
        node.visit(new Walker(visitor)) ;
    }
    
    static class Walker implements SqlNodeVisitor
    {
        public SqlNodeVisitor visitor ;
        private Walker(SqlNodeVisitor visitor) { this.visitor = visitor ; }
        
        private void visit1(SqlNodeBase1 sqlNode)
        {
            sqlNode.visit(visitor) ;
            sqlNode.getSubNode().visit(this) ;
        }
        
        private void visit2(SqlNodeBase2 sqlNode)
        {
            sqlNode.visit(visitor) ;
            sqlNode.getLeft().visit(this) ;
            sqlNode.getRight().visit(this) ;
        }

        public void visit(SqlProject sqlNode)
        { visit1(sqlNode) ; }
    
        public void visit(SqlDistinct sqlNode)
        { visit1(sqlNode) ; }

        public void visit(SqlRestrict sqlNode)
        { visit1(sqlNode) ; }
        
        public void visit(SqlRename sqlNode)
        {
            sqlNode.visit(visitor) ;
            sqlNode.getSubNode().visit(this) ;
        }

        @Override
        public void visit(SqlTable sqlNode)
        { sqlNode.visit(visitor) ; }
        
        @Override
        public void visit(SqlJoinInner sqlNode)
        { visit2(sqlNode) ; }
    
        @Override
        public void visit(SqlJoinLeftOuter sqlNode)
        { visit2(sqlNode) ; }

        @Override
        public void visit(SqlUnion sqlNode)
        { visit2(sqlNode) ; }

        @Override
        public void visit(SqlCoalesce sqlNode)
        {
            sqlNode.visit(visitor) ;
            sqlNode.getJoinNode().visit(this) ;
        }

        public void visit(SqlSlice sqlNode)
        { visit1(sqlNode) ; }

        @Override
        public void visit(SqlSelectBlock sqlNode)
        { visit1(sqlNode) ; }
    }
}
