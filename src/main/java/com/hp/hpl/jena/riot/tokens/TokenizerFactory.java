/*
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.tokens;

import java.io.InputStream ;
import java.io.Reader ;

import org.openjena.atlas.io.PeekInputStream ;
import org.openjena.atlas.io.PeekReader ;


public class TokenizerFactory
{
    public static Tokenizer makeTokenizer(Reader reader)
    {
        PeekReader peekReader = PeekReader.make(reader) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }
    
    public static Tokenizer makeTokenizer(InputStream in)
    {
        if ( false )
        {
            // Byte parser
            // This is the fastest way but is less tested
            // At most 10% faster.
            PeekInputStream pin = PeekInputStream.make(in) ;
            Tokenizer tokenizer = new TokenizerBytes(pin) ;
            return tokenizer ;
        }
        
        // Most tested way
        PeekReader peekReader = PeekReader.makeUTF8(in) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }

    public static Tokenizer makeTokenizerString(String str)
    {
        PeekReader peekReader = PeekReader.readString(str) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }
}

/*
 * (c) Copyright 2010 Talis Information Ltd.
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