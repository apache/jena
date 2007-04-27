/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.core.sqlnode;

import static com.hp.hpl.jena.sparql.util.StringUtils.str;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.core.Annotations;
import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Equal;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.util.Pair;
import com.hp.hpl.jena.sdb.util.SetUtils;
import com.hp.hpl.jena.sdb.util.alg.Transform;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

// This is not a general purpose SQL writer - it needs only work with the
// SQL node trees that the SDB compiler generate.
// 
// It just writes out the tree - it does not optimize it in anyway (that
// happens before this stage). 

public class GenerateSQLVisitor implements SqlNodeVisitor
{
    // Annotate should ensureEndofLine ?
    private static Log log = LogFactory.getLog(GenerateSQLVisitor.class) ;
    
    private IndentedWriter out ;
    int level = 0 ;
    
    public static boolean outputAnnotations = true ;
    private static final int annotationColumn = 40 ;
    private static boolean commentSQLStyle = true ;
    
    public GenerateSQLVisitor(IndentedWriter out)
    { this.out = out ; }
    
    public void visit(SqlProject sqlNode)
    {
        out.print("SELECT ") ;
        annotate(sqlNode) ; 
        out.ensureStartOfLine() ;
        out.incIndent() ;
        // SELECT vars
        String sep = "" ;
        if ( sqlNode.getCols().size() == 0 )
        {
            log.info("No SELECT columns") ;
            out.print("*") ;
        }
        
        // Put common prefix on same line
        String currentPrefix = null ; 
        String splitMarker = SQLUtils.getSQLmark() ;
        
        for ( Pair<Var, SqlColumn> c : sqlNode.getCols() )
        {
            out.print(sep) ;
            sep = ", " ;
            
            if ( c.cdr() == null )
                log.warn("Null SqlColumn for "+str(c.car())) ;    

            Var aliasVar = c.car() ;
            String p = null ;
            
            if ( aliasVar == null )
            {
                splitMarker = "." ;
                p = c.cdr().asString() ;
            } else {
                p = aliasVar.getName() ;
            }
            // Var name formatting. 
            // V_1_lex, etc etc
            
            int j = p.lastIndexOf(splitMarker) ;
            String x = p.substring(0, j) ;
            if ( currentPrefix != null && ! x.equals(currentPrefix) )
                out.println() ;
            
            currentPrefix = x ;
            out.print(c.cdr().asString()) ;
            
            if ( aliasVar != null )
            {
                String varLabel = c.car().getName() ;
                out.print(" AS ") ;
                out.print(varLabel) ;
            }
        }
        out.decIndent() ;
        out.println() ;
        out.println("FROM") ;

        SqlNode sqlNode2 = sqlNode.getSubNode() ;
        
        // Project-restrict : can combine 
        if ( sqlNode2.isRestrict() )
        {
            SqlRestrict r = sqlNode.getSubNode().asRestrict() ;
            // Special Project-Restrict-Node case.
            out.incIndent() ; 
            r.getSubNode().visit(this);
            out.decIndent() ;
            out.println() ;
            genWHERE(r.getConditions()) ;
            return ;
        }
        
        boolean needBrackets = false ;
        if ( sqlNode2.isCoalesce() )
            needBrackets = true ;
        out.incIndent() ; 
        outputNode(sqlNode2, needBrackets) ;
        out.decIndent() ; 

//        // Not a project-restrict.
//        // Generate expression for the FROM
//        out.incIndent() ;
//        sqlNode.getSubNode().visit(this) ;
//        out.decIndent() ;
    }

