/******************************************************************
 * File:        DebugRules.java
 * Created by:  Dave Reynolds
 * Created on:  15-Apr-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: DebugRules.java,v 1.5 2005-02-21 12:18:08 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.PrintUtil;

import java.util.*;
import java.io.*;

/** * Using during debuging of the rule systems.
 * Runs a named set of rules (can contain axioms and rules) and
 * lists all the resulting entailments.
 *  * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a> * @version $Revision: 1.5 $ on $Date: 2005-02-21 12:18:08 $ */
public class DebugRules {

    /** The name of the rule set to load */
    public static final String ruleFile = "etc/temp.rules";
    
    /** The parsed set of rules */
    public List ruleset;
    
    /** Constructor - loads the rules */
    public DebugRules(String rulefileName) throws IOException {
        ruleset = Rule.parseRules(Util.loadResourceFile(rulefileName));
    }
    
    /** Run a single test */
    public void run() {
        
        BasicForwardRuleReasoner reasoner = new BasicForwardRuleReasoner(ruleset);
        InfGraph result = reasoner.bind(new GraphMem());
        System.out.println("Final graph state");
        for (Iterator i = result.find(null, null, null); i.hasNext(); ) {
            System.out.println(PrintUtil.print((Triple)i.next()));
        }
        
    }
    
    public static void main(String[] args) {
        try {
            DebugRules tester = new DebugRules(ruleFile);
            tester.run();
        } catch (Exception e) {
            System.out.println("Problem: " + e);
        }
    }
    
}

/*
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
