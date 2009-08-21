/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;

public class Explain
{
    //static IndentedLineBuffer iBuff = new IndentedLineBuffer() ;
    
    // CHANGE ME.
    static public final Logger    logExec    = LoggerFactory.getLogger("com.hp.hpl.jena.tdb.exec") ;
//    static public boolean explaining = false ;
    // MOVE ME to  ARQConstants
    public static final Symbol symLogExec = TDB.symLogExec ; //ARQConstants.allocSymbol("logExec") ;
    
    // ---- Query
    
    public static void explain(Query query, Context context)
    {
        explain("Query", query, context) ;
    }
    
    public static void explain(String message, Query query, Context context)
    {
        if ( explaining(context) )
        {
            // One line.
            // Careful - currently ARQ version needed
            IndentedLineBuffer iBuff = new IndentedLineBuffer() ;
            iBuff.getIndentedWriter().setFlatMode(true) ;
            query.serialize(iBuff.getIndentedWriter()) ;
            String x = iBuff.asString() ;
            
            _explain(message, x) ;
        }
    }
    
    
    // ---- Algebra
    
    public static void explain(Op op, Context context)
    {
        explain("Algebra", op, context) ;
    }
    
    public static void explain(String message, Op op, Context context)
    {
        if ( explaining(context) )
            _explain(message, op.toString()) ;
    }
    
    // ---- BGP
    
    public static void explain(BasicPattern bgp, Context context)
    {
        explain("BGP", bgp, context) ; 
    }
    
    public static void explain(String message, BasicPattern bgp, Context context)
    {
        if ( explaining(context) )
            _explain(message, bgp.toString()) ;
    }

    // ----
    
    private static void _explain(String reason, String explanation)
    {
        while ( explanation.endsWith("\n") || explanation.endsWith("\r") )
            explanation = StrUtils.chop(explanation) ;
        if ( explanation.contains("\n") )
            explanation = reason+"\n"+explanation ;
        else
            explanation = reason+" :: "+explanation ;
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