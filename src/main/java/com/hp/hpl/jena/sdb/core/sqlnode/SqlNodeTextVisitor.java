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

import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sdb.core.Annotations;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;

public class SqlNodeTextVisitor implements SqlNodeVisitor
{
    private static final String DelimOpen = "" ;
    private static final String DelimClose = "" ;
    
    final static boolean closeOnSameLine = true ;
    private IndentedWriter out ;
    private boolean withAnnotations = true ;
    private static final int annotationColumn = 40 ; 

    public SqlNodeTextVisitor(IndentedWriter out) { this.out = out ; }
    public SqlNodeTextVisitor(IndentedWriter out, boolean withAnnotations)
    { this.out = out ; this.withAnnotations = withAnnotations ; }
    
    public void visit(SqlProject sqlNode)
    {
        start(sqlNode, "Project", sqlNode.getAliasName()) ;
        if ( sqlNode.getCols().size() == 0 )
            out.println("<no cols>") ;
        else
            print(sqlNode.getCols()) ;
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }

    private void print(List<ColAlias> cols)
    {
        boolean first = true ; 
        String currentPrefix = null ; 
        for ( ColAlias c : cols )
        {
            if ( ! first ) out.print(" ") ;
            first = false ;
            
            // Choose split points.
            String cn = c.getColumn().getFullColumnName() ;
            int j = cn.lastIndexOf(".") ;
            if ( j == -1 )
                currentPrefix = null ;
            else
            {
                String x = cn.substring(0, j) ;
                if ( currentPrefix != null && ! x.equals(currentPrefix) )
                    out.println() ;

                currentPrefix = x ;
            }
            
            // Print
            out.print(c.getColumn().getFullColumnName()) ;
            if ( c.getAlias() != null )
            {
                out.print("/") ;
                out.print(c.getAlias().getColumnName()) ;
            }
        }
        out.ensureStartOfLine() ;
    }
    
    public void visit(SqlDistinct sqlNode)
    {
        start(sqlNode, "Distinct", sqlNode.getAliasName()) ;
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }
    
    public void visit(SqlRestrict sqlNode)
    {
        start(sqlNode, "Restrict", null) ;
        print(sqlNode.getConditions());
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }
    
    private void print(SqlExprList exprs)
    {
        for ( SqlExpr c : exprs )
            out.println(c.toString()) ;
    }

    public void visit(SqlRename sqlRename)
    {
        start(sqlRename, "Rename", sqlRename.getAliasName()) ;
        out.incIndent() ;
        if ( ! sqlRename.getIdScope().isEmpty() )
            out.println(sqlRename.getIdScope().toString()) ;
        if ( ! sqlRename.getNodeScope().isEmpty() )
            out.println(sqlRename.getNodeScope().toString()) ;
        out.decIndent() ;
        sqlRename.getSubNode().visit(this) ;
        finish() ;
    }
    
    @Override
    public void visit(SqlTable sqlNode)
    {
//        if ( ! sqlNode.hasOneNote() )
//            addAnnotations(sqlNode) ;
        out.print(DelimOpen) ;
        out.print("Table ") ;
        out.print(sqlNode.getAliasName()) ;
        out.print(DelimClose) ;
//        if ( sqlNode.hasOneNote() )
            addAnnotations(sqlNode) ;
    }

    @Override
    public void visit(SqlJoinInner sqlJoin)
    { visitJoin(sqlJoin) ; }
 
    @Override
    public void visit(SqlJoinLeftOuter sqlJoin)
    { visitJoin(sqlJoin) ; }
    
    @Override
    public void visit(SqlUnion sqlUnion)
    {
        out.ensureStartOfLine() ;
        start(sqlUnion, "Union", sqlUnion.getAliasName()) ;
        if ( sqlUnion.getLeft() == null )
            out.println("<null>") ;
        else
            sqlUnion.getLeft().visit(this) ;
        out.println() ;
        if ( sqlUnion.getRight() == null )
            out.println("<null>") ;
        else
            sqlUnion.getRight().visit(this) ;
        finish() ;
    }
    
