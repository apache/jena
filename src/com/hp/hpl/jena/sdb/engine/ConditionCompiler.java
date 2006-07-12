/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.query.core.Binding;
import com.hp.hpl.jena.query.core.Var;
import com.hp.hpl.jena.query.engine1.plan.PlanFilter;
import com.hp.hpl.jena.query.expr.Expr;
import com.hp.hpl.jena.sdb.condition.C_Regex;
import com.hp.hpl.jena.sdb.condition.C_Var;
import com.hp.hpl.jena.sdb.condition.SDBConstraint;
import com.hp.hpl.jena.sdb.exprmatch.Action;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchString;
import com.hp.hpl.jena.sdb.exprmatch.ActionMatchVar;
import com.hp.hpl.jena.sdb.exprmatch.MapResult;

public class ConditionCompiler
{
    private static Log log = LogFactory.getLog(ConditionCompiler.class) ;
    
    // -------- Constraints

    // --- regex : testing a term (in a variable)
    private static ExprPattern regex1 = new ExprPattern("regex(?a1, ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    private static ExprPattern regex2 = new ExprPattern("regex(?a1, ?a2, 'i')",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    // --- regex : testing the lexical form of a term (in a variable)
    private static ExprPattern regex3 = new ExprPattern("regex(str(?a1), ?a2)",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    private static ExprPattern regex4 = new ExprPattern("regex(str(?a1), ?a2, 'i')",
                                                        new String[]{ "a1" , "a2" },
                                                        new Action[]{ new ActionMatchVar() ,
                                                                      new ActionMatchString()}) ;
    
    // --- starts-with
    private static ExprPattern startsWith1 = new ExprPattern("fn:starts-with(?a1, ?a2)",
                                                             new String[]{ "a1" , "a2" },
                                                             new Action[]{ new ActionMatchVar() ,
                                                                           new ActionMatchString()}) ;

    private static ExprPattern startsWith2 = new ExprPattern("fn:starts-with(str(?a1), ?a2)",
                                                             new String[]{ "a1" , "a2" },
                                                             new Action[]{ new ActionMatchVar() ,
                                                                           new ActionMatchString()}) ;
    
    public PlanSDBConstraint match(PlanFilter planFilter)
    {
        MapResult rMap = null ;
        Expr expr = planFilter.getConstraint().getExpr() ;
        if ( expr == null )
            return null ;
        SDBConstraint c = compile(expr, null) ;
        PlanSDBConstraint psc = new PlanSDBConstraint(c, planFilter, true) ;
        return psc ;
    }
    
    public SDBConstraint compile(PlanSDBConstraint planConstraint, Binding binding)
    {
        try {
            // A bit crude - unpack and recompile.
            return compile(planConstraint.getOriginal().getConstraint().getExpr(), binding) ;
        } catch (NullPointerException ex)
        {
            return null ;
        }
            
    }
    
    private SDBConstraint compile(Expr expr, Binding binding)
    {
        if ( binding != null )
            expr = expr.copySubstitute(binding) ;
        
        MapResult rMap = null ;
        if ( (rMap = regex1.match(expr)) != null )
        {
            //log.info("Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
            // TODO - think about need for the C_ parallel class hierarchy of constraints
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            SDBConstraint c = new C_Regex(new C_Var(var), pattern, false) ;
            return c ;
        }
        
        if ( (rMap = startsWith1.match(expr)) != null )
        {
            LogFactory.getLog(ConditionCompiler.class).info("startsWith - Matched: ?a1 = "+rMap.get("a1")+" : ?a2 = "+rMap.get("a2")) ;
            Var var = new Var(rMap.get("a1").getVar()) ;
            String pattern = rMap.get("a2").getConstant().getString() ;
            // Unfinished
            return null ;
            // becomes; isNotNull(var) AND var LIKE 'pattern%'
        }
        
        // Not recognized
        return null ;

    }
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