/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.lang;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.sparql.lang.ParserBase;
import com.hp.hpl.jena.sparql.sse.SSEParseException;

public class ParserSSEBase extends ParserBase
{
    private ParseHandler handler = null ;
    
    public void setHandler(ParseHandler handler) { this.handler = handler ; }
    
    protected void parseStart()
    { handler.parseStart() ; }
    
    protected void parseFinish()
    { handler.parseFinish() ; }

    protected void listStart(int line, int column)
    { handler.listStart(line, column) ; }

    protected void listFinish(int line, int column)
    { handler.listFinish(line, column) ; }

    protected void emitBNode(int line, int column, String label)
    { handler.emitBNode(line, column, label) ; }

    protected void emitIRI(int line, int column, String iriStr)
    { handler.emitIRI(line, column, iriStr) ; }

    protected void emitPName(int line, int column, String pname)
    { handler.emitPName(line, column, pname) ; }

    protected void emitSymbol(int line, int column, String pname)
    {
        // XXX Fix escapes
        handler.emitSymbol(line, column, pname) ;
    }

    protected void emitVar(int line, int column, String varName)
    { handler.emitVar(line, column, varName) ; }
    
    protected void emitLiteral(int currLine, int currColumn, String lex, String lang, String dt_iri, String dt_pname)
    { 
        // XXX Fix escapes
        if ( lang != null )
        {
            if ( dt_iri != null || dt_pname != null )
                throwParseException("Internal error (lang and datatype)", currLine, currColumn) ;
        }
        else
        {
            if ( dt_iri != null && dt_pname != null )
                throwParseException("Internal error (datatype from IRI and pname)", currLine, currColumn) ;
        }
        handler.emitLiteral(currLine, currColumn, lex, lang, dt_iri, dt_pname) ;
    }

    protected void emitLiteralInteger(int beginLine, int beginColumn, String image)
    { 
        emitLiteral(beginLine, beginColumn, image, null, XSDDatatype.XSDinteger.getURI(), null) ;
    }

    protected void emitLiteralDecimal(int beginLine, int beginColumn, String image)
    {
        emitLiteral(beginLine, beginColumn, image, null, XSDDatatype.XSDdecimal.getURI(), null) ;
    }

    protected void emitLiteralDouble(int beginLine, int beginColumn, String image)
    {
        emitLiteral(beginLine, beginColumn, image, null, XSDDatatype.XSDdouble.getURI(), null) ;
    }

    public static void throwParseException(String msg, int line, int column)
    {
        throw new SSEParseException("Line " + line + ", column " + column + ": " + msg,
                                    line, column) ;
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