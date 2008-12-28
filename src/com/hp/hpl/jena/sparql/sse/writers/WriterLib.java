/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.writers;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class WriterLib
{
    static final int UNDEF = 0 ;
    public static final int NL = 1 ;
    public static final int NoNL = -1 ;
    public static final int NoSP = -2 ;
    
    // ---- Support
    
    // -- Normal markers
    
    /** Start a tagged item - usual bracketting */
    public static void start(IndentedWriter out, String tag, int linePolicy)
    { _start(out, tag, linePolicy, Tags.LPAREN) ; }
    
    /** Finish a taggeditem - usual bracketting */
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

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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