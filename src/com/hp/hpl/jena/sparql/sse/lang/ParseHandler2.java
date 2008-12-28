/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import com.hp.hpl.jena.sparql.sse.Item;

/** Splitter for parser handlers.
 *  Calls both, first one first.
 * @author Andy Seaborne
 */

public class ParseHandler2 implements ParseHandler
{
    private ParseHandler handler1 ;
    private ParseHandler handler2 ;
    
    public ParseHandler2(ParseHandler handler1, ParseHandler handler2)
    {
        this.handler1 = handler1 ;
        this.handler2 = handler2 ;
    }

    public Item getItem()
    {
        Item item = handler1.getItem() ;
        if ( item == null )
            item = handler2.getItem() ;
        return item ;
    }

    public void parseStart()
    {
        handler1.parseStart() ;
        handler2.parseStart() ; 
        
    }

    public void parseFinish()
    {
        handler1.parseFinish() ;
        handler2.parseFinish() ; 
    }

    public void listStart(int line, int column)
    {
        handler1.listStart(line, column) ;
        handler2.listStart(line, column) ;
        
    }

    public void listFinish(int line, int column)
    {
        handler1.listFinish(line, column) ;
        handler2.listFinish(line, column) ;
    }

    public void emitBNode(int line, int column, String label)
    { 
        handler1.emitBNode(line, column, label) ;
        handler2.emitBNode(line, column, label) ;
    }

    public void emitIRI(int line, int column, String iriStr)
    {
        handler1.emitIRI(line, column, iriStr) ;
        handler2.emitIRI(line, column, iriStr) ;
    }

    public void emitLiteral(int line, int column, String lex, String lang, String datatype_iri, String datatype_pn)
    {
        handler1.emitLiteral(line, column, lex, lang, datatype_iri, datatype_pn) ;
        handler2.emitLiteral(line, column, lex, lang, datatype_iri, datatype_pn) ;
    }

    public void emitPName(int line, int column, String pname)
    {
        handler1.emitPName(line, column, pname) ;
        handler2.emitPName(line, column, pname) ;
    }

    public void emitSymbol(int line, int column, String symbol)
    {
        handler1.emitSymbol(line, column, symbol) ;
        handler2.emitSymbol(line, column, symbol) ;
    }

    public void emitVar(int line, int column, String varName)
    {
        handler1.emitVar(line, column, varName) ;
        handler2.emitVar(line, column, varName) ;
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