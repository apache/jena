/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            17 Sept 2001
 * Filename           $RCSfile: DAMLDataInstance.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-03-12 17:16:32 $
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
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////

import com.hp.hpl.jena.ontology.daml.PropertyAccessor;

import java.util.Iterator;

import com.hp.hpl.jena.ontology.daml.impl.DatatypeTranslator;


/**
 * A data instance is a specific type of DAML object that represents the instantiation
 * of a DAML datatype.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLDataInstance.java,v 1.2 2003-03-12 17:16:32 ian_dickinson Exp $
 */
public interface DAMLDataInstance
    extends DAMLCommon
{
    // Constants
    //////////////////////////////////



    // External signature methods
    //////////////////////////////////

    /**
     * Property accessor for <code>daml:sameIndividualAs</code> property on a DAML instance.
     *
     * @return a property accessor
     */
    public PropertyAccessor prop_sameIndividualAs();


    /**
     * Return an iterator over all of the instances that are the same as this one,
     * by generating the transitive closure over the <code>daml:samePropertyAs</code>
     * property.
     *
     * @return an iterator whose values will all be DAMLInstance objects
     */
    public Iterator getSameInstances();


    /**
     * Answer the Datatype translator for values encoded by the datatype of this
     * instance.
     *
     * @return the datatype translator defined by the DAMLDatatype that is the rdf:type
     *         of this instance, or null if it is not defined.
     */
    public DatatypeTranslator getTranslator();


    /**
     * Answer the value of this instance as a Java object, translated from the
     * serialised RDF representation by the Dataype's translator.
     *
     * @return the value of this instance, or null if either the translator or the
     *         serialised value is defined
     */
    public Object getValue();


    /**
     * Set the value of this instance to the given Java value, which will be
     * serialised into the RDF graph by the datatype's translator
     */
    public void setValue( Object value );

}