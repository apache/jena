/*
  (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: RegexpTreeGenerator.java,v 1.7 2004-09-02 13:46:14 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.regexptrees;

import java.util.List;

/**
     A RegexpTreeGenerator supplies appropriate RegexpTrees; different users
     of the RegexpTree parsers can supply specialised generators that produce
     instances appropriate to their needs, or fail by throwing an exception.
     @author hedgehog
*/
public interface RegexpTreeGenerator
    {
    /**
         Answer some instance of AnySingle (a pattern that matches any one
         character). May return the same instance on each call.
    */
    public abstract RegexpTree getAnySingle();

    /**
         Answer some instance of StartOfLine (a pattern that matches the start of
         a line). May return the same instance on each call.
    */
    public abstract RegexpTree getStartOfLine();

    /**
         Answer some instance of EndOfLine (a pattern that matches the end of
         a line). May return the same instance on each call.
    */
    public abstract RegexpTree getEndOfLine();

    /**
         Answer some instance of Text which matches the literal character
         <code>ch</code>.
    */
    public abstract RegexpTree getText( char ch );

    /**
         Answer an instance of ZeroOrMore with repeated content <code>d</code>.
    */
    public abstract RegexpTree getZeroOrMore( RegexpTree d );

    /**
         Answer an instance of OneOrMore with repeated content <code>d</code>.
    */
    public abstract RegexpTree getOneOrMore( RegexpTree d );

    /**
         Answer an instance of Optional with content <code>d</code>.
    */
    public abstract RegexpTree getOptional( RegexpTree d );

    /**
         Answer a RegexpTree which for matching the sequence of operands 
         in the list. Every element must be a RegexpTree. If the list contains
         exactly one element, it is strongly recommended that that element be
         returned. If the list is empty, it is recommended that Nothing be returned.
    */
    public abstract RegexpTree getSequence( List operands );

    /**
         Answer a RegexpTree for matching one of a set of alternative operand
         expressions from the list. Every element must be a RegexpTree. If the
         list has exactly one element, it is recommended that that element be 
         returned.
    */
    public abstract RegexpTree getAlternatives( List operands );

    /**
         Answer an empty RegexpTree (corresponding to nothing in a parsed
         expression, and matching the empty string).
    */
    public abstract RegexpTree getNothing();

    /**
         Answer a RegexpTree that encodes a match which accepts (reject=false)
         or rejects (reject=true) any (all) of the characters in <code>chars</code>.
    */
    public abstract RegexpTree getClass( String chars, boolean reject );
    
    /**
         Answer a RegexpTree that wraps parentheses around an operand. The
         index is non-zero if this is a back-reference referrable object.
    */
    public abstract RegexpTree getParen( RegexpTree operand, int index );

    /**
         Answer a RegexpTree that refers back to noted parenthesisation n.
    */
    public abstract RegexpTree getBackReference( int n );
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