/******************************************************************
 * File:        BoundRDFSReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Graph.java,v 1.8 2002/11/29 23:21:13 jjc Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.transitiveReasoner.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.GraphMem;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.UniqueExtendedIterator;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * An RDFS reasoner that has been bound to both a TBox and an ABox.
 * It cannot be bound any futher. Once this Bound reasoner has been
 * created all the class, property and associated declarations have
 * been extracted and cached and all queries are answerable directly
 * from the cached results or from query rewrites.
 * 
 * <p>Initially the subClass/subProperty caches are shared with
 * the parent RDFSReasoner so they can be shared across instance data.
 * However, if any update includes any such declarations then the caches
 * have to be cloned and separated.</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision$ on $Date: 2000/06/22 16:03:33 $
 */
public class BoundRDFSReasoner implements Reasoner {

//=======================================================================
// variables

    /** The precomputed cache of the subClass graph */
    protected TransitiveGraphCache subClassCache;

    /** Flag to indicate that this cache has already been split off from
     * the parent reasoner */
    protected boolean haveSplitSubClassCache = false;

    /** The precomputed cache of the subProperty graph */
    protected TransitiveGraphCache subPropertyCache;

    /** Router which maps queries onto different cache components that can answer then */
    protected PatternRouter router;
    
    /** The graph registered as the data */
    protected Finder fdata = null;
    
    /** Cache of axiomatci triples to be included in the tripleCache */
    protected FGraph axioms = new FGraph(new GraphMem());

    /** Cache of precomputed triples which are added to data - this includes 
     * the tbox, axioms and forward deductions */
    protected Finder tripleCache;

    /** Flag to control whether properties are eagerly scanned */
    /** TODO: Replace this with something set by means of the RDF Config file */
    protected boolean scanProperties = true;
    
//=======================================================================
// static rules and axioms
        
    /** log4j logger */
    protected static Logger logger = Logger.getLogger(BoundRDFSReasoner.class);
    
    /** The RDFS forward rule set */
    protected static BaseFRule[] rules = new BaseFRule[] {
        new AssertFRule("?x rdf:type rdfs:Class -> ?x rdfs:subClassOf rdfs:Resource"),
        new AssertFRule("?x rdf:type rdfs:Class -> ?x rdfs:subClassOf ?x"),
        new AssertFRule("?x rdf:type rdf:Property -> ?x rdfs:subPropertyOf ?x"),
        new BackchainFRule("?p rdfs:subPropertyOf ?q -> ?s ?q ?o <- ?s ?p ?o"),
        new BackchainFRule("?c rdfs:subClassOf ?d -> ?s rdf:type ?d <- ?s rdf:type ?c"),
        new BackchainFRule("?p rdfs:domain ?z -> ?s rdf:type ?z <- ?s ?p _"),
        new BackchainFRule("?p rdfs:range ?z -> ?o rdf:type ?z <- _ ?p ?s")
    }; // end of RDFS rule set definitions
    
    /** The RDFS special case backward rule set */
    protected static BRWRule[] brules = new BRWRule[] {
        new ResourceBRWRule(),
        new PropertyBRWRule()
    };
    
