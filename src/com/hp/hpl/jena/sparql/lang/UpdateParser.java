/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.lang;

import java.io.InputStream ;

import org.openjena.atlas.io.PeekReader ;

import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.update.UpdateRequest ;

/** This class provides the root of lower level access to all the parsers.
 *  Each subclass hides the details of the per-language exception handlers and other
 *  javacc details to provide a methods that deal with setting up Query objects
 *  and using QueryException exceptions for problems.    
 */

public abstract class UpdateParser
{
    public final UpdateRequest parse(UpdateRequest request, String updateString) throws QueryParseException
    {
        // Sort out BOM
        if ( updateString.startsWith("\uFEFF") )
            updateString = updateString.substring(1) ;
        return parse$(request, updateString) ;
    }

    protected abstract UpdateRequest parse$(UpdateRequest request, String updateString) throws QueryParseException ;

    public UpdateRequest parse(UpdateRequest request, InputStream input) throws QueryParseException
    {
        // :-( Wrap in something that we can use to look for a BOM.
        // TODO Move POM processing to grammar and reverse this.
        PeekReader pr = PeekReader.makeUTF8(input) ;
        return parse$(request, pr) ;
    }
    
    protected abstract UpdateRequest parse$(UpdateRequest request, PeekReader pr) throws QueryParseException ;

    public static boolean canParse(Syntax syntaxURI)
    {
        return UpdateParserRegistry.get().containsFactory(syntaxURI) ;
    }
    
    public static UpdateParser createParser(Syntax syntaxURI)
    {
        return UpdateParserRegistry.get().createParser(syntaxURI) ;
    }

    // Do any testing of updates after the construction of the parse tree.
    protected void validateParsedUpdate(UpdateRequest request)
    {
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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