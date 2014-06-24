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

package com.hp.hpl.jena.util;

import java.util.NoSuchElementException;

/**
 * A tokenizer, similar to java's StringTokenizer but allows for quoted
 * character strings which can include other separators.
 */
public class Tokenizer {
    
    /** The string being parsed */
    protected String source;
    
    /** The index of the first unreturned char in source */
    protected int p;

    /** The set of delimiter characters */
    protected String delim;
    
    /** If true then delimiters should be returned as tokens */
    protected boolean returnDelims;
    
    /** Literal string delimiters */
    protected String literalDelim;
    
    /** The lex state */
    protected int state;
    
    /** A lookahead for tokens */
    protected String lookahead;
    
    /** State flag: normal parse */
    protected static final int NORMAL = 1;
    
    /** State flag: start of literal */
    protected static final int LITERAL_START = 2;
    
    /** State flag: end of literal */
    protected static final int LITERAL_END = 3;
    
    /**
     * Constructor.
     * @param str the source string to be parsed
     * @param delim The set of delimiter characters
     * @param literalDelim Literal string delimiters
     * @param returnDelims If true then delimiters should be returned as tokens
     */
    public Tokenizer(String str, String delim, String literalDelim, boolean returnDelims) {
        this.source = str;
        this.delim = delim;
        this.literalDelim = literalDelim;
        this.returnDelims = returnDelims;
        p = 0; 
        state = NORMAL;
    }
    
    /**
     * Return the next token.
     * @throws java.util.NoSuchElementException if there are no more tokens available
     */
    public String nextToken() {
        String result = null;
        if (lookahead != null) {
            result = lookahead;
            lookahead = null;
        } else {
            result = getNextToken();
        }
        if (result == null) {
            throw new NoSuchElementException("No more elements in tokenized string");
        } 
        if (!returnDelims) {
            if (result.length() == 1) {
                char c = result.charAt(0);
                if (delim.indexOf(c) != -1 || literalDelim.indexOf(c) != -1) {
                    return nextToken();
                }
            }
        }
        return result;
    }
    
    /**
     * Test if there are more tokens which can be returned.
     */
    public boolean hasMoreTokens() {
        if (lookahead == null) lookahead = getNextToken();
        return lookahead != null;
    }
    
    /**
     * Find the next token which can either be a delimiter or a real token.
     */
    private String getNextToken() {
        if (p >= source.length()) {
            return null;
        }
        switch(state) {
        case NORMAL:
            if (is(literalDelim)) {
                state = LITERAL_START;
                p++;
                return source.substring(p-1, p);
            } else if (is(delim)) {
                p++;
                return source.substring(p-1, p);
            } else {
                int start = p;
                p++;
                while (p < source.length() && ! is(delim)) p++;
                return source.substring(start, p);
            }
        case LITERAL_START:
            char delim = source.charAt(p-1);
            StringBuilder literal = new StringBuilder();
            while (p < source.length()) {
                char c = source.charAt(p);
                if (c == '\\') {
                    p++;
                    if (p >= source.length()) break;
                    c = source.charAt(p);
                } else {
                    if (c == delim) break;
                }
                literal.append(c);
                p++;
            }
            state = LITERAL_END;
            return literal.toString();
        case LITERAL_END:
            state = NORMAL;
            p++;
            return source.substring(p-1, p);
        }
        return null;
    }
    
    
    /**
     * Returns true if the current character is contained in the given classification.
     */
    private boolean is(String classification) {
        return classification.indexOf(source.charAt(p)) != -1;
    }
    
    public static void main(String[] args) {
        System.out.println("Starting");
        Tokenizer tokenizer = new Tokenizer("foo     ''  'a literal' \"a double literal\" 'literal with \\\" in it' 'literal with unquoted\"in it'", "()[], \t\n\r", "'\"", true);
        while (tokenizer.hasMoreTokens()) {
            String t = tokenizer.nextToken();
            System.out.println("Token: [" +  t + "]");
        }
    }
}
