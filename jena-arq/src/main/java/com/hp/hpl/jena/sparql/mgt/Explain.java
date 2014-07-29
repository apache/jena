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

package com.hp.hpl.jena.sparql.mgt ;

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.QuadPattern ;
import com.hp.hpl.jena.sparql.path.Path ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.writers.WriterNode ;
import com.hp.hpl.jena.sparql.util.Context ;

/**
 * Execution logging for query processing on a per query basis. This class
 * provides an overlay on top of the system logging to provide control of log
 * message down to a per query basis. The associated logging channel must also
 * be enabled.
 * 
 * An execution can detail the query, the algebra and every point at which the
 * dataset is touched.
 * 
 * Caution: logging can be a significant cost for small queries and for
 * memory-backed datasets because of formatting the output and disk or console
 * output overhead.
 * 
 * @see ARQ#logExec
 * @see ARQ#getExecutionLogging
 * @see ARQ#setExecutionLogging
 */

public class Explain {
    /**
     * Control whether messages include multiple line output. In multiple line
     * output, subsequent lines start with a space to help log file parsing.
     */
    public static boolean MultiLineMode = true ;

    /*
     * The logging system provided levels: TRACE < DEBUG < INFO < WARN < ERROR <
     * FATAL Explain logging is always at logging level INFO. Per query: SYSTEM
     * > EXEC (Query) > DETAIL (Algebra) > DEBUG (every BGP)
     * 
     * Control: tdb:logExec = true (all), or enum
     * 
     * Document: Include setting different loggers etc for log4j.
     */

    // Need per-query identifier.

    // These are the per-execution levels.

    /** Information level for query execution. */
    public static enum InfoLevel {
        /** Log each query */
        INFO {
            @Override
            public int level() {
                return 10 ;
            }
        },

        /** Log each query and it's algebra form after optimization */
        FINE {
            @Override
            public int level() {
                return 20 ;
            }
        },

        /** Log query, algebra and every database access (can be expensive) */
        ALL {
            @Override
            public int level() {
                return 30 ;
            }
        },

        /** No query execution logging. */
        NONE {
            @Override
            public int level() {
                return -1 ;
            }
        } ;

        abstract public int level() ;

        public static InfoLevel get(String name) {
            if ( "ALL".equalsIgnoreCase(name) )
                return ALL ;
            if ( "FINE".equalsIgnoreCase(name) )
                return FINE ;
            if ( "INFO".equalsIgnoreCase(name) )
                return INFO ;
            if ( "NONE".equalsIgnoreCase(name) )
                return NONE ;
            return null ;
        }
    }

    static public final Logger logExec = ARQ.getExecLogger() ;
    static public final Logger logInfo = ARQ.getInfoLogger() ;

    //
    // // MOVE ME to ARQConstants
    // public static final Symbol symLogExec = TDB.symLogExec ;
    // //ARQConstants.allocSymbol("logExec") ;

    // ---- Query

    public static void explain(Query query, Context context) {
        explain("Query", query, context) ;
    }

    public static void explain(String message, Query query, Context context) {
        if ( explaining(InfoLevel.INFO, logExec, context) ) {
            // One line or indented multiline format
            IndentedLineBuffer iBuff = new IndentedLineBuffer() ;
            if ( true )
                iBuff.incIndent() ;
            else
                iBuff.setFlatMode(true) ;
            query.serialize(iBuff) ;
            String x = iBuff.asString() ;
            _explain(logExec, message, x, true) ;
        }
    }

    // ---- Algebra

    public static void explain(Op op, Context context) {
        explain("Algebra", op, context) ;
    }

    private static final boolean MultiLinesForOps = true ;
    private static final boolean MultiLinesForPatterns = true ;

    public static void explain(String message, Op op, Context context) {
        if ( explaining(InfoLevel.FINE, logExec, context) ) {
            try (IndentedLineBuffer iBuff = new IndentedLineBuffer()) {
                if ( MultiLinesForOps )
                    iBuff.incIndent() ;
                else
                    iBuff.setFlatMode(true) ;
                op.output(iBuff) ;
                String x = iBuff.asString() ;
                _explain(logExec, message, x, true) ;
            }
        }
    }

    // ---- BGP and quads

    public static void explain(BasicPattern bgp, Context context) {
        explain("BGP", bgp, context) ;
    }

    public static void explain(String message, BasicPattern bgp, Context context) {
        if ( explaining(InfoLevel.ALL, logExec, context) ) {
            try (IndentedLineBuffer iBuff = new IndentedLineBuffer()) {
                if ( MultiLinesForPatterns )
                    iBuff.incIndent() ;
                formatTriples(iBuff, bgp) ;
                iBuff.flush() ;
                String str = iBuff.toString() ;
                _explain(logExec, message, str, false) ;
            }
        }
    }

    public static void explain(String message, QuadPattern quads, Context context) {
        if ( explaining(InfoLevel.ALL, logExec, context) ) {
            try (IndentedLineBuffer iBuff = new IndentedLineBuffer()) {
                if ( MultiLinesForPatterns )
                    iBuff.incIndent() ;
                formatQuads(iBuff, quads) ;
                iBuff.flush() ;
                String str = iBuff.toString() ;
                _explain(logExec, message, str, false) ;
            }
        }
    }