    /** The RDFS built in axioms */
    protected static Triple[] baseAxioms = new Triple[] {
        BaseFRule.parseTriple("rdf:type rdfs:range rdfs:Class"),
        
        BaseFRule.parseTriple("rdfs:Resource rdf:type rdfs:Class"),
        BaseFRule.parseTriple("rdfs:Literal rdf:type rdfs:Class"),
        BaseFRule.parseTriple("rdf:Statement rdf:type rdfs:Class"),
        BaseFRule.parseTriple("rdf:nil rdf:type rdf:List"),
        BaseFRule.parseTriple("rdf:XMLLiteral rdf:type rdfs:Datatype"),

        BaseFRule.parseTriple("rdf:subject rdf:type rdf:Property"),
        BaseFRule.parseTriple("rdf:predicate rdf:type rdf:Property"),
        BaseFRule.parseTriple("rdf:object rdf:type rdf:Property"),
        BaseFRule.parseTriple("rdf:first rdf:type rdf:Property"),
        BaseFRule.parseTriple("rdf:rest rdf:type rdf:Property"),
        BaseFRule.parseTriple("rdf:type rdf:type rdf:Property"),
        BaseFRule.parseTriple("rdfs:range rdf:type rdf:Property"),
        BaseFRule.parseTriple("rdfs:domain rdf:type rdf:Property"),
        
        BaseFRule.parseTriple("rdfs:subPropertyOf rdfs:domain rdf:Property"),
        BaseFRule.parseTriple("rdfs:subPropertyOf rdfs:range rdf:Property"),
        BaseFRule.parseTriple("rdfs:subClassOf rdfs:domain rdfs:Class"),
        BaseFRule.parseTriple("rdfs:subClassOf rdfs:range rdfs:Class"),
        
        // These may be redundant
        BaseFRule.parseTriple("rdfs:subPropertyOf rdfs:subPropertyOf rdfs:subPropertyOf"),
        BaseFRule.parseTriple("rdfs:subClassOf rdfs:subPropertyOf rdfs:subClassOf"),
        BaseFRule.parseTriple("rdf:subject rdfs:subPropertyOf rdf:subject"),
        BaseFRule.parseTriple("rdf:predicate rdfs:subPropertyOf rdf:predicate"),
        BaseFRule.parseTriple("rdf:object rdfs:subPropertyOf rdf:object"),
        BaseFRule.parseTriple("rdf:first rdfs:subPropertyOf rdf:first"),
        BaseFRule.parseTriple("rdf:rest rdfs:subPropertyOf rdf:rest"),
        BaseFRule.parseTriple("rdf:type rdfs:subPropertyOf rdf:type"),
        BaseFRule.parseTriple("rdfs:range rdfs:subPropertyOf rdfs:range"),
        BaseFRule.parseTriple("rdfs:domain rdfs:subPropertyOf rdfs:domain")
    };
    
//=======================================================================
// constructors

    /**
     * Constructor
     * @param tbox the vocabularly data being reasoned over
     * @param data the raw data graph being bound to the reasoner
     * @param sPropertyCache a cache of subPropertyOf relations from the box
     * @param sClassCache a cache of subClassOf relations from the tbox
     * @param scanProperties set to true to force an eager scan of statements looking for 
     *                        container properties
     */
    public BoundRDFSReasoner( Finder tbox, Graph data,
                               TransitiveGraphCache sPropertyCache,
                               TransitiveGraphCache sClassCache, boolean scanProperties) {
        this.subPropertyCache = sPropertyCache.deepCopy();
        this.subClassCache = sClassCache;
        this.scanProperties = scanProperties;
        
        // Combine a place to hold axioms and local deductions and the tbox into single cache
        if (tbox == null) {
            tripleCache = axioms;
        } else {
            tripleCache = FinderUtil.cascade(axioms, tbox);
        }
        fdata = new FGraph(data);
        
        // Check for vocabulary definitions in the data graph
        if (data != null && 
            (RDFSReasoner.checkOccurance(RDFSReasoner.subPropertyOf, data, subPropertyCache) ||
             RDFSReasoner.checkOccurance(RDFSReasoner.subClassOf, data, subPropertyCache) ||
             RDFSReasoner.checkOccurance(RDFSReasoner.domainP, data, subPropertyCache) ||
             RDFSReasoner.checkOccurance(RDFSReasoner.rangeP, data, subPropertyCache) )) {
            
            // The data graph contains some ontology knowledge so split the caches
            // now and rebuild them using merged data
            Finder tempTbox = tbox == null ? fdata : FinderUtil.cascade(tbox, fdata);

            splitSubClassCache();
            TransitiveReasoner.cacheSubProp(tempTbox, subPropertyCache);
            TransitiveReasoner.cacheSubClass(tempTbox, subPropertyCache, subClassCache);
            // Cache the closures of subPropertyOf because these are likely to be
            // small and accessed a lot
            subPropertyCache.setCaching(true);
        }     
        
        // add axioms
        for (int i = 0; i < baseAxioms.length; i++) {
            axioms.getGraph().add(baseAxioms[i]);
        }
        TransitiveReasoner.cacheSubProp(axioms, subPropertyCache);
        
        // identify all properties and collection properties
        // This can be disabled in which case queries of the form (*,type,Property) will be
        // slower and no ContainerMembershipProperty assertions will be detected
        if (scanProperties) {
            ExtendedIterator it = tripleCache.findWithContinuation(new TriplePattern(null, null, null), fdata);
            HashSet properties = new HashSet();
            String memberPrefix = RDF.getURI() + "_";
            while (it.hasNext()) {
                Triple triple = (Triple)it.next();
                Node prop = triple.getPredicate();
                if (prop.equals(RDF.type.getNode()) && prop.equals(RDF.Property.getNode()) ) {
                    prop = triple.getSubject();
                }
                if (properties.add(prop)) {
                    // Unseen property - add the subPropertyOf statement
                    subPropertyCache.addRelation(prop, prop);
                    if (prop.getURI().startsWith(memberPrefix)) {
                        // A container property
                        axioms.getGraph().add(new Triple(prop, RDF.type.getNode(), RDFS.ContainerMembershipProperty.getNode()));
                        subPropertyCache.addRelation(prop, RDFS.member.getNode());
                    }
                }
            }
        }
        
        // set up the router which connect queries to the appropriate processing element
        router = new PatternRouter();
        router.register(subPropertyCache);
        router.register(subClassCache);
        
        // Run the forward rules to preload the tripleCache and build the backward rules
        checkAllForwardRules();
        
        // Add fixed backward rules
        for (int i = 0; i < brules.length; i++) {
            addBRule(brules[i]);
        }
        
        //logger.debug("Bound RDFS reasoner state at end of constructor");
        //logger.debug(this);
        
    }
    
//=======================================================================
// global methods

