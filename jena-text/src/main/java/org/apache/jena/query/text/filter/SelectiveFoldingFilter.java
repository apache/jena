/**
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

package org.apache.jena.query.text.filter;

import java.io.IOException;
import java.util.Objects;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.ArrayUtil;

/**
 * A Lucene filter based on ASCIIFoldingFilter, but that allows the
 * caller to provide a white list for selective folding filter. Entries in the
 * white list are ignored during the filtering. This is useful for languages
 * that require certain characters to be ignored.
 *
 * @see ASCIIFoldingFilter
 */

public final class SelectiveFoldingFilter extends TokenFilter {
    /*
     * text:defineFilter :selectiveFoldingFilter ;
     * text:filter [
     *      a text:GenericFilter ;
     *      text:class "org.apache.jena.query.text.filter.SelectiveFoldingFilter" ;
     *      text:params (
     *           [ 
     *                text:paramName "whitelisted" ;
     *                text:paramType text:TypeSet ;
     *                text:paramValue ("ç" "á")
     *           ]
     *      )
     * ]
     */

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    /**
     * List of whitelisted characters.
     */
    private CharArraySet whitelisted;

    public SelectiveFoldingFilter(TokenStream input, CharArraySet whitelisted) {
        super(input);
        Objects.requireNonNull(whitelisted, "You must provide the list of whiltelisted characters.");
        this.whitelisted = CharArraySet.unmodifiableSet(CharArraySet.copy(whitelisted));
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            final char[] buffer = termAtt.buffer();
            final int length = termAtt.length();
            // prepare the output char array, adapted from ASCIIFoldingFilter
            final int maxSizeNeeded = 4 * length;
            char[] output = new char[ArrayUtil.oversize(maxSizeNeeded, Character.BYTES)];
            for (int i = 0; i < length; ++i) {
                final char c = buffer[i];
                if (c >= '\u0080' && !whitelisted.contains(c)) {
                    // here we are using the method that will iterate always over a list with a
                    // single char
                    ASCIIFoldingFilter.foldToASCII(buffer, i, output, i, 1);
                } else {
                    output[i] = c;
                }
            }
            termAtt.copyBuffer(output, 0, length);
            return true;
        }
        return false;
    }
}
