/******************************************************************
 * File:        OWLBRuleReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  12-May-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: OWLFBRuleReasoner.java,v 1.5 2003-06-08 17:49:17 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import java.util.*;
import java.io.*;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.*;

/**
 * An backward chaining implementation of the experimental OWL closure rules.
 * <p>
 * TODO: Replace intersection-translation step by rule based alternative (or failing that
 * figure out what should be done at the bindSchema stage).
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2003-06-08 17:49:17 $
 */
public class OWLFBRuleReasoner extends FBRuleReasoner {
    
    /** The location of the OWL rule definitions on the class path */
    protected static final String RULE_FILE = "etc/owl-fb.rules";
    
    /** The parsed rules */
    protected static List ruleSet;
    
    /** The precomputed axiom closure and compiled rule set */
    protected static FBRuleInfGraph preload; 
    
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(OWLRuleReasoner.class);
    
    /**
     * Constructor
     */
    public OWLFBRuleReasoner() {
        super(loadRules(), OWLFBRuleReasonerFactory.theInstance());
        
    }
    
    /**
     * Internal constructor, used to generated a partial binding of a schema
     * to a rule reasoner instance.
     */
    private OWLFBRuleReasoner(OWLFBRuleReasoner parent, InfGraph schemaGraph) {
        super(parent.rules, schemaGraph, parent.factory);
    }
    
    /**
     * Return the rule set, loading it in if necessary
     */
    public static List loadRules() {
        if (ruleSet == null) {
            try {
                ruleSet = Rule.parseRules(Util.loadResourceFile(RULE_FILE));
            } catch (IOException e) {
                throw new ReasonerException("Can't load rules file: " + RULE_FILE, e);
            }
        }
        return ruleSet;
    }
    
    
    /**
     * Precompute the implications of a schema graph. The statements in the graph
     * will be combined with the data when the final InfGraph is created.
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        if (schemaGraph != null) {
            throw new ReasonerException("Can only bind one schema at a time to an OWLRuleReasoner");
        }
        FBRuleInfGraph graph = new FBRuleInfGraph(this, augmentRules(rules, tbox), getPreload(), tbox);
        graph.prepare();
        return new OWLFBRuleReasoner(this, graph);
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
        FBRuleInfGraph graph =  null;
        InfGraph schemaArg = schemaGraph == null ? getPreload() : (FBRuleInfGraph)schemaGraph; 
        List baseRules = ((FBRuleInfGraph)schemaArg).getRules();
        graph = new FBRuleInfGraph(this, augmentRules(baseRules, data), schemaArg);
        graph.setDerivationLogging(recordDerivations);
        graph.setTraceOn(traceOn);
        graph.rebind(data);
                
        return graph;
    }
    
    /**
     * Get the single static precomputed rule closure.
     */
    public InfGraph getPreload() {
//        return null;        // Disable preload for now, causes problems
        synchronized (OWLFBRuleReasoner.class) {
            if (preload == null) {
                preload = new FBRuleInfGraph(this, rules, null);
                preload.prepare();
            }
            return preload;
        }
    }
    
    /**
     * Check a source graph for intersection statements and return a rule set
     * augmented by new intersection rules (or the original rule set if no
     * augmentations are needed).
     */
    protected static List augmentRules(List baseRules, Graph data) {
        Iterator i = data.find(null, OWL.intersectionOf.asNode(), null);
        if (i.hasNext()) {
            List newrules = (List) ((ArrayList) baseRules).clone();
            while(i.hasNext()) {
                translateIntersectionOf((Triple)i.next(), newrules, data);
            }
            return newrules;
        } else {
            return baseRules;
        }
    }
    
    /**
     * Translation code to compile intersectionOf declarations.
     * In the future this will be replaced by a rule-baesd translator which will cope
     * with dynamic expressions.
     * @param decl a triple of the form (C owl:intersectionOf [..])
     * @param rules a list of rules to be extended
     * @param data the source data to use as a context for this processing
     */
    protected static void translateIntersectionOf(Triple decl, List rules, Graph data) {
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
            ir.setBackward(false);
           rules.add(ir);
//           System.out.println("Adding rule: " + ir.toString());
           // Recognition rule elements
           recognitionBody.add(new TriplePattern(var, RDF.type.asNode(), description));
        }
        List recognitionHead = new ArrayList(1);
        recognitionHead.add(new TriplePattern(var, RDF.type.asNode(), className));
        Rule rr = new Rule("intersectionRecognition", recognitionHead, recognitionBody);
        rr.setBackward(true);
//        System.out.println("Adding rule: " + rr.toString());
        rules.add(rr);
    }
    
    /**
     * Translation code to translate a list of intersection elements into a 
     * Java list of corresponding class names or restriction functors.
     * @param node the list node currently being processed
     * @param data the source data to use as a context for this processing
     * @param elements the list of elements found so far
     */
    protected static void translateIntersectionList(Node node, Graph data, List elements) {
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