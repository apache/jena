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

package org.apache.jena.riot.tokens;

import java.io.ByteArrayInputStream ;
import java.io.InputStream ;
import java.io.Reader ;

import org.apache.jena.atlas.io.PeekReader ;
import org.apache.jena.atlas.lib.StrUtils ;

public class TokenizerFactory
{
    /** Discouraged - be careful about character sets */ 
    public static Tokenizer makeTokenizer(Reader reader)
    {
        PeekReader peekReader = PeekReader.make(reader) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }
    
    public static Tokenizer makeTokenizerUTF8(InputStream in)
    {
        // BOM will be removed
        PeekReader peekReader = PeekReader.makeUTF8(in) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }
   
    public static Tokenizer makeTokenizerASCII(InputStream in)
    {
        PeekReader peekReader = PeekReader.makeASCII(in) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }
    
    public static Tokenizer makeTokenizerASCII(String string)
    {
        byte b[] = StrUtils.asUTF8bytes(string) ;
        ByteArrayInputStream in = new ByteArrayInputStream(b) ;
        return makeTokenizerASCII(in) ;
    }
    
    public static Tokenizer makeTokenizerString(String str)
    {
        PeekReader peekReader = PeekReader.readString(str) ;
        Tokenizer tokenizer = new TokenizerText(peekReader) ;
        return tokenizer ;
    }
}
