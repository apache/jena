/******************************************************************
 * File:        RDFSRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  06-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RDFSRuleReasoner.java,v 1.1 2003-04-17 15:24:24 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.io.*;
import org.apache.log4j.Logger;
import java.util.*;

/**
 * An pure forward chaining implementation of the RDFS closure rules
 * based upon the basic forward rule interpreter. The normal mixed
 * forward/backward implementation is generally preferred but this has 
 * two possible uses. First, it is a test and demonstration of the forward
 * chainer. Second, if you want all the RDFS entailments for an entire 
 * dataset the forward chainer will be more efficient.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-04-17 15:24:24 $
 */
public class RDFSRuleReasoner extends BasicForwardRuleReasoner {
    
    /** The location of the OWL rule definitions on the class path */
    public static final String RULE_FILE = "etc/rdfs.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(RDFSRuleReasoner.class);
    
    /**
     * Constructor
     */
    public RDFSRuleReasoner() {
        super(loadRules());
    }
    
    /**
     * Return the RDFS rule set, loading it in if necessary
     */
    public static List loadRules() {
        if (ruleSet == null) {
            try {
                ruleSet = Rule.parseRules(Util.loadResourceFile(RULE_FILE));
            } catch (IOException e) {
                logger.error("Can't load rules file: " + RULE_FILE);
            }
        }
        return ruleSet;
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
