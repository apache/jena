/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            7 Sept 2001
 * Filename           $RCSfile: DatatypeTranslator.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-03-12 17:16:11 $
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
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.ontology.daml.DAMLModel;



/**
 * Interface that defines the service of serialising and deserialising a Java object
 * representing a datatype to its XML Schema representation.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DatatypeTranslator.java,v 1.1 2003-03-12 17:16:11 ian_dickinson Exp $
 */
public interface DatatypeTranslator
{
    // Constants
    //////////////////////////////////




    // External signature methods
    //////////////////////////////////

    /**
     * Given an RDF node (resource or literal), return the Java value that the node
     * value encodes.  E.g. if a node has a value of '125' and a type of <code>xsd:integer</code>
     * return the corresponding integer.  Note that due to Java's limitations of there
     * being no general return type that includes the scalars, scalar values will be returned
     * as the corresponding object type (e.g. Integer instead of int).
     *
     * @param node The RDF node that represents the encoded value
     * @return the corresponding Java object.
     */
    public Object deserialize( RDFNode node );


    /**
     * Given a value and a model, answer an rdf node that encapsulates the value. This may
     * include arbitrary side-effects on the model, such as adding a type statement for
     * the resource.
     *
     * @param value The Java value to be serialised
     * @param model The RDF model to which the value will be serialised
     * @return the RDF node representing value.
     */
    public RDFNode serialize( Object value, DAMLModel model );
}
