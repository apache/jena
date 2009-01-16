/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.serializer;

import java.util.Map;

import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.PrefixMapping2;

public class PrologueSerializer
{
    public static void output(IndentedWriter out, Prologue prologue)
    {
        printBase(prologue, out) ;
        printPrefixes(prologue, out) ;
    }
    
//  public String toString()
//  { return PrintUtils.toString(this) ; }
//  
//  public String toString(PrefixMapping pmap)
//  {
//      IndentedLineBuffer buff = new IndentedLineBuffer() ;
//      IndentedWriter out = buff.getIndentedWriter() ;
//      this.output(out) ;
//      return buff.toString() ;
//  }
//
//  public void output(IndentedWriter out)
//  {
//      printBase(out) ;
//      printPrefixes(out) ;
//  }
//  
    
    private static void printBase(Prologue prologue, IndentedWriter out)
    {
        if ( prologue.getBaseURI() != null && prologue.explicitlySetBaseURI() )
        {
            out.print("BASE    ") ;
            out.print("<"+prologue.getBaseURI()+">") ;
            out.newline() ;
        }
    }

    public static void printPrefixes(Prologue prologue, IndentedWriter out)
    {
        Map<String, String> pmap = null ;

        if ( prologue.getPrefixMapping() instanceof PrefixMapping2 )
        {
            PrefixMapping2 pm2 = (PrefixMapping2)prologue.getPrefixMapping() ;
            pmap = pm2.getNsPrefixMap(false) ;
        }
        else
        {
            Map<String, String> _pmap = prologue.getPrefixMapping().getNsPrefixMap() ;
            pmap = _pmap ;
        }

        if ( pmap.size() > 0 )
        {
            //boolean first = true ;
            for (String k : pmap.keySet())
            {
                String v = pmap.get(k) ;
                out.print("PREFIX  ") ;
                out.print(k) ;
                out.print(':') ;
                out.print(' ', 4-k.length()) ;
                // Include at least one space 
                out.print(' ') ;
                out.print(FmtUtils.stringForURI(v)) ;
                out.newline() ;
            }
        }
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