/******************************************************************
 * File:        Tokenizer.java
 * Created by:  Dave Reynolds
 * Created on:  24-Jun-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: Tokenizer.java,v 1.3 2004-12-06 13:50:24 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.util;

import java.util.NoSuchElementException;

/**
 * A tokenizer, similar to java's StringTokenizer but allows for quoted
 * character strings which can include other separators.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2004-12-06 13:50:24 $
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
        if (lookahead != null) lookahead = getNextToken();
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
            int start = p;
            while (isLiteral() && p < source.length()) p++;
            state = LITERAL_END;
            return source.substring(start, p);
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

    /**
     * Returns true if the current character a legal literal innard
     */
    private boolean isLiteral() {
        if (is(literalDelim)) {
            // check for previous escape
            if (source.charAt(p-1) == '\\') return true;
            return false;
        } else {
            return true;
        }
    }
} 


/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/