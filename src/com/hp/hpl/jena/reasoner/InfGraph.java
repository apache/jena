/******************************************************************
 * File:        InfGraph.java
 * Created by:  Dave Reynolds
 * Created on:  10-Jan-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: InfGraph.java,v 1.5 2003-04-15 21:17:58 jeremy_carroll Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import java.util.Iterator;

/**
 * Extends the Graph interface to give additional means to query an inferred
 * graph. Many entailments from the raw data are made to appear as if they
 * are extract triples in the inferred graph and so appear through the
 * normal Graph.find interface. 
 * 
 * However, here are two extensions required. Firstly, the ability to
 * ask about global properties of the whole graph (e.g. consistency). Secondly,
 * the ability to temporarily construct expressions (encoded in RDF) which 
 * form more complex queries.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.5 $ on $Date: 2003-04-15 21:17:58 $
 */
public interface InfGraph extends Graph {

    /**
     * Return the raw RDF data Graph being processed (i.e. the argument
     * to the Reasonder.bind call that created this InfGraph).
     */
    public Graph getRawGraph();
    
    /**
     * Return the Reasoner which is being used to answer queries to this graph.
     */
    public Reasoner getReasoner();

    /**
     * Test a global boolean property of the graph. This might included
     * properties like consistency, OWLSyntacticValidity etc.
     * It remains to be seen what level of generality is needed here. We could
     * replace this by a small number of specific tests for common concepts.
     * @param property the URI of the property to be tested 
     * @return a Node giving the value of the global property, this may 
     * be a boolean literal, some other literal value (e.g. a size).
     */    
    public Node getGlobalProperty(Node property);
    
    /**
     * A convenience version of getGlobalProperty which can only return
     * a boolean result.
     */
    public boolean testGlobalProperty(Node property);
    
    /**
     * Test the consistency of the bound data. This normally tests
     * the validity of the bound instance data against the bound
     * schema data. 
     * @return a ValidityReport structure
     */
    public ValidityReport validate();
    
    /**
     * An extension of the Graph.find interface which allows the caller to 
     * encode complex expressions in RDF and then refer to those expressions
     * within the query triple. For example, one might encode a class expression
     * and then ask if there are any instances of this class expression in the
     * InfGraph.
     * @param subject the subject Node of the query triple, may be a Node in 
     * the graph or a node in the parameter micro-graph or null
     * @param property the property to be retrieved or null
     * @param object the object Node of the query triple, may be a Node in 
     * the graph or a node in the parameter micro-graph.    
     * @param param a small graph encoding an expression which the subject and/or
     * object nodes refer.
     */
    public ExtendedIterator find(Node subject, Node property, Node object, Graph param);
    
    /**
     * Switch on/off drivation logging
     */
    public void setDerivationLogging(boolean logOn);
   
    /**
     * Return the derivation of the given triple (which is the result of
     * some previous find operation).
     * Not all reasoneers will support derivations.
     * @return an iterator over Derivation records or null if there is no derivation information
     * available for this triple.
     */
    public Iterator getDerivation(Triple triple);
    
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

