/******************************************************************
 * File:        RDFSRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  06-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RDFSForwardRuleReasoner.java,v 1.4 2004-11-29 16:39:01 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.util.*;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.reasoner.BaseInfGraph;
import com.hp.hpl.jena.reasoner.ReasonerFactory;

/** 
 * A pure forward chaining implementation of the RDFS closure rules
 * based upon the basic forward rule interpreter. The normal mixed
 * forward/backward implementation is generally preferred but this has 
 * two possible uses. First, it is a test and demonstration of the forward
 * chainer. Second, if you want all the RDFS entailments for an entire 
 * dataset the forward chainer will be more efficient.
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a> 
 * @version $Revision: 1.4 $ on $Date: 2004-11-29 16:39:01 $ */

public class RDFSForwardRuleReasoner extends GenericRuleReasoner {    
    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/rdfs.rules";
//    public static final String RULE_FILE = "etc/rdfs-noresource.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /**
     * Constructor
     */
    public RDFSForwardRuleReasoner(ReasonerFactory parent) {
        super(loadRules(), parent);
//        setMode(FORWARD_RETE);
        setMode(FORWARD);
    }
    
    /**
     * Return the RDFS rule set, loading it in if necessary
     */
    public static List loadRules() {
        if (ruleSet == null) ruleSet = loadRules( RULE_FILE ); 
        return ruleSet;
    }

    /**
     * Return the Jena Graph Capabilties that the inference graphs generated
     * by this reasoner are expected to conform to.
     */
    public Capabilities getGraphCapabilities() {
        if (capabilities == null) {
            capabilities = new BaseInfGraph.InfFindSafeCapabilities();
        }
        return capabilities;
    }
        
}

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
