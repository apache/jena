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
    public abstract RegexpTree getSequence( List<? extends RegexpTree> operands );

    /**
         Answer a RegexpTree for matching one of a set of alternative operand
         expressions from the list. Every element must be a RegexpTree. If the
         list has exactly one element, it is recommended that that element be 
         returned.
    */
    public abstract RegexpTree getAlternatives( List <? extends RegexpTree> operands );

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
