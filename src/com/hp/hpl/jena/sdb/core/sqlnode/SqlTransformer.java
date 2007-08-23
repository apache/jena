/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import java.util.Stack;

import com.hp.hpl.jena.sparql.util.ALog;

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
                ALog.warn(this, "Stack is not aligned") ;
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

        public void visit(SqlRestrict sqlRestrict)
        { visit1(sqlRestrict) ; }
        
        public void visit(SqlTable sqlTable)
        { visit0(sqlTable) ; }

        public void visit(SqlRename sqlRename)
        { visit1(sqlRename) ; }
        
        public void visit(SqlJoinInner sqlJoin)
        { visit2(sqlJoin) ; }

        public void visit(SqlJoinLeftOuter sqlJoin)
        { visit2(sqlJoin) ; }

        public void visit(SqlCoalesce sqlCoalesce)
        { visit1(sqlCoalesce) ; }

        public void visit(SqlSlice sqlSlice)
        { visit1(sqlSlice) ; }
        
    }
    
}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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