/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            10 Jan 2001
 * Filename           $RCSfile: DAMLLoader.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-02-03 22:49:39 $
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
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.HashSet;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLClass;
import com.hp.hpl.jena.ontology.daml.DAMLCommon;

import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAML_OIL_2000_12;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.rdf.model.impl.SelectorImpl;

import com.hp.hpl.jena.mem.ModelMem;




/**
 * Loads DAML ontologies from either input readers or named files. The
 * declarations in the DAML source are loaded into a DAMLModel object.  Will also
 * process embedded Ontology elements, to load imported ontologies. This behaviour
 * can be controlled, by using {@link #setLoadImportedOntologies} to turn the loading
 * of imports on or off overall, or by using
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLLoader.java,v 1.4 2003-02-03 22:49:39 ian_dickinson Exp $
 */
public class DAMLLoader
{
    // Constants
    //////////////////////////////////


    /* Misc constants */

    /** Prefix for a URI to a file */
    public static final String FILE_URI_PREFIX = "file://";

    /* Status flags */

    /** {@link #getStatus Status} flag: OK */
    public static final long STATUS_OK = 0L;

    /** {@link #getStatus Status} flag: input source is not available (e&#046;g&#046; a file cannot be opened) */
    public static final long STATUS_INPUT_UNAVAILABLE = 1L;

    /** {@link #getStatus Status} flag: an I/O error occurred - details will be in log file. */
    public static final long STATUS_IO_ERROR = 2L;

    /** {@link #getStatus Status} flag: a syntax error occurred in the DAML file- details will be in log file. */
    public static final long STATUS_SYNTAX_ERROR = 4L;

    /** {@link #getStatus Status} flag: a misc error occurred - report a bug to the maintainer! */
    public static final long STATUS_MISC_ERROR = 8L;


    // Static variables
    //////////////////////////////////

    /** The signature for the constructor we want to invoke on the DAML values */
    static Class[] s_constructSig = new Class[] {String.class,
                                                 DAMLModel.class,
                                                 DAMLVocabulary.class};


    // Instance variables
    //////////////////////////////////

    /** A status flag we can use to inform callers what just happened */
    private long m_status = STATUS_OK;

    /** A reference to the storage service this class is using */
    private DAMLModel m_damlModel = null;

    /** For efficiency, keep a list of the ontologies that were loaded into this store */
    private Vector m_sources = new Vector();

    /** Flag to control whether imported technologies are loaded automatically */
    private boolean m_loadImportedOntologies = true;

    /** Vector of ontologies that are not automatically fetched when loading containing ontologies */
    private Vector m_importBlockList = new Vector();

    /** Flag used to control the import blocking behaviour */
    private boolean m_useImportBlocking = true;

    /** Dictionary that holds the DAML resources that correspond to loaded RDF resources.
     *  Key = original rdf Resource,  value = corresponding DAMLCommon instance
     */
    private Hashtable m_rdfDamlMap = new Hashtable();

    /** Queue of resources to check for being instances */
    private HashSet m_postCheckResources = new HashSet();

    /** Dispatch table to handle different kinds of resource in the RDF model once parsed */
    private ResourceDispatcher[] m_dispatchTable = {
        new ResourceDispatcher( DAML_OIL.Class,                 DAMLClassImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.Class,         DAMLClassImpl.class ),
        new ResourceDispatcher( RDFS.Class,                     DAMLClassImpl.class ),

        new ResourceDispatcher( DAML_OIL_2000_12.Disjoint,      DAMLDisjointImpl.class ),

        new ResourceDispatcher( DAML_OIL.Restriction,           DAMLRestrictionImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.Restriction,   DAMLRestrictionImpl.class ),

        new ResourceDispatcher( DAML_OIL.List,                  DAMLListImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.List,          DAMLListImpl.class ),

        new ResourceDispatcher( DAML_OIL.Ontology,              DAMLOntologyImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.Ontology,      DAMLOntologyImpl.class ),

        new ResourceDispatcher( DAML_OIL.Property,              DAMLPropertyImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.Property,      DAMLPropertyImpl.class ),
        new ResourceDispatcher( RDF.Property,                   DAMLPropertyImpl.class ),

        new ResourceDispatcher( DAML_OIL.DatatypeProperty,      DAMLDatatypePropertyImpl.class ),
        new ResourceDispatcher( DAML_OIL.ObjectProperty,        DAMLObjectPropertyImpl.class ),

        new ResourceDispatcher( DAML_OIL_2000_12.UniqueProperty, DAMLPropertyImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.TransitiveProperty, DAMLPropertyImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.UnambiguousProperty, DAMLPropertyImpl.class ),

        new ResourceDispatcher( DAML_OIL.UniqueProperty,        DAMLPropertyImpl.class ),
        new ResourceDispatcher( DAML_OIL.TransitiveProperty,    DAMLObjectPropertyImpl.class ),
        new ResourceDispatcher( DAML_OIL.UnambiguousProperty,   DAMLObjectPropertyImpl.class ),

        new ResourceDispatcher( DAML_OIL.Thing,                 DAMLInstanceImpl.class ),
        new ResourceDispatcher( DAML_OIL_2000_12.Thing,         DAMLInstanceImpl.class )
    };