    public void visit(SqlRestrict sqlNode)
    {
        annotate(sqlNode) ;
        if ( sqlNode.getConditions().size() == 0 )
        {
            log.warn("No conditions associated with this restriction") ;
            sqlNode.getSubNode().visit(this) ;
            return ;
        }
        
        SqlNode node2 = sqlNode.getSubNode() ;
        if ( node2.isJoin() && ! node2.asJoin().getJoinType().equals(JoinType.INNER) )
        {
            log.warn("restrict/"+node2.asJoin().getJoinType()+" not supported") ;
            return ;
        }
            
        if ( node2.isJoin() )
        {
            // Common with QueryCompilerBase.join?? 
            // Push condition into the inner join ON clause
            // Avoid mutating the Join - create a new one and merge the conditions.
            // (As we do not use the old join, we could reuse it alias)
            SqlJoin j = node2.asJoin() ;
            if ( j.getJoinType() != JoinType.INNER )
                log.warn("Unexpected: restrict on join type "+ j.getJoinType() ) ;
            
            //SqlJoin j2 = SqlJoin.create(j.getJoinType(), j.getLeft(), j.getRight(), SDBConstants.gen(j.getAliasName())) ;
            SqlJoin j2 = SqlJoin.create(j.getJoinType(), j.getLeft(), j.getRight(), j.getAliasName()) ;
            j2.getConditions().addAll(j.getConditions()) ;
            j2.getConditions().addAll(sqlNode.getConditions()) ;
            j2.visit(this) ;
            return ;
        }
        
        if ( node2.isTable() )
        {
            // Only occurs in a JOIN and so the () has already been done.
            //out.print("( ") ;
            //out.incIndent() ;
            out.println("SELECT *") ;
            out.print("FROM ") ;
            out.print(node2.asTable().getTableName()) ;
            out.print(" AS ") ;
            out.print(node2.asTable().getAliasName()) ;
            annotate(node2.asTable()) ;
            out.ensureStartOfLine() ;
            genWHERE(sqlNode.getConditions()) ;
            //out.println() ;
            //out.decIndent() ;
            //out.print(")") ;
            return ;
        }
        
        log.warn("restrict/unrecognized sub node: \n"+node2) ;
        return ;
    }
    
    private void genTables(SqlNode sqlNode)
    {
        log.warn("genTables called: \n--\n"+sqlNode+"\n--") ;
//        Collection<SqlTable> tables = sqlNode.tablesInvolved() ;
//        String sep = "" ;
//        for ( SqlTable table : tables )
//        {
//            out.print(sep) ;
//            sep = " " ;
//            out.print(table.getTableName()) ;
//            out.print(" AS ") ;
//            out.print(table.getAliasName()) ;
//        }
    }
    
    private void genWHERE(SqlExprList conditions)
    {
        out.print("WHERE") ;
        out.print(" ") ;
        out.incIndent() ;
        conditionList(conditions) ;
        out.decIndent() ;
    }
    
    public void visit(SqlTable table)
    {
        out.print(table.getTableName()) ;
        out.print(" AS ") ;
        out.print(table.getAliasName()) ;
        annotate(table) ;
    }

    public void visit(SqlJoinInner join)
    {
        join = rewrite(join) ;
        visitJoin(join) ;
    }

    public SqlJoinInner rewrite(SqlJoinInner join)
    {
        if ( ! join.getRight().isInnerJoin() )
            return join ;

        // if ( join(A, join(B, C)) ) rewrite as join(join(A,B),C)
        // this then is written without brackets (and so scope changing)
        // TODO abstract as organiseJoin(List<join elements>)
        // and remember to do top down to find maximal join trees
        
        SqlJoinInner right = join.getRight().asInnerJoin() ;

        String alias1 = join.getAliasName() ;
        String alias2 = right.getAliasName() ;

        SqlNode sn_a = join.getLeft() ;
        SqlNode sn_b = right.getLeft() ;
        SqlNode sn_c = right.getRight() ;

        SqlExprList conditions = new SqlExprList(join.getConditions()) ; 
        conditions.addAll(right.getConditions()) ; 

        Set<SqlTable> tables_ab = sn_a.tablesInvolved() ;
        tables_ab.addAll(sn_b.tablesInvolved()) ;

        SqlExprList newCond_ab = new SqlExprList() ;  // Goes to new join(A,B)
        SqlExprList newCond_c = new SqlExprList() ;   // Goes to new join(,C)
        // Place conditions
        for ( SqlExpr e : conditions )
        {
            Set<SqlColumn> cols = e.getColumnsNeeded() ;
            // columns to tables.
            Set<SqlTable> tables = tables(cols) ;
            // Are the tables contained in tables_ab?
            tables.removeAll(tables_ab) ;

            if ( tables.size() == 0 )
                newCond_ab.add(e) ;
            else
                newCond_c.add(e) ;
        }
        if ( newCond_ab.size()+newCond_c.size() != conditions.size() )
            log.fatal(String.format("Conditions mismatch: (%d,%d,%d)",
                                    newCond_ab.size(), newCond_c.size(), conditions.size())) ;


        SqlJoinInner join2 = new SqlJoinInner(sn_a, sn_b) ;
        join2.addConditions(newCond_ab) ;
        join2 = new SqlJoinInner(join2, sn_c) ;
        join2.addConditions(newCond_c) ;
        return join2 ;
    }
    