    /**
     * Returns the scanProperties flag.
     * 
     * <p>If this is set to true then when a reasoner instance is constructed
     * the whole data graph is scanned to detect all properties and the
     * results are cached. This is expensive but without this
     * some cases of rdf:_n properties will not be handled.</p>
     * 
     * <p>This method is just here for development purposes and will
     * be replaced by the configuration machinery</p>
     * @return boolean
     */
    public boolean getScanProperties() {
        return scanProperties;
    }

    /**
     * Sets the scanProperties flag
     * 
     * <p>If this is set to true then when a reasoner instance is constructed
     * the whole data graph is scanned to detect all properties and the
     * results are cached. This is expensive but without this
     * some cases of rdf:_n properties will not be handled.</p>
     * 
     * <p>This method is just here for development purposes and will
     * be replaced by the configuration machinery</p>
     * @param scanProperties The scanProperties to set
     */
    public void setScanProperties(boolean scanProperties) {
        this.scanProperties = scanProperties;
    }

//=======================================================================
// methods

     
    /**
     * This reasoner does not allow dynamic rebinding of rule sets.
     * 
     * @param tbox schema containing the property and class declarations
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        throw new ReasonerException("Attempt to bind multiple schemas - disallowed for now");
    }
     
    /**
     * This reasoner does not allow dynamic rebinding of data sets.
     * 
     * @param data the raw data to be processed
     */
    public InfGraph bind(Graph data) throws ReasonerException {
        throw new ReasonerException("Attempt to bind multiple datasets - disallowed for now");
    }

    /**
     * Basic pattern lookup interface.
     * @param pattern a TriplePattern to be matched against the data
     * @return a ExtendedIterator over all Triples in the data set
     *  that match the pattern
     */
    public ExtendedIterator find(TriplePattern pattern) {
        return findWithContinuation(pattern, null);
    }
    
    /**
     * Extended find interface used in situations where the implementator
     * may or may not be able to answer the complete query. It will
     * attempt to answer the pattern but if its answers are not known
     * to be complete then it will also pass the request on to the nested
     * Finder to append more results.
     * @param pattern a TriplePattern to be matched against the data
     * @param continuation either a Finder or a normal Graph which
     * will be asked for additional match results if the implementor
     * may not have completely satisfied the query.
     */
    public ExtendedIterator findWithContinuation(TriplePattern pattern, Finder continuation) {
        logger.debug("Reasoner called on pattern: " + pattern);
        return new UniqueExtendedIterator(router.find(pattern, tripleCache, continuation,this));
    }
    
