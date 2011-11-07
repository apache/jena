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

/**
    Text - represents literal text for the match, to be taken as it stands. May
    include material that was meta-quoted in the original patterm. There are
    two sub-classes, one for strings and one for single characters; the factory
    methods ensure that there are no single-character TextString instances.
    
    @author kers
*/

public abstract class Text extends RegexpTree
    {
    public static Text create( String s )
        { 
        return s.length() == 1
            ? (Text) new TextChar( s.charAt( 0 ) )
            : new TextString( s ); 
        }
    
    public static Text create( char ch )
        { return new TextChar( ch ); }
    
    static class TextString extends Text
        {
        protected String literal;
        
        TextString( String s ) 
            { literal = s; }
        
        @Override
        public String getString()
            { return literal; }
        
        @Override
        public String toString()
            { return "<text.s '" + literal + "'>"; }
        
        @Override
        public boolean equals( Object x )
            { return x instanceof TextString && literal.equals( ((TextString) x).literal ); }
        
        @Override
        public int hashCode()
            { return literal.hashCode(); }
        }
    
    static class TextChar extends Text
        {
        protected char ch;
        
        TextChar( char ch ) 
            { this.ch = ch; }
        
        @Override
        public String getString()
            { return "" + ch; }
        
        @Override
        public String toString()
            { return "<text.ch '" + ch + "'>"; }
        
        @Override
        public boolean equals( Object x )
            { return x instanceof TextChar && ch == ((TextChar) x).ch; }
        
        @Override
        public int hashCode()
            { return ch; }
        }
    
    @Override
    public abstract boolean equals( Object x );
    
    @Override
    public abstract int hashCode();
    
    public abstract String getString();
    
    }
