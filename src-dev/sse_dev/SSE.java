/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sse_dev;

import java.io.*;

import sse_dev.parser.ParseException;
import sse_dev.parser.SSE_Parser;
import sse_dev.parser.TokenMgrError;

import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.util.FileUtils;

public class SSE
{
    public static Item parseFile(String filename)
    {
        try {
            InputStream in = new FileInputStream(filename) ;
            return parse(in) ;
        } 
        catch (FileNotFoundException ex)
        { throw new NotFoundException("Not found: "+filename) ; }
    }
    
    public static Item parseString(String str)
    {
        return parse(new StringReader(str)) ;
    }
    
    public static Item parse(InputStream in)
    {
        Reader reader = FileUtils.asBufferedUTF8(in) ;
        return parse(reader) ;
    }
    
    private static Item parse(Reader reader)
    {
        SSE_Parser p = new SSE_Parser(reader) ;
        try
        {
            return p.parse() ;
       } 
       catch (ParseException ex)
       { throw new SSEParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn) ; }
       catch (TokenMgrError tErr)
       { 
           // Last valid token : not the same as token error message - but this should not happen
           int col = p.token.endColumn ;
           int line = p.token.endLine ;
           throw new SSEParseException(tErr.getMessage(), line, col) ;
       }
       //catch (JenaException ex)  { throw new TurtleParseException(ex.getMessage(), ex) ; }
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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