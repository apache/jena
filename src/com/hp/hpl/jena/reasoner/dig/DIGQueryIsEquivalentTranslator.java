/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10-Dec-2003
 * Filename           $RCSfile: DIGQueryIsEquivalentTranslator.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-05-18 09:56:34 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;


/**
 * <p>
 * Translator to map variants of owl:equivalentClass to the DIG &lt;equivalents&gt; query, 
 * where the query is testing if two concepts are indeed equivalent (rather than listing the
 * atoms that are, in fact, equivalent to a given concept, which is what 
 * {@link DIGQueryEquivalentsTranslator} does).
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGQueryIsEquivalentTranslator.java,v 1.8 2004-05-18 09:56:34 ian_dickinson Exp $
 */
public class DIGQueryIsEquivalentTranslator 
    extends DIGQueryTranslator
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** URI of the predicate we are testing for */
    protected String m_predicate;
    
    protected Node m_qSubject;
    protected Node m_qObject;
    

    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a translator for the DIG query 'equivalents'.</p>
     * @param predicate The predicate URI to trigger on
     * @param lhs If true, the free variable is the subject of the triple
     */
    public DIGQueryIsEquivalentTranslator( String predicate ) {
        super( null, null, null );
        m_predicate = predicate;
    }
    

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer a query that will generate a query to see if two concepts are equivalent</p>
     */
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        return translatePattern( pattern, da, null );
    }


    public Document translatePattern( TriplePattern pattern, DIGAdapter da, Model premises ) {
        DIGConnection dc = da.getConnection();
        Document query = dc.createDigVerb( DIGProfile.ASKS, da.getProfile() );
        
        // re-order the argument so that we can ask equivalent between one atom 
        // and one expression (can't do more because of DIG limitations)
        m_qSubject = pattern.getSubject();
        m_qObject = pattern.getObject();
        
        if (m_qSubject.isBlank() && m_qObject.isBlank()) {
            LogFactory.getLog( getClass() ).warn( "DIG 1.1 cannot handle isConcept query with two expressions" );
            return null;
        }
        else if (m_qObject.isBlank()) {
            // we want subject to be an expression if there is one
            Node temp = m_qSubject;
            m_qSubject = m_qObject;
            m_qObject = temp;
        }
        
        // we have to introduce a bNode, in the mode of the OWL comprehension axioms, if
        // the query is of the form :c owl:unionOf [A,B]
        Node p = pattern.getPredicate();
        if (!m_qObject.isBlank() &&
            (p.getURI().equals( da.getOntLanguage().UNION_OF().getURI()) ||
             p.getURI().equals( da.getOntLanguage().INTERSECTION_OF().getURI()) ||
             p.getURI().equals( da.getOntLanguage().COMPLEMENT_OF().getURI()) ) ||
             p.getURI().equals( da.getOntLanguage().ONE_OF().getURI())
            )
        {
            if (premises == null) {
                LogFactory.getLog( getClass() ).warn( "Cannot add comprehension axiom bNode for query because premises model is null" );
            }
            else {
                // create a bNode that has the same relationship to the class expression operands as the given
                Resource comp = premises.createResource( da.getOntLanguage().CLASS() );
                premises.add( comp, premises.getProperty( p.getURI() ), premises.getRDFNode( m_qSubject ) );
                m_qSubject = comp.getNode();
            }
        }
        
        Element equivalents = da.createQueryElement( query, DIGProfile.EQUIVALENTS );
        da.addClassDescription( equivalents, m_qSubject, premises );
        
        return query;
    }


    /**
     * <p>Answer an iterator of triples that match the original find query.</p>
     */
    public ExtendedIterator translateResponse( Document response, TriplePattern query, DIGAdapter da ) {
        return conceptSetNameCheck( response, da, m_qObject, query.asTriple() );
    }
    
    
    /**
     * <p>Check whether the pattern matches the preconditions for the translation
     * step.  This means that the subject and object must be concepts or bNodes.
     * A limitation on DIG means that both cannot be expressions (bNodes). The 
     * predicate must be equivalence, or one of the boolean definition 
     * relations (in which case the query will have to introduce a bNode as a
     * comprehension step).
     */
    public boolean checkTriple( TriplePattern pattern, DIGAdapter da, Model premises ) {
        Node object = pattern.getObject();
        Node subject = pattern.getSubject();
        Node pred = pattern.getPredicate();
        
        // ignore patterns with vars
        boolean pass = subject.isConcrete() && object.isConcrete() && pred.isConcrete();
        
        // at least one of subject and object is a concept
        pass = pass && ((object.isBlank() || da.isConcept( object, premises )) &&
                        (subject.isBlank()) || da.isConcept( subject, premises ) &&
                        (!subject.isBlank() || !object.isBlank()));
        
        // appropriate predicate
        pass = pass &&
                (pred.getURI().equals( m_predicate ) ||
                 pred.getURI().equals( da.getOntLanguage().UNION_OF().getURI() ) ||
                 pred.getURI().equals( da.getOntLanguage().INTERSECTION_OF().getURI() ) ||
                 pred.getURI().equals( da.getOntLanguage().COMPLEMENT_OF().getURI() ) ||
                 pred.getURI().equals( da.getOntLanguage().ONE_OF().getURI() )
                );
        
        return pass;
    }

    public boolean trigger( TriplePattern pattern, DIGAdapter da, Model premises ) {
        return super.trigger( pattern, da, premises );
    }

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