    // Constructors
    //////////////////////////////////

    /**
     * Construct a new DAMLLoader that will load definitions into the given DAML model.
     *
     * @param m The DAML model that will contain the loaded definitions.
     */
    DAMLLoader( DAMLModel m ) {
        m_damlModel = m;

        // initialise the list of blocked imports (for efficiency, nothing more)
        initialiseImportBlockList();
    }


    // External signature methods
    //////////////////////////////////


    /**
     * Answer the DAML model that this loader is using to store the loaded definitions.
     *
     * @return A reference to a knowledge store.
     */
    public DAMLModel getDAMLModel() {
        return m_damlModel;
    }


    /**
     * Read the DAML descriptions from the document at the given URL.
     *
     * @param source The URL of the DAML source document to be read
     * @param base The base URI to use for relative names
     * @param lang The encoding language for the source document, or null for the default.
     */
    void read( URL source, String base, String lang ) {
        InputStream sourceStream = null;

        // remember this source
        addSource( source.toString() );

        try {
            // try to open a reader to to the url
            sourceStream = source.openStream();

            // load the contents of the file
            read( new InputStreamReader( sourceStream ), base, lang );
        }
        catch (IOException e) {
            Log.severe( "IO error while reading URL: " + source, e );
            setStatus( STATUS_IO_ERROR );
        }
        finally {
            if (sourceStream != null) {
                try {
                    sourceStream.close();
                }
                catch (IOException ignore) {}
            }
        }

    }


    /**
     * Read the DAML descriptions from the document at the given URI.
     *
     * @param uri The source URI to be loaded, as a string
     * @param base The base URI to use for relative names
     * @param lang The encoding language for the source document, or null for the default.
     */
    void read( String uri, String base, String lang ) {
        try {
            // load the descriptions, and place them into the this model
            read( new URL( uri ), base, lang );
        }
        catch (MalformedURLException e) {
            Log.severe( "Could not parse URI: " + uri, e );
        }
        catch (RuntimeException e) {
            Log.severe( "Saw runtime exception: " + e, e );
        }
    }



	/**
	 * Read the descriptions from the given input reader, whose
	 * document base URI is as given.
	 *
	 * @param in An input stream for the definitions document
	 * @param baseURI The definition document's base URI (necessary to resolve
	 *            relative names)
	 * @param lang The encoding language for the source document, or null for the default.
	 */
	void read( InputStream in, String baseURI, String lang ) {
		// either this source document is new, or we are reloading anyway
		// first pull the descriptions in to a temporary model
		try {
			Model sourceModel = new ModelMem();

			// collect the RDF sources, including those from imported ontologies
			collectRDFSources( sourceModel, in, baseURI, lang );

			// finally add the statements to the main model, and build the DAML wrapper objects
			processRDFStatements( sourceModel );
		}
		catch (RDFException e) {
			Log.severe( "Error occurred in reading RDF source: " + e, e );
			setStatus( STATUS_SYNTAX_ERROR );
		}
	}