    static final Transform<SqlColumn, SqlTable> colToTable = new Transform<SqlColumn, SqlTable>() {
        public SqlTable convert(SqlColumn item) { return item.getTable() ; }
    } ;
    
    private static Set<SqlTable> tables(Set<SqlColumn> cols)
    {
        return SetUtils.convert(cols, colToTable) ;
    }

    public void visit(SqlJoinLeftOuter join)    { visitJoin(join) ; }

    public void visit(SqlCoalesce sqlNode)
    {
        out.print("SELECT ") ;
        
        boolean first = true ;
        SqlJoin join = sqlNode.getJoinNode() ;
        // Rough draft code.
        for ( Var v : sqlNode.getCoalesceVars() )
        {
            if ( ! first )
                out.print(", ") ;
            SqlColumn col = sqlNode.getIdScope().findScopeForVar(v).getColumn() ;
            SqlColumn leftCol = join.getLeft().getIdScope().findScopeForVar(v).getColumn() ;
            SqlColumn rightCol = join.getRight().getIdScope().findScopeForVar(v).getColumn() ;
            
            out.print("COALESCE(") ;
            out.print(leftCol.toString()) ;
            out.print(", ") ;
            out.print(rightCol.toString()) ;
            out.print(") AS "+col.getColumnName()) ;
            first = false ;
        }
        
        // And other vars we want.
        
        for ( Var v : sqlNode.getNonCoalesceVars() )
        {
            if ( ! first )
                out.print(", ") ;
            first = false ;
            
            // Need generated names.
            SqlColumn col = sqlNode.getIdScope().findScopeForVar(v).getColumn() ;
            SqlColumn col2 = join.getIdScope().findScopeForVar(v).getColumn() ;
            out.print(col2+" AS "+col.getColumnName()) ;
        }
        out.ensureStartOfLine() ;

        out.incIndent() ;       // INC
        out.println("FROM") ;
        join.visit(this) ;
        out.ensureStartOfLine() ;
        // Alias and annotations handled by outputNode
    }

    protected void visitJoin(SqlJoin join) { visitJoin(join, join.getJoinType().sqlOperator()) ; }
    protected void visitJoin(SqlJoin join, String joinOperatorName)
    {
        SqlNode left = join.getLeft() ;
        SqlNode right = join.getRight() ;
        
//        if ( left.isTable() || right.isTable() )
//        {
//            // Special cases  Join(Table, Join) and  Join(Join, Table)
//            if ( left.isTable() && join.isInnerJoin() && right.isInnerJoin() )
//            {
//                join.getConditions() ;
//            }
//            
//            if ( right.isTable() ) {}
//        }
        
        // can we linearise the format? (drop the () and indentation)
        if ( left.isJoin() &&
            // assume left join is same precedence and left-associative with inner join
             //left.asJoin().getJoinType() == join.getJoinType() && 
             left.getAliasName() == null ) 
            outputNode(left, false) ;
        else
        {
            out.incIndent() ;
            outputNode(left, true) ;
            out.decIndent() ;
        }
        out.println() ;
        //out.print(" ") ;
        
        out.print(joinOperatorName) ;
        annotate(join) ;
        out.println() ;

        // Aliasing and scoping - may need sub-SELECT - or just don't generate
        // such SqlNode structures, leaving only COALESCE as the sub-SELECT case
        
        boolean bracketsRight = true ;
//        if ( right.isInnerJoin() && join.isInnerJoin() && no conditions )
//            bracketsRight = false ;
        
        if ( bracketsRight )
            // Why?
            out.incIndent() ;
        outputNode(right, bracketsRight) ;
        if ( bracketsRight )
            out.decIndent() ;
        out.println() ;
        out.print("ON ") ;
        if ( join.getConditions().size() > 0 )
            conditionList(join.getConditions()) ;
        else
        {
            out.print(" ( ") ;
            out.print(leftJoinNoConditionsString()) ;
            out.print(" )") ;
        }
    }

