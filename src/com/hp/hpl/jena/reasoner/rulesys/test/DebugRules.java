/******************************************************************
 * File:        DebugRules.java
 * Created by:  Dave Reynolds
 * Created on:  15-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: DebugRules.java,v 1.2 2003-05-05 15:16:01 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.PrintUtil;

import java.util.*;
import java.io.*;

/** * Using during debuging of the rule systems.
 * Runs a named set of rules (can contain axioms and rules) and
 * lists all the resulting entailments.
 *  * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a> * @version $Revision: 1.2 $ on $Date: 2003-05-05 15:16:01 $ */
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
