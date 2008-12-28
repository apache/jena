/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import com.hp.hpl.jena.sparql.sse.Item;


/** Tracing parser handler - logs what the core parse sees */ 

public class ParseHandlerDebug implements ParseHandler
{
    int count = 0 ;
    
    private void indent()
    {
        for ( int i = 0 ; i < count ; i++ ) System.out.print("  ") ;
    }

    private void start(int line, int column)
    { 
        System.out.print("["+line+", "+column+"]  ") ; 
        indent() ;
    }

    public Item getItem()       { return null ; }
    
    public void parseStart()
    { System.out.println("<<<<") ; }

    public void parseFinish()
    { System.out.println(">>>>") ; }

    public void listStart(int line, int column)
    { 
        start(line, column) ;
        count++ ;
        System.out.println("(") ;
    }

    public void listFinish(int line, int column)
    {
        count-- ;         
        start(line, column) ;
        System.out.println(")") ;
    }



    public void emitBNode(int line, int column, String label)
    { 
        start(line, column) ;
        System.out.println("BNode: "+label) ;
    }


    public void emitIRI(int line, int column, String iriStr)
    { 
        start(line, column) ;
        System.out.println("IRI: "+iriStr) ;
    }

    public void emitLiteral(int line, int column, String lex, String lang, String datatype_iri, String datatype_pn)
    { 
        start(line, column) ;
        if ( lang != null )
            System.out.println("Literal: "+lex+" @"+lang) ;
        else if ( datatype_iri != null )
            System.out.println("Literal: "+lex+" ^^"+datatype_iri) ;
        else if ( datatype_pn != null )
            System.out.println("Literal: "+lex+" ^^"+datatype_pn) ;
    }

    public void emitPName(int line, int column, String pname)
    { 
        start(line, column) ;
        System.out.println("PName: "+pname) ;
    }

    public void emitSymbol(int line, int column, String symbol)
    { 
        start(line, column) ;
        System.out.println("Symbol: "+symbol) ;
    }

    public void emitVar(int line, int column, String varName)
    { 
        start(line, column) ;
        System.out.println("Var: "+varName) ;
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