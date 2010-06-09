/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.atlas.json.io.parserjavacc.javacc;

import org.openjena.atlas.json.io.JSONHandler ;
import org.openjena.atlas.json.io.JSONHandlerBase ;

public class JSON_ParserBase
{
    JSONHandler handler = new JSONHandlerBase() ;
    
    public void setHandler(JSONHandler handler)
    { 
        if ( handler == null )
            this.handler = new JSONHandlerBase() ;
        else
            this.handler = handler ;
    }
    
    // All the signals from the parsing process.
    protected void startParse()                 { handler.startParse() ; }
    protected void finishParse()                { handler.finishParse() ; }
    
    protected void startObject()                { handler.startObject() ; }
    protected void finishObject()               { handler.finishObject() ; }

    protected void startPair()                  { handler.startPair() ; }
    protected void keyPair()                    { handler.keyPair() ; }
    protected void finishPair()                 { handler.finishPair() ; }
    
    protected void startArray()                 { handler.startArray() ; }
    protected void element()                    { handler.element() ; }
    protected void finishArray()                { handler.finishArray() ; }

    protected void valueString(String image)
    {
        // Strip quotes
        image = image.substring(1,image.length()-1) ;
        handler.valueString(image) ;
    }
        
    protected void valueInteger(String image)   { handler.valueInteger(image) ; }
    protected void valueDecimal(String image)   { handler.valueDecimal(image) ; }
    protected void valueDouble(String image)    { handler.valueDouble(image) ; }
    protected void valueBoolean(boolean b)      { handler.valueBoolean(b) ; }
    protected void valueNull()                  { handler.valueNull() ; }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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