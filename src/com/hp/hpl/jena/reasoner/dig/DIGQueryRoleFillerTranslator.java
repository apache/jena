/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            13 May 2004
 * Filename           $RCSfile: DIGQueryRoleFillerTranslator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-05-18 09:55:14 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.*;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.xml.SimpleXMLPath;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * <p>
 * Translator that generates a DIG roleFillers query in response to a find queries:
 * <pre>
 * :a :r :b
 * </pre>
 * where both a and b are known.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS $Id: DIGQueryRoleFillerTranslator.java,v 1.1 2004-05-18 09:55:14 ian_dickinson Exp $
 */
public class DIGQueryRoleFillerTranslator 
    extends DIGQueryTranslator
{

    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a translator for the DIG query 'roleFillers'.</p>
     * @param predicate The predicate URI to trigger on
     */
    public DIGQueryRoleFillerTranslator() {
        super( null, null, null );
    }
    

    // External signature methods
    //////////////////////////////////


    /**
     * <p>Answer a query that will list the role fillers for an individual-role pair</p>
     */
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        DIGConnection dc = da.getConnection();
        Document query = dc.createDigVerb( DIGProfile.ASKS, da.getProfile() );
        
        Element instances = da.createQueryElement( query, DIGProfile.ROLE_FILLERS );
        da.addNamedElement( instances, DIGProfile.INDIVIDUAL, da.getNodeID( pattern.getSubject() ) );
        da.addNamedElement( instances, DIGProfile.RATOM, da.getNodeID( pattern.getPredicate() ) );
        
        return query;
    }


    /**
     * <p>Answer an iterator of triples that match the original find query.</p>
     */
    public ExtendedIterator translateResponse( Document response, TriplePattern query, DIGAdapter da ) {
        // evaluate a path through the return value to give us an iterator over catom names
        SimpleXMLPath p = new SimpleXMLPath( true );
        p.appendElementPath( DIGProfile.INDIVIDUAL_SET );
        p.appendElementPath( DIGProfile.INDIVIDUAL );
        p.appendAttrPath( DIGProfile.NAME );
        
        // and evaluate it
        List matches = new ArrayList();
        ExtendedIterator iNodes = p.getAll( response ).mapWith( new DIGValueToNodeMapper() );
        try {
            while (iNodes.hasNext()) {
                com.hp.hpl.jena.graph.Node result = (com.hp.hpl.jena.graph.Node) iNodes.next();
                if (result.equals( query.getObject() )) {
                    matches.add( query.asTriple() );
                    break;
                }
            }
        }
        finally {
            iNodes.close();
        }
        
        // will contain either zero or one result
        return WrappedIterator.create( matches.iterator() );
    }
    
    
    public Document translatePattern( TriplePattern pattern, DIGAdapter da, Model premises ) {
        return translatePattern( pattern, da );
    }

    public boolean checkSubject( com.hp.hpl.jena.graph.Node subject, DIGAdapter da, Model premises ) {
        return subject.isConcrete() && da.isIndividual( subject );
    }

    public boolean checkObject( com.hp.hpl.jena.graph.Node object, DIGAdapter da, Model premises ) {
        return object.isConcrete() && da.isIndividual( object );
    }

    public boolean checkPredicate( com.hp.hpl.jena.graph.Node predicate, DIGAdapter da, Model premises ) {
        // check that the predicate is not a datatype property
        if (predicate.isConcrete()) {
            Resource p = (Resource) da.m_sourceData.getRDFNode( predicate );
            return !da.m_sourceData.contains( p, RDF.type, da.m_sourceData.getProfile().DATATYPE_PROPERTY() );
        }
        else {
            return false;
        }
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