    /**
     * Read the descriptions from the given input reader, whose
     * document base URI is as given.
     *
     * @param reader An input reader for the definitions document
     * @param baseURI The definition document's base URI (necessary to resolve
     *            relative names)
     * @param lang The encoding language for the source document, or null for the default.
     */
    void read( Reader reader, String baseURI, String lang ) {
        // either this source document is new, or we are reloading anyway
        // first pull the descriptions in to a temporary model
        try {
            Model sourceModel = new ModelMem();

            // collect the RDF sources, including those from imported ontologies
            collectRDFSources( sourceModel, reader, baseURI, lang );

            // finally add the statements to the main model, and build the DAML wrapper objects
            processRDFStatements( sourceModel );
        }
        catch (RDFException e) {
            Log.severe( "Error occurred in reading RDF source: " + e, e );
            setStatus( STATUS_SYNTAX_ERROR );
        }
    }


    /**
     * Add the statements from the given external model into the model
     * this loader is attached to.  NB Does not process daml:imports statements.
     *
     * @param model A source model to load statements from
     */
    void add( Model model ) {
        processRDFStatements( model );
    }


    /**
     * <p>
     * Initialise the list of well-known ontologies that we don't bother
     * to load in we detect an import statement.  Currently, the default value
     * for this list is:
     * <ul>
     *     <li> The DAML 2000/12 release </li>
     *     <li> The DAML 2001/3 release </li>
     *     <li> The RDF Schema 2000/01 release </li>
     * </ul>
     * </p><p>
     * This list can be modified in several ways: use {@link #getImportBlockList}
     * to get reference to the list and add/remove elements as required, override
     * this method in a sub-class, or turn off all automatic importing with
     * {@link #setLoadImportedOntologies}.
     */
    protected void initialiseImportBlockList() {
        m_importBlockList.add( getURIRoot( DAMLVocabulary.NAMESPACE_DAML_2000_12_URI ) );
        m_importBlockList.add( getURIRoot( DAMLVocabulary.NAMESPACE_DAML_2001_03_URI ) );
        m_importBlockList.add( getURIRoot( RDFS.getURI() ) );
    }


    /**
     * Answer an iterator over the set of URI strings that <b>will not</b>
     * be loaded if encountered in an import statement, even if autoloading of
     * ontologies (see {@link #getLoadImportedOntologies()}) is on.
     *
     * @return iterator over the set of blocked imports, as URI strings.
     */
    public Iterator getImportBlockList() {
        return m_importBlockList.iterator();
    }


    /**
     * Answer true if a given URI is blocked from being imported: that is, it is
     * assumed to be well-known and will not be automatically imported.  Will
     * answer false if import blocking is switched off altogether
     * (see {@link #setUseImportBlocking}).
     *
     * @param uri The uri to be tested
     * @return true if the URI should not be loaded.
     */
    public boolean isBlockedImport( String uri ) {
        return m_useImportBlocking  &&   m_importBlockList.contains( uri );
    }


    /**
     * Add the given URI to the list of ontology urls that will not be loaded
     * if encountered in an imports statement in the loaded ontology.
     *
     * @param uri The URI of the ontology to block from autoloading, as a String.
     */
    public void addImportBlock( String uri ) {
        // maintain the invariant that there is only copy of each entry
        if (!m_importBlockList.contains( uri )) {
            m_importBlockList.add( uri );
        }
    }


    /**
     * Remove the given URI from the list of ontology urls that will not be loaded
     * if encountered in an imports statement in the loaded ontology.  Has no effect
     * if the URI is not in the list.
     *
     * @param uri The URI of the ontology to no longer block from autoloading, as a String.
     */
    public void removeImportBlock( String uri ) {
        m_importBlockList.remove( uri );
    }


    /**
     * Answer true if the loader is to process imported ontologies (except for the
     * ones on the don't load list). Default true.
     *
     * @return True if imported ontologies should be loaded as they are encountered.
     */
    public boolean getLoadImportedOntologies() {
        return m_loadImportedOntologies;
    }


    /**
     * Set the flag to control whether imported ontologies are to be loaded. Default
     * true.
     *
     * @param loadImports If true, ontologies that are included in this one, via
     *                     the &lt;imports&gt; element are loaded as they are discovered.
     */
    public void setLoadImportedOntologies( boolean loadImports ) {
        m_loadImportedOntologies = loadImports;
    }


