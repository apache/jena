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

package com.hp.hpl.jena.sparql.sse.lang;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.sparql.lang.ParserBase ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;

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
