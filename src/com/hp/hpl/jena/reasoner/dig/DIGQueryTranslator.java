/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGQueryTranslator.java,v $
 * Revision           $Revision: 1.14 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-07 09:56:36 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import java.util.Iterator;

import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.xml.SimpleXMLPath;
import com.hp.hpl.jena.util.xml.SimpleXMLPathElement;


/**
 * <p>
 * Base class for translators that map incoming RDF find patterns to DIG queries.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGQueryTranslator.java,v 1.14 2004-12-07 09:56:36 andy_seaborne Exp $)
 */
public abstract class DIGQueryTranslator {
    // Constants
    //////////////////////////////////

    public static final String ALL = "*";
    
    
    // Static variables
    //////////////////////////////////

    
    // Instance variables
    //////////////////////////////////

    /** The node that the incoming subject must match */
    private Node m_subject;
    
    /** The node that the incoming object must match */
    private Node m_object;
    
    /** The node that the incoming predicate must match */
    private Node m_pred;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct an abstract translator, given the URI's of nodes to match against
     * or null to represent 
     */
    public DIGQueryTranslator( String subject, String predicate, String object ) {
        m_subject = mapNode( subject );
        m_pred    = mapNode( predicate );
        m_object  = mapNode( object );
    }
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Translate the given pattern to a DIG query, and pass it on to the DIG
     * adapter as a query.  Translate the results of the query back to a
     * triple stream via an extended iterator. Assumes this method is called
     * contingent on a successful {@link #trigger}.</p>
     * @param pattern The pattern to translate to a DIG query
     * @param da The DIG adapter through which we communicate with a DIG reasoner
     */
    public ExtendedIterator find( TriplePattern pattern, DIGAdapter da ) {
        DIGConnection dc = da.getConnection();
        
        // pose the query to the dig reasoner
        Document query = translatePattern( pattern, da );
        if (query == null) {
            LogFactory.getLog( getClass() ).warn( "Could not find pattern translator for nested DIG query " + pattern );
        }
        Document response = da.getConnection().sendDigVerb( query, da.getProfile() );
        
        boolean warn = dc.warningCheck( response );
        if (warn) {
            for (Iterator i = dc.getWarnings();  i.hasNext(); ) {
                LogFactory.getLog( getClass() ).warn( i.next() );
            }
        }
        
        // translate the response back to triples
        return translateResponse( response, pattern, da );
    }
    
    
    /**
     * <p>Translate the given pattern (with given premises)
     * to a DIG query, and pass it on to the DIG
     * adapter as a query.  Translate the results of the query back to a
     * triple stream via an extended iterator.</p>
     * @param pattern The pattern to translate to a DIG query
     * @param da The DIG adapter through which we communicate with a DIG reasoner
     * @param premises Model conveying additional information about the resources
     * in the subject or object
     */
    public ExtendedIterator find( TriplePattern pattern, DIGAdapter da, Model premises ) {
        DIGConnection dc = da.getConnection();
        
        // pose the query to the dig reasoner
        Document query = translatePattern( pattern, da, premises );
        if (query == null) {
            LogFactory.getLog( getClass() ).warn( "Could not find pattern translator for nested DIG query " + pattern );
            return NullIterator.instance;
        }
        else {
            Document response = da.getConnection().sendDigVerb( query, da.getProfile() );
            
            boolean warn = dc.warningCheck( response );
            if (warn) {
                for (Iterator i = dc.getWarnings();  i.hasNext(); ) {
                    LogFactory.getLog( getClass() ).warn( i.next() );
                }
            }
            
            // translate the response back to triples
            return translateResponse( response, pattern, da );
        }
    }
    
    
    /**
     * <p>Answer true if this translator applies to the given triple pattern.</p>
     * @param pattern An incoming patter to match against
     * @param da The current dig adapter
     * @param premises An optional Model that is used to convey the statements in the additional
     * premises to the query
     * @return True if this translator applies to the pattern.
     */
    public boolean trigger( TriplePattern pattern, DIGAdapter da, Model premises ) {
        return trigger( m_subject, pattern.getSubject(), premises ) &&
               trigger( m_object, pattern.getObject(), premises )   &&
               trigger( m_pred, pattern.getPredicate(), premises )  &&
               checkTriple( pattern, da, premises );
    }
    
    
    /**
     * <p>An optional post-trigger check on the consituents of the triple pattern. By default,
     * delegates to a check on each of the subjec, object and predicate.  However, this method
     * may be overridden by sub-classes to provide a more context-sensitive test.</p>
     * @param pattern The triple pattern
     * @param da The current dig adapter
     * @param premises Model denoting premises to the query, or null
     * @return True if the pattern conforms to the prerequisites for a given translation step
     */
    public boolean checkTriple( TriplePattern pattern, DIGAdapter da, Model premises ) {
        return checkSubject( pattern.getSubject(), da, premises )       &&
               checkObject( pattern.getObject(), da, premises )         &&
               checkPredicate( pattern.getPredicate(), da, premises );

    }
    
    
    /**
     * <p>Additional test on the subject of the incoming find pattern. Default
     * is to always match</p>
     * @param subject The subject resource from the incoming pattern
     * @param da The current dig adapter
     * @param premises A model that conveys additional information about the premises
     * of the query, which might assist the check to suceed or fail. By default it
     * is ignored.
     * @return True if this subject matches the trigger condition expressed by this translator instance
     */
    public boolean checkSubject( Node subject, DIGAdapter da, Model premises ) {
        return true;
    }
    
    
    /**
     * <p>Additional test on the object of the incoming find pattern. Default
     * is to always match</p>
     * @param object The object resource from the incoming pattern
     * @param da The current dig adapter
     * @param premises A model that conveys additional information about the premises
     * of the query, which might assist the check to suceed or fail. By default it
     * is ignored.
     * @return True if this object matches the trigger condition expressed by this translator instance
     */
    public boolean checkObject( Node object, DIGAdapter da, Model premises ) {
        return true;
    }
    
    
    /**
     * <p>Additional test on the predicate of the incoming find pattern. Default
     * is to always match</p>
     * @param pred The predicate resource from the incoming pattern
     * @param da The current dig adapter
     * @param premises A model that conveys additional information about the premises
     * of the query, which might assist the check to suceed or fail. By default it
     * is ignored.
     * @return True if this predicate matches the trigger condition expressed by this translator instance
     */
    public boolean checkPredicate( Node pred, DIGAdapter da, Model premises ) {
        return true;
    }


    /**
     * <p>Answer an XML document that presents the translation of the query into DIG query language.</p>
     */
    public abstract Document translatePattern( TriplePattern query, DIGAdapter da );
    
    /**
     * <p>Answer an XML document that presents the translation of the query into DIG query language,
     * given that either the subject or object may be expressions defined by the statements
     * in the premises model.</p>
     */
    public abstract Document translatePattern( TriplePattern pattern, DIGAdapter da, Model premises );

    /**
     * <p>Answer an extended iterator over the triples that result from translatig the given DIG response
     * to RDF.</p>
     */
    public abstract ExtendedIterator translateResponse( Document Response, TriplePattern query, DIGAdapter da );
   
    
    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Answer a node corresponding to the given URI.</p>
     * @param uri A node URI, or the special string *, or null.
     * @return A Jena Node corresponding to the given URI
     */
    protected Node mapNode( String uri ) {
        if (uri == null) {
            return null;
        }
        else {
            return (uri.equals( ALL )) ? Node_RuleVariable.WILD : Node.createURI( uri );
        }
    }


    /**
     * <p>A node matches a trigger (lhs) node if either the lhs is null, or
     * the nodes are equal. Note: not matching in the same sense as triple patterns.</p>
     * @param lhs The trigger node to match against
     * @param rhs The incoming pattern node
     * @param premises A model that conveys additional information about the premises
     * of the query, which might assist the trigger to suceed or fail. By default it
     * is ignored.
     * @return True if match
     */
    protected boolean trigger( Node lhs, Node rhs, Model premises ) {
        return (lhs == null) || lhs.equals( rhs );
    }
    
    
    /**
     * <p>Answer true if the given document is the response &lt;true&gt; from a DIG reasoner.
     * @param response The document encoding the response
     * @return True iff this is the response <code>true</code>.
     */
    protected boolean isTrue( Document response ) {
        return new SimpleXMLPath( true )
               .appendElementPath( DIGProfile.TRUE )
               .getAll( response )
               .hasNext();
    }
    
    
    /**
     * <p>Answer true if the given document is the response &lt;false&gt; from a DIG reasoner.
     * @param response The document encoding the response
     * @return True iff this is the response <code>false</code>.
     */
    protected boolean isFalse( Document response ) {
        return new SimpleXMLPath( true )
               .appendElementPath( DIGProfile.FALSE )
               .getAll( response )
               .hasNext();
    }
    
    
    /**
     * <p>Translate a concept set document into an extended iterator
     * of triples, placing the concept identities into either the subject
     * or object position in the returned triple.</p>
     * @param response The response XML document
     * @param query The original query
     * @param object Flag to indicate that the concept names should occupy the subject field
     * of the returned triple, otherwise the object
     */
    protected ExtendedIterator translateConceptSetResponse( Document response, TriplePattern query, boolean object ) {
        return translateNameSetResponse( response, query, object, 
                                         new String[] {DIGProfile.CONCEPT_SET, DIGProfile.SYNONYMS, DIGProfile.CATOM} );
    }
    

    /**
     * <p>Translate a role set document into an extended iterator
     * of triples, placing the concept identities into either the subject
     * or object position in the returned triple.</p>
     * @param response The response XML document
     * @param query The original query
     * @param object Flag to indicate that the role names should occupy the subject field
     * of the returned triple, or the object
     */
    protected ExtendedIterator translateRoleSetResponse( Document response, TriplePattern query, boolean object ) {
        return translateNameSetResponse( response, query, object, 
                                         new String[] {DIGProfile.ROLE_SET, DIGProfile.SYNONYMS, DIGProfile.RATOM} );
    }
    

    /**
     * <p>Translate an instance set document into an extended iterator
     * of triples, placing the concept identities into either the subject
     * or object position in the returned triple.</p>
     * @param response The response XML document
     * @param query The original query
     * @param object Flag to indicate that the instance names should occupy the subject field
     * of the returned triple, or the object
     */
    protected ExtendedIterator translateIndividualSetResponse( Document response, TriplePattern query, boolean object ) {
        return translateNameSetResponse( response, query, object, 
                                         new String[] {DIGProfile.INDIVIDUAL_SET, DIGProfile.INDIVIDUAL} );
    }
    

    /**
     * <p>Translate an document encoding a set of named entities into an extended iterator
     * of triples, placing the concept identities into either the subject
     * or object position in the returned triple.</p>
     * @param response The response XML document
     * @param query The original query
     * @param object Flag to indicate that the instance names should occupy the subject field
     * of the returned triple, or the object
     * @param path The element name path to follow from the root
     */
    protected ExtendedIterator translateNameSetResponse( Document response, TriplePattern query, boolean object, String[] path ) {
        // evaluate a path through the return value to give us an iterator over catom names
        SimpleXMLPath p = new SimpleXMLPath( true );
        
        // build the path
        for (int i = 0;  i < path.length; i++) {
            p.appendElementPath( path[i] );
        }
        p.appendAttrPath( DIGProfile.NAME );
        
        // and evaluate it
        ExtendedIterator iNodes = p.getAll( response ).mapWith( new DIGValueToNodeMapper() );
        
        // return the results as triples
        if (object) {
            return iNodes.mapWith( new TripleObjectFiller( query.getSubject(), query.getPredicate() ) );
        }
        else {
            return iNodes.mapWith( new TripleSubjectFiller( query.getPredicate(), query.getObject() ) );
        }
    }
    
    /**
     * <p>Check if a document representing a concept-set response from the DIG reasoner
     * contains a given node as a value, and, if so, return a singleton iterator over the
     * given result triple.</p>
     * @param response The XML document to process
     * @param da The DIG adapter
     * @param node The node we are seeking
     * @param result The triple to return if node occurs in the concept set in response
     * @return The singeleton iterator over result, or the null iterator if node is not present
     * in the response.
     */
    protected ExtendedIterator conceptSetNameCheck( Document response, DIGAdapter da, Node node, Triple result ) {
        // evaluate a path through the return value to give us an iterator over catom names
        ExtendedIterator catoms = new SimpleXMLPath( true )
                                     .appendElementPath( DIGProfile.CONCEPT_SET )
                                     .appendElementPath( DIGProfile.SYNONYMS )
                                     .appendElementPath( SimpleXMLPathElement.ALL_CHILDREN )
                                     .getAll( response );
                                          
        // search for the object name
        String oName = da.getNodeID( node );

        boolean seekingTop = oName.equals( da.getOntLanguage().THING().getURI() );
        boolean seekingBottom = oName.equals( da.getOntLanguage().NOTHING().getURI() );
        
        boolean found = false;
        while (!found && catoms.hasNext()) {
            Element name = (Element) catoms.next();
            
            found = (seekingTop    && name.getNodeName().equals( DIGProfile.TOP )) ||
                    (seekingBottom && name.getNodeName().equals( DIGProfile.BOTTOM )) ||
                    name.getAttribute( DIGProfile.NAME ).equals( oName );
        }

        // the resulting iterator is either of length 0 or 1
        return found ? (ExtendedIterator) new SingletonIterator( result )
                     : NullIterator.instance;
    }

    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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
