/*
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdql.parser;


import org.apache.commons.logging.*;

import com.hp.hpl.jena.graph.query.Expression;
import com.hp.hpl.jena.graph.query.IndexValues;
import com.hp.hpl.jena.graph.query.Valuator;
import com.hp.hpl.jena.graph.query.VariableIndexes;
import com.hp.hpl.jena.rdql.Constraint;
import com.hp.hpl.jena.rdql.EvalFailureException;
import com.hp.hpl.jena.rdql.Query;

/** A node that is an RDQL expression that can be evaiulated to true or false
 * i.e. found in the AND clause */ 

abstract class ExprNode 
    extends SimpleNode            // So it is parser generated
    implements
        Expression , Valuator     // Part of jena.graph.query.*
        , Constraint              // Part of RDQL package
        , Expr                    // Part of the RDQL parser package
{
    static Log log = LogFactory.getLog(ExprNode.class) ;
    Query query ;
    VariableIndexes varIndexes ;
    
    public ExprNode(RDQLParser p, int i) { super(p, i); }
    public ExprNode(int i) { super(i); }

    
    // --- interface Constraint : map to Expr. 
    public boolean isSatisfied(Query q, IndexValues env)
    {
        return evalBool( q, env ) ;
    }
    
    public void postParse(Query q)
    {
        super.postParse(q) ;
        query = q ;
    }
    
    // Per query execution processing.

    public Valuator prepare(VariableIndexes vi)
    {
        varIndexes = vi ;
        if (children != null)
        {
            for (int i = 0; i < children.length; ++i)
            {
                Object n = children[i];
                if ( n instanceof Expression )
                {
                    ((Expression)n).prepare(vi);
                }
            }
        }
        // This object is its own Valuator (if its an expression)
        // Effect is to return the top node - rest were ignored.
        return this ;
    }

    /**
     	Answer the result of evaluating the constraint expression; implements
     	Expression.evalObject(*).
    */
    public Object evalObject( IndexValues iv )
    {
        return evalNode( query, iv ) ;
    }

    /**
     	Answer the result of evaluating the constraint expression as a
     	primitive boolean value; implements Expression.evalBool(*).
    */
    public boolean evalBool( IndexValues iv )
    {
        return evalBool( query, iv ) ;
    }
    
    /**
     	Answer the result of evaluating the constraint expression with 
     	a specified query - helper function to allow isSatisfied() to be
     	implemented in terms of the eval methods.
    */
    protected boolean evalBool( Query q, IndexValues iv)
    {
        NodeValue v = evalNode( q, iv ) ;
        return v == null ? false : v.getBoolean() ;
    }
    
    /**
     	Answer the NodeValue result of evaluating the constraints
     	relative to the query and with the specified environment.
     	Traps and handles exceptions. This code used to be in
     	isSatisfied() until Expressions gained evalObject(), then
     	it was factored out so that it could be shared by both
     	evalXXX() methods.
    */
    public NodeValue evalNode( Query q, IndexValues env )
        {
            try {
                return eval(q, env) ;
            }
            catch (EvalFailureException e) //Includes EvalTypeException
            {
                // Check all exceptions possible.
                //expr = null ;
                return null ;
            }
            catch (Exception e)
            {
                log.warn("RDQL : general exception!", e) ;
                // Shouldn't happen
                return null ;
            }
        }

    // com.hpl.hpl.jena.graph.query.Expression
    // -- Variables
    public boolean isVariable()      { return false; }
    public String getName()          { return null; } // For variables only

    // -- Constants (literals - but that name is confusing with RDF literals). 
    public boolean isConstant()      { return false; }
    public Object getValue()         { return null; } // For constants

    // -- Functions
    public boolean isApply()         { return false; }
    public String getFun()           { return null; } // For URI of the function
    public int argCount()            { return 0; }    // For functions
    public Expression getArg(int i)  { return null; }

    static final String exprBaseURI = "urn:x-jena:expr:" ; 
    protected String constructURI(String className)
    {
        if ( className.lastIndexOf('.') > -1 )
            className = className.substring(className.lastIndexOf('.')+1) ;
        return exprBaseURI+className ;
    }
    
    // Used in printing a query - not for getting the string
    // value of a expression (which itself must be unquoted)
    public String toString()
    {
        return asInfixString() ;
    }
}

/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
