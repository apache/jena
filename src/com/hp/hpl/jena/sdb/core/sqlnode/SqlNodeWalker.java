/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    
        public void visit(SqlRestrict sqlNode)
        { visit1(sqlNode) ; }
        
        public void visit(SqlRename sqlNode)
        {
            sqlNode.visit(visitor) ;
            sqlNode.getSubNode().visit(this) ;
        }

        public void visit(SqlTable sqlNode)
        { sqlNode.visit(visitor) ; }
        
        public void visit(SqlJoinInner sqlNode)
        { visit2(sqlNode) ; }
    
        public void visit(SqlJoinLeftOuter sqlNode)
        { visit2(sqlNode) ; }

        public void visit(SqlCoalesce sqlNode)
        {
            sqlNode.visit(visitor) ;
            sqlNode.getJoinNode().visit(this) ;
        }

        public void visit(SqlSlice sqlNode)
        { visit1(sqlNode) ; }

        public void visit(SqlSelectBlock sqlNode)
        { visit1(sqlNode) ; }
    }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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