    /**
     * Set the flag to control whether certain well-known imports are blocked
     * from being automatically loaded if they encountered in an <code>imports</code>
     * statement. Note that automatic loading of all imports can be switched of
     * with {@link #setLoadImportedOntologies}.
     *
     * @param useBlocking If true, well-known URI's will be blocked from being
     *                    autoloaded, even if autoloading is on
     */
    public void setUseImportBlocking( boolean useBlocking ) {
        m_useImportBlocking = useBlocking;
    }


    /**
     * Answer true if well-known URI's will be blocked from being autoloaded.
     *
     * @return True if imports are blocked.
     * @see #setUseImportBlocking
     */
    public boolean getUseImportBlocking() {
        return m_useImportBlocking;
    }


    /**
     * Answer true if the ontology identified by the given URI has been loaded.
     *
     * @param uri The URI of the ontology
     * @return true if the ontology has already been loaded by the knowledge store.
     */
    public boolean isLoadedOntology( String uri ) {
        return (getSourcePosition( uri ) != -1);
    }



    /**
     * Answer the status of the last operation.  Returns a set of status flags
     * (or'ed together) from all operations since the last {@link #resetStatus}.
     *
     * @return The current status, as a set of flags or'ed together into a long.
     */
    public long getStatus() {
        return m_status;
    }


    /**
     * Clear the status flags. Resets the status to {@link #STATUS_OK}. Note that
     * status is cleared automatically at the start of a {@link com.hp.hpl.jena.ontology.daml.DAMLModel#read( java.lang.String ) read}.
     */
    public void resetStatus() {
        setStatus( STATUS_OK );
    }



    // Internal implementation methods
    //////////////////////////////////


    /**
     * Set the status.  The given flag is or'ed to the current status.
     *
     * @param status A status flag, which should be one of the STATUS_xxx flags.
     */
    protected void setStatus( long status ) {
        m_status |= status;
    }


    /**
     * Collect all of the RDF statements for the model to be processed, including
     * the transitive closure of the imported models if appropriate.  Each import
     * should only be processed once.
     */
    protected void collectRDFSources( Model m, String uri, String baseURI, String lang )
        throws RDFException
    {
        // remember this source
        addSource( uri );

        InputStream sourceStream = null;

        try {
            URL source = new URL( uri );

            // try to open a reader to to the url
            sourceStream = source.openStream();

            // load the contents of the file
            collectRDFSources( m,  sourceStream , baseURI, lang );
        }
        catch (IOException e) {
            Log.severe( "IO error while reading URL: " + uri, e );
            setStatus( STATUS_IO_ERROR );
        }
        finally {
            if (sourceStream != null) {
                try {
                    sourceStream.close();
                }
                catch (IOException ignore) {}
            }
        }
    }


	/**
	 * Collect all of the RDF statements for the model to be processed, including
	 * the transitive closure of the imported models if appropriate.  Each import
	 * should only be processed once.
	 */
	protected void collectRDFSources( Model m, InputStream reader, String baseURI, String lang )
		throws RDFException
	{
		// load the input as an RDF document
		if (lang == null) {
		   m.read( reader, baseURI );
		}
		else {
		   m.read( reader, baseURI, lang );
		}

		// we load any included ontologies (so that they are available when processing the
		// statements in this document
		loadImportedOntologies( m, DAML_OIL.Ontology );
		loadImportedOntologies( m, DAML_OIL_2000_12.Ontology );
	}

    /**
     * Collect all of the RDF statements for the model to be processed, including
     * the transitive closure of the imported models if appropriate.  Each import
     * should only be processed once.
     * @deprecated
     */
    protected void collectRDFSources( Model m, Reader reader, String baseURI, String lang )
        throws RDFException
    {
        // load the input as an RDF document
        if (lang == null) {
           m.read( reader, baseURI );
        }
        else {
           m.read( reader, baseURI, lang );
        }

        // we load any included ontologies (so that they are available when processing the
        // statements in this document
        loadImportedOntologies( m, DAML_OIL.Ontology );
        loadImportedOntologies( m, DAML_OIL_2000_12.Ontology );
    }


