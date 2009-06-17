/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.writers;

import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class WriterBasePrefix
{
    private static final int NL = WriterLib.NL ;
    private static final int NoNL = WriterLib.NoNL ;
    private static final int NoSP = WriterLib.NoSP ;

    public static interface Fmt { void format() ; }
    //public static Fmt fmt = new Fmt(){ public void format() {}} ;

    /** Output, write the thing with formater fmt */
    public static void output(IndentedWriter iWriter, Fmt fmt, Prologue prologue)
    {
        boolean printBase = false ;

        boolean closeBase = printBase(iWriter, prologue) ;
        boolean closePrefix = printPrefix(iWriter, prologue) ;
        
        if ( fmt != null )
            fmt.format() ;

        if ( closeBase )
            WriterLib.finish(iWriter, Tags.tagBase) ;
        if ( closePrefix )
            WriterLib.finish(iWriter, Tags.tagPrefix) ;
        iWriter.ensureStartOfLine() ;
        iWriter.flush();
    }
    
    private static boolean printBase(IndentedWriter iWriter, Prologue prologue)
    {
        String baseURI = prologue.getBaseURI() ;
        
        if ( baseURI != null )
        {
            WriterLib.start(iWriter, Tags.tagBase, NoNL) ;   
            iWriter.print(FmtUtils.stringForURI(baseURI)) ;
            iWriter.println();
            return true ;
        }
        return false ;
    }
    
    private static boolean printPrefix(IndentedWriter iWriter, Prologue prologue)
    {
        PrefixMapping prefixMapping = prologue.getPrefixMapping() ;

        if ( prefixMapping != null )
        {
            Map<String, String> m = prefixMapping.getNsPrefixMap() ;
            if ( ! m.isEmpty() )
            {
                int s = iWriter.getCol() ;
                WriterLib.start(iWriter, Tags.tagPrefix, NoNL) ;
                WriterLib.start(iWriter) ;

                // Indent to this col.
                int len = iWriter.getCurrentOffset() ;

                iWriter.incIndent(len) ;
                Iterator<String> iter = m.keySet().iterator();
                boolean first = true ;
                for ( ; iter.hasNext() ; )
                {
                    if ( ! first )
                        iWriter.println() ;
                    first = false ;
                    String prefix = iter.next();
                    String uri = prefixMapping.getNsPrefixURI(prefix) ;
                    // Base relative URI = but not prefix mappings!
                    uri = FmtUtils.stringForURI(uri, prologue.getBaseURI()) ;
                    WriterLib.start(iWriter) ;
                    iWriter.print(prefix) ;
                    iWriter.print(": ") ;
                    iWriter.print(uri) ;
                    WriterLib.finish(iWriter) ;
                }
                iWriter.decIndent(len) ;
                WriterLib.finish(iWriter) ;

                iWriter.ensureStartOfLine() ;
                return true ;
            }
        }
        return false ;
    }
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