    @Override
    public void visit(SqlCoalesce sqlNode)
    {
        start(sqlNode, "Coalesce", sqlNode.getAliasName()) ;

        boolean first = true ;
        SqlJoin join = sqlNode.getJoinNode() ;
        
        for ( Var v : sqlNode.getCoalesceVars()  )
        {
            if ( ! first ) out.print(" ") ;
            first = false ;
            SqlColumn col = sqlNode.getIdScope().findScopeForVar(v).getColumn() ;
            out.print(v.toString()) ;
            SqlColumn leftCol = join.getLeft().getIdScope().findScopeForVar(v).getColumn() ;
            SqlColumn rightCol = join.getRight().getIdScope().findScopeForVar(v).getColumn() ;
            out.print("["+leftCol+"/"+rightCol+"]") ;
        }
        for ( Var v : sqlNode.getNonCoalesceVars()  )
        {
            if ( ! first ) out.print(" ") ;
            first = false ;
            out.print(v.toString()) ;
            // and where is came from.
            SqlColumn col = join.getIdScope().findScopeForVar(v).getColumn() ;
            out.print("["+col+"]") ;
        }
        
        out.ensureStartOfLine() ;
        visitJoin(sqlNode.getJoinNode()) ;
        finish() ;
    }

    public void visit(SqlSlice sqlNode)
    {
        String startStr = "--" ;
        String lengthStr = "--" ;
        
        if ( sqlNode.getStart() >= 0 )
            startStr = Long.toString(sqlNode.getStart()) ;
        if ( sqlNode.getLength() >= 0 )
            lengthStr = Long.toString(sqlNode.getLength()) ;
        
        String str = String.format("(%s, %s)", startStr, lengthStr) ;
        
        start(sqlNode, "Slice "+str, null) ;
        out.incIndent() ;
        sqlNode.getSubNode().visit(this) ;
        out.decIndent() ;
        finish() ;
    }

    @Override
    public void visit(SqlSelectBlock sqlNode)
    { 
        start(sqlNode, "SqlSelectBlock", sqlNode.getAliasName()) ;
        if ( sqlNode.getDistinct() )
            out.println("Distinct") ;

        out.incIndent() ;
        print(sqlNode.getCols()) ;
        print(sqlNode.getConditions()) ;
        out.decIndent() ;
        
        if ( sqlNode.getStart() >= 0 || sqlNode.getLength() >= 0 )
        {
            String startStr = "--" ;
            String lengthStr = "--" ;
            if ( sqlNode.getStart() >= 0 )
                startStr = Long.toString(sqlNode.getStart()) ;
            if ( sqlNode.getLength() >= 0 )
                lengthStr = Long.toString(sqlNode.getLength()) ;
            String str = String.format("Slice: (%s, %s)", startStr, lengthStr) ;
            out.print(str) ;
        }
        sqlNode.getSubNode().visit(this) ;
        finish() ;
    }

    
    int depth = 0 ;
    
    private void visitJoin(SqlJoin sqlJoin)
    {
        depth ++ ;
        out.ensureStartOfLine() ;
        start(sqlJoin, sqlJoin.getJoinType().printName(), sqlJoin.getAliasName()) ;
        if ( sqlJoin.getLeft() == null )
            out.println("<null>") ;
        else
            sqlJoin.getLeft().visit(this) ;
        out.println() ;
        if ( sqlJoin.getRight() == null )
            out.println("<null>") ;
        else
            sqlJoin.getRight().visit(this) ;
        outputConditionList(sqlJoin.getConditions()) ;
        finish() ;
        depth -- ;
    }
    
    
    private void addAnnotations(Annotations n)
    {
        if ( ! withAnnotations ) return ;
        if ( n == null || !n.hasNotes() ) return ;
        
        boolean first = true ;
        
//        if ( n.getNotes().size() == 1 )
//        {
//            out.pad(annotationColumn) ;
//            out.print(" -- ") ;
//            out.print(n.getNotes().get(0)) ;
//            return ;
//        }
        
        for ( String s : n.getNotes() )
        {
            if ( !first )
                out.ensureStartOfLine() ;
            first = false ;
            out.pad(annotationColumn, true ) ;
            out.print(" -- ") ;
            out.print(s) ;
        }
    }
    
    private void outputConditionList(Collection<SqlExpr>cond)
    {   
        boolean first = true ;
        for ( SqlExpr c : cond )
        {
            out.println() ;
            first = false ;
            out.print(DelimOpen) ;
            out.print("Condition ") ;
            out.print(c.toString()) ;
            addAnnotations(c) ;
            out.print(DelimClose) ;
        }
    }

    
    private void start(SqlNode sqlNode, String label, String alias)
    {
        out.print(DelimOpen) ;
        out.print(label) ;
        if ( alias != null )
        {
            out.print("/") ;
            out.print(alias) ;
        }
        addAnnotations(sqlNode) ;
        out.incIndent() ;
        out.println() ;
    }
    
    private void finish()
    {
        if ( ! closeOnSameLine )
            out.println() ;
        out.print(DelimClose) ;
        out.decIndent() ;
    }
}
