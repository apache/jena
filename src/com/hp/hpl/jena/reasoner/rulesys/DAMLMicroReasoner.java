/******************************************************************
 * File:        DAMLMicroReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  23-Jul-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: DAMLMicroReasoner.java,v 1.5 2004-12-07 09:56:28 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.util.*;

import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.reasoner.BaseInfGraph;
import com.hp.hpl.jena.reasoner.ReasonerFactory;

/**
 * We do not support DAML inference. This is a slightly extended version
 * of the RDFS reasoner to support some interesting subsets of DAML
 * that correspond roughly to what was there in Jena1. We hope.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2004-12-07 09:56:28 $
 */
public class DAMLMicroReasoner  extends GenericRuleReasoner {
   
    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/daml-micro.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /**
     * Constructor
     */
    public DAMLMicroReasoner(ReasonerFactory parent) {
        super(loadRules(), parent);
        setMode(HYBRID);
        setTransitiveClosureCaching(true);
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
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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