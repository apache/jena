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

package com.hp.hpl.jena.graph.query.regexptrees;

import java.util.*;

/**
     Parse Perl5 patterns into RegexpTree structures, or throw an exception for
     cases that haven't been implemented.
     
 	@author hedgehog
*/
public class PerlPatternParser
    {
    /**
         The string being parsed, as supplied to the constructor(s).
    */
    protected final String toParse;
    
    /**
         The index into the string of the next undealt-with character, ie, it starts at 0.
    */
    protected int pointer;
    
    /**
         The length of the string to parse, used as a limit.
    */
    protected final int limit;
    
    /**
         The generator for the RegexpTree nodes to be used in the parse.
    */
    protected RegexpTreeGenerator generator;
    
    /**
        Count of how many back-references match-points seen so far.
    */
    protected int matchPointsSeen;
    
    /**
         The digits, in order.
    */
    public static final String digits = "0123456789";
    
    /**
         The characters that are (non-)matchable by \w[W].
    */
    public static final String wordChars =
        digits
        + "abcdefghijklmnopqrstuvwxyz"
        + "_"
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        ;
    
    /**
         Initialise this parser with the string to parse and with the default
         generator (SimpleGenerator).
    */
    public PerlPatternParser( String toParse )
        { this( toParse, new SimpleGenerator() ); }
    
    /**
         Initialise this parser with the string to parse and with the generator to
         use for node construction.
    */
    public PerlPatternParser( String toParse, RegexpTreeGenerator gen )
        { this.toParse = toParse; 
        this.limit = toParse.length();
        this.generator = gen; }
    
    /**
        Answer the result of parsing the given string as a sequence of alternatives.
    */
    public static RegexpTree parse( String string )
        { return new PerlPatternParser( string ) .parseAlts(); }
    
    /**
        Answer the result of parsing the given string as a sequence of alternatives,
        using the supplied generator for the pattern nodes.
    */    
    public static RegexpTree parse( String string, RegexpTreeGenerator gen )
        { return new PerlPatternParser( string, gen ) .parseAlts(); }
    
    /**
         Exception thrown if a syntax error is detected. Further details are in the
         error message - it doesn't seem worth worrying about having different
         classes for different errors. Possibly this should be a non-static class so
         that it can get at the current context?
    */
    public static class SyntaxException extends RuntimeException
        {
        public SyntaxException( String message )
            { super( message ); }
        }
    
    /**
         Answer the string that this parser is parsing.
    */
    public String getString()
        { return toParse; }
    
    /**
         Answer the current index into the parse string.
    */
    public int getPointer()
        { return pointer; }
    
    /**
        Answer the character under the pointer, and advance the pointer.
    */
    protected char nextChar()
        { 
        return toParse.charAt( pointer++ ); 
        }

    /**
         Parse a single atom and return the tree for it, advancing the pointer. This
         does not deal with quantifiers, for which see parseQuantifier. Unmatched
         right parentheses, unexpected (hence unbound) quantifiers, and those things
         that aren't implemented, throw exceptions. An empty atom is permitted
         (at the end of a string or before a |).
    */
    public RegexpTree parseAtom()
        {
        if (pointer < limit)
            {
            char ch = nextChar();
            switch (ch)
                {
                case '.':   return generator.getAnySingle();
                case '^':   return generator.getStartOfLine();
                case '$':   return generator.getEndOfLine();
                case '|':   pointer -= 1; return generator.getNothing();
                case '[':   return parseClass();
                case ')':   pointer -= 1; return generator.getNothing(); 
                case '(':   return parseParens();
                case '\\':  return parseBackslash(); 
                case '*':
                case '+':
                case '?':
                case '{': throw new PerlPatternParser.SyntaxException( "unbound quantifier " + ch );
                case ']':
                case '}':
                default: return generator.getText( ch );       
                }
            }
        return generator.getNothing();
        }
    
    /**
         Parse a class expression and answer an appropriate tree.
    */
    protected RegexpTree parseClass()
        {
        StringBuffer b = new StringBuffer();
        boolean negated = parseClassNegation();
        while (true)
            {
            int ch = nextClassChar();
            if (ch == ']') break;
            if (ch == '-' && b.length() > 0)
                {
                char begin = (char) (b.charAt( b.length() - 1 ) + 1);
                char end = (char) Math.abs( nextClassChar() );
                for (char i = begin; i <= end; i += 1) b.append( i );
                }
            else
                b.append( (char) Math.abs( ch ) );
            }
        pointer += 1;
        return generator.getClass( b.toString(), negated );
        }

    /**
         Answer the next character, if it's suitable for part of a class expression,
         negated if it's been escaped. Iffy.
    */
    private int nextClassChar()
        {
        char ch = nextChar();
        if (ch == '\\')
            {
            RegexpTree t = parseAtom();
            if (t instanceof Text) return -((Text) t).getString().charAt( 0 );
            throw new SyntaxException( "not allowed in class" );
            }
        else
            return ch;
        }

    protected boolean parseClassNegation()
        {
        if (toParse.charAt( pointer ) == '^')
            { pointer += 1; return true; }
        else
            return false;
        }

    /**
    	Parse a parenthesised expression. Throw a SyntaxException if the closing
        bracket is missing. Answer the wrapped sub-expression. Does not cater
        for the (? ...) stuff.
    */
    protected RegexpTree parseParens()
        {
        RegexpTree operand = parseAlts();
        if (pointer < limit && toParse.charAt( pointer ) == ')') pointer += 1;
        else throw new SyntaxException( "missing closing bracket" );
        matchPointsSeen += 1;
        return generator.getParen( operand, matchPointsSeen );
        }

    /**
         Parse a backslash-escape and answer the appropriate regexp tree.
         Unhandled escapes throw an exception.
    */
    private RegexpTree parseBackslash()
        {
        char ch = nextChar();
        if ("bBAZnrtfdDwWSsxc0123456789".indexOf( ch ) < 0)
            return generator.getText( ch );
        else if (ch == 'n')
            return generator.getText( '\n' );
        else if (ch == 'r')
            return generator.getText( '\r' );
        else if (ch == 'f')
            return generator.getText( '\f' );
        else if (ch == 't')
            return generator.getText( '\t' );
        else if (ch == 's')
            return generator.getClass( " \r\n\t\f", false );
        else if (ch == 'S') 
            return generator.getClass( " \r\n\t\f", true );
        else if (ch == 'd')
            return generator.getClass( digits, false );
        else if (ch == 'D')
            return generator.getClass( digits, true );
        else if (ch == 'w')
            return generator.getClass( wordChars, false );
        else if (ch == 'W')
            return generator.getClass( wordChars, true );
        else if ('0' <= ch && ch <= '9')
            return backReferenceOrOctalChar( ch );
        else if (ch == 'x')
            return hexEscape();
        else if (ch == 'c')
            return control( nextChar() );
        else    
            throw new PerlPatternParser.SyntaxException( "can't do \\" + ch + " yet" );
        }
    
    /**
         Answer a RegexpTree representing the single character which is CTRL-ch.
    */
    protected RegexpTree control( char ch )
        { return Text.create( (char) (ch - 'A' + 1) ); }

    /**
        Answer a RegexpTree representing the single character whose value is
        given by the next two hexadecimal digits.
    */
    protected RegexpTree hexEscape()
        {
        char hi = nextChar(), lo = nextChar();
        return Text.create( (char) (deHex( hi ) * 16 + deHex( lo )) );
        }

    /**
         Answer the integer value corresponding to the hex digit <code>ch</code>.
    */
    private int deHex( char ch )
        {
        if (Character.isDigit( ch )) return ch - '0';
        if ('a' <= ch && ch <= 'f') return 10 + ch - 'a';
        if ('A' <= ch && ch <= 'F') return 10 + ch - 'A';
        throw new SyntaxException( "'" + ch + "' is not a hex digit" );
        }

    /**
        Answer the backreference or octal character described by \nnnn sequences.
    */
    protected RegexpTree backReferenceOrOctalChar( char ch )
        {
        char [] chars = new char[20];
        int index = 0;
        chars[index++] = ch;
        while (pointer < limit)
            {
            ch = nextChar();
            if (!Character.isDigit( ch )) break;
            chars[index++] = ch;
            }
        int n = numeric( chars, 10, index );
        return 0 < n && n <= matchPointsSeen
            ? generator.getBackReference( n )
            : generator.getText( numeric( chars, 8, index ) );
        }

    /**
         Answer the numeric value represented by chars[0..limit-1] in the given base.
    */
    protected char numeric( char [] chars, int base, int limit )
        {
        int result = 0;
        for (int i = 0; i < limit; i += 1) result = result * base + (chars[i] - '0');
        return (char) result;
        }

    /**
         Parse any quantifier and answer the quantified version of the argument
         tree <code>d</code>. TODO: handle non-greedy quantifiers. (These will
         currently generate syntax errors when their flagging ? is encountered by
         parseAtom.)
    */
    public RegexpTree parseQuantifier( RegexpTree d )
        {
        if (pointer < limit)
            {
            char ch = toParse.charAt( pointer );
            switch (ch)
                {
                case '*':
                    pointer += 1;
                    return generator.getZeroOrMore( d );
                    
                case '+':
                    pointer += 1;
                    return generator.getOneOrMore( d );
                    
                case '?':
                    pointer += 1;
                    return generator.getOptional( d );
                    
                case '{':
                    throw new SyntaxException( "numeric quantifiers not done yet" );
                }
            }
        return d;
        }
    
    /**
         Parse an element (an atom and any following quantifier) and answer the
         possibly-quantified tree.
    */
    public RegexpTree parseElement()
        { return parseQuantifier( parseAtom() ); }

    /**
    	Parse a sequence of elements [possibly-quantified atoms] and answer the
        sequence (singular sequences may be reduced to its single element).
    */
    public RegexpTree parseSeq()
        {
        List<RegexpTree> operands = new ArrayList<RegexpTree>();
        while (true)
            {
            RegexpTree next = parseElement();
            if (next.equals( generator.getNothing() ) ) break;
            operands.add( next );
            }
        return generator.getSequence( operands );
        }

    /**
         Parse an alternation of sequences and answer an alternative tree (or the
         single component if there is just one alternative).
    */
    public RegexpTree parseAlts()
        {
        List<RegexpTree> operands = new ArrayList<RegexpTree>();
        while (true)
            {
            operands.add( parseSeq() );
            if (pointer < limit && toParse.charAt( pointer ) == '|') pointer += 1;
            else break;
            }
        return generator.getAlternatives( operands );
        }
    }
