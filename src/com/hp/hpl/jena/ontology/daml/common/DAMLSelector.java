/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            18 Sept 2001
 * Filename           $RCSfile: DAMLSelector.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-01-23 15:14:20 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright Hewlett-Packard Company 2001
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
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.common;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.impl.SelectorImpl;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Property;

import com.hp.hpl.jena.ontology.daml.DAMLCommon;
import com.hp.hpl.jena.ontology.daml.DAMLModel;

import com.hp.hpl.jena.util.Log;

import java.util.Iterator;


/**
 * <p>An extension to the standard Jena selector for querying models, which respects the
 * equivalence that DAML can assert between values.  That is, for example, this selector will
 * match a statement:
 * <code><pre>
 * myns:ian isCalled "ian".
 * </pre></code>
 *
 * with a selector:
 * <code><pre>
 * new DAMLSelector( resIan, model.getProperty( myns + "sAppel" );
 * </pre></code>
 * if it is known that <code>myns:ian daml:samePropertyAs myns:sAppel</code>.
 * </p><p>
 * Equivalences between classes, properties, instances (individuals) and arbitrary
 * values (using <code>daml:equivalentTo</code>) are respected.  This does inevitably add a significant
 * overhead to accessing the values from the model, but is necessary for correctly
 * processing DAML models.
 * </p><p>
 * Note that the current release of Jena does not permit us to correctly process
 * equivalence among resources efficiently.  Therefore, as a performance hack,
 * {@link com.hp.hpl.jena.ontology.daml.DAMLModel#setUseEquivalence} has been added to the
 * model to allow this behaviour to be turned off under user control. By default,
 * equivalence will be tested.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLSelector.java,v 1.2 2003-01-23 15:14:20 ian_dickinson Exp $
 */
public class DAMLSelector
    extends SelectorImpl
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The subject resource */
    protected Resource m_subject = null;

    /** The predicate */
    protected Property m_predicate = null;

    /** The object */
    protected RDFNode m_object = null;


    // Constructors
    //////////////////////////////////

    /**
     * Create a DAML selector.  Note due to problems with the underlying store.listProperties()
     * implementation, this selector iterates over <b>all</b> statements in the model,
     * by setting the normal selector subject, predicate and object to null.  This
     * is ridiculously inefficient, but necessary.  TODO: fix this, once Jena has
     * been fixed.
     *
     * @param subject The subject of the statement, or null.
     * @param predicate The predicate of the statement, or null.
     * @param object The object of the statement, or null.
     */
    public DAMLSelector(Resource subject, Property predicate, RDFNode object) {
        super();
        m_subject = subject;
        m_predicate = predicate;
        m_object = object;
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Answer true if a statement should be included in a selection.  This method
     * tests whether the supplied statement satisfies the subject, predicate and
     * object constraints of the selector, including testing whether other members
     * of known equivalence classes of the subject, predicate or object will
     * satisfy the constraints.
     *
     * @param stmt the statement to be tested
     * @return true if the statement satisfies the subject, object
     *         and predicate constraints and the selects constraint.
     */
    public boolean test( Statement stmt ) {
       return (m_subject == null   || matches( m_subject, stmt.getSubject() ))     &&
              (m_predicate == null || matches( m_predicate, stmt.getPredicate() )) &&
              (m_object == null    || matches( m_object, stmt.getObject() ));
    }



    // Internal implementation methods
    //////////////////////////////////


    /**
     * Answer true if the given subjects match, which will be true if they
     * <ul>
     *   <li> have the same URI, or </li>
     *   <li> are classes and are equivalent under <code>daml:sameClassAs<code>, or </li>
     *   <li> are properties and are equivalent under <code>daml:samePropertyAs<code>, or </li>
     *   <li> are instances and are equivalent under <code>daml:sameIndividualAs<code>, or </li>
     *   <li> are literals and their values match, or </li>
     *   <li> are equivalent under <code>daml:sameEquivalentTo<code> </li>
     * </ul>
     *
     * @param node0 An RDF node
     * @param node1 An RDF node
     * @return true if node0 matches node1
     */
    protected boolean matches( RDFNode node0, RDFNode node1 ) {
        // if they are equal they match
        if (node0.equals( node1 )) {
            return true;
        }

        // are we looking at DAML resources?
        if (node0 instanceof DAMLCommon  &&  node1 instanceof Resource) {
            // check for equivalence testing being disabled
            DAMLModel m = ((DAMLCommon) node0).getDAMLModel();
            if (m == null) {
                m = (node1 instanceof DAMLCommon) ? ((DAMLCommon) node1).getDAMLModel() : null;
            }

            if (m != null  && m.getUseEquivalence()) {
                // yes, equivalence checking is turned on ... so check the equivalent terms
                return checkEquivalenceClass( ((DAMLCommon) node0).getEquivalentValues(), (Resource) node1 );
            }
        }

        return false;
    }



    /**
     * Answer true if the second resource is a member of the equivalence class denoted
     * by the first iterator.
     *
     * @param equiv An iterator over resources
     * @param res A resource
     * @return true if res is equal to any of the members of equiv
     */
    protected boolean checkEquivalenceClass( Iterator equiv, Resource res ) {
        while (equiv.hasNext()) {
            // check for equality with each member of the equivalence class
            RDFNode n = (RDFNode) equiv.next();
            Log.finest( "Checking equivalence of " + res + ", and " + n );

            if (res.equals( n )) {
                Log.finest( "Equivalence = true");
                return true;
            }
        }

        return false;
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