    // public static void explainHTTP(String message, String request, Context
    // context)
    // {
    // if ( explaining(InfoLevel.ALL, logExec,context) )
    // {
    // IndentedLineBuffer iBuff = new IndentedLineBuffer() ;
    // if ( true )
    // iBuff.incIndent() ;
    // else
    // iBuff.setFlatMode(true) ;
    // ???
    // iBuff.flush() ;
    // String str = iBuff.toString() ;
    // _explain(logExec, message, str, false) ;
    // }
    // }

    // TEMP : quad list that looks right.
    // Remove when QuadPatterns roll through from ARQ.

    private static void formatQuads(IndentedLineBuffer out, QuadPattern quads) {
        SerializationContext sCxt = SSE.sCxt((SSE.defaultPrefixMapWrite)) ;

        boolean first = true ;
        for ( Quad qp : quads ) {
            if ( !first ) {
                if ( ! MultiLinesForPatterns )
                    out.print(" ") ;
            } else
                first = false ;
            out.print("(") ;
            if ( qp.getGraph() == null )
                out.print("_") ;
            else
                WriterNode.output(out, qp.getGraph(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getSubject(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getPredicate(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getObject(), sCxt) ;
            out.print(")") ;
            if ( MultiLinesForPatterns )
                out.println() ;
        }
    }

    private static void formatTriples(IndentedLineBuffer out, BasicPattern triples) {
        SerializationContext sCxt = SSE.sCxt((SSE.defaultPrefixMapWrite)) ;

        boolean first = true ;
        for ( Triple qp : triples ) {
            if ( !first ) {
                if ( !MultiLinesForPatterns )
                    out.print(" ") ;
            } else
                first = false ;
            out.print("(") ;
            WriterNode.output(out, qp.getSubject(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getPredicate(), sCxt) ;
            out.print(" ") ;
            WriterNode.output(out, qp.getObject(), sCxt) ;
            out.print(")") ;
            if ( MultiLinesForPatterns )
                out.println() ;
        }
    }

    // ----

    private static void _explain(Logger logger, String reason, String explanation, boolean newlineAlways) {
        // "explanation" should already be indented with some whitespace
        while (explanation.endsWith("\n") || explanation.endsWith("\r"))
            explanation = StrUtils.chop(explanation) ;
        if ( newlineAlways || explanation.contains("\n") )
            explanation = reason + "\n" + explanation ;
        else
            explanation = reason + " :: " + explanation ;
        _explain(logger, explanation) ;
        // System.out.println(explanation) ;
    }

    private static void _explain(Logger logger, String explanation) {
        logger.info(explanation) ;
    }

    // General information
    public static void explain(Context context, String message) {
        if ( explaining(InfoLevel.INFO, logInfo, context) )
            _explain(logInfo, message) ;
    }

    public static void explain(Context context, String format, Object... args) {
        if ( explaining(InfoLevel.INFO, logInfo, context) ) {
            // Caveat: String.format is not cheap.
            String str = String.format(format, args) ;
            _explain(logInfo, str) ;
        }
    }

    // public static boolean explaining(InfoLevel level, Context context)
    // {
    // return explaining(level, logExec, context) ;
    // }

    public static boolean explaining(InfoLevel level, Logger logger, Context context) {
        if ( !_explaining(level, context) )
            return false ;
        return logger.isInfoEnabled() ;
    }

    private static boolean _explaining(InfoLevel level, Context context) {
        if ( level == InfoLevel.NONE )
            return false ;

        Object x = context.get(ARQ.symLogExec, null) ;

        if ( x == null )
            return false ;

        // Enum level.
        if ( level.level() == InfoLevel.NONE.level() )
            return false ;

        if ( x instanceof InfoLevel ) {
            InfoLevel z = (InfoLevel)x ;
            if ( z == InfoLevel.NONE )
                return false ;
            return (z.level() >= level.level()) ;
        }

        if ( x instanceof String ) {
            String s = (String)x ;

            if ( s.equalsIgnoreCase("info") )
                return level.equals(InfoLevel.INFO) ;
            if ( s.equalsIgnoreCase("fine") )
                return level.equals(InfoLevel.FINE) || level.equals(InfoLevel.INFO) ;
            if ( s.equalsIgnoreCase("all") )
                // All levels.
                return true ;
            // Backwards compatibility.
            if ( s.equalsIgnoreCase("true") )
                return true ;
            if ( s.equalsIgnoreCase("none") )
                return false ;

        }

        return Boolean.TRUE.equals(x) ;
    }

    // Path
    public static void explain(Node s, Path path, Node o, Context context) {
        explain("Path", s, path, o, context) ;
    }

    public static void explain(String message, Node s, Path path, Node o, Context context) {
        if ( explaining(InfoLevel.ALL, logExec, context) ) {
            String str = s + " " + path + " " + o ;
            _explain(logExec, message, str, false) ;
        }
    }
}
