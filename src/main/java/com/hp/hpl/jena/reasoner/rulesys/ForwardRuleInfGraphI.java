/******************************************************************
 * File:        ForwardRuleInfGraphI.java
 * Created by:  Dave Reynolds
 * Created on:  28-May-2003
 * 
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ForwardRuleInfGraphI.java,v 1.2 2009-08-02 15:06:55 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * This interface collects together the operations on the InfGraph which
 * are needed to support the forward rule engine. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2009-08-02 15:06:55 $
 */
public interface ForwardRuleInfGraphI extends InfGraph, SilentAddI {
    
    /**
     * Return true if tracing should be acted on - i.e. if traceOn is true
     * and we are past the bootstrap phase.
     */
    public boolean shouldTrace();
        
    /**
     * Adds a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void addBRule(Rule brule);
        
    /**
     * Deletes a new Backward rule as a rules of a forward rule process. Only some
     * infgraphs support this.
     */
    public void deleteBRule(Rule brule);
    
    /**
     * Return the Graph containing all the static deductions available so far.
     * Triggers a prepare if the graph has not been prepared already.
     */
    @Override
    public Graph getDeductionsGraph();
    
    /**
     * Return the Graph containing all the static deductions available so far.
     * Does not trigger a prepare action.
     */
    public Graph getCurrentDeductionsGraph();
    
    /**
     * Add a new deduction to the deductions graph.
     */
    public void addDeduction(Triple t);
    
    /**
     * Search the combination of data and deductions graphs for the given triple pattern.
     * This may different from the normal find operation in the base of hybrid reasoners
     * where we are side-stepping the backward deduction step.
     */
    public ExtendedIterator<Triple> findDataMatches(Node subject, Node predicate, Node object);

    /**
     * Return true if derivation logging is enabled.
     */
    public boolean shouldLogDerivations();
    
    /**
     * Logger a dervivation record against the given triple.
     */
    public void logDerivation(Triple t, Derivation derivation);
    
    /**
     * Set to true to cause functor-valued literals to be dropped from rule output.
     * Default is true.
     */
    public void setFunctorFiltering(boolean param) ;

}


/*
    (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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