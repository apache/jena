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
     The base implementation of <code>RegexpTreeGenerator</code>
 	@author hedgehog
*/
public class SimpleGenerator implements RegexpTreeGenerator
    {
    @Override
    public RegexpTree getAnySingle() { return RegexpTree.ANY; }
    @Override
    public RegexpTree getStartOfLine() { return RegexpTree.SOL; }
    @Override
    public RegexpTree getEndOfLine() { return RegexpTree.EOL; }
    @Override
    public RegexpTree getNothing() { return RegexpTree.NON; }
    
    @Override
    public RegexpTree getText( char ch ) { return Text.create( ch ); }
    
    @Override
    public RegexpTree getZeroOrMore( RegexpTree d ) { return new ZeroOrMore( d ); }
    @Override
    public RegexpTree getOneOrMore( RegexpTree d ) { return new OneOrMore( d ); }
    @Override
    public RegexpTree getOptional( RegexpTree d ) { return new Optional( d ); }
    
    @Override
    public RegexpTree getSequence( List<? extends RegexpTree> operands ) { return Sequence.create( operands ); }
    @Override
    public RegexpTree getAlternatives( List<? extends RegexpTree> operands ) { return Alternatives.create( operands ); }
    
    @Override
    public RegexpTree getBackReference( int n ) { return new BackReference( n ); }
    
    @Override
    public RegexpTree getClass( String chars, boolean reject ) 
        { return reject ? (RegexpTree) new NoneOf( chars ) : new AnyOf( chars ); }
    
    @Override
    public RegexpTree getParen( RegexpTree operand, int index ) 
        { return new Paren( operand, index ); }
    }