    /**
     * Load the imported ontologies mentioned by Ontology instances in the given
     * model.
     *
     * @param model An RDF model of the ontology we're loading
     * @param ontologyInstanceType Resource denoting the rdf:type of the Ontology element
     *                             that will contain the import statement
     */
    protected void loadImportedOntologies( Model model, DAMLClass ontologyInstanceType )
        throws RDFException
    {
        // first check that we are loading included ontologies
        if (getLoadImportedOntologies()) {
            // first find if there is an Ontology resource
            ResIterator i0 = model.query( new SelectorImpl( null, RDF.type, ontologyInstanceType ) )
                                  .listSubjects();
            ResIterator i1 = model.query( new SelectorImpl( null, DAML_OIL.type, ontologyInstanceType ) )
                                  .listSubjects();

            // we collect the imported URI's first, then load them ... otherwise we're
            // iterating over a model that is being updated, which is not guaranteed to be safe
            Vector imports = new Vector();
            for (Iterator j = new ConcatenatedIterator( i0, i1 );  j.hasNext();  ) {
                Resource ontologyRes = (Resource) j.next();

                for (NodeIterator i = model.listObjectsOfProperty( ontologyRes, ontologyInstanceType.getVocabulary().imports() ); i.hasNext(); ) {
                    // add the value of each import, as a string, to the collection
                    imports.add( i.nextNode().toString() );
                }
            }

            // now we can import the ontologies mentioned, subject to some checks
            for (Iterator i = imports.iterator();  i.hasNext();  ) {
                String importedURI = (String) i.next();

                // we don't bother loading some well-known URI's for efficiency, nor
                // do we load the same URI twice
                if ((getSourcePosition( importedURI ) < 0)  &&  (!isBlockedImport( importedURI ))) {
                    collectRDFSources( model, importedURI, importedURI, null );
                }
            }
        }
    }


    /**
     * Iterate through all of the top-level resources in the input model and
     * interpret them as DAML objects, and hence make the appropriate assertions
     * in the knowledge store.
     *
     * @param sourceModel The RDF model that contains all of the source RDF statements
     *                    we have loaded from the source document.
     */
    protected void processRDFStatements( Model sourceModel ) {
        try {
            // get the DAML values that correspond to the vanilla RDF resources
            mapRDFValuesToDAML( sourceModel );

            // now replace the actual statements in the main model
            for (StmtIterator i = sourceModel.listStatements();  i.hasNext();  ) {
                // get the statement in vanilla RDF form
                Statement sRDF = i.nextStatement();

                // recreate it with DAML objects
                getDAMLModel().add( (Resource) mapDAMLNode( sRDF.getSubject() ),
                                    (Property) mapDAMLNode( sRDF.getPredicate() ),
                                    mapDAMLNode( sRDF.getObject() ) );
            }
        }
        catch (RDFException e) {
            Log.severe( "RDF exception while processing DAML statements: " + e, e );
            setStatus( STATUS_MISC_ERROR );
        }
    }


