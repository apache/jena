/*
    (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
    [See end of file]
    $Id: SimpleGenerator.java,v 1.3 2004-08-17 14:56:52 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph.query.regexptrees;

import java.util.List;

/**
     The base implementation of <code>RegexpTreeGenerator</code>
 	@author hedgehog
*/
public class SimpleGenerator implements RegexpTreeGenerator
    {
    public RegexpTree getAnySingle() { return new AnySingle(); }
    public RegexpTree getStartOfLine() { return new StartOfLine(); }
    public RegexpTree getEndOfLine() { return new EndOfLine(); }
    public RegexpTree getText( char ch ) { return new Text( "" + ch ); }
    public RegexpTree getZeroOrMore( RegexpTree d ) { return new ZeroOrMore( d ); }
    public RegexpTree getOneOrMore( RegexpTree d ) { return new OneOrMore( d ); }
    public RegexpTree getOptional( RegexpTree d ) { return new Optional( d ); }
    public RegexpTree getSequence( List operands ) { return Sequence.create( operands ); }
    public RegexpTree getAlternatives( List operands ) { return Alternatives.create( operands ); }
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