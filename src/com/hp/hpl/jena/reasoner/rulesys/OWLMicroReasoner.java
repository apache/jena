/******************************************************************
 * File:        OWLMicroReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  21-Mar-2004
 * 
 * (c) Copyright 2004, Hewlett-Packard Development Company, LP, all rights reserved.
 * [See end of file]
 * $Id: OWLMicroReasoner.java,v 1.1 2004-03-22 17:10:12 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.reasoner.*;

import java.io.IOException;
import java.util.*;

/**
 * Reasoner configuration for the OWL micro reasoner.
 * This only supports:
 * <ul>
 * <li>RDFS entailments</li>
 * <li>basic OWL axioms like ObjectProperty subClassOf Property</li>
 * <li>intersectionOf, equivalentClass and forward implication of unionOf sufficient for traversal
 * of explicit class hierachies<.li>
 * <li>Property axioms (inversOf, SymmetricProperty, TransitiveProperty, equivalentProperty)</li>
 * </ul>
 * There is some experimental support for the cheaper class restriction handlingly which
 * should not be relied on at this point.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2004-03-22 17:10:12 $
 */
public class OWLMicroReasoner extends GenericRuleReasoner implements Reasoner {

    /** The location of the OWL rule definitions on the class path */
    protected static final String MICRO_RULE_FILE = "etc/owl-fb-micro.rules";
    
    /** The parsed rules */
    protected static List microRuleSet;
    
    /**
     * Return the rule set, loading it in if necessary
     */
    public static List loadRules() {
        if (microRuleSet == null) {
            try {
                microRuleSet = Rule.parseRules(Util.loadResourceFile(MICRO_RULE_FILE));
            } catch (IOException e) {
                throw new ReasonerException("Can't load rules file: " + MICRO_RULE_FILE, e);
            }
        }
        return microRuleSet;
    }
    
    
    /**
     * Constructor
     */
    public OWLMicroReasoner(ReasonerFactory factory) {
        super(loadRules(), factory);
        setOWLTranslation(true);
        setMode(HYBRID);
        setTransitiveClosureCaching(true);
    }
    

}


/*
    (c) Copyright Hewlett-Packard Development Company, LP 2004
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