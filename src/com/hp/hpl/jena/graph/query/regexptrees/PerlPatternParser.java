/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: PerlPatternParser.java,v 1.6 2004-08-17 15:15:08 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.query.regexptrees;

import java.util.*;

/**
     Parse Perl5 patterns into RegexpTree structures, or throw an exception for
     cases that haven't been implemented.
     
 	@author hedgehog
*/
public class PerlPatternParser
    {
    final protected String toParse;
    protected int pointer;
    protected RegexpTreeGenerator gen;
    
    public PerlPatternParser( String toParse )
        { this( toParse, new SimpleGenerator() ); }
    
    public PerlPatternParser( String toParse, RegexpTreeGenerator gen )
        { this.toParse = toParse; 
        this.gen = gen; }
    
    public RegexpTree parseAtom()
        {
        if (pointer < toParse.length())
            {
            char ch = toParse.charAt( pointer++ );
            if (ch == '.')
                return gen.getAnySingle();
            else if (ch == '^')
                return gen.getStartOfLine();
            else if (ch == '$')
                return gen.getEndOfLine();
            else if (ch == '|' || ch == ')' || ch == ']')
                { pointer -= 1; return gen.getNothing(); }
            else if (notSpecial( ch ))
                return gen.getText( ch );
            else
                throw new RegexpTree.UnsupportedException();
            }
        return gen.getNothing();
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
                    return gen.getZeroOrMore( d );
                    
                case '+':
                    pointer += 1;
                    return gen.getOneOrMore( d );
                    
                case '?':
                    pointer += 1;
                    return gen.getOptional( d );
                    
                case '{':
                    throw new RegexpTree.UnsupportedException();
                    
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
    public RegexpTree parseSeq()
        {
        List operands = new ArrayList();
        while (true)
            {
            RegexpTree next = parseElement();
            if (next.equals( gen.getNothing() ) ) break;
            operands.add( next );
            }
        return gen.getSequence( operands );
        }

    public RegexpTree parseAlts()
        {
        List operands = new ArrayList();
        while (true)
            {
            RegexpTree next = parseSeq();
            operands.add( next );
            if (pointer < toParse.length() && toParse.charAt( pointer ) == '|') pointer += 1;
            else break;
            }
        return gen.getAlternatives( operands );
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