    /**
     * For each resource that is the subject of a statement in the loaded model,
     * map it to the corresponding DAML resource and save the mapping in a global
     * dictionary.
     *
     * @param sourceModel The model that contains the RDF statements we are loading.
     */
    protected void mapRDFValuesToDAML( Model sourceModel ) {
        try {
            // iterate through all the known statements so that we can create the DAML shadow classes
            for (StmtIterator statements = sourceModel.listStatements();  statements.hasNext(); ) {
                // get the statement, and extract its components
                Statement s = statements.nextStatement();

                Resource subj = s.getSubject();
                Property prop = s.getPredicate();
                RDFNode obj = s.getObject();

                // now map them to DAML values
                mapRDFResourceToDAML( sourceModel, subj );
                mapRDFResourceToDAML( sourceModel, prop );
                if (obj instanceof com.hp.hpl.jena.rdf.model.Resource) {
                    mapRDFResourceToDAML( sourceModel, (Resource) obj );
                }
            }

            // now check the postponed resources, to see if they correspond to instances
            for (Iterator i = m_postCheckResources.iterator();  i.hasNext();  ) {
                // get the resource and its type
                Resource r = (Resource) i.next();

                // get the type statement for the resource, if it has one
                Resource type = findType( sourceModel, r );

                if (type != null) {
                    // can we match the type to a DAML class?
                    if (isDAMLClass( type )) {
                        // yes - so map the original resource to an instance object
                        m_rdfDamlMap.put( r, ((DAMLModelImpl) getDAMLModel()).index( new DAMLInstanceImpl( r.getURI(), getDAMLModel(), VocabularyManager.getVocabulary( type.getURI() ) ) ) );
                    }
                    else if (isDAMLDatatype( type )) {
                        // the type matches a known datatype, so this is a value from concrete domain
                        m_rdfDamlMap.put( r, ((DAMLModelImpl) getDAMLModel()).index( new DAMLDataInstanceImpl( r.getURI(), getDAMLModel(), VocabularyManager.getVocabulary( r.getURI() ) ) ) );

                        // we also create the Datatype object itself, in place of the type
                        if (!m_rdfDamlMap.containsKey( type )) {
                            m_rdfDamlMap.put( type, new DAMLDatatypeImpl( type.getURI(), getDAMLModel(),
                                                                          VocabularyManager.getVocabulary( type.getURI() )) );
                        }
                    }
                    else {
                        Log.debug( "Failed to match resource <" + r.getURI() +
                                   "> of type <" +
                                   (type == null ? "unknown" : type.getURI()) +
                                   "> in dispatch table" );
                    }
                }
            }
        }
        catch (RDFException e) {
            Log.severe( "RDF exception while processing DAML statements: " + e, e );
            setStatus( STATUS_MISC_ERROR );
        }
    }


    /**
     * For a given resource, attempt to map it to a specific instance of a DAML value
     * if it's typed as a DAML object.  Only do this once for each RDF resource.
     *
     * @param model The model that is the source of the RDF we are processing
     * @param r The RDF resource from the source model.
     */
    protected void mapRDFResourceToDAML( Model sourceModel, Resource r ) {
        // first check if we have seen this resource already
        if (m_rdfDamlMap.containsKey( r )) {
            // been there. done that. got the t-shirt
            return;
        }
        else {
            // now, is this a value we've seen before?
            DAMLCommon damlRes = getDAMLModel().getDAMLValue( r.getURI() );

            if (damlRes != null) {
                // we know the correspondance for this URI, but it's not yet in the map
                // for the DAML document we're processing at present
                m_rdfDamlMap.put( r, damlRes );
            }
            else {
                // otherwise, we try to match it based on its type
                // we assume that all the resources we are interested in have an RDF type
                Resource type = findType( sourceModel, r );

                // only proceed if we have a type
                if (type != null) {
                    boolean dispatched = false;

                    // no existing DAML object, so attempt to match each one in the dispatch table
                    for (int i = 0;  (!dispatched)  &&  (i < m_dispatchTable.length);  i++) {
                        dispatched = m_dispatchTable[i].dispatch( r, type );
                    }

                    // check for failure to handle a resource, and save for later to check for an Instance instance
                    if (!dispatched) {
                        m_postCheckResources.add( r );
                    }
                }
            }
        }
    }


    /**
     * Answer the DAML value that corresponds to the given RDF node, if we
     * have one, otherwise return the original node.
     *
     * @param node An RDF node
     * @return A DAML value corresponding to the node, or otherwise the node itself.
     */
    protected RDFNode mapDAMLNode( RDFNode node ) {
        Resource rDAML = (Resource) m_rdfDamlMap.get( node );
        return (rDAML == null) ? node : rDAML;
    }


    /**
     * Answer true if a resource corresponds to a DAML class, either in the known model or
     * the freshly loaded classes.
     *
     * @param resource The resource to match against
     * @return true if the resource denotes a class
     */
    protected boolean isDAMLClass( Resource resource ) {
        // first try the main model
        DAMLCommon dClass = getDAMLModel().getDAMLValue( resource.getURI() );
        if (dClass != null  &&  dClass instanceof com.hp.hpl.jena.ontology.daml.DAMLClass) {
            return true;
        }

        // otherwise, try the daml classes that have been loaded from the current model
        dClass = (DAMLCommon) m_rdfDamlMap.get( resource );
        return dClass != null  &&  dClass instanceof com.hp.hpl.jena.ontology.daml.DAMLClass;
    }