    protected String leftJoinNoConditionsString() { return "1 = 1" ; }
    
    // Interaction with annotations
    static boolean allOnOneLine = false ;
    public void conditionList(SqlExprList conditions)
    {
        if ( conditions.size() == 0 )
            return ;
        
        out.print("( ") ;
        
        String sep = " AND " ;
        
        boolean first = true ;
        boolean lastAnnotated = false ;
            
        for ( SqlExpr c : conditions )
        {
            if ( ! first )
            {
                if ( ! allOnOneLine )
                    out.println();
                out.print(sep) ;
            }
            boolean needsParens = ! ( c instanceof S_Equal ) ;
            
            // TODO Interact with SqlExpr precedence printing
            if ( needsParens ) 
                out.print("( ") ;
            out.print(c.asSQL()) ;
            if ( needsParens ) 
                out.print(" )") ;
            if ( ! allOnOneLine )
                lastAnnotated = annotate(c) ;
            first = false ;
        }
        if ( ! allOnOneLine && lastAnnotated )
            out.println("") ;
        out.print(" )") ;
        first = true ; 
        
        if ( allOnOneLine )
        {            
            for ( SqlExpr c : conditions )
            {
                if ( c.hasNotes() )
                {
                    if ( !first ) out.println() ;
                    annotate(c) ;
                    first = false ;
                }
            }
        }
    }
    
    
    private void outputNode(SqlNode sqlNode, boolean mayNeedBrackets)
    {
        if ( sqlNode.isTable() )
        {
            sqlNode.visit(this) ;
            return ;
        }

        level ++ ;
        // 
        if ( mayNeedBrackets )
        {
            out.print("( ") ;
            out.incIndent() ;
        }
        sqlNode.visit(this) ;
        if ( mayNeedBrackets )
        {
            out.decIndent() ;
            out.ensureStartOfLine() ;
            out.print(")") ;
        }
            // Every derived table (SELECT ...) must have an alias.
            // Is there a more principled way to do this? .isDerived?
//            if ( sqlNode.isRestrict() || sqlNode.isProject())
//                out.print(" AS "+sqlNode.getAliasName()) ;
        if ( sqlNode.getAliasName() != null )
            out.print(" AS "+sqlNode.getAliasName()) ;
        annotate(sqlNode) ;
        level -- ;
    }

    private boolean annotate(Annotations sqlNode)
    { return annotate(sqlNode, annotationColumn) ; }

    // return true if annotation was output and it runs to end-of-line  
    private boolean annotate(Annotations sqlNode, int indentationColumn)
    {
        if ( ! outputAnnotations )
            return false ;
        
        boolean first = true ;
        for ( String s : sqlNode.getNotes() )
        {
            if ( !first ) out.println();
            first = false; 
            out.pad(indentationColumn, true) ;
            if ( commentSQLStyle )
            {
                out.print(" -- ") ; out.print(s) ;
            }else{
                out.print(" /* ") ; out.print(s) ; out.print(" */") ;
            }
        }
        return !commentSQLStyle || !first ;  
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