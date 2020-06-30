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

import java.io.InputStream;

/** @deprecated Use {@code TokenizerText.create()...} */
@Deprecated
public class TokenizerFactory {
    // Just in case anyone is uing this operation, the only and proper old world operation. 
    /** @deprecated Use {@code TokenizerText.create().source(in).build();} */
    @Deprecated
    public static Tokenizer makeTokenizerUTF8(InputStream in) {
        return TokenizerText.create().source(in).build();
    }
    
//
//    private static ErrorHandler dftErrorHandler = null;
//
//    /** Discouraged - be careful about character sets */
//    @Deprecated
//    public static Tokenizer makeTokenizer(Reader reader) {
//        return TokenizerText.create().source(reader).build();
//    }
//
//    /** Discouraged - be careful about character sets */
//    @Deprecated
//    public static Tokenizer makeTokenizer(Reader reader, ErrorHandler errorHandler) {
//        return TokenizerText.create().source(reader).errorHandler(errorHandler).build();
//    }
//
//    /** Safe use of a StringReader */
//    public static Tokenizer makeTokenizer(StringReader reader) {
//        return TokenizerText.create().source(reader).build();
//    }
//
//    /** Safe use of a StringReader */
//    public static Tokenizer makeTokenizer(StringReader reader, ErrorHandler errorHandler) {
//        return TokenizerText.create().source(reader).errorHandler(errorHandler).build();
//    }
//
//    public static Tokenizer makeTokenizerUTF8(InputStream in) {
//        return makeTokenizerUTF8(in, dftErrorHandler);
//    }
//
//    public static Tokenizer makeTokenizerUTF8(InputStream input, ErrorHandler errorHandler) {
//        // BOM will be removed
//        return TokenizerText.create().source(input).errorHandler(errorHandler).build();
//    }
//
//    public static Tokenizer makeTokenizerASCII(InputStream input) {
//        return TokenizerText.create().source(input).asciiOnly(true).build();
//    }
//
//    public static Tokenizer makeTokenizerASCII(InputStream input, ErrorHandler errorHandler) {
//        return TokenizerText.create().source(input).asciiOnly(true).errorHandler(errorHandler).build();
//    }
//
//    public static Tokenizer makeTokenizerString(String str) {
//        return TokenizerText.create().fromString(str).build();
//    }
//
//    public static Tokenizer makeTokenizerString(String str, ErrorHandler errorHandler) {
//        return TokenizerText.create().fromString(str).errorHandler(errorHandler).build();
//    }
}
