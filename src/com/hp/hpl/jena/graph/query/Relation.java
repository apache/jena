/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Relation.java,v 1.1.1.1 2002-12-19 19:14:01 bwm Exp $
*/

package com.hp.hpl.jena.graph.query;

/**
	@author kers
<br>
    A Relation is a predicate that relates two Valuators; the abstract
    class captures the commonality of "has two operands" and
    methods for evaluating them.
*/

import com.hp.hpl.jena.graph.*;

public abstract class Relation extends PredicateBase implements Predicate
    {
    protected Valuator L;
    protected Valuator R;

    /** make a Relation with the given left & right operands */
    public Relation( Valuator L, Valuator R ) { this.L = L; this.R = R; }

    /** evaluate the left operand against the domain _d_ */
    public Node valueL( Domain d ) { return L.eval( d ); }

    /** evaluate the right operand against the domain _d_ */
    public Node valueR( Domain d ) { return R.eval( d ); }

    /** see if the operands satisfy the relation */
    public abstract boolean evaluateBool( Domain d );

    /** shared code for the equality relation */
    public boolean evaluateEquals( Domain d )
        { return valueL( d ).equals( valueR( d ) ); }
    }

/*
    (c) Copyright Hewlett-Packard Company 2002
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
