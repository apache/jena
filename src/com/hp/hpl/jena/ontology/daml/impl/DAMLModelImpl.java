/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLModelImpl.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-06-18 21:56:07 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import java.net.*;
import java.io.*;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.vocabulary.*;



/**
 * <p>
 * Implementation for the DAML model interface, which is a specialisation of a Jena
 * RDF store for the application of storing and manipulating DAML objects.  The
 * specialisations include storing a set of DAML wrapper objects that provide a
 * convenience interface to the underlying RDF statements, and providing a set of
 * indexes for efficiently retrieving these objects.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLModelImpl.java,v 1.9 2003-06-18 21:56:07 ian_dickinson Exp $
 */
public class DAMLModelImpl
    extends OntModelImpl
    implements DAMLModel
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /** Encodes which Java class to instantiate for a given DAML class */
    protected static Object[][] DAML_CLASS_TABLE = new Object[][] {
        // DAML class instance                   Corresponding java class
        { DAML_OIL.Class,                        DAMLClassImpl.class },
        { RDFS.Class,                            DAMLClassImpl.class },

        { DAML_OIL.Restriction,                  DAMLRestrictionImpl.class },

        { DAML_OIL.List,                         DAMLListImpl.class },

        { DAML_OIL.Ontology,                     DAMLOntologyImpl.class },

        { DAML_OIL.Property,                     DAMLPropertyImpl.class },
        { RDF.Property,                          DAMLPropertyImpl.class },

        { DAML_OIL.DatatypeProperty,             DAMLDatatypePropertyImpl.class },
        { DAML_OIL.ObjectProperty,               DAMLObjectPropertyImpl.class },

        { DAML_OIL.UniqueProperty,               DAMLPropertyImpl.class },
        { DAML_OIL.TransitiveProperty,           DAMLObjectPropertyImpl.class },
        { DAML_OIL.UnambiguousProperty,          DAMLObjectPropertyImpl.class }
    };


    // Instance variables
    //////////////////////////////////

    /** The loader that will load DAML source documents for this store */
    private DAMLLoader m_loader = new DAMLLoader( this );



    // Constructors
    //////////////////////////////////

    /**
     * Constructor, initialises internal data structures.
     */
    public DAMLModelImpl( OntModelSpec spec, Model m ) {
        super( spec, m );

        // create well-known values
        initStore();
    }



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
    public DAMLOntology createDAMLOntology( String uri ) {
        return (DAMLOntology) createOntResource( DAMLOntology.class, getProfile().ONTOLOGY(), uri );
    }


    /**
     * <p>Create an (optionally anonymous) instance of the given class.</p>
     *
     * @param damlClass The class of the newly created DAMLInstance
     * @param uri The URI for the new instance, or null to create an anonymous instance.
     * @return A new DAMLInstance object.
     */
    public DAMLInstance createDAMLInstance( DAMLClass damlClass, String uri ) {
        return (DAMLInstance) createOntResource( DAMLInstance.class, damlClass, uri );
    }


    /**
     * <p>Create an (optionally anonymous) DAML class.</p>
     *
     * @param uri The URI for the new class, or null to create an anonymous class.
     * @return A new DAMLClass object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLClass createDAMLClass( String uri ) {
        return (DAMLClass) createOntResource( DAMLClass.class, getProfile().CLASS(), uri );
    }


    /**
     * <p>Create a DAML property. Note that it is recommended
     * to use one of the more specific property classes from the new DAML release:
     * see {@link #createDAMLObjectProperty} or {@link #createDAMLDatatypeProperty}.</p>
     *
     * @param uri The URI for the new property. May not be null.
     * @return A new DAMLProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLProperty createDAMLProperty( String uri ) {
        return (DAMLProperty) createOntResource( DAMLProperty.class, getProfile().PROPERTY(), uri );
    }


    /**
     * <p>Create a DAML object property. An object property has ontology individuals
     * (instances) in its range, whereas a datatype property has concrete data literals
     * in the range.</p>
     *
     * @param uri The URI for the new object property. May not be null.
     * @return A new <code>DAMLObjectProperty</code> object.
     */
    public DAMLObjectProperty createDAMLObjectProperty( String uri ) {
        return (DAMLObjectProperty) createOntResource( DAMLObjectProperty.class, getProfile().OBJECT_PROPERTY(), uri );
    }


    /**
     * <p>Create an (optionally anonymous) DAML datatype property. A datatype property has 
     * concrete data literals
     * in its range, whereas an object property has ontology individuals (instances)  
     * in the range.</p>
     *
     * @param uri The URI for the new datatype property. May not be null.
     * @return A new DAMLDatatypeProperty object.
     */
    public DAMLDatatypeProperty createDAMLDatatypeProperty( String uri ) {
        return (DAMLDatatypeProperty) createOntResource( DAMLDatatypeProperty.class, getProfile().DATATYPE_PROPERTY(), uri );
    }

    /**
     * <p>Create an empty DAML list.</p>
     *
     * @return A new empty DAMLList.
     */
    public DAMLList createDAMLList() {
        return (DAMLList) getResource( DAML_OIL.nil.getURI() ).as( DAMLList.class );
    }


    /**
     * <p>Create a new DAML list containing the given elements.</p>
     *
     * @param elements An iterator over the elements to be added to the list
     * @return A new empty DAMLList.
     */
    public DAMLList createDAMLList( Iterator elements ) {
        DAMLList l = createDAMLList();
        if (elements.hasNext()) {
            // put the first element on the list
            RDFNode n = (RDFNode) elements.next();
            l = (DAMLList) l.cons( n );
            
            // now add the remaining elements to the end of the list
            while (elements.hasNext()) {
                l.add( (RDFNode) elements.next() );
            }
        }
        
        return l;
    }


    /**
     * <p>Create a new DAML list containing the given elements.</p>
     *
     * @param elements An array of RDFNodes that will be the elements of the list
     * @return A new empty DAMLList.
     */
    public DAMLList createDAMLList( RDFNode[] elements ) {
        return createDAMLList( Arrays.asList( elements ).iterator() );
    }


    /**
     * <p>Create an (optionally anonymous) DAML Restriction.</p>
     *
     * @param uri The URI for the new restriction, or null to create
     *            an anonymous restriction.
     * @return A new DAMLRestriction object.
     */
    public DAMLRestriction createDAMLRestriction( String uri ) {
        return (DAMLRestriction) createOntResource( DAMLRestriction.class, getProfile().RESTRICTION(), uri );
    }


    /**
     * <p>Create a new DAML value that is a member of the given class.  The appropriate
     * {@link DAMLCommon} sub-class will be instantiated, so, for example, if the <code>damlClass</code>
     * is {@link DAML_OIL#Restriction}, a {@link DAMLRestriction}
     * object will be returned.  Note that if a URI is given, and a value with that
     * URI already exists in the model, that instance will be returned instead of
     * creating a new DAML value. This is necessary to maintain consistency of the model.</p>
     *
     * @param uri The URI of the new DAML value, or null for an anonymous value
     * @param damlClass The class to which the new DAML value will belong
     * @return An instance of a DAMLCommon value that corresponds to the given class.
     */
    public DAMLCommon createDAMLValue( String uri, Resource damlClass ) {
        Class javaClass = DAMLInstance.class; 
        
        // see if we can match the DAML class to a known type
        for (int i = 0;  i < DAML_CLASS_TABLE.length;  i++) {
            if (DAML_CLASS_TABLE[i][0].equals( damlClass )) {
                javaClass = (Class) DAML_CLASS_TABLE[i][1];
                break;
            }
        }

        return (DAMLCommon) createOntResource( javaClass, damlClass, uri );
    }


    /**
     * <p>Answer the DAML value that corresponds to the given URI, if it exists in the
     * model.  If the URI does not match any of the resources presently in the model,
     * null is returned.</p>
     *
     * @param uri The URI of the DAML resource to look for.
     * @return An existing DAML resource from the model, matching uri, or null if
     *         no such resource is found.
     */
    public DAMLCommon getDAMLValue( String uri ) {
        // work-around a strange design choice in Model.getResource(), which will create a new resource
        // if one is not found in the model
        return (containsResource( uri )) ? (DAMLCommon) getResource( uri ).as( DAMLCommon.class ) : null;
    }


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
    public DAMLCommon getDAMLValue( String uri, DAMLClass damlClass ) {
        DAMLCommon res = getDAMLValue( uri );

        return (res == null  &&  damlClass != null) ? createDAMLValue( uri, damlClass ) : res;
    }


    /**
     * <p>Answer an iterator over all DAML classes that are presently in the model.</p>
     *
     * @return An iterator over all currently defined classes (including Restrictions).
     */
    public ExtendedIterator listDAMLClasses() {
        return new UniqueExtendedIterator( findByTypeAs( getProfile().CLASS(), null, DAMLClass.class ) );
    }


    /**
     * <p>Answer an iterator over all DAML properties that are presently in the model.</p>
     *
     * @return An iterator over all currently defined properties (i.e. rdf:Property and
     *         all sub-classes).
     */
    public ExtendedIterator listDAMLProperties() {
        return new UniqueExtendedIterator( findByTypeAs( getProfile().PROPERTY(), null, DAMLProperty.class ) );
    }


    /**
     * <p>Answer an iterator over all DAML instances that are presently in the model.</p>
     *
     * @return An iterator over all currently defined DAML instances.
     */
    public ExtendedIterator listDAMLInstances() {
        return new UniqueExtendedIterator(
             ((ExtendedIterator) listIndividuals()).mapWith( 
                    new Map1() {public Object map1(Object x){ return ((Resource) x).as( DAMLInstance.class );} } 
                ) );
    }


    /**
     * <p>Answer a resource from the current model with the given uri, viewed as a DAML Class.</p>
     * @param uri The uri of the resource to fetch
     * @return The class resource with the given URI, or null
     */
    public DAMLClass getDAMLClass( String uri ) {
        return containsResource( uri ) ? (DAMLClass) getResource( uri ).as( DAMLClass.class ) : null;
    }
    
    /**
     * <p>Answer a resource from the current model with the given uri, viewed as a DAML Property.</p>
     * @param uri The uri of the resource to fetch
     * @return The property resource with the given URI, or null
     */
    public DAMLProperty getDAMLProperty( String uri ) {
        return containsResource( uri ) ? (DAMLProperty) getResource( uri ).as( DAMLProperty.class ) : null;
    }
    
    /**
     * <p>Answer a resource from the current model with the given uri, viewed as a DAML Instance.</p>
     * @param uri The uri of the resource to fetch
     * @return The instance resource with the given URI, or null
     */
    public DAMLInstance getDAMLInstance( String uri ) {
        return containsResource( uri ) ? (DAMLInstance) getResource( uri ).as( DAMLInstance.class ) : null;
    }
    

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
    public Model read( String uri, String base, String lang ) {
        try {
            URL url = new URL( uri );
            return read( url.openStream(), base, lang );
        }
        catch (IOException e) {
            throw new OntologyException( "I/O error while reading from uri " + uri );
        }
    }


    /**
     * <p>Answer a reference to the loader for this DAML model</p>
     *
     * @return a DAMLLoader reference
     */
    public DAMLLoader getLoader() {
        return m_loader;
    }


    /**
     * </p>Answer true if the most recent load operation was successful. If not,
     * consult {@link DAMLLoader#getStatus} for details, and check error log.</p>
     *
     * @return True if the most recent model load was successful
     */
    public boolean getLoadSuccessful() {
        return getLoader().getStatus() == DAMLLoader.STATUS_OK;
    }


    /**
     * <p>Answer a reference to the XML datatype registry for this model, that can be used to
     * map between XML data marked up using XML Schema data descriptions, and Java objects.
     * This method has changed since Jena1, and now uses the much more clearly defined mechanism
     * for datatypes that has been specified for RDF.  This updated specification is represented
     * in Jena2 via the <code>com.hp.hpl.jena.datatypes</code> package.
     * </p>
     *
     * @return An XML datatype mapper
     */
    public TypeMapper getDatatypeRegistry() {
        return TypeMapper.getInstance();
    }


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
    public void setUseEquivalence( boolean useEquivalence ) {
    }


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
    public boolean getUseEquivalence() {
        return true;
    }




    // Internal implementation methods
    //////////////////////////////////

    /**
     * Initialise the store with well-known values.
     */
    protected void initStore() {
    }


    /**
     * Answer true if the model contains the given resource.
     *
     * @param uri The string URI of the resource to test
     * @return True if the resource appears in any subject, predicate or object position in the model.
     */
    protected boolean containsResource( String uri ) {
        // first try as a subject
        Resource r = getResource( uri );
        StmtIterator i0 = listStatements( r, null, (RDFNode) null );
        if (i0.hasNext()) {
            i0.close();
            return true;
        }
        else {
            i0.close();
        }

        // now as object
        StmtIterator i1 = listStatements( null, null, r );
        if (i1.hasNext()) {
            i1.close();
            return true;
        }
        else {
            i1.close();
        }

        // now as predicate: note that this URI may not be a valid predicate URI
        Property p = getProperty( uri );
        StmtIterator p0 = listStatements( null, p, (RDFNode) null );
        if (p0.hasNext()) {
            p0.close();
            return true;
        }
        else {
            p0.close();
        }

        // not in the model
        return false;
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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
