/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLModel.java,v $
 * Revision           $Revision: 1.11 $
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
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.daml.impl.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.util.*;




/**
 * <p>Interface that encapsulates the capability of storing and retrieving DAML
 * ontology information from the underlying storage or persistence service. The
 * DAML model is an extension of a single Jena RDF model, which is used to store the
 * information from all loaded ontologies.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLModel.java,v 1.11 2004-12-06 13:50:18 andy_seaborne Exp $
 */
public interface DAMLModel
    extends OntModel
{
    // Constants
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Create an (optionally anonymous) Ontology (big-'O') element, 
     * which holds meta-information for the ontology (small-'o').  
     * <b>N.B.</b> This does not create a new
     * ontology, it simply makes an entry in the current model.</p>
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
     * <p>Create an (optionally anonymous) instance of the given class.</p>
     *
     * @param damlClass The class of the newly created DAMLInstance
     * @param uri The URI for the new instance, or null to create an anonymous instance.
     * @return A new DAMLInstance object.
     */
    public DAMLInstance createDAMLInstance( DAMLClass damlClass, String uri );


    /**
     * <p>Create an anonymous data instance, which has the given datatype and value.</p>
     * @param datatype A resource denoting the datatype of the new data instance object
     * @param value The value of the data instance
     * @return A new DAMLDataInstance object.
     */
    public DAMLDataInstance createDAMLDataInstance( Resource datatype, Object value );


    /**
     * <p>Create an anonymous data instance, which has the given datatype and value.</p>
     * @param datatype A resource denoting the datatype of the new data instance object
     * @param value The value of the data instance
     * @return A new DAMLDataInstance object.
     */
    public DAMLDataInstance createDAMLDataInstance( RDFDatatype datatype, Object value );


    /**
     * <p>Create an anonymous data instance, which has the given value and an appropriate datatype.</p>
     * @param value The value of the data instance
     * @return A new DAMLDataInstance object.
     */
    public DAMLDataInstance createDAMLDataInstance( Object value );
        

    /**
     * <p>Create an (optionally anonymous) DAML class.</p>
     *
     * @param uri The URI for the new class, or null to create an anonymous class.
     * @return A new DAMLClass object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLClass createDAMLClass( String uri );


    /**
     * <p>Create a DAML property. Note that it is recommended
     * to use one of the more specific property classes from the new DAML release:
     * see {@link #createDAMLObjectProperty} or {@link #createDAMLDatatypeProperty}.</p>
     *
     * @param uri The URI for the new property. May not be null.
     * @return A new DAMLProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLProperty createDAMLProperty( String uri );


    /**
     * <p>Create a DAML object property. An object property has ontology individuals
     * (instances) in its range, whereas a datatype property has concrete data literals
     * in the range.</p>
     *
     * @param uri The URI for the new object property. May not be null.
     * @return A new <code>DAMLObjectProperty</code> object.
     */
    public DAMLObjectProperty createDAMLObjectProperty( String uri );


    /**
     * <p>Create an (optionally anonymous) DAML datatype property. A datatype property has 
     * concrete data literals
     * in its range, whereas an object property has ontology individuals (instances)  
     * in the range.</p>
     *
     * @param uri The URI for the new datatype property. May not be null.
     * @return A new DAMLDatatypeProperty object.
     */
    public DAMLDatatypeProperty createDAMLDatatypeProperty( String uri );


    /**
     * <p>Create an empty DAML list.</p>
     *
     * @return A new empty DAMLList.
     */
    public DAMLList createDAMLList();


    /**
     * <p>Create a new DAML list containing the given elements.</p>
     *
     * @param elements An iterator over the elements to be added to the list
     * @return A new empty DAMLList.
     */
    public DAMLList createDAMLList( Iterator elements );


    /**
     * <p>Create a new DAML list containing the given elements.</p>
     *
     * @param elements An array of RDFNodes that will be the elements of the list
     * @return A new empty DAMLList.
     */
    public DAMLList createDAMLList( RDFNode[] elements );


    /**
     * <p>Create an (optionally anonymous) DAML Restriction.</p>
     *
     * @param uri The URI for the new restriction, or null to create
     *            an anonymous restriction.
     * @return A new DAMLRestriction object.
     */
    public DAMLRestriction createDAMLRestriction( String uri );


    /**
     * <p>Create a DAML Datatype representing values from some concrete domain.</p>
     *
     * @param uri The URI that is both the URI of this datatype value, and the identifier
     *             of the concrete domain type (e.g. as an XSD datatype).
     * @return A new DAMLDatatype object.
     */
    public DAMLDatatype createDAMLDatatype( String uri );


    /**
     * <p>Create a new DAML value that is a member of the given class.  The appropriate
     * {@link DAMLCommon} sub-class will be instantiated, so, for example, if the <code>damlClass</code>
     * is <code>daml:Restriction</code>, a {@link DAMLRestriction}
     * object will be returned.  Note that if a URI is given, and a value with that
     * URI already exists in the model, that instance will be returned instead of
     * creating a new DAML value. This is necessary to maintain consistency of the model.</p>
     *
     * @param uri The URI of the new DAML value, or null for an anonymous value
     * @param damlClass The class to which the new DAML value will belong
     * @return An instance of a DAMLCommon value that corresponds to the given class.
     */
    public DAMLCommon createDAMLValue( String uri, Resource damlClass );


    /**
     * <p>Answer the DAML value that corresponds to the given URI, if it exists in the
     * model.  If the URI does not match any of the resources presently in the model,
     * null is returned.</p>
     *
     * @param uri The URI of the DAML resource to look for.
     * @return An existing DAML resource from the model, matching uri, or null if
     *         no such resource is found.
     */
    public DAMLCommon getDAMLValue( String uri );


    /**
     * <p>Answer the DAML value that corresponds to the given URI, if it exists in the
     * model.  If the URI does not match any of the resources presently in the model,
     * create a new DAML resource with the given URI and vocabulary, from the given
     * DAML class.</p>
     *
     * @param uri The URI of the DAML resource to look for.
     * @param damlClass The class of the new resource to create if no existing resource
     *                  is found.
     * @return An existing DAML resource from the model, matching uri, or a new
     *         resource if no existing resource is found.
     */
    public DAMLCommon getDAMLValue( String uri, DAMLClass damlClass );

    /**
     * <p>Answer a resource from the current model with the given uri, viewed as a DAML Class.</p>
     * @param uri The uri of the resource to fetch
     * @return The class resource with the given URI, or null
     */
    public DAMLClass getDAMLClass( String uri );
    
    /**
     * <p>Answer a resource from the current model with the given uri, viewed as a DAML Property.</p>
     * @param uri The uri of the resource to fetch
     * @return The property resource with the given URI, or null
     */
    public DAMLProperty getDAMLProperty( String uri );
    
    /**
     * <p>Answer a resource from the current model with the given uri, viewed as a DAML Instance.</p>
     * @param uri The uri of the resource to fetch
     * @return The instance resource with the given URI, or null
     */
    public DAMLInstance getDAMLInstance( String uri );
    
    /**
     * <p>Answer an iterator over all DAML classes that are presently in the model.</p>
     *
     * @return An iterator over all currently defined classes (including Restrictions).
     */
    public ExtendedIterator listDAMLClasses();


    /**
     * <p>Answer an iterator over all DAML properties that are presently in the model.</p>
     *
     * @return An iterator over all currently defined properties (i.e. rdf:Property and
     *         all sub-classes).
     */
    public ExtendedIterator listDAMLProperties();


    /**
     * <p>Answer an iterator over all DAML instances that are presently in the model.</p>
     *
     * @return An iterator over all currently defined DAML instances.
     */
    public ExtendedIterator listDAMLInstances();


    /**
     * <p>Answer a reference to the loader for this DAML model</p>
     *
     * @return a DAMLLoader reference
     */
    public DAMLLoader getLoader();


    /**
     * </p>Answer true if the most recent load operation was successful. If not,
     * consult {@link DAMLLoader#getStatus} for details, and check error log.</p>
     *
     * @return True if the most recent model load was successful
     */
    public boolean getLoadSuccessful();


    /**
     * <p>Answer a reference to the XML datatype registry for this model, that can be used to
     * map between XML data marked up using XML Schema data descriptions, and Java objects.
     * This method has changed since Jena1, and now uses the much more clearly defined mechanism
     * for datatypes that has been specified for RDF.  This updated specification is represented
     * in Jena2 via the <code>com.hp.hpl.jena.datatypes</code> package.
     * </p>
     * <p>
     * Note that the type mapper returned is the shared, global singleton instance of the type mapper.
     * </p>
     *
     * @return An XML datatype mapper
     */
    public TypeMapper getDatatypeRegistry();


    /**
     * <p>Read the ontology indicated by the given uri.  Note that, depending on the settings in the
     * embedded {@link DAMLLoader}, ontology import statements embedded in this document will be
     * processed and the ontologies fetched and loaded.</p>
     *
     * @param uri The URI identifying an ontology to be added.
     * @param base The base URI for any relative names that are loaded from the source document
     * @param lang Denotes the language the statements are represented in.
     * @return self.
     * @see Model#read( String, String )
     */
    public Model read( String uri, String base, String lang );


    /**
     * <p>Flag to control whether accessing the DAML store will take into account equivalence classes for
     * properties and resources, using <code>daml:equivalentTo</code> and similar
     * statements.  In Jena 2, equivalence processing is delegated to the inference
     * engine that is used to wrap the graph.  Therefore, setting a flag at this API level
     * is not useful, and this method is therefore deprecated.</p>
     *
     * @param useEquivalence If true, accessing properties and resources will check for
     *                       equivalent values, at a cost of reduced performance.
     * @deprecated Not useful in Jena2, since equivalence processing is handled by the inference graph.
     */
    public void setUseEquivalence( boolean useEquivalence );


    /**
     * <p>Answer true if the model will consider equivalence classes when accessing
     * properties and resources.  See {@link #setUseEquivalence} for details.
     * In Jena 2, equivalence processing is delegated to the inference
     * engine that is used to wrap the graph.  Therefore, setting a flag at this API level
     * is not useful, and this method is therefore deprecated.</p>
     *
     * @return True if equivalence classes are being considered.
     * @deprecated Not useful in Jena2, since equivalence processing is handled by the inference graph.
     */
    public boolean getUseEquivalence();
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
