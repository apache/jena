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

package com.hp.hpl.jena.sdb.core.sqlexpr;


public class SqlExprWalker
{
    public static void walk(SqlExpr expr, SqlExprVisitor visitor)
    {
        expr.visit(new Walker(visitor)) ;
    }
    
    private static class Walker implements SqlExprVisitor
    {
        private SqlExprVisitor visitor ;

        private Walker(SqlExprVisitor visitor) { this.visitor = visitor ;  }
        
        @Override
        public void visit(SqlColumn column) { column.visit(visitor) ; }
        
        @Override
        public void visit(SqlConstant constant) { constant.visit(visitor) ; }
        
        @Override
        public void visit(SqlFunction1 expr)
        {
            expr.getExpr().visit(this) ;
            expr.visit(visitor) ;
        }
    
        @Override
        public void visit(SqlExpr1 expr)
        {
            expr.getExpr().visit(this) ;
            expr.visit(visitor) ;
        }
        @Override
        public void visit(SqlExpr2 expr)
        {
            expr.getLeft().visit(this) ;
            expr.getRight().visit(this) ;
            expr.visit(visitor) ;
        }
    
        @Override
        public void visit(S_Like like)         
        {
            like.getExpr().visit(this) ;
            like.visit(visitor) ;
        }
        
        @Override
        public void visit(S_Regex regex)
        {
            regex.getExpr().visit(this) ;
            regex.visit(visitor) ;
        }
    }
}
