/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: PerlPatternParser.java,v 1.2 2004-08-16 18:30:57 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query.regexptrees;

import java.util.*;

public class PerlPatternParser
    {
    final protected String toParse;
    protected int pointer;
    
    public PerlPatternParser( String toParse )
        { this.toParse = toParse; }
    
    public RegexpTree parseAtom()
        {
        if (pointer < toParse.length())
            {
            char ch = toParse.charAt( pointer++ );
            if (ch == '.')
                return new AnySingle();
            else if (ch == '^')
                return new StartOfLine();
            else if (ch == '$')
                return new EndOfLine();
            else if (notSpecial( ch ))
                return new Text( "" + ch );
            else
                return null;
            }
        return null;
        }
    
    public static boolean notSpecial( char ch )
        {
        switch (ch)
            {
            case '.':
            case '\\':
            case '*': case '+':
            case '?':
            case '|':
            case '^': case '$':
            case '(': case ')':
            case '{': case '}':
            case '[': case ']':
                return false;
            default: 
                return true;
            }
        }
    
    public String getString()
        { return toParse; }
    
    public int getPointer()
        { return pointer; }
    
    public RegexpTree parseQuantifier( RegexpTree d )
        {
        if (pointer < toParse.length())
            {
            char ch = toParse.charAt( pointer );
            switch (ch)
                {
                case '*':
                    pointer += 1;
                    return new ZeroOrMore( d );
                    
                case '+':
                    pointer += 1;
                    return new OneOrMore( d );
                    
                case '?':
                    pointer += 1;
                    return new Optional( d );
                    
                case '{':
                    return null;
                    
                default:
                    return d;
                }
            }
        else
            return d;
        }

    /**
    	@return
    */
    public Object parseSeq()
        {
        List operands = new ArrayList();
        while (true)
            {
            RegexpTree next = parseElement();
            if (next == null) break;
            operands.add( next );
            }
        return Sequence.create( operands );
        }
    
    private RegexpTree parseElement()
        {
        RegexpTree atom = parseAtom();
        if (atom == null) return null;
        return parseQuantifier( atom );
        }
    }

/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP
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