    /**
     * Answer true if a resource corresponds to a DAML datatype, which we can tell
     * if its type URI is in the type registry for mapping concrete domains.
     *
     * @param resource The resource that may represent a concrete datatype
     */
    protected boolean isDAMLDatatype( Resource resource ) {
        return getDAMLModel().getDatatypeRegistry().isRegisteredType( resource.getURI() );
    }


    /**
     * Answer the position of the given ontology in the list of ontology entries,
     * or -1 if it is not present.
     *
     * @param uri The URI of the ontology we are looking for.
     * @return The position of the ontology in the list, or -1
     */
    protected int getSourcePosition( String uri ) {
        return (m_sources.indexOf( uri ));
    }


    /**
     * Add a source URI to the list of sources we're maintaining.
     *
     * @param uri The URI we have just loaded.
     */
    protected void addSource( String uri ) {
        m_sources.add( uri );
    }



    /**
     * Utility method to remove get the root name of a URI (e.g. everything up to the anchor).
     *
     * @param uri A uri to process
     * @return the root of the uri, up to but not including the anchor char.
     */
    private String getURIRoot( String uri ) {
        // RDF has peculiar rules about separating names from namespaces
        int splitPoint = Util.splitNamespace( uri );
        return (splitPoint < 0) ? uri : uri.substring( 0, splitPoint - 1 );
    }


    /**
     * Answer one of the type statements for this object. Only one type is returned,
     * so if a resource has more than one type the choice will be non-deterministic -
     * subject to the heuristic that we eliminate less specific classes in favour of
     * more specific ones.
     *
     * @param model The RDF model to do the lookup in
     * @param res The resource whose type is required
     * @return a statement with res as subject and rdf:type or one of its aliases
     *         as predicate
     */
    private Resource findType( Model model, Resource res ) {
        HashSet types = new HashSet();

        // first try rdf:type (i.e. the most common occurrence)
        try {
            for (StmtIterator t = res.listProperties( RDF.type );  t.hasNext(); ) {
                RDFNode typeNode = ((Statement) t.nextStatement()).getObject();
                if (typeNode instanceof Resource) {
                    types.add( typeNode );
                }
            }
        }
        catch (RDFException ignore) {}

        // then try aliases to rdf:type, such as daml:type
        for (Iterator i = DAMLHierarchy.getInstance().getEquivalentValues( RDF.type );  i.hasNext();  ) {
            Property typeAlias = (Property) i.next();

            try {
                for (StmtIterator t  = res.listProperties( typeAlias );  t.hasNext(); ) {
                    RDFNode typeNode = ((Statement) t.nextStatement()).getObject();
                    if (typeNode instanceof Resource) {
                        types.add( typeNode );
                    }
                }
            }
            catch (RDFException ignore) {}
        }

        // heuristically, we discard any types that are super-types of others in the set
        HashSet heurTypes = new HashSet();
        for (Iterator i = types.iterator();  i.hasNext();  ) {
            DAMLHierarchy damlHier = DAMLHierarchy.getInstance();

            // get the type object
            Resource type = (Resource) i.next();
            String typeURI = type.getURI();
            boolean discard = false;

            for (Iterator j = types.iterator();  (!discard && j.hasNext()); ) {
                // set a flag if the value we're testing is a known super-class of another type in the set
                discard = damlHier.isDAMLSubClassOf( ((Resource) j.next()).getURI(), typeURI );
            }

            // <ickyHack>
            // there's one nasty case which is worth treating specially, which is a unique
            // datatype property. In this case, we want to keep DatatypeProperty, rather than
            // UniqueProperty, as the type. This is because later on UniqueProperty will get mapped to
            // DAMLProperty, which is a super-class of DatatypeProperty.
            discard = discard ||
                      (type.equals( DAML_OIL.UniqueProperty ) &&
                       types.contains( DAML_OIL.DatatypeProperty ));
            // </ickyHack>

            // only keep more specific classes, so don't bother if it's a super-class
            if (!discard) {
                heurTypes.add( type );
            }
        }

        // now, heurTypes contains the set of types of this resource, with some
        // additional hueristic filtering applied.  We pick a random member of
        // this set to return.
        Iterator i = heurTypes.iterator();
        return (Resource) (i.hasNext() ? i.next() : null);
    }




    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * A dispatcher class for different types of resource that we can handle.
     */
    protected class ResourceDispatcher
    {
        // Instance variables
        /////////////////////

