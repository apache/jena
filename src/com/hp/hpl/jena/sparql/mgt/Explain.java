/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.mgt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.StrUtils;
import com.hp.hpl.jena.sparql.util.Symbol;

public class Explain
{
    // CHANGE ME.
    static public final Logger    logExec    = LoggerFactory.getLogger("com.hp.hpl.jena.tdb.exec") ;
//    static public boolean explaining = false ;
    // MOVE ME to  ARQConstants
    public static final Symbol symLogExec = ARQConstants.allocSymbol("logExec") ;
    
    // ---- Query
    
    public static void explain(Query query, Context context)
    {
        explain("Query", query, context) ;
    }
    
    public static void explain(String message, Query query, Context context)
    {
        if ( explaining(context) )
            _explain(message, query.toString(), false) ;
    }
    
    
    // ---- Algebra
    
    public static void explain(Op op, Context context)
    {
        explain("Algebra", op, context) ;
    }
    
    public static void explain(String message, Op op, Context context)
    {
        if ( explaining(context) )
            _explain(message, op.toString(), false) ;
    }
    
    // ---- BGP
    
    public static void explain(BasicPattern bgp, Context context)
    {
        explain("BGP", bgp, context) ; 
    }
    
    public static void explain(String message, BasicPattern bgp, Context context)
    {
        if ( explaining(context) )
            _explain(message, bgp.toString(), false) ;
    }

    // ----
    
    private static void _explain(String reason, String explanation, boolean oneLine)
    {
        if ( oneLine )
        {
            //??
        }
        
        while ( explanation.endsWith("\n") || explanation.endsWith("\r") )
            explanation = StrUtils.chop(explanation) ;
        explanation = reason+"\n"+explanation ;
        _explain(explanation) ;
        //System.out.println(explanation) ;
    }
    
    private static void _explain(String explanation)
    {
        logExec.info(explanation) ;
    }

    public static void explain(Context context, String message)
    {
        if ( explaining(context) )
            _explain(message) ;
    }

    
    
    public static boolean explaining(Context context)
    {
        return context.isTrue(symLogExec) && logExec.isInfoEnabled() ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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