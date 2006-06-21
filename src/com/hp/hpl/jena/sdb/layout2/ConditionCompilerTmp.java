/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.util.List;

import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.core.Constraint;
import com.hp.hpl.jena.query.expr.*;
import com.hp.hpl.jena.sdb.core.*;
import com.hp.hpl.jena.sdb.core.sqlexpr.S_Regex;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExpr;
import com.hp.hpl.jena.sdb.util.Pair;

//TODO Condition compiler to be replaced by ExprMatcher.

public class ConditionCompilerTmp
{
//    public static boolean recognize(Block block, Constraint c)
//    {
//        if ( ! c.isExpr() )
//            return false ;
//        return recognize(block, c.getExpr()) ;
//    }
//    
//    public static boolean recognize(Block block, Expr expr)
//    {
//        RecognizeExpr v = new RecognizeExpr() ;
//        expr.visit(v) ;
//        return v.recognized ;
//    }
    
    public static SqlExpr make(CompileContext context, List<Pair<Node, SqlColumn>> projectVarCols, Constraint c)
    {
        if ( ! c.isExpr() )
            return null ;
        return make(context, projectVarCols, c.getExpr()) ;
    }
    
    public static SqlExpr make(CompileContext context, List<Pair<Node, SqlColumn>> projectVarCols, Expr expr)
    {
        CompileExpr v = new CompileExpr(context, projectVarCols) ;
        expr.visit(v) ;
        return v.condition ;
    }
    
//    static class RecognizeExpr extends ExprVisitorBase
//    {
//        boolean recognized = false ;
//
//        public void visit(ExprNodeFunction ex)
//        {
//            if ( ex instanceof E_Regex )
//                visitRegex((E_Regex)ex) ;
//        }
//
//        public void visitRegex(E_Regex ex)
//        {
//            Expr arg1 = ex.getRegexExpr() ;
//            Expr pattern = ex.getPattern() ;
//            Expr flags = ex.getFlags() ;
//            
//            if ( ! arg1.isVariable() )
//                return ;
//            if ( ! pattern.isConstant() || ! pattern.getConstant().isString() )
//                return ;
//            if ( flags != null && ( ! flags.isConstant() || ! flags.getConstant().isString() ) )
//                return ;
//            // (var, string, string)
//            recognized = true ;
//        }
//    }
    
    static class CompileExpr extends ExprVisitorBase
    {
        SqlExpr condition = null ;
        CompileContext context = null ;
        List<Pair<Node, SqlColumn>> projectVarCols = null ;
       
        CompileExpr(CompileContext context, List<Pair<Node, SqlColumn>> projectVarCols)
        { 
            this.context = context ;
            this.projectVarCols = projectVarCols ;
        }

        @Override
        public void visit(ExprFunction ex)
        {
            if ( ex instanceof E_Regex )
                visitRegex((E_Regex)ex) ;
        }

        public void visitRegex(E_Regex ex)
        {
            Expr arg1 = ex.getRegexExpr() ;
            Expr pattern = ex.getPattern() ;
            Expr flags = ex.getFlags() ;
            
            Node var = null ;
            boolean strSeen = false ;
            boolean acceptableOperand = false ;

            // -- Argument
            if ( arg1.isVariable() )
                var = arg1.getVar().getAsNode() ;
            else if ( arg1.isExpr() && arg1.getExpr() instanceof E_Str )
            {
                strSeen = true ;
                Expr e = ((E_Str)arg1.getExpr()).getArg(1) ;
                if ( e.isVariable() )
                    var = e.getVar().getAsNode() ;
            }
            
            if ( var == null )
                return ;

            // -- Pattern and flags
            if ( ! pattern.isConstant() || ! pattern.getConstant().isString() )
                return ;
            if ( flags != null && ( ! flags.isConstant() || ! flags.getConstant().isString() ) )
                return ;
            String f = null ;
            if ( flags != null ) f = flags.getConstant().asString() ;

            SqlColumn id = findColumn(var) ;
            if ( id == null )
            {
                LogFactory.getLog(this.getClass()).warn("Not found ; offramp for "+var) ;
                return ;
            }
            
            // Better: is str seen =>
            SqlColumn colLex = new SqlColumn(id.getTable(), TableNodes.colLex) ;
            condition = new S_Regex(colLex, pattern.getConstant().asString(), f) ;
            // Else check type.
            // ...
        }
        
        private SqlColumn findColumn(Node var)
        {
            SqlColumn id = null ;
            for ( Pair<Node, SqlColumn> x : projectVarCols)
            {
                if ( x.car().equals(var) )
                    return x.cdr() ;
            }
            return null ;
        }
    }
    

    private Constraint compileRegex(Constraint c)
    {
        if ( ! (c instanceof E_Regex) )
            return null ;
        
        E_Regex exprRegex = (E_Regex)c ;
        
        Expr arg1 = exprRegex.getArg(1) ;
        Expr arg2 = exprRegex.getArg(2) ;
        Expr arg3 = exprRegex.getArg(3) ;
        if ( ! arg1.isVariable() )
            return null ;
        return c ;
    }
    
//    private Constraint extractValueComparision(Constraint c)
//    {
//        if ( ! (c instanceof E_LessThan) )
//            return null ;
//        E_LessThan e = (E_LessThan)c ;
//
//        Expr e1 = e.getLeft() ;
//        Expr e2 = e.getRight() ;
//        
//        if ( !e1.isConstant() && !e2.isConstant() )
//            return null ;
//
//        if ( !e1.isVariable() && !e2.isVariable() )
//            return null ;
//        
//        return c ;
//    }
//    

    

}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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