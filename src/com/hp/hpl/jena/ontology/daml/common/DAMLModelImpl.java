/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLModelImpl.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-02-20 17:11:57 $
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
package com.hp.hpl.jena.ontology.daml.common;


// Imports
///////////////
import java.io.Reader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.SimpleSelector;

import com.hp.hpl.jena.mem.ModelMem;

import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.OneToManyMap;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLCommon;
import com.hp.hpl.jena.ontology.daml.DAMLClass;
import com.hp.hpl.jena.ontology.daml.DAMLObjectProperty;
import com.hp.hpl.jena.ontology.daml.DAMLInstance;
import com.hp.hpl.jena.ontology.daml.DAMLOntology;
import com.hp.hpl.jena.ontology.daml.DAMLProperty;
import com.hp.hpl.jena.ontology.daml.DAMLDatatypeProperty;
import com.hp.hpl.jena.ontology.daml.DAMLDatatype;
import com.hp.hpl.jena.ontology.daml.DAMLList;
import com.hp.hpl.jena.ontology.daml.DAMLRestriction;

import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAML_OIL_2000_12;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;



/**
 * <p>
 * Implementation for the DAML model interface, which is a specialisation of a Jena
 * RDF store for the application of storing and manipulating DAML objects.  The
 * specialisations include storing a set of DAML wrapper objects that provide a
 * convenience interface to the underlying RDF statements, and providing a set of
 * indexes for efficiently retrieving these objects.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLModelImpl.java,v 1.5 2003-02-20 17:11:57 ian_dickinson Exp $
 */
