/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLModel.java,v $
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
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.ontology.daml.impl.DAMLLoader;
import com.hp.hpl.jena.ontology.daml.impl.XMLDatatypeRegistry;

import java.util.Iterator;

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;



/**
 * Interface that encapsulates the capability of storing and retrieving DAML
 * ontology information from the underlying storage or persistence service. The
 * DAML model is an extension of a single Jena RDF model, which is used to store the
 * information from all loaded ontologies.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLModel.java,v 1.2 2003-03-12 17:16:32 ian_dickinson Exp $
 */
public interface DAMLModel
    extends Model
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * Create an (optionally anonymous) Ontology (big-'O') element, 
     * which holds meta-information for the ontology (small-'o').  
     * <b>N.B.</b> This does not create a new
     * ontology, it simply makes an entry in the current model.
     *
     * @param uri The URI for the new Ontology, or null to create an anonymous 
     *            Ontology. Ideally provide the URL in which the Ontology is 
     *            stored.
     *            Conventionally, in the RDF/XML serialization, we have
     *            <pre>
     *             &lt;daml:Ontology rdf:about=""&gt;
     *            </pre>
     *            The empty URIref in the above RDF/XML is known as a
     *            <Q>same document reference</Q> and expands to the
     *            URL of the current file.
     * @return A new DAMLOntology object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLOntology createDAMLOntology( String uri );


    /**
     * Create an (optionally anonymous) instance of the given class.
     *
     * @param damlClass The class of the newly created DAMLInstance
     * @param uri The URI for the new instance, or null to create an anonymous instance.
     * @return A new DAMLInstance object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLInstance createDAMLInstance( DAMLClass damlClass, String uri );


    /**
     * Create an (optionally anonymous) DAML class.
     *
     * @param uri The URI for the new class, or null to create an anonymous class.
     * @return A new DAMLClass object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLClass createDAMLClass( String uri );


    /**
     * Create an (optionally anonymous) DAML property. Note that it is recommended
     * to use one of the more specific property classes from the new DAML release:
     * see {@link #createDAMLObjectProperty} or {@link #createDAMLDatatypeProperty}.
     *
     * @param uri The URI for the new property, or null to create an anonymous property.
     * @return A new DAMLProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLProperty createDAMLProperty( String uri );


    /**
     * Create an (optionally anonymous) DAML object property.
     *
     * @param uri The URI for the new object property, or null to create an
     *            anonymous object property.
     * @return A new DAMLObjectProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLObjectProperty createDAMLObjectProperty( String uri );


    /**
     * Create an (optionally anonymous) DAML datatype property.
     *
     * @param uri The URI for the new datatype property, or null to create
     *            an anonymous datatype property.
     * @return A new DAMLDatatypeProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLDatatypeProperty createDAMLDatatypeProperty( String uri );

    /**
     * Create an (optionally anonymous) DAML datatype.
     *
     * @param uri The URI for the new datatype, or null to create
     *            an anonymous datatype.
     * @return A new DAMLDatatype object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLDatatype createDAMLDatatype( String uri );
    
    /**
     * Create an (optionally anonymous) DAML list.
     *
     * @param uri The URI for the new list, or null to create
     *            an anonymous list.
     * @return A new DAMLList object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLList createDAMLList( String uri );


    /**
     * Create an (optionally anonymous) DAML Restriction.
     *
     * @param uri The URI for the new restriction, or null to create
     *            an anonymous restriction.
     * @return A new DAMLRestriction object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLRestriction createDAMLRestriction( String uri );


    /**
     * Create a new DAML value that is a member of the given class.  The appropriate
     * DAMLCommon sub-class will be instantiated, so, for example, if the DAMLClass
     * is {@link com.hp.hpl.jena.vocabulary.DAML_OIL#Restriction}, a {@link DAMLRestriction}
     * object will be returned.  Note that if a URI is given, and a value with that
     * URI already exists in the model, that instance will be returned instead of
     * creating a new DAML value. This is necessary to maintain consistency of the model.
     *
     * @param uri The URI of the new DAML value, or null for an anonymous value
     * @param damlClass The class to which the new DAML value will belong
     * @param vocabulary The vocabulary to use for the newly created classs, or null to use
     *                   the default vocabulary
     * @return an instance of a DAMLCommon value that corresponds to the given class.
     */
    public DAMLCommon createDAMLValue( String uri, Resource damlClass, DAMLVocabulary vocabulary );


    /**
     * Answer the DAML value that corresponds to the given URI, if it exists in the
     * model.  If the URI does not match any of the resources presently in the model,
     * null is returned.
     *
     * @param uri The URI of the DAML resource to look for.
     * @return An existing DAML resource from the model, matching uri, or null if
     *         no such resource is found.
     */
    public DAMLCommon getDAMLValue( String uri );


    /**
     * Answer the DAML value that corresponds to the given URI, if it exists in the
     * model.  If the URI does not match any of the resources presently in the model,
     * create a new DAML resource with the given URI and vocabulary, from the given
     * DAML class.
     *
     * @param uri The URI of the DAML resource to look for.
     * @param damlClass The class of the new resource to create if no existing resource
     *                  is found.
     * @param vocabulary The vocabulary to use for the new value (if needed), or null
     *                   to use the default vocabulary.
     * @return An existing DAML resource from the model, matching uri, or a new
     *         resource if no existing resource is found.
     */
    public DAMLCommon getDAMLValue( String uri, DAMLClass damlClass, DAMLVocabulary vocabulary );


    /**
     * Answer an iterator over all DAML classes that are presently in the model.
     *
     * @return an iterator over all currently defined classes (including Restrictions).
     */
    public Iterator listDAMLClasses();


    /**
     * Answer an iterator over all DAML properties that are presently in the model.
     *
     * @return an iterator over all currently defined properties (i.e. rdf:Property and
     *         all sub-classes).
     */
    public Iterator listDAMLProperties();


    /**
     * Answer an iterator over all DAML instances that are presently in the model.
     *
     * @return an iterator over all currently defined DAML instances.
     */
    public Iterator listDAMLInstances();


    /**
     * Answer a reference to the loader for this DAML model
     *
     * @return a DAMLLoader reference
     */
    public DAMLLoader getLoader();


    /**
     * Answer true if the most recent load operation was successful. If not,
     * consult {@link DAMLLoader#getStatus} for details, and check error log.
     *
     * @return true if the most recent model load was successful
     */
    public boolean getLoadSuccessful();


    /**
     * Answer a reference to the XML datatype registry for this model, that can be used to
     * map between XML data marked up using XML Schema data descriptions, and Java objects.
     * This registry is also used to detect resources in the model that correspond to
     * {@link com.hp.hpl.jena.ontology.daml.DAMLDataInstance} objects, and type declarations that correspond to
     * {@link com.hp.hpl.jena.ontology.daml.DAMLDatatype} objects.
     *
     * @return an XML data translator registry
     */
    public XMLDatatypeRegistry getDatatypeRegistry();


    /**
     * Read the ontology indicated by the given uri.  Note that, depending on the settings in the
     * embedded {@link DAMLLoader}, ontology import statements embedded in this document will be
     * processed and the ontologies fetched and loaded.
     *
     * @param uri The URI identifying an ontology to be added.
     * @param base The base URI for any relative names that are loaded from the source document
     * @param lang Denotes the language the statements are represented in.
     * @return self.
     * @see com.hp.hpl.jena.rdf.model.Model#read( java.lang.String, java.lang.String )
     */
    public Model read( String uri, String base, String lang );


    /**
     * Flag to control whether accessing the DAML store will take into account equivalence classes for
     * properties and resources, using <code>daml:equivalentTo</code> and similar
     * statements.  Turning this flag on is correct according to the DAML semantics, but
     * will impose a significant performance problem in the current version.  Turning
     * the flag off will improve performance, at the cost of not conforming strictly
     * to the DAML specification.
     *
     * @param useEquivalence If true, accessing properties and resources will check for
     *                       equivalent values, at a cost of reduced performance.
     */
    public void setUseEquivalence( boolean useEquivalence );


    /**
     * Answer true if the model will consider equivalence classes when accessing
     * properties and resources.  See {@link #setUseEquivalence} for details.
     *
     * @return true if equivalence classes are being considered.
     */
    public boolean getUseEquivalence();
}
