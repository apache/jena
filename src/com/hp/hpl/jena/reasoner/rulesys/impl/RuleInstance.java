/******************************************************************
 * File:        RuleInstance.java
 * Created by:  Dave Reynolds
 * Created on:  03-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RuleInstance.java,v 1.5 2003-05-15 21:34:32 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

/**
 * Part of the backward chaining rule interpreter. A RuleInstance
 * links an instance of a rule to the GoalResults object for which it is
 * generating results. It is a simple data structure which is shared amongst
 * a set of RuleSets to reduce the store turn over needed for RuleState creation.
 * <p>
 * Encapuslation warning: this object is used in the tight inner loop of the engine so we access its
 * field pointers directly rather than through accessor methods.
 * </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2003-05-15 21:34:32 $
 */
public class RuleInstance {

    /** The rule being processed */
    protected Rule rule;
    
    /** The parent goal table entry which contains this continuation point */
    protected GoalResults generator;
    
    /** The enclosing rule engine */
    protected BRuleEngine engine;
    
    /** The head clause whose bindings are being sought */
    protected TriplePattern head;
    
    /** Set to true if the first two body clauses were reordered for performance */
    protected boolean clausesReordered = false;
    
    /** If the clauses are reordered this contains the index of the second clause */
    protected int secondClause;
    
    /**
     * Constructor. Create a new continuation point for a rule in
     * the context of a specific goal represented by the table entry.
     */
    RuleInstance(GoalResults results, Rule rule, TriplePattern head) {
        this.generator = results;
        this.rule = rule;
        this.engine = results.getEngine();
        this.head = head;
    }
     
}



/*
    (c) Copyright Hewlett-Packard Company 2003
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