public class DAMLModelImpl
    extends ModelMem
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
        { DAML_OIL_2000_12.Class,                DAMLClassImpl.class },
        { RDFS.Class,                            DAMLClassImpl.class },

        { DAML_OIL_2000_12.Disjoint,             DAMLDisjointImpl.class },

        { DAML_OIL.Restriction,                  DAMLRestrictionImpl.class },
        { DAML_OIL_2000_12.Restriction,          DAMLRestrictionImpl.class },

        { DAML_OIL.List,                         DAMLListImpl.class },
        { DAML_OIL_2000_12.List,                 DAMLListImpl.class },

        { DAML_OIL.Ontology,                     DAMLOntologyImpl.class },
        { DAML_OIL_2000_12.Ontology,             DAMLOntologyImpl.class },

        { DAML_OIL.Property,                     DAMLPropertyImpl.class },
        { DAML_OIL_2000_12.Property,             DAMLPropertyImpl.class },
        { RDF.Property,                          DAMLPropertyImpl.class },

        { DAML_OIL.DatatypeProperty,             DAMLDatatypePropertyImpl.class },
        { DAML_OIL.ObjectProperty,               DAMLObjectPropertyImpl.class },

        { DAML_OIL_2000_12.UniqueProperty,       DAMLPropertyImpl.class },
        { DAML_OIL_2000_12.TransitiveProperty,   DAMLPropertyImpl.class },
        { DAML_OIL_2000_12.UnambiguousProperty,  DAMLPropertyImpl.class },

        { DAML_OIL.UniqueProperty,               DAMLPropertyImpl.class },
        { DAML_OIL.TransitiveProperty,           DAMLObjectPropertyImpl.class },
        { DAML_OIL.UnambiguousProperty,          DAMLObjectPropertyImpl.class }
    };


    // Instance variables
    //////////////////////////////////

    /** The loader that will load DAML source documents for this store */
    private DAMLLoader m_loader = new DAMLLoader( this );

    /*
     * The following looks complex, because it is rather.  Here's a summary: references to all of the
     * DAML ontology wrapper objects we create are stored in m_stores.  Each entry in the array is
     * an ArrayList corresponding to the objects of type, say, DAMLClass.  This allows us to iterate
     * over DAML classes as a set. m_nameIndex and m_namespaceIndex map URI components to values
     * (1 to many), and m_uriIndex maps URI to value (1 to 1).
     */

    /** The set of indexes to different daml values */
    protected Hashtable m_indexes = new Hashtable();

    /** An index of values by type and name only (i&#046;e&#046; collecting namespace entries together) */
    protected OneToManyMap m_nameIndex = new OneToManyMap();

    /** An index of classes by namespace and type only (i&#046;e&#046; collecting named entries together) */
    protected OneToManyMap m_namespaceIndex = new OneToManyMap();

    /** A registry for mapping XML data to Java types */
    protected XMLDatatypeRegistry m_xmlDatatypeRegistry = new XMLDatatypeRegistry();

    /** Flag to control whether we use equivalence classes or not. Default true */
    protected boolean m_useEquivalence = true;



    // Constructors
    //////////////////////////////////

    /**
     * Constructor, initialises internal data structures.
     */
    public DAMLModelImpl() {
        // create well-known values
        initStore();
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Create an (optionally anonymous) Ontology (big-'O') element,
     * which holds meta-information for the ontology (small-'o').
     * <b>N.B.</b> This does not create a new
     * ontology, it simply makes an entry in the current model.
     *
     *
     * @param uri The URI for the new Ontology, or null to create an anonymous
     *            Ontology. Ideally provide the URL in which the Ontology is
     *            stored.
     *            Conventionally, in the RDF/XML serialization
     *            <pre>
     *             &lt;daml:Ontology rdf:about=""&gt;
     *            </pre>
     *            The empty URIref in the above RDF/XML is known as a
     *            <Q>same document reference</Q> and expands to the
     *            URL of the current file.
     *
     * @return A new DAMLOntology object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLOntology createDAMLOntology( String uri ) {
        return (DAMLOntology) index( new DAMLOntologyImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }


    /**
     * Create an (optionally anonymous) instance of the given class.
     *
     * @param damlClass The class of the newly created DAMLInstance
     * @param uri The URI for the new instance, or null to create an anonymous instance.
     * @return A new DAMLInstance object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLInstance createDAMLInstance( DAMLClass damlClass, String uri ) {
        DAMLInstance instance = new DAMLInstanceImpl( uri, this, VocabularyManager.getDefaultVocabulary() );
        instance.setRDFType( damlClass );
        return (DAMLInstance) index( instance );
    }


    /**
     * Create an (optionally anonymous) DAML class.
     *
     * @param uri The URI for the new class, or null to create an anonymous class.
     * @return A new DAMLClass object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLClass createDAMLClass( String uri ) {
        return (DAMLClass) index( new DAMLClassImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }


    /**
     * Create an (optionally anonymous) DAML property. Note that it is recommended
     * to use one of the more specific property classes from the new DAML release:
     * see {@link #createDAMLObjectProperty} or {@link #createDAMLDatatypeProperty}.
     *
     * @param uri The URI for the new property, or null to create an anonymous property.
     * @return A new DAMLProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLProperty createDAMLProperty( String uri ) {
        return (DAMLProperty) index( new DAMLPropertyImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }


    /**
     * Create an (optionally anonymous) DAML object property.
     *
     * @param uri The URI for the new object property, or null to create an
     *            anonymous object property.
     * @return A new DAMLObjectProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLObjectProperty createDAMLObjectProperty( String uri ) {
        return (DAMLObjectProperty) index( new DAMLObjectPropertyImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }


    /**
     * Create an (optionally anonymous) DAML datatype property.
     *
     * @param uri The URI for the new datatype property, or null to create
     *            an anonymous datatype property.
     * @return A new DAMLDatatypeProperty object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLDatatypeProperty createDAMLDatatypeProperty( String uri ) {
        return (DAMLDatatypeProperty) index( new DAMLDatatypePropertyImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }

    /**
     * Create an (optionally anonymous) DAML datatype.
     *
     * @param uri The URI for the new datatype, or null to create
     *            an anonymous datatype.
     * @return A new DAMLDatatype object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLDatatype createDAMLDatatype( String uri ) {
        return (DAMLDatatype) index( new DAMLDatatypeImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }

    /**
     * Create an (optionally anonymous) DAML list.
     *
     * @param uri The URI for the new list, or null to create
     *            an anonymous list.
     * @return A new DAMLList object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLList createDAMLList( String uri ) {
        return (DAMLList) index( new DAMLListImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }


    /**
     * Create an (optionally anonymous) DAML Restriction.
     *
     * @param uri The URI for the new restriction, or null to create
     *            an anonymous restriction.
     * @return A new DAMLRestriction object, which is created by adding the
     *         appropriate statements to the RDF model.
     */
    public DAMLRestriction createDAMLRestriction( String uri ) {
        return (DAMLRestriction) index( new DAMLRestrictionImpl( uri, this, VocabularyManager.getDefaultVocabulary() ) );
    }


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
     * @param vocabulary The vocabulary to use for the new value, or null to use
     *                   the default vocabulary.
     * @return an instance of a DAMLCommon value that corresponds to the given class.
     */
    public DAMLCommon createDAMLValue( String uri, Resource damlClass, DAMLVocabulary vocabulary ) {
        boolean found = false;
        DAMLCommon instance = null;

        // see if we can match the DAML class to a known type
        for (int i = 0;  !found  &&  i < DAML_CLASS_TABLE.length;  i++) {
            Resource dClass = (Resource) DAML_CLASS_TABLE[i][0];
            Class jClass = (Class) DAML_CLASS_TABLE[i][1];

            if (dClass.equals( damlClass )) {
                // found a match
                found = true;

                // make the instance
                try {
                    // get the constructor we want to invoke and make a new instance
                    instance = (DAMLCommon) jClass.getDeclaredConstructor( DAMLLoader.s_constructSig )
                                                  .newInstance( new Object[] {uri, this, VocabularyManager.getVocabulary( damlClass.getURI() )} );
                }
                catch (Exception e) {
                    Log.debug( "Failed to construct DAML value " + jClass.getName(), e );
                    throw new RuntimeException( "Unexpected error while constructing DAML value from RDF model: " + e );
                }
            }
        }

        // if we get here without finding a match, assume it's an instance
        if (!found  &&  (damlClass instanceof DAMLClass)) {
            instance = new DAMLInstanceImpl( uri, this, VocabularyManager.getVocabulary( damlClass.getURI() ) );
        }

        // ensure that we record the type of the new value in the model
        try {
            if (instance != null) {
                add( instance, RDF.type, damlClass );

                // index the new value
                index( instance );
            }
        }
        catch (RDFException e) {
            Log.severe( "RDFException while adding statment to model: " + e, e );
        }

        return instance;
    }


    /**
     * Answer the DAML value that corresponds to the given URI, if it exists in the
     * model.  If the URI does not match any of the resources presently in the model,
     * null is returned.
     *
     * @param uri The URI of the DAML resource to look for.
     * @return An existing DAML resource from the model, matching uri, or null if
     *         no such resource is found.
     */
    public DAMLCommon getDAMLValue( String uri ) {
        Resource res = null;

        // work-around a bug in Model.getResource(), which will create a new resource
        // if one is not found in the model
        if (containsResource( uri )) {
            try {
                res = getResource( uri );
            }
            catch (RDFException e) {
                Log.severe( "RFD exception while getting resource: " + e, e );
            }

            // check if the resource corresponds to a DAML value
            if (res != null  &&  res instanceof com.hp.hpl.jena.ontology.daml.DAMLCommon) {
                return (DAMLCommon) res;
            }
        }

        return null;
    }


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
    public DAMLCommon getDAMLValue( String uri, DAMLClass damlClass, DAMLVocabulary vocabulary ) {
        DAMLCommon res = getDAMLValue( uri );

        if (res == null  &&  damlClass != null) {
            return createDAMLValue( uri, damlClass, vocabulary );
        }
        else {
            return res;
        }
    }


    /**
     * Answer an iterator over all DAML classes that are presently in the model.
     *
     * @return an iterator over all currently defined classes (including Restrictions).
     */
    public Iterator listDAMLClasses() {
        return getIndex( DAML_OIL.Class.getURI() ).iterator();
    }


    /**
     * Answer an iterator over all DAML properties that are presently in the model.
     *
     * @return an iterator over all currently defined properties (i&#046;e&#046; rdf:Property and
     *         all sub-classes).
     */
    public Iterator listDAMLProperties() {
        return getIndex( DAML_OIL.Property.getURI() ).iterator();
    }


    /**
     * Answer an iterator over all DAML instances that are presently in the model.
     *
     * @return an iterator over all currently defined DAML instances.
     */
    public Iterator listDAMLInstances() {
        return getIndex( DAML_OIL.Thing.getURI() ).iterator();
    }


    /**
     * Read the ontology indicated by the given uri.  Note that, depending on the settings in the
     * embedded {@link DAMLLoader}, ontology import statements embedded in this document will be
     * processed and the ontologies fetched and loaded.
     *
     * @param uri The URI identifying an ontology to be added, which is assumed to be
     *            represented in the XML serialisation of RDF.
     * @return self.
     */
    public Model read( String uri ) {
        return read( uri, uri, null );
    }


    /**
     * Read the ontology indicated by the given uri.  Note that, depending on the settings in the
     * embedded {@link DAMLLoader}, ontology import statements embedded in this document will be
     * processed and the ontologies fetched and loaded.
     *
     * @param uri The URI identifying an ontology to be added, which is assumed to be
     *            represented in the XML serialisation of RDF.
     * @param lang The encoding language of the source document
     * @return self.
     */
    public Model read( String uri, String lang ) {
        return read( uri, uri, lang );
    }


    /**
     * Read the ontology from the given reader, assuming that its base URI is
     * the given URI.  Note that, depending on the settings in the
     * embedded {@link DAMLLoader}, ontology import statements embedded in this document will be
     * processed and the ontologies fetched and loaded.
     *
     * @param in A reader, from which will be read the DAML definitions, which are assumed to be
     *            represented in the XML serialisation of RDF.
     * @param base The base URI for any relative definitions that are loaded.
     * @return self.
     */
    public Model read( Reader in, String base ) {
        return read( in, base, null );
    }



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
    public Model read( String uri, String base, String lang ) {
        // clear the status flag
        getLoader().resetStatus();
        getLoader().read( uri, base, lang );

        return this;
    }


    /**
     * Read the ontology from the given reader, assuming that its base URI is
     * the given URI.  Note that, depending on the settings in the
     * embedded {@link DAMLLoader}, ontology import statements embedded in this document will be
     * processed and the ontologies fetched and loaded.  Use {@link #getLoadSuccessful} to
     * check whether the load operation was successful.
     *
     * @param in A reader, from which will be read the DAML definitions.
     * @param base The base URI for any relative definitions that are loaded.
     * @param lang Denotes the language the statements are represented in.
     * @return self.
     */
    public Model read( Reader in, String base, String lang ) {
        // clear the status flag
        getLoader().resetStatus();

        try {
            // load the descriptions, and place them into the this model
            getLoader().read( in, base, lang );

            // check the loader status, to see if we were successful
            if (!getLoadSuccessful()) {
                Log.info( "Errors reported during model load: check log file for details, status code = " + getLoader().getStatus() );
            }
        }
        catch (RuntimeException e) {
            Log.severe( "Saw runtime exception: " + e, e );
        }

        return this;
    }


    /**
     * <p>
     * Add all of the statements from the given model into this model. This
     * method has been extended over the standard behaviour in {@link Model#add( Model )},
     * in that it will perform the necessary processing to recognise the DAML values
     * that are present as a result of the merge.
     * </p><p>
     * <b>NB</b> this method does not recognise and process <code>daml:imports</code>
     * statements, so only the statements from the given model will be loaded.
     * </p>
     *
     * @param model A model whose statements will be added to this DAMLModel
     * @return This model
     */
    public Model add( Model model ) {
        getLoader().add( model );
        return this;
    }


    /**
     * Answer the DAML Loader that is used by this store to load DAML source
     * documents.  Fine control over loading policy, such as whether imported
     * ontologies are automatically loaded, are controlled by the loader class.
     *
     * @return a reference to this model's loader.
     */
    public DAMLLoader getLoader() {
        return m_loader;
    }


    /**
     * Answer true if the most recent load operation was successful. If not,
     * consult {@link DAMLLoader#getStatus} for details, and check error log.
     *
     * @return true if the most recent model load was successful
     */
    public boolean getLoadSuccessful() {
        return getLoader().getStatus() == DAMLLoader.STATUS_OK;
    }


    /**
     * Answer a reference to the XML datatype registry for this model, that can be used to
     * map between XML data marked up using XML Schema data descriptions, and Java objects.
     * This registry is also used to detect resources in the model that correspond to
     * {@link com.hp.hpl.jena.ontology.daml.DAMLDataInstance} objects, and type declarations that correspond to
     * {@link com.hp.hpl.jena.ontology.daml.DAMLDatatype} objects.
     *
     * @return an XML data translator registry
     */
    public XMLDatatypeRegistry getDatatypeRegistry() {
        return m_xmlDatatypeRegistry;
    }


    /**
     * Add an existing DAML object to the indexes maintained by this store.
     *
     * @param damlObj An object impplementing the DAML common interface.
     * @return Returns the stored object, to allow chained method calls.
     */
    DAMLCommon index( DAMLCommon damlObj ) {
        // get the key for the object
        Object key = ((DAMLCommonImpl) damlObj).getKey();

        // get the index for this key type and add the value
        getIndex( key ).add( damlObj );

        return damlObj;
    }


    /**
     * Remove an existing DAML object from the indexes maintained by this store.
     *
     * @param damlObj An object impplementing the DAML common interface.
     * @return Returns the stored object, to allow chained method calls.
     */
    void unindex( DAMLCommon damlObj ) {
        // get the key for the object
        Object key = ((DAMLCommonImpl) damlObj).getKey();

        // get the index for this key type and add the value
        getIndex( key ).remove( damlObj );
    }


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
    public void setUseEquivalence( boolean useEquivalence ) {
        m_useEquivalence = useEquivalence;
    }


    /**
     * Answer true if the model will consider equivalence classes when accessing
     * properties and resources.  See {@link #setUseEquivalence} for details.
     *
     * @return true if equivalence classes are being considered.
     */
    public boolean getUseEquivalence() {
        return m_useEquivalence;
    }




    // Internal implementation methods
    //////////////////////////////////

    /**
     * Initialise the store with well-known values.
     */
    protected void initStore() {
    }


    /**
     * Answer the index that corresponds to the given key.
     *
     * @param key The key object
     * @return the corresponding
     */
    protected ArrayList getIndex( Object key ) {
        ArrayList index = (ArrayList) m_indexes.get( key );
        if (index == null) {
            index = new ArrayList();
            m_indexes.put( key, index );
        }

        return index;
    }


    /**
     * Answer true if the model contains the given resource.
     * 
     * @param uri The string URI of the resource to test
     * @return True if the resource appears in any subject, predicate or object position in the model.
     */
    protected boolean containsResource( String uri ) {
        try {
            // first try as a subject
            Resource r = getResource( uri );
            StmtIterator i0 = listStatements( new SimpleSelector( r, null, (RDFNode) null ) );
            if (i0.hasNext()) {
                i0.close();
                return true;
            }
            else {
                i0.close();
            }

            // now as object
            StmtIterator i1 = listStatements( new SimpleSelector( null, null, r ) );
            if (i1.hasNext()) {
                i1.close();
                return true;
            }
            else {
                i1.close();
            }

            // now as predicate: note that this URI may not be a valid predicate URI
            // so we ignore any RDFException in this region
            try {
                Property p = getProperty( uri );
                StmtIterator p0 = listStatements( new SimpleSelector( null, p, (RDFNode) null ) );
                if (p0.hasNext()) {
                    p0.close();
                    return true;
                }
                else {
                    p0.close();
                }
            }
            catch (RDFException ignore) {}
        }
        catch (RDFException e) {
            Log.severe( "RDF exception: " + e, e );
        }

        // not in the model
        return false;
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
