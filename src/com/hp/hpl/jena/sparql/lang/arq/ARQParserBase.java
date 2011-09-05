/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang.arq;
import org.openjena.atlas.json.io.JSONHandler ;
import org.openjena.atlas.json.io.JSONHandlerBase ;
import org.openjena.atlas.lib.NotImplemented ;

import com.hp.hpl.jena.sparql.lang.ParserQueryBase ;

class ARQParserBase
    extends ParserQueryBase
    implements ARQParserConstants
{
    // JSON
    JSONHandler handler = new JSONHandlerBase() ;
    
    public void setHandler(JSONHandler handler)
    { 
        if ( handler == null )
            this.handler = new JSONHandlerBase() ;
        else
            this.handler = handler ;
    }
    
    // All the signals from the parsing process.
    protected void jsonStartParse()                 { handler.startParse() ; }
    protected void jsonFinishParse()                { handler.finishParse() ; }
    
    protected void jsonStartObject()                { handler.startObject() ; }
    protected void jsonFinishObject()               { handler.finishObject() ; }

    protected void jsonStartPair()                  { handler.startPair() ; }
    protected void jsonKeyPair()                    { handler.keyPair() ; }
    protected void jsonFinishPair()                 { handler.finishPair() ; }
    
    protected void jsonStartArray()                 { handler.startArray() ; }
    protected void jsonElement()                    { handler.element() ; }
    protected void jsonFinishArray()                { handler.finishArray() ; }

    protected void jsonValueString(String image)
    {
        // Strip quotes
        image = image.substring(1,image.length()-1) ;
        handler.valueString(image) ;
    }
        
    protected void jsonValueKeyString(String image) { handler.valueString(image) ; }
    protected void jsonValueInteger(String image)   { handler.valueInteger(image) ; }
    protected void jsonValueDecimal(String image)   { handler.valueDecimal(image) ; }
    protected void jsonValueDouble(String image)    { handler.valueDouble(image) ; }
    protected void jsonValueBoolean(boolean b)      { handler.valueBoolean(b) ; }
    protected void jsonValueNull()                  { handler.valueNull() ; }
    
    protected void jsonValueVar(String image)       { throw new NotImplemented("yet") ; }
}

/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
