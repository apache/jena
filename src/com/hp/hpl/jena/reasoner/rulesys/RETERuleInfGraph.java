/******************************************************************
 * File:        RETERuleInfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  12-Jun-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: RETERuleInfGraph.java,v 1.7 2004-12-07 09:56:29 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;

import java.util.*;

/**
 * RETE implementation of the forward rule infernce graph.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2004-12-07 09:56:29 $
 */
public class RETERuleInfGraph extends BasicForwardRuleInfGraph {

    /**
     * Constructor. Creates a new inference graph to which a (compiled) rule set
     * and a data graph can be attached. This separation of binding is useful to allow
     * any configuration parameters (such as logging) to be set before the data is added.
     * Note that until the data is added using {@link #rebind rebind} then any operations
     * like add, remove, find will result in errors.
     * 
     * @param reasoner the parent reasoner 
     * @param schema the (optional) schema data which is being processed
     */
    public RETERuleInfGraph(Reasoner reasoner, Graph schema) {
        super(reasoner, schema);
    }    

    /**
     * Constructor. Creates a new inference graph based on the given rule set. 
     * No data graph is attached at this stage. This is to allow
     * any configuration parameters (such as logging) to be set before the data is added.
     * Note that until the data is added using {@link #rebind rebind} then any operations
     * like add, remove, find will result in errors.
     * 
     * @param reasoner the parent reasoner 
     * @param rules the list of rules to use this time
     * @param schema the (optional) schema or preload data which is being processed
     */
    public RETERuleInfGraph(Reasoner reasoner, List rules, Graph schema) {
        super(reasoner, rules, schema);
    }    

     /**
      * Constructor. Creates a new inference graph based on the given rule set
      * then processes the initial data graph. No precomputed deductions are loaded.
      * 
      * @param reasoner the parent reasoner 
      * @param rules the list of rules to use this time
      * @param schema the (optional) schema or preload data which is being processed
      * @param data the data graph to be processed
      */
     public RETERuleInfGraph(Reasoner reasoner, List rules, Graph schema, Graph data) {
         super(reasoner, rules, schema, data);
     }

    /**
     * Instantiate the forward rule engine to use.
     * Subclasses can override this to switch to, say, a RETE imlementation.
     * @param rules the rule set or null if there are not rules bound in yet.
     */
    protected void instantiateRuleEngine(List rules) {
        if (rules != null) {
            engine = new RETEEngine(this, rules);
        } else {
            engine = new RETEEngine(this);
        }
    }

    /**
     * Add one triple to the data graph, run any rules triggered by
     * the new data item, recursively adding any generated triples.
     */
    public synchronized void performAdd(Triple t) {
        if (!isPrepared) prepare();
        fdata.getGraph().add(t);
        engine.add(t);
    }
    
    /** 
     * Removes the triple t (if possible) from the set belonging to this graph. 
     */   
    public void performDelete(Triple t) {
        if (!isPrepared) prepare();
        if (fdata != null) {
            Graph data = fdata.getGraph();
            if (data != null) {
                data.delete(t);
            }
        }
        engine.delete(t);
        fdeductions.getGraph().delete(t);
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