        /** Holds the URI of the type of the node for a default match */
        protected Resource m_typeRes = null;

        /** The class that this dispatcher will construct by default */
        protected Class m_defaultClass = null;


        // Constructors
        /////////////////////

        /**
         * Constructor - accepts the rdfs:type object of the resource to match on for default
         * matches.
         *
         * @param typeRes The Resource denoting the rdfs:type to match.
         * @param cls The class for the default handler to create instances of, or null
         */
        protected ResourceDispatcher( Resource typeRes, Class cls ) {
            m_typeRes = typeRes;
            m_defaultClass = cls;
        }


        /**
         * Constructor - does not initialise a default match.  Method matches() should
         * be overridden to provide specialised match conditions.
         */
        protected ResourceDispatcher() {
        }


        // External signature methods
        /////////////////////////////

        /**
         * Test for a match with the locally stored parameters, and, if a match occurs
         * dispatch to the appropriate handler.
         *
         * @param r The resource to be dispatched
         * @param typeRes The value of the resource's rdfs:type if it has one
         * @return True if this handler was able to dispatch the resource.
         */
        protected boolean dispatch( Resource r, Resource typeRes  ) {
            if (matches( r, typeRes )) {
                // get the DAML object that is equivalent to the given resource
                Resource damlShadow = handleResource( r, getDAMLModel(), typeRes );

                // save it in the global dictionary for later processing
                m_rdfDamlMap.put( r, damlShadow );

                return true;
            }
            else {
                return false;
            }
        }


        /**
         * Default matcher: answer true if the node's type URI is the same as the one
         * cached in this dispatcher object.
         *
         * @param res The resource itself (to allow more sophisticated matches in sub-classes)
         * @param typeRes The resource denoting the node's rdfs:type, or null.
         */
        protected boolean matches( Resource res, Resource typeRes ) {
            if (m_typeRes == null) {
                Log.warning( "Attempting default match in resource dispatcher, but with null type URI!" );
                return false;
            }
            else {
                return m_typeRes.equals( typeRes );
            }
        }


        /**
         * Handler method to consume the resource once it has been matched. Default
         * action is to construct a new instance of the class that was supplied
         * to the constructor of this dispatcher.
         *
         * @param r The resource that is being handled
         * @param m The DAML model that will store the resulting resource
         * @param typeRes The rdf:type of this resource
         * @return The DAML model resource corresponding to the given resource, or null if error.
         */
        protected Resource handleResource( Resource r, DAMLModel m, Resource typeRes ) {
            if (m_defaultClass != null) {
                // default action is to create a DAML object for r and index it
                return ((DAMLModelImpl) m).index( makeInstance( m_defaultClass, r.getURI(), m, typeRes.getURI() ) );
            }
            else {
                Log.warning( "Default dispatcher attempted to create a DAML value with no class available." );
                return null;
            }
        }


        // Internal implementation methods
        ///////////////////////////////////

        /**
         * Generate an instance of the given class, with the given resource
         * as its primary.
         *
         * @param cls The class object for which we want a new instance
         * @param uri The URI of the new object
         * @param model The DAML model that the object is attached to
         * @param typeURI The URI of the rdf:type of this resource
         * @return A new DAMLCommon object
         */
        protected DAMLCommon makeInstance( Class cls, String uri, DAMLModel model, String typeURI ) {
            try {
                // get the constructor we want to invoke and make a new instance
                return (DAMLCommon) cls.getDeclaredConstructor( s_constructSig )
                                       .newInstance( new Object[] {uri, model, VocabularyManager.getVocabulary( typeURI )} );
            }
            catch (Exception e) {
                Log.debug( "Failed to construct DAML value " + cls.getName(), e );
                throw new RuntimeException( "Unexpected error while constructing DAML value from RDF model: " + e );
            }
        }

    } // end inner class ResourceDispatcher


}
