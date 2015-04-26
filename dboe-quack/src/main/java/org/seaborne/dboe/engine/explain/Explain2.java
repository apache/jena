/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.explain;

import java.io.PrintStream ;
import java.util.HashSet ;

import org.slf4j.Logger ;

import org.apache.jena.query.ARQ ;

public class Explain2
{
    private static final Logger log = ARQ.getExecLogger() ;
    
    static ExplainCategory generateCategory(String shortName) {
        return ExplainCategory.create(shortName) ;
    }
    
    // Explain categories
    public static ExplainCategory QUERY         = allocSymbol("QUERY") ;
    public static ExplainCategory ALGEBRA       = allocSymbol("ALGEBRA") ;
    public static ExplainCategory EXECUTION     = allocSymbol("EXECUTION") ;
    
    static private ExplainCategory allocSymbol(String label) {
        return ExplainCategory.create(label) ;
    }
    
    private static HashSet<ExplainCategory> active = new HashSet<ExplainCategory>() ;
    
//    @SuppressWarnings("hiding")
//    enum ExplainCategory { QUERY, ALGEBRA, EXECUTION, STEP }
//    
//    static EnumSet<ExplainCategory> active = EnumSet.noneOf(ExplainCategory.class) ;
    
    public static void setActive(ExplainCategory eCat) {
        active.add(eCat) ;
    }
    
    public static boolean isActive(ExplainCategory eCat) {
        return active.contains(eCat) ;
    }

    public static void remove(ExplainCategory eCat) {
        active.remove(eCat) ;
    }
    
    private static final PrintStream out = System.out ;
    
    // Long form markers.
    private static final String startMarker     = ">> " ;
    private static final String messageMarker   = "..     " ;
    private static final String finishMarker    = "<< " ;

    public static void explainNoLog(ExplainCategory eCat, String fmt, Object ... args) {
        if ( active.contains(eCat) ) {
            out.print(">> ") ; out.println(eCat.getlabel()) ;
            out.printf(fmt, args) ;
            if ( ! fmt.endsWith("\n") )
                out.println() ;
            out.print("<< ") ; out.println(eCat.getlabel()) ;
        }
    }
    
    public static void explain(ExplainCategory eCat, Object obj) {
        if ( active.contains(eCat) ) {
            Logger log = ARQ.getExecLogger() ;
            if ( log.isInfoEnabled() ) {
                output(log, eCat, String.valueOf(obj)) ;
            }
        }
    }
    
    private static PrintStream output = System.out ; 
    public static void explain(ExplainCategory eCat, String fmt, Object ... args) {
        if ( true ) {
            explain(ARQ.getExecLogger(), eCat, fmt, args) ;
            return ;
        }
        
        // Without logger.
        if ( active.contains(eCat) ) {
            String msg ;
            if ( args == null || args.length == 0 ) {
                msg = fmt ;
                if ( msg.endsWith("\n") )
                    msg = fmt.substring(0, fmt.length()-1) ;
            }
            else
                msg = String.format(fmt, args) ;

            if ( msg.contains("\n") ) {
                String lines[] = msg.split("\n") ;
                output.println(startMarker + eCat.getlabel()) ;
                for (String line : lines)
                    output.println(messageMarker + line) ;
                output.println(finishMarker + eCat.getlabel()) ;
            } else {
                output.println(msg) ;
            }
            output.flush() ;
        }
    }

    
    public static void explain(Logger log, ExplainCategory eCat, String fmt, Object ... args) {
        if ( active.contains(eCat) ) {
            if ( log.isInfoEnabled() ) {
                String msg ;
                if ( args == null || args.length == 0 ) {
                    msg = fmt ;
                    if ( msg.endsWith("\n") )
                        msg = fmt.substring(0, fmt.length()-1) ;
                }
                else
                    msg = String.format(fmt, args) ;
                output(log, eCat, msg) ;
            }
        }
    }

    private static void output(Logger log, ExplainCategory eCat, String msg) {
        if ( msg.contains("\n") ) {
            String lines[] = msg.split("\n") ;
            log.info(startMarker + eCat.getlabel()) ;
            for (String line : lines)
                log.info(messageMarker + line) ;
            log.info(finishMarker + eCat.getlabel()) ;
        } else {
            log.info(msg) ;
        }
    }
}
