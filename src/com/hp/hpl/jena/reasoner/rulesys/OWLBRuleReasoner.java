/******************************************************************
 * File:        OWLBRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: OWLBRuleReasoner.java,v 1.2 2003-05-19 17:15:56 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.impl.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.*;

/**
 * An backward chaining implementation of the experimental OWL closure rules.
 * <p>
 * TODO: Replace intersection-translation step by rule based alternative (or failing that
 * figure out what should be done at the bindSchema stage).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-05-19 17:15:56 $
 */
public class OWLBRuleReasoner extends BasicBackwardRuleReasoner {
    
    /** The location of the OWL rule definitions on the class path */
    protected static final String RULE_FILE = "etc/owl-b.rules";
//    protected static final String RULE_FILE = "etc/owl-b-debug.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(OWLRuleReasoner.class);
    
    /** Performance statistics - the total number of rule firings used during data bind operations so far. */
    protected static long nRulesFired = 0;
    
    /** Performance statistics - the total elapsed time spent during data bind operations so far, in ms. */
    protected static long timeCost = 0;
    
    /**
     * Constructor
     */
    public OWLBRuleReasoner() {
        super(loadRules());
        
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    private OWLBRuleReasoner(BasicBackwardRuleReasoner parent, Graph schemaGraph) {
        super(parent, schemaGraph);
    }
    
    /**
     * Return the rule set, loading it in if necessary
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
    
    /**
     * Precompute the implications of a schema graph.
     * The practicality benefit of this has not yet been fully checked out.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        return new OWLBRuleReasoner(this, tbox);
    }
    
    /**
     * Precompute the implications of a schema Model. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Model tbox) throws ReasonerException {
        return new OWLBRuleReasoner(this, tbox.getGraph());
    }
        
    /**
     * Attach the reasoner to a set of RDF data to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * 
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    public InfGraph bind(Graph data) throws ReasonerException {
        // First process the  data looking for any intersection declarations
        // that we translate into addtiional rules procedurally (for now at least)
        long startTime = System.currentTimeMillis();
        Iterator i = data.find(null, OWL.intersectionOf.asNode(), null); 
        ArrayList rules = (ArrayList) ruleSet;
        if (i.hasNext()) {
            rules = (ArrayList) rules.clone();
            while(i.hasNext()) {
                translateIntersectionOf((Triple)i.next(), rules, data);
            }
        }
        ruleStore = new RuleStore(rules);

        // Now create the inference graph        
        BasicBackwardRuleInfGraph graph = new BasicBackwardRuleInfGraph(this, data, ruleStore);
        graph.setDerivationLogging(recordDerivations);
        graph.setRuleThreshold(nRulesThreshold);
        graph.setTraceOn(traceOn);
        long endTime = System.currentTimeMillis();
        timeCost += (double)(endTime - startTime);
        //nRulesFired += graph.getNRulesFired();
        
        return graph;
    }
    
    /**
     * Print (to the default logging channel at Info level) a summary of the 
     * total number of rules fired and the time taken by 
     * the reasoner instances created thus far.
     */
    public static void printStats() {
        logger.info("Fired " + nRulesFired + " over " + (timeCost/1000.0) + " s = " 
                     + (nRulesFired*1000/timeCost) + " r/s");
    }
    
    /**
     * Translation code to compile intersectionOf declarations.
     * In the future this will be replaced by a rule-baesd translator which will cope
     * with dynamic expressions.
     * @param decl a triple of the form (C owl:intersectionOf [..])
     * @param rules a list of rules to be extended
     * @param data the source data to use as a context for this processing
     */
    private void translateIntersectionOf(Triple decl, List rules, Graph data) {
        Node className = decl.getSubject();
        List elements = new ArrayList();
        translateIntersectionList(decl.getObject(), data, elements);
        // Generate the corresponding ruleset
        List recognitionBody = new ArrayList();
        Node var = new Node_RuleVariable("?x", 0);
        for (Iterator i = elements.iterator(); i.hasNext(); ) {
            Node description = (Node)i.next();
            // Implication rule
            Rule ir = new Rule("intersectionImplication", new Object[] {
                                new TriplePattern(className, RDFS.subClassOf.asNode(), description)
                                }, new Object[0]);
           rules.add(ir);
           //System.out.println("Adding rule: " + ir.toString());
           // Recognition rule elements
           recognitionBody.add(new TriplePattern(var, RDF.type.asNode(), description));
        }
        List recognitionHead = new ArrayList(1);
        recognitionHead.add(new TriplePattern(var, RDF.type.asNode(), className));
        Rule rr = new Rule("intersectionRecognition", recognitionHead, recognitionBody);
        //System.out.println("Adding rule: " + rr.toString());
        rules.add(rr);
    }
    
    /**
     * Translation code to translate a list of intersection elements into a 
     * Java list of corresponding class names or restriction functors.
     * @param node the list node currently being processed
     * @param data the source data to use as a context for this processing
     * @param elements the list of elements found so far
     */
    private void translateIntersectionList(Node node, Graph data, List elements) {
        if (node.equals(RDF.nil.asNode())) {
            return; // end of list
        } 
        Node description = Util.getPropValue(node, RDF.first.asNode(), data);
        // Translate the first description element
        if (data.contains(description, RDF.type.asNode(), OWL.Restriction.asNode())) {
            // Process a restriction element
            Node onprop = Util.getPropValue(description, OWL.onProperty.asNode(), data);
            Node value;
            if ((value = Util.getPropValue(description, OWL.allValuesFrom.asNode(), data)) != null) {
                elements.add(Functor.makeFunctorNode("all", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.someValuesFrom.asNode(), data)) != null) {
                elements.add(Functor.makeFunctorNode("some", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.minCardinality.asNode(), data)) != null) {
                elements.add(Functor.makeFunctorNode("min", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.maxCardinality.asNode(), data)) != null) {
                elements.add(Functor.makeFunctorNode("max", new Node[] {onprop, value}));
            } else if ((value = Util.getPropValue(description, OWL.cardinality.asNode(), data)) != null) {
                elements.add(Functor.makeFunctorNode("max", new Node[] {onprop, value}));
                elements.add(Functor.makeFunctorNode("min", new Node[] {onprop, value}));
            }
        } else {
            // Assume its a class name
            elements.add(description);
        }
        // Process the list tail
        Node next = Util.getPropValue(node, RDF.rest.asNode(), data);
        translateIntersectionList(next, data, elements);
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