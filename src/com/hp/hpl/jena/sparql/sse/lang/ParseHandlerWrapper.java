/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import com.hp.hpl.jena.sparql.sse.Item;

/** Warpper parser handler with pass-through for all operations.
 *  Calls both, first one first.
 * @author Andy Seaborne
 */

public class ParseHandlerWrapper implements ParseHandler
{
    private ParseHandler handler ;
    
    public ParseHandlerWrapper(ParseHandler handler)
    {
        this.handler = handler ;
    }

    public Item getItem()
    {
        return handler.getItem() ;
    }

    public void parseStart()
    {
        handler.parseStart() ;
    }

    public void parseFinish()
    {
        handler.parseFinish() ;
    }

    public void listStart(int line, int column)
    {
        handler.listStart(line, column) ;
    }

    public void listFinish(int line, int column)
    {
        handler.listFinish(line, column) ;
    }

    public void emitBNode(int line, int column, String label)
    { 
        handler.emitBNode(line, column, label) ;
    }

    public void emitIRI(int line, int column, String iriStr)
    {
        handler.emitIRI(line, column, iriStr) ;
    }

    public void emitLiteral(int line, int column, String lex, String lang, String datatype_iri, String datatype_pn)
    {
        handler.emitLiteral(line, column, lex, lang, datatype_iri, datatype_pn) ;
    }

    public void emitPName(int line, int column, String pname)
    {
        handler.emitPName(line, column, pname) ;
    }

    public void emitSymbol(int line, int column, String symbol)
    {
        handler.emitSymbol(line, column, symbol) ;
    }

    public void emitVar(int line, int column, String varName)
    {
        handler.emitVar(line, column, varName) ;
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