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

package com.hp.hpl.jena.sparql.sse.writers;

import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class WriterLib
{
    static final int UNDEF = 0 ;
    public static final int NL = 1 ;
    public static final int NoNL = -1 ;
    public static final int NoSP = -2 ;
    
    // ---- Support

    // -- Normal markers
    
    /** Start a tagged item - all one line - usual bracketting */
    public static void startOneLine(IndentedWriter out, String tag)
    {
        _start(out, Tags.LPAREN) ;
        out.print(tag) ;
        out.print(" ") ;
    }
    
    /** Finish a tagged item - all one line - usual bracketting */
    public static void finishOneLine(IndentedWriter out, String tag)
    {
        _finish(out, Tags.RPAREN) ;
    }
    
    /** Start a tagged item - usual bracketting */
    public static void start(IndentedWriter out, String tag, int linePolicy)
    { _start(out, tag, linePolicy, Tags.LPAREN) ; }

    /** Finish a tagged item - usual bracketting */
    public static void finish(IndentedWriter out, String tag)
    { _finish(out, tag, Tags.RPAREN) ; }

    /** Start an item - no tag - usual bracketting */
    public static void start(IndentedWriter out)
    { _start(out, Tags.LPAREN) ; }

    /** Finish an item - no tag - usual bracketting */
    public static void finish(IndentedWriter out)
    {  _finish(out, Tags.RPAREN) ;  }

    // -- With the other markers (conventionally, short things)
    
    /** Start an item - alternative bracketting */
    public static void start2(IndentedWriter out, String tag, int linePolicy)
    { _start(out, tag, linePolicy, Tags.LBRACKET) ; }
    
    /** Finish an item - alternative bracketting */
    public static void finish2(IndentedWriter out, String tag)
    { _finish(out, tag, Tags.RBRACKET) ;  }
    
    /** Start an item - no tag - alternative bracketting */
    public static void start2(IndentedWriter out)
    { _start(out, Tags.LBRACKET) ; }

    /** Finish an item - no tag - alternative bracketting */
    public static void finish2(IndentedWriter out)
    { _finish(out, Tags.RBRACKET) ;  }
    
    // ---- Workers
    
    private static void _start(IndentedWriter out, String tag, int linePolicy, String startMarker)
    {
        _start(out, startMarker) ;
        out.print(tag) ;

        switch (linePolicy)
        {
            case NL:    out.println(); break ;
            case NoNL:  out.print(" ") ; break ;
            case NoSP:  break ;
            case UNDEF: throw new ARQInternalErrorException("Explicit tag not no line policy") ;
        }
        out.incIndent() ; 
    }
    
    private static void _finish(IndentedWriter out, String tag, String finishMarker)
    {
        out.decIndent() ;
        _finish(out, finishMarker) ;
    }
    
    private static void _start(IndentedWriter out, String startMarker)
    { out.print(startMarker) ; }
    
    private static void _finish(IndentedWriter out, String finishMarker)
    { out.print(finishMarker) ; }
}
