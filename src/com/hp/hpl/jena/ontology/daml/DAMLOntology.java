/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLOntology.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:18 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.NodeIterator;


/**
 * Encapsulates the properties known for a given source ontology.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLOntology.java,v 1.4 2004-12-06 13:50:18 andy_seaborne Exp $
 */
public interface DAMLOntology
    extends DAMLCommon
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    /**
     * Property value accessor for the version info property of the ontology.
     *
     * @return A literal accessor that gives access to the version info of the ontology.
     */
    public LiteralAccessor prop_versionInfo();


    /**
     * Answer an iteration of resources that represent the URI's of the
     * ontologies that this ontology imports.
     *
     * @return An iterator over the resources representing imported ontologies
     */
    public NodeIterator getImportedOntologies();


    /**
     * Add the given ontology to the list of ontologies managed by the
     * knowledge store, and add it as an imoport property to this ontology object.
     *
     * @param uri The URI of the model.
     */
    public void addImportedOntology( String uri );

}



/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

