/******************************************************************
 * File:        RDFSReasoner.java
 * Created by:  Dave Reynolds
 * Created on:  21-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: RDFSReasoner.java,v 1.12 2003-05-27 15:50:24 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.Util;
import com.hp.hpl.jena.reasoner.transitiveReasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * An RDFS reasoner suited to modest vocabularies but large instance
 * data. It does eager processing on the class and property declarations
 * and caches the results. This means that the initial creation can
 * be slow. However, if the vocabulary and instance data can be
 * separated then at least the class lattice results can be reused.
 * <p>
 * Instance related rules are implemented using a very simple rewrite
 * system. Triple queries that match a rule are rewritten and reapplied 
 * as queries. This is reasonably efficient for fairly ground queries,
 * especially where the predicate is ground. It performs redundant
 * passes over the data for unground queries, especially any that
 * need that might match (*, type, Resource) or (*, type, Property)!</p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.12 $ on $Date: 2003-05-27 15:50:24 $
 */
public class RDFSReasoner extends TransitiveReasoner implements Reasoner {
    /** The domain property */
    public static Node domainP;
    
    /** The range property */
    public static Node rangeP;
    
    /** Note if the reasoner is configured to scan for member properties */
    protected boolean scanProperties = true;
    
    // Static initializer
    static {
        domainP = RDFS.domain.getNode();
        rangeP = RDFS.range.getNode();
    }
    
    /** Constructor */
    public RDFSReasoner() {
        super();
    }
     
    /** 
     * Constructor 
     * @param configuration set of configuration information, this should be an RDF Graph
     * containing a resource corresponding this this reasoner with attached properties. The
     * only meaningful configuration property at present is scanProperties.
     */
    public RDFSReasoner(Model configuration) {
        super();
        if (configuration != null) {
            Boolean flag = checkBinaryPredicate(RDFSReasonerFactory.scanProperties, configuration);
            if (flag != null) scanProperties = flag.booleanValue();
        }
    }
    
    /**
     * Private constructor used by bindSchema when
     * returning a partially bound reasoner instance.
     */
    protected RDFSReasoner(Finder tbox, 
                    TransitiveGraphCache subClassCache, 
                    TransitiveGraphCache subPropertyCache,
                    boolean scanProperties) {
        super(tbox, subClassCache, subPropertyCache);
        this.scanProperties = scanProperties;
    }

    /**
     * Determine whether the given property is recognized and treated specially
     * by this reasoner. This is a convenience packaging of a special case of getCapabilities.
     * @param property the property which we want to ask the reasoner about, given as a Node since
     * this is part of the SPI rather than API
     * @return true if the given property is handled specially by the reasoner.
     */
    public boolean supportsProperty(Property property) {
        ReasonerFactory rf = RDFSReasonerFactory.theInstance();
        Model caps = rf.getCapabilities();
        Resource root = caps.getResource(rf.getURI());
        return caps.contains(root, ReasonerRegistry.supportsP, property);
    }
     
    /**
     * Helper method - extracts the truth of a boolean configuration
     * predicate.
     * @param pred the predicate to be tested
     * @param configuration the configuration model
     * @return null if there is no setting otherwise a Boolean giving the setting value
     */
    private Boolean checkBinaryPredicate(Property predicate, Model configuration) {
        Resource base = configuration.getResource(RDFSReasonerFactory.URI);
        StmtIterator i = base.listProperties(predicate);
        if (i.hasNext()) {
            return new Boolean(i.nextStatement().getObject().toString().equalsIgnoreCase("true"));
        } else {
            return null;
        }
    }

    /**
     * Extracts all of the subClass and subProperty declarations from
     * the given schema/tbox and caches the resultant graphs.
     * It can only be used once, can't stack up multiple tboxes this way.
     * This limitation could be lifted - the only difficulty is the need to
     * reprocess all the earlier tboxes if a new subPropertyOf subPropertyOf
     * subClassOf is discovered.
     * @param tbox schema containing the property and class declarations
     */
    public Reasoner bindSchema(Graph tbox) throws ReasonerException {
        if (this.tbox != null) {
            throw new ReasonerException("Attempt to bind multiple rulesets - disallowed for now");
        }
        FGraph ftbox = new FGraph(tbox);
        TransitiveGraphCache sCc = new TransitiveGraphCache(directSubClassOf, subClassOf);
        TransitiveGraphCache sPc = new TransitiveGraphCache(directSubPropertyOf, subPropertyOf);
        TransitiveReasoner.cacheSubProp(ftbox, sPc);
        TransitiveReasoner.cacheSubClass(ftbox, sPc, sCc);
        sPc.setCaching(true);
        return new RDFSReasoner(ftbox, sCc, sPc, scanProperties);
    }
    
     
    /**
     * Attach the reasoner to a set of RDF ddata to process.
     * The reasoner may already have been bound to specific rules or ontology
     * axioms (encoded in RDF) through earlier bindRuleset calls.
     * @param data the RDF data to be processed, some reasoners may restrict
     * the range of RDF which is legal here (e.g. syntactic restrictions in OWL).
     * @return an inference graph through which the data+reasoner can be queried.
     * @throws ReasonerException if the data is ill-formed according to the
     * constraints imposed by this reasoner.
     */
    public InfGraph bind(Graph data) throws ReasonerException {
        return new RDFSInfGraph(this, data);
    }   
    
    /**
     * Switch on/off drivation logging.
     * If set to true then the InfGraph created from the bind operation will start
     * life with recording of derivations switched on. This is currently only of relevance
     * to rule-based reasoners.
     * <p>
     * Default - false.
     */
    public void setDerivationLogging(boolean logOn) {
        // Irrelevant to this reasoner
    }
    
    /**
     * Set a configuration parameter for the reasoner. The only supported parameter at present is:
     * are:
     * <ul>
     * <li>RDFSReasonerFactory.scanProperties - set this to Boolean true to
     * enable scanning of all properties looking for container membership properties, default on. </li>
     * </ul>
     * 
     * @param parameterUri the uri identifying the paramter to be changed
     * @param value the new value for the parameter, typically this is a wrapped
     * java object like Boolean or Integer.
     */
    public void setParameter(String parameterUri, Object value) {
        if (parameterUri.equals(RDFSReasonerFactory.scanProperties.getURI())) {
            scanProperties = Util.convertBooleanPredicateArg(parameterUri, value);
        } else {
            throw new IllegalParameterException(parameterUri);
        }
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

