/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import org.slf4j.Logger ;
import atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer ;
import com.hp.hpl.jena.sparql.util.Symbol ;
import com.hp.hpl.jena.tdb.TDB ;

/** Execution logging for query processing on a per query basis.
 * This class provides an overlay on top of the system logging to provide
 * control of log message down to a per query basis. The associated logging channel
 * must also be enabled.  
 * 
 * An execution can detail the query, the algebra and every point at which the dataset is touched.
 *  
 *  Caution: logging can be a significant cost for small queries
 *  because of disk or console output overhead.
 *  
 *  @see{TDB.logExec}
 *  @see{TDB.setExecutionLogging}
 */

public class Explain
{
    // Logging: TRACE < DEBUG < INFO < WARN < ERROR < FATAL
    /* Design:
     * Logger level: always INFO?
     * Per query: SYSTEM > EXEC (Query) > DETAIL (Algebra) > DEBUG (every BGP)
     * 
     * Control:
     *   tdb:logExec = true (all), or enum
     * 
Document:
  Include setting different loggers etc for log4j.
     */

    // Need per-query identifier.
    
    // These are the per-execution levels.
    
    static enum InfoLevel
    {   INFO, FINE, ALL
//        @Override
//        abstract public String toString() ;  
    }

    // CHANGE ME.
    static public final Logger logExec = TDB.logExec ;
    static public final Logger logInfo = TDB.logInfo ;
    
    // MOVE ME to ARQConstants
    public static final Symbol symLogExec = TDB.symLogExec ; //ARQConstants.allocSymbol("logExec") ;
    
    // ---- Query
    
    public static void explain(Query query, Context context)
    {
        explain("Query", query, context) ;
    }
    
    public static void explain(String message, Query query, Context context)
    {
        if ( explaining(InfoLevel.INFO, logExec, context) )
        {
            // One line.
            // Careful - currently ARQ version needed
            IndentedLineBuffer iBuff = new IndentedLineBuffer() ;
            iBuff.getIndentedWriter().setFlatMode(true) ;
            query.serialize(iBuff.getIndentedWriter()) ;
            String x = iBuff.asString() ;
            
            _explain(logExec, message, x) ;
        }
    }
    
    // ---- Algebra
    
    public static void explain(Op op, Context context)
    {
        explain("Algebra", op, context) ;
    }
    
    public static void explain(String message, Op op, Context context)
    {
        if ( explaining(InfoLevel.FINE, logExec, context) )
            _explain(logExec, message, op.toString()) ;
    }
    
    // ---- BGP
    
    public static void explain(BasicPattern bgp, Context context)
    {
        explain("BGP", bgp, context) ; 
    }
    
    public static void explain(String message, BasicPattern bgp, Context context)
    {
        if ( explaining(InfoLevel.ALL, logExec,context) )
            _explain(logExec, message, bgp.toString()) ;
    }

    // ----
    
    private static void _explain(Logger logger, String reason, String explanation)
    {
        while ( explanation.endsWith("\n") || explanation.endsWith("\r") )
            explanation = StrUtils.chop(explanation) ;
        if ( explanation.contains("\n") )
            explanation = reason+"\n"+explanation ;
        else
            explanation = reason+" :: "+explanation ;
        _explain(logger, explanation) ;
        //System.out.println(explanation) ;
    }
    
    private static void _explain(Logger logger, String explanation)
    {
        logger.info(explanation) ;
    }

    // General information
    public static void explain(Context context, String message)
    {
        if ( explaining(InfoLevel.INFO, logInfo, context) )
            _explain(logInfo, message) ;
    }

    public static void explain(Context context, String format, Object... args)
    {
        if ( explaining(InfoLevel.INFO, logInfo, context) )
        {
            // Caveat: String.format is not cheap.
            String str = String.format(format, args) ;
            _explain(logInfo, str) ;
        }
    }
    
    public static boolean explaining(InfoLevel level, Logger logger, Context context)
    {
        if ( ! _explaining(level, context) ) return false ;
        return logger.isInfoEnabled() ;
        
        
    }
    
    private static boolean _explaining(InfoLevel level, Context context)
    {
        Object x = context.get(symLogExec, null) ;
        if ( x == null )
            return false ;
        
        // Enum equality.
        if ( level.equals(x) ) return true ;
        
        if ( x instanceof String )
        {
            String s = (String)x ;
            if ( s.equalsIgnoreCase("info") )
                return level.equals(InfoLevel.INFO) ;
            if ( s.equalsIgnoreCase("fine") ) 
                return level.equals(InfoLevel.FINE) || level.equals(InfoLevel.INFO) ;
            if ( s.equalsIgnoreCase("all") )
                // All levels.
                return true ;
            if ( s.equalsIgnoreCase("true") ) 
                return true ;
        }
        
        return Boolean.TRUE.equals(x) ;
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