    /**
     * Variant on find called by backward rules, additional 
     * argument used to pass set of instantiated rules to prevent
     * run-away rule firing.
     */
    public ExtendedIterator findNested(TriplePattern pattern, Finder continuation, HashSet firedRules) {
        logger.debug("Reasoner called on pattern: " + pattern);
        return router.find(pattern, tripleCache, continuation,this, firedRules);
    }
    
    /**
     * Variant on find called by special backward rules that only 
     * access the raw data and axioms and bypass further rules
     */
    public ExtendedIterator findRawWithContinuation(TriplePattern pattern, Finder continuation) {
        logger.debug("Reasoner raw called on pattern: " + pattern);
        return tripleCache.findWithContinuation(pattern, continuation);
    }
    
    /**
     * Variant on find called by special backward rules that need
     * to list all pre-registered properties. The iterator returns Nodes
     * not triples.
     */
    public ExtendedIterator findProperties() {
        logger.debug("Reasoner called on findProperties");
        return subPropertyCache.listAllProperties();
    }
    
    /**
     * Variant on find called by special backward rules that need
     * to list check for a specific preregistered property.
     */
    public boolean isProperty(Node prop) {
        logger.debug("Reasoner called on isProperty");
        return subPropertyCache.isProperty(prop);
    }

//=======================================================================
// helper methods
    
    /**
     * Run all the builtin forward rules, on all the elements in the tbox and data
     * graphs. Checkes for all subProperties of the properties mentioned in the
     * rules themselves.
     */
    private void checkAllForwardRules() {
        // Build a search path for the rules
        Finder caches = FinderUtil.cascade(subPropertyCache, subClassCache, tripleCache);
        // Check all rules sequentially
        for (int i = 0; i < rules.length; i++) {
            BaseFRule rule = rules[i];
            TriplePattern head = rule.getHead();
            Node pPattern = head.getPredicate();
            if (pPattern.isVariable()) {
                checkRule(head, rule, caches);
            } else {
                // Check out all subProperties of the given predicate
                TriplePattern spPatt = new TriplePattern(null, TransitiveReasoner.subPropertyOf, pPattern);
                ExtendedIterator sps = subPropertyCache.find(spPatt);
                while (sps.hasNext()) {
                    TriplePattern altHead = new TriplePattern(
                                                    head.getSubject(),
                                                    ((Triple)sps.next()).getSubject(),
                                                    head.getObject());
                    checkRule(altHead, rule, caches);
                }
            }
        }
    }
    
    /**
     * Run a single rule, with the rewritten head, against the data
     */
    private void checkRule(TriplePattern altHead, BaseFRule rule, Finder caches) {
        Iterator it = caches.findWithContinuation(altHead, fdata);
        while (it.hasNext()) {
            Triple t = (Triple)it.next();
            rule.bindAndFire(t, this);
        }
    }
    
    
    /**
     * Assert a triple into the triple cache.
     * Called by FRules when they fire
     */
    public void assertTriple(Triple t) {
        axioms.getGraph().add(t);
    }
    
    /**
     * Add a new backchaining rule into the rule set.
     * Called by FRules when they fire
     */
    public void addBRule(BRWRule rule) {
        router.register(rule);
    }
    
    /**
     * Separate the cache of subClassOf relations from the parent reasoner
     * because new added data has changed the class lattice
     */
    private void splitSubClassCache() {
        if (!haveSplitSubClassCache) {
            subClassCache = subClassCache.deepCopy();
            haveSplitSubClassCache = true;
        }
    }
    
    /**
     * Printable version of the whole reasoner state.
     * Used during debugging
     */
    public String toString() {
        StringBuffer state = new StringBuffer();
        TriplePattern all = new TriplePattern(null, null, null);
        if (tripleCache != null) {
            state.append("axioms + tbox\n");
            for (Iterator i = tripleCache.find(all); i.hasNext(); ) {
                state.append(TriplePattern.simplePrintString((Triple)i.next()));
                state.append("\n");
            }
        }
        if (fdata != null) {
            state.append("Bound raw data\n");
            for (Iterator i = fdata.find(all); i.hasNext(); ) {
                state.append(TriplePattern.simplePrintString((Triple)i.next()));
                state.append("\n");
            }
        }
        if (router != null) {
            state.append("Rule set\n");
            state.append(router.toString());
        }
        return state.toString();
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
