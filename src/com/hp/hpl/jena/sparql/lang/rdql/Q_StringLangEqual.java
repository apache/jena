/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2004 Fujitsu Laboratories of America, Inc.
 * [See end of file]
 */

// This facility was provided by Zhexuan Song [zsong@fla.fujitsu.com] (Jeff) by
// modifying Q_StringEqual from the original Jena distribution.
// Thanks to Jeff for this.

package com.hp.hpl.jena.sparql.lang.rdql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.query.Expression;
import com.hp.hpl.jena.graph.query.IndexValues;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class Q_StringLangEqual extends ExprNodeRDQL implements ExprRDQL, ExprBoolean
{
    ExprRDQL left ;
    ExprRDQL right ;

    protected static final String printName = "lang=" ;
    protected static final String opSymbol = "langeq" ;
    
    Q_StringLangEqual(int id) { super(id); }
    
    Q_StringLangEqual(RDQLParser p, int id) { super(p, id); }

    protected boolean rawEval(RDQL_NodeValue x, RDQL_NodeValue y)
    {
        if ( x.isNode() && x.getNode().isLiteral() &&
             y.isNode() && y.getNode().isLiteral() )
        {
            Node xNode = x.getNode() ;
            Node yNode = y.getNode() ;
            String nodeLang = xNode.getLiteralLanguage().toUpperCase();
            String queryLang = yNode.getLiteralLexicalForm().toUpperCase();
            /**
             * Here is the logic to compare language code
             * If the query langauge has -, such as zh-tw, we must do 
             * a complete match. So "zh-tw" will not match "zh-t".
             * If the query language does not have -, such as en, we should do
             * a substring match. So "en-gb" should match "en".
             */
            if (queryLang.indexOf("-") >= 0)
            {
                return nodeLang.equals(queryLang);
            }
            else
            {
                int pos = nodeLang.indexOf("-");
                if (pos > 0)
                    nodeLang = nodeLang.substring(0, pos);
                return nodeLang.equals(queryLang);
            }
        }
        return false;
    }
    
    public RDQL_NodeValue evalRDQL(Query q, IndexValues env)
    {
        RDQL_NodeValue x = left.evalRDQL(q, env) ;
        RDQL_NodeValue y = right.evalRDQL(q, env) ;
        
        boolean b = rawEval(x, y) ;
                
        NodeValueSettable result ;
        if ( x instanceof NodeValueSettable )
            result = (NodeValueSettable)x ;
        else if ( y instanceof NodeValueSettable )
            result = (NodeValueSettable)y ;
        else
            result = new WorkingVar() ;
        result.setBoolean(b) ;
        return result ;
    }
    @Override
    public void jjtClose()
    {
        int n = jjtGetNumChildren() ;
        if ( n != 2 )
            throw new QueryException("Q_StringLangEqual: Wrong number of children: "+n) ;
        left = (ExprRDQL)jjtGetChild(0) ;
        right = (ExprRDQL)jjtGetChild(1) ;
    }
    
    // graph.query.Expression
    @Override
    public boolean isApply()         { return true ; }
    @Override
    public String getFun()           { return this.getClass().getName() ; } // For URI of the function
    @Override
    public int argCount()            { return 2; }
    @Override
    public Expression getArg(int i)  
    {
        if ( i == 0 && left instanceof Expression )
            return (Expression)left ;
        if ( i == 1 && right instanceof Expression )
            return (Expression)right ;
        return null;
    }
    // ----
    
    public String asInfixString()
    {
        return RDQLQueryPrintUtils.asInfixString2(left, right, printName, opSymbol) ;
    }
    public String asPrefixString()
    {
        return RDQLQueryPrintUtils.asPrefixString(left, right, printName, opSymbol) ;
    }

    @Override
    public String toString()
    {
        return asInfixString() ;
    }

    @Override
    public void format(IndentedWriter writer)
    {
        RDQLQueryPrintUtils.format(writer, left, right, printName, opSymbol) ;
    }
}

/* (c) Copyright 2004 Fujitsu Laboratories of America, Inc.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 * 
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer 
 *      in the documentation and/or other materials provided with
 *      the distribution.
 *   3. The name of the author may not be used to endorse or promote
 *      products derived from this software without specific prior
 *      written permission.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY FUJITSU LABORATORIES AMERICA (FLA) ``AS IS''
 * AND FLA WILL NOT BE OBLIGATED TO PROVIDE ANY SUPPORT, ASSISTANCE,
 * INSTALLATION OR OTHER SERVICES. FLA IS NOT OBLIGATED TO PROVIDE ANY
 * UPDATES, ENHANCEMENTS OR EXTENSIONS. FLA SPECIFICALLY DISCLAIMS ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. IN NO
 * EVENT SHALL FLA BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. IN NO EVENT WILL FLA'S TOTAL
 * LIABILITY, IF ANY, EXCEED ANY CASH PAYMENT RECEIVED BY FLA FOR THIS
 * SOFTWARE.
 */

/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
