/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGReasonerFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-12-04 16:38:21 $
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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;



/**
 * <p>
 * Factory class for generating instances of DIG reasoners.  Implements singleton pattern.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGReasonerFactory.java,v 1.1 2003-12-04 16:38:21 ian_dickinson Exp $)
 */
public class DIGReasonerFactory 
    implements ReasonerFactory
{

    // Constants
    //////////////////////////////////

    /** Static URI for this reasoner type */
    public static final String URI = "http://jena.hpl.hp.com/2003/DIGReasoner";
    

    // Static variables
    //////////////////////////////////

    /** The singleton instance */
    private static DIGReasonerFactory s_instance = new DIGReasonerFactory();
    
    
    // Instance variables
    //////////////////////////////////

    /** A model denoting the standard capabilities of a DIG reasoner */
    private Model m_capabilities = null;
    
    
    // Constructors
    //////////////////////////////////

    /** Private constructor to enforce singleton pattern */
    private DIGReasonerFactory() {}
    
    
    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the singleton instance of the factory.</p>
     */
    public static DIGReasonerFactory getInstance() {
        return s_instance;
    }
    
    
    /**
     * <p>Answer a new DIG reasoner instance, optionally configured with the given
     * configuration resource.</p>
     * @param configuration A resource whose properties denote the configuration of
     * the reasoner instance, or null to rely on the default configuration.
     */
    public Reasoner create( Resource configuration ) {
        return new DIGReasoner( null, this, configuration );
    }
    

    /* (non-Javadoc)
     * @see com.hp.hpl.jena.reasoner.ReasonerFactory#getCapabilities()
     */
    public Model getCapabilities() {
        if (m_capabilities == null) {
            m_capabilities = ModelFactory.createDefaultModel();
            Resource base = m_capabilities.createResource(getURI());
            base.addProperty(ReasonerVocabulary.nameP, "DIG external Reasoner")
                .addProperty(ReasonerVocabulary.descriptionP, "Adapter for external (i.e. non-Jena) DIG reasoner." )
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subClassOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.subPropertyOf)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.member)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.range)
                .addProperty(ReasonerVocabulary.supportsP, RDFS.domain)
                // TODO - add OWL elements supported
                .addProperty(ReasonerVocabulary.versionP, "0.1");
        }
        
        return m_capabilities;
    }

    /**
     * <p>Answer the URI of this reasoner factory</p>
     */
    public String getURI() {
        return URI;
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
