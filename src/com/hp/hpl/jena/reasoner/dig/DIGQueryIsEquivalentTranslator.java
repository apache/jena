/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10-Dec-2003
 * Filename           $RCSfile: DIGQueryIsEquivalentTranslator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-12 00:08:05 $
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.util.xml.SimpleXMLPath;


/**
 * <p>
 * Translator to map owl:equivalentClass to the DIG &lt;equivalents&gt; query.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: DIGQueryIsEquivalentTranslator.java,v 1.1 2003-12-12 00:08:05 ian_dickinson Exp $
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

    
    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a translator for the DIG query 'equivalents'.</p>
     * @param predicate The predicate URI to trigger on
     * @param lhs If true, the free variable is the subject of the triple
     */
    public DIGQueryIsEquivalentTranslator( String predicate ) {
        super( null, predicate, null );
    }
    

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer a query that will generate the class hierachy for a concept</p>
     */
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        DIGConnection dc = da.getConnection();
        Document query = dc.createDigVerb( DIGProfile.ASKS, da.getProfile() );
        
        Element equivalents = da.addElement( query.getDocumentElement(), DIGProfile.EQUIVALENTS );
        da.addClassDescription( equivalents, pattern.getSubject() );
        
        return query;
    }


    /**
     * <p>Answer an iterator of triples that match the original find query.</p>
     */
    public ExtendedIterator translateResponse( Document response, TriplePattern query, DIGAdapter da ) {
        // evaluate a path through the return value to give us an iterator over catom names
        ExtendedIterator catomNames = new SimpleXMLPath( true )
                                          .appendElementPath( DIGProfile.CONCEPT_SET )
                                          .appendElementPath( DIGProfile.SYNONYMS )
                                          .appendElementPath( DIGProfile.CATOM )
                                          .appendAttrPath( DIGProfile.NAME )
                                          .getAll( response );
                                          
        // search for the object name
        String oName = da.getNodeID( query.getObject() );
        boolean found = false;
        
        while (!found && catomNames.hasNext()) {
            found = oName.equals( catomNames.next() );
        }

        // the resulting iterator is either of length 0 or 1
        return found ? (ExtendedIterator) new SingletonIterator( new Triple( query.getSubject(), query.getPredicate(), query.getObject() ) )
                     : NullIterator.instance;
    }
    
    
    public boolean checkSubject( com.hp.hpl.jena.graph.Node subject ) {
        return subject.isConcrete();
    }
    
    public boolean checkObject( com.hp.hpl.jena.graph.Node object ) {
        return object.isConcrete();
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
