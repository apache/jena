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

import java.util.Stack;

import org.apache.jena.atlas.logging.Log ;


public class SqlTransformer
{
    public static SqlNode transform(SqlNode sqlNode, SqlTransform transform)
    {
        SqlTransformVisitor v = new SqlTransformVisitor(transform) ;
        sqlNode.visit(v) ;
        return v.result() ;
    }
    
    private static class SqlTransformVisitor implements SqlNodeVisitor
    {

        Stack<SqlNode> stack = new Stack<SqlNode>() ;
        private SqlTransform transform ;

        public SqlTransformVisitor(SqlTransform transform)
        {
            this.transform = transform ; 
        }

        public SqlNode result()
        {
            if ( stack.size() != 1 )
                Log.warn(this, "Stack is not aligned") ;
            return stack.pop() ; 
        }

        private void visit0(SqlNodeBase0 n)
        {
            stack.push(n.apply(transform)) ;
        }

        private void visit1(SqlNodeBase1 n1)
        {
            n1.getSubNode().visit(this) ;
            SqlNode s = stack.pop() ;
            stack.push(n1.apply(transform, s)) ;
        }
        
        private void visit2(SqlNodeBase2 n2)
        {
            n2.getLeft().visit(this) ;
            SqlNode left = stack.pop() ;
            n2.getRight().visit(this) ;
            SqlNode right = stack.pop() ;
            stack.push(n2.apply(transform, left, right)) ;
        }

        public void visit(SqlProject sqlProject)
        {
            sqlProject.getSubNode().visit(this) ;
            SqlNode s = stack.pop() ;
            SqlNode p = transform.transform(sqlProject, s) ;
            stack.push(p) ;
        }

        public void visit(SqlDistinct sqlDistinct)
        { visit1(sqlDistinct) ; }

        public void visit(SqlRestrict sqlRestrict)
        { visit1(sqlRestrict) ; }
        
        @Override
        public void visit(SqlTable sqlTable)
        { visit0(sqlTable) ; }

        public void visit(SqlRename sqlRename)
        { visit1(sqlRename) ; }
        
        @Override
        public void visit(SqlJoinInner sqlJoin)
        { visit2(sqlJoin) ; }

        @Override
        public void visit(SqlJoinLeftOuter sqlJoin)
        { visit2(sqlJoin) ; }

        @Override
        public void visit(SqlUnion sqlUnion)
        { visit2(sqlUnion) ; }

        @Override
        public void visit(SqlCoalesce sqlCoalesce)
        { visit1(sqlCoalesce) ; }

        public void visit(SqlSlice sqlSlice)
        { visit1(sqlSlice) ; }

        @Override
        public void visit(SqlSelectBlock sqlSelectBlock)
        { visit1(sqlSelectBlock) ; }
    }
    
}
