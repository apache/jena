/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OntDocumentManager.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-04 20:37:07 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved. 
 * (see footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import java.util.*;

import org.apache.log4j.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.ontology.impl.OntologyGraph;



/**
 * <p>
 * Provides services for managing ontology documents, including loading imported
 * documents, and locally caching documents from resolvable URL's to improve
 * load performance.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntDocumentManager.java,v 1.5 2003-04-04 20:37:07 ian_dickinson Exp $
 */
public class OntDocumentManager
{
    // Constants
    //////////////////////////////////

    /** The default path for searching for the metadata on locally cached ontologies */ 
    public static final String DEFAULT_METADATA_PATH = "file:etc/ont-policy.rdf;file:ont-policy.rdf";

    /** Delimiter between path entries */
    public static final String PATH_DELIMITER = ";";
    
    /** Namespace for ontology metadata resources and properties */
    public static final String NS = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#";
    

    // Static variables
    //////////////////////////////////

    /** Holds vocabulary terms */
    private static final Model vocab = ModelFactory.createDefaultModel();
    
    /** rdf:type for ontology specification nodes in meta-data file */
    public static final Resource ONTOLOGY_SPEC = vocab.createResource( NS + "OntologySpec" );
    
    /** Represents the public URI of an ontology; also used to derive the namespace */
    public static final Property PUBLIC_URI = vocab.createProperty( NS + "publicURI" );
    
    /** Represents the alternative local copy of the public ontology; assumed to be resolvable, hence URL not URI */
    public static final Property ALT_URL = vocab.createProperty( NS + "altURL" );
    
    /** Represents the standard prefix for this namespace */
    public static final Property PREFIX = vocab.createProperty( NS + "prefix" );
    
    /** Represents the ontology language used to encode the ontology */
    public static final Property LANGUAGE = vocab.createProperty( NS + "language" );
    
    /** rdf:type for document manager policy nodes */
    public static final Resource DOC_MGR_POLICY = vocab.createResource( NS + "DocumentManagerPolicy" );

    /** Defines boolean policy choice of caching loaded models */
    public static final Property CACHE_MODELS = vocab.createProperty( NS, "cacheModels" );
    
    /** Defines boolean policy choice of loading the imports closure */
    public static final Property PROCESS_IMPORTS = vocab.createProperty( NS, "processImports" );
    
    /** Default document manager instance */
    private static OntDocumentManager s_instance = new OntDocumentManager();
    
    
    // Instance variables
    //////////////////////////////////

    /** The search path for metadata */
    protected String m_searchPath = DEFAULT_METADATA_PATH;
    
    /** Mapping from public URI to local cache (typically file) URL's for efficiently loading models */
    protected Map m_altMap = new HashMap();
    
    /** Mapping of public URI's to prefixes */
    protected Map m_prefixMap = new HashMap();
    
    /** Mapping of prefixes to public URI's (inverse of m_prefixMap) */
    protected Map m_uriMap = new HashMap();
    
    /** Mapping of public URI's to loaded models */
    protected Map m_graphMap = new HashMap();
    
    /** Mapping of public public URI's to language resources */
    protected Map m_languageMap = new HashMap();
    
    /** Logger for this class */
    private Logger m_log = Logger.getLogger( getClass() );
    
    /** Flag: cache models as they are loaded */
    protected boolean m_cacheGraphs = true;
    
    /** Flag: process the imports closure */
    protected boolean m_processImports = true;
    
    /** The factory we're using to create graphs in the union */
    protected GraphFactory m_graphFactory = null;
    
    /** Flag to turn on use of inferencing graph for ont document graphs */
    protected boolean m_useInference = false;
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Initialise a document manager by searching the default path for ontology
     * metadata about known ontologies cached locally.
     * </p>
     */
    public OntDocumentManager() {
        this( DEFAULT_METADATA_PATH );
    }


    /**
     * <p>
     * Initialise a document manager by searching the given path for ontology
     * metadata about known ontologies cached locally.
     * </p>
     * 
     * @param path The search path to search for initial metadata, which will
     * also replace the current search path for this document manager.  Use
     * null to prevent loading of any initial ontology metadata.
     */
    public OntDocumentManager( String path ) {
        m_searchPath = (path == null) ? "" : path;
        initialiseMetadata( m_searchPath, false );
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * OntDocumentManager is not a singleton, but a global default instance is available
     * for applications where a single shared document manager is sufficient.
     * </p>
     * 
     * @return The default, global instance of a document manager
     */
    public static OntDocumentManager getInstance() {
        return s_instance;
    }
    
    
    /**
     * <p>
     * Answer the path used to search for the ontology metadata to load. The format is
     * a ';' separated list of URI's. The first URI on the path that is readable is
     * taken to be the location of the local ontology metadata.
     * </p>
     * 
     * @return The ontology metadata search path, as a string.
     */
    public String getMetadataSearchPath() {
        return m_searchPath;
    }


    /**
     * <p>
     * Change the search path for loading ontology metadata to the given path. If
     * <code>replace</code> is true, any existing mappings are removed before the
     * new path is searched.  Otherwise, existing data will only be replaced if
     * it is clobbered by keys loaded from the metadata loaded from the new path.
     * </p>
     * 
     * @param path The new metadata search path (see {@link #getMetadataSearchPath} for format)
     * @param replace If true, clear existing mappings first
     */
    public void setMetadataSearchPath( String path, boolean replace ) {
        m_searchPath = path;
        initialiseMetadata( path, replace );
    }
    
    
    /**
     * <p>
     * Answer an iterator over the ontology documents this document mananger is managing.
     * </p>
     * 
     * @return An Iterator ranging over the public URI strings for the known 
     * (i&#046;e&#046; cached) document models
     */
    public Iterator listDocuments() {
        return m_altMap.keySet().iterator();
    }
    
    
    /**
     * <p>
     * Answer the URL of the alternative copy of the ontology document with the given URI, if known,
     * or the URI unchanged if no alternative is known.
     * </p>
     * 
     * @param uri The ontology document to lookup
     * @return The resolvable location of the alternative copy, if known, or <code>uri</code> otherwise 
     */
    public String getCacheURL( String uri ) {
        String alt = (String) m_altMap.get( uri );
        return (alt == null) ? uri : alt;
    }
    
    
    /**
     * <p>
     * Answer the representation of the ontology document with the given URI, if known.
     * </p>
     * 
     * @param uri The ontology document to lookup
     * @return The URI of the representation language, or null.
     */
    public String getLanguage( String uri ) {
        return (String) m_languageMap.get( uri );
    }
    
    
    /**
     * <p>
     * Answer the prefix for the qnames in the given document, if known.
     * </p>
     * 
     * @param uri The ontology document to lookup
     * @return  The string to use as a prefix when serialising qnames in the
     *          given document's namespace, or null if not known
     */
    public String getPrefixForURI( String uri ) {
        return (String) m_prefixMap.get( uri );
    }
    
    
    /**
     * <p>
     * Answer the base URI for qnames with the given prefix, if known.
     * </p>
     * 
     * @param prefix A prefix string
     * @return The basename that the prefix expands to, or null
     */
    public String getURIForPrefix( String prefix ) {
        return (String) m_uriMap.get( prefix );
    }
    
    
    /**
     * <p>
     * Answer the cached model corresponding to the given document, if known.
     * </p>
     * 
     * @param uri The ontology document to lookup
     * @return The ontology model for the document, or null if the model is not
     *      known.
     */
    public Graph getGraph( String uri ) {
        return (Graph) m_graphMap.get( uri );
    }
    
    
    /**
     * <p>
     * Add a prefix mapping between the given public base URI and the 
     * given prefix.
     * </p>
     * 
     * @param uri The base URI that <code>prefix</code> expands to
     * @param prefix A qname prefix
     */
    public void addPrefixMapping( String uri, String prefix ) {
        m_uriMap.put( prefix, uri );
        m_prefixMap.put( uri, prefix );
    }
    
    
    /**
     * <p>
     * Add an entry for an alternative copy of the document with the given document
     * URI.
     * </p>
     * 
     * @param docURI The public URI of the ontology document
     * @param locationURL A locally resolvable URL where an alternative copy of the 
     *         ontology document can be found
     */
    public void addAltEntry( String docURI, String locationURL ) {
        m_altMap.put( docURI, locationURL );
    }
    
    
    /**
     * <p>
     * Add an entry that <code>graph</code> is the appropriate graph to use
     * for the given ontology document
     * </p>
     * 
     * @param docURI The public URI of the ontology document
     * @param graph A graph containing the triples from the document
     */
    public void addGraph( String docURI, Graph graph ) {
        if (m_cacheGraphs) {
            m_graphMap.put( docURI, graph );
        }
    }
    
    
    /**
     * <p>
     * Add an entry that <code>language</code> is the URI defining the
     * representation language for the given document
     * </p>
     * 
     * @param docURI The public URI of the ontology document
     * @param language A string defining the URI of the language
     */
    public void addLanguageEntry( String docURI, String language ) {
        m_languageMap.put( docURI, language );
    }
    
    
    /**
     * <p>
     * Remove all managed entries for the given document.
     * </p>
     * 
     * @param docURI The public URI for an ontology document
     */
    public void forget( String docURI ) {
        m_altMap.remove( docURI );
        m_uriMap.remove( docURI );
        m_prefixMap.remove( docURI );
        m_graphMap.remove( docURI );
        m_languageMap.remove( docURI );
    }
    
    
    /**
     * <p>
     * Answer the ontology model that results from loading the document with the
     * given URI.  This may be a cached model, if this document manager's policy
     * is to cache loaded models.  If not, or if no model is cached, the document
     * will be read into a suitable model.  The model will contain the imports closure 
     * of the ontology, if that is the current policy of this document manager.
     * </p>
     * 
     * @param uri Identifies the model to load.
     * @param lang URI denoting the language of the ontology to be loaded
     * @return An ontology model containing the statements from the ontology document.
     */
    public Model getOntology( String uri, String lang ) {
        OntModel m = ModelFactory.createOntologyModel( lang, null, this );
        m.read( uri );
        
        return m;
    }
    
    
    /**
     * <p>
     * Answer the graph factory this document manager is using to build composite
     * graphs.  If no graph factory has been set, return a default factory that creates
     * a new in-memory graph each time.
     * </p>
     * 
     * @return A GraphFactory that will provide a graph for building composite graphs
     */
    public GraphFactory getDefaultGraphFactory() {
        if (m_graphFactory == null) {
            // construct the default graph factory
            m_graphFactory = new GraphFactory() {
                                 // default is to create a new in-memory graph with rdfs reasoning turned on
                                 public Graph getGraph() {
                                     Graph graph = new GraphMem(); 
                                     
                                     if (m_useInference) {
                                         // TODO: we will want a better way of doing this in J2P3
                                         Reasoner reasoner = RDFSReasonerFactory.theInstance().create( null );
                                         graph = reasoner.bind( graph );
                                     }
                                     
                                     return graph; 
                                 }
                             };
        }
        
        return m_graphFactory;
    }
    
    
    /**
     * <p>
     * Set the graph factory that this document manager will use when building 
     * composite models to hold the imports closure of an ontology model. 
     * </p>
     * 
     * @param graphFactory The new graph factory to use
     */
    public void setDefaultGraphFactory( GraphFactory graphFactory ) {
        m_graphFactory = graphFactory;
    }
    
    
    /**
     * <p>
     * Answer the policy flag indicating whether the imports statements of 
     * loaded ontologies will be processed to build a union of graphs.
     * </p>
     * 
     * @return True if imported models will be included in a loaded model
     */
    public boolean getProcessImports() {
        return m_processImports;
    }
    
    
    /**
     * <p>
     * Answer true if the graphs loaded by this document manager from a given
     * URI will be cached, so that they can be re-used in other compound
     * ontology models.
     * </p>
     * 
     * @return If true, a cache is mainted of loaded models from their URI's.
     */
    public boolean getCacheGraphs() {
        return m_cacheGraphs;
    }
    
    
    /**
     * <p>
     * Set the policy flag for processing imports of loaded ontologies.
     * </p>
     * 
     * @param processImports If true, load imported ontologies during load
     * @see #getProcessImports
     */
    public void setProcessImports( boolean processImports ) {
        m_processImports = processImports;
    }
    
    
    /**
     * <p>
     * Set the policy flag that indicates whether loaded models are cached by URI
     * </p>
     * 
     * @param cacheModels If true, models will be cached by URI
     * @see #getCacheModels()
     */
    public void setCacheGraphs( boolean cacheGraphs ) {
        m_cacheGraphs = cacheGraphs;
    }
    
    
    /**
     * <p>
     * Inspect the statements in the graph expressed by the given model, and load
     * into the model any imported documents.  Imports statements are recognised according
     * to the model's language profile.  An occurs check allows cycles of importing
     * safely.  This method will do nothing if the {@link #getProcessImports() policy}
     * for this manager is not to process imports.  If the {@link #getCacheModels() cache policy}
     * for this doc manager allows, models will be cached by URI and re-used where possible.
     * </p>
     * 
     * @param model An ontology model whose imports are to be loaded.
     * @param readState A state-vector containing the key elements of the state 
     *          of the read operation so far
     */
    public void loadImports( OntModel model, OntReadState readState ) {
        if (m_processImports) {
            // first time through, we initialise the queue of imports to process
            if (readState.getQueue() == null) {
                readState.setQueue( model.listImportedOntologyURIs() );
            }
            else {
                // add the imports from this model to the existing queue
                readState.getQueue().addAll( model.listImportedOntologyURIs() );
            }
        
            while (!readState.getQueue().isEmpty()) {
                // we process the import statements as a FIFO queue
                String importURI = (String) readState.getQueue().remove( 0 );
                
                if (!model.hasLoadedImport( importURI )) {
                    // this file has not been processed yet 
                    loadImport( model, importURI, readState );
                }
            }
        }
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>
     * Initialise the mappings for uri's and prefixes by loading metadata
     * from an RDF model.
     * </p>
     * 
     * @param path The URI path to search for a loadable model
     * @param replace If true, clear existing mappings first
     */
    protected void initialiseMetadata( String path, boolean replace ) {
        // first clear out any old mappings if necessary
        if (replace) {
            m_altMap.clear();
            m_prefixMap.clear();
            m_graphMap.clear();
        }
        
        // search the path for metadata about locally cached models
        Model metadata = findMetadata( path );
        
        if (metadata != null) {
            loadMetadata( metadata );
        }
    }
    
    
    /**
     * <p>
     * Search the given path for a resolvable URI, from which we load a model
     * (assuming RDF/XML).  
     * </p>
     * 
     * @param path The path to search
     * @return A model loaded by resolving an entry on the path, or null if
     * no path entries succeed.
     */
    protected Model findMetadata( String path ) {
        StringTokenizer pathElems = new StringTokenizer( path, PATH_DELIMITER );
        Model m = ModelFactory.createDefaultModel();
        boolean loaded = false;
        
        // try to read each path entry in turn
        while (!loaded  &&  pathElems.hasMoreTokens()) {
            try {
                m.read( pathElems.nextToken() ); 
                loaded = true;
            }
            catch (RDFException e) {
                // we take this to mean that the resolution failed, so try another path element
                System.err.println( "ex " + e );
                e.printStackTrace();
            }
        }
        
        // only return m if we found some metadata
        return loaded ? m : null;
    }
    
    
    /**
     * <p>
     * Load the ontology specification metadata from the model into the local
     * mapping tables.
     * </p>
     * 
     * @param metadata A model containing metadata about ontologies.
     */
    protected void loadMetadata( Model metadata ) {
        for (ResIterator i = metadata.listSubjectsWithProperty( RDF.type, ONTOLOGY_SPEC ); i.hasNext(); ) {
            Resource root = i.nextResource();

            Statement s = root.getProperty( PUBLIC_URI );
            if (s != null) {
                // this will be the key in the mappings
                String publicURI = s.getResource().getURI();
                
                Resource cacheURL = null;
                String prefix = null;
                Resource language = null;

                // there may be a cached copy for this ontology
                try { 
                    s = root.getProperty( ALT_URL );
                    addAltEntry( publicURI, s.getResource().getURI() );
                } catch (RDFException ignore) {}
                
                // there may be a standard prefix for this ontology
                try { 
                    s = root.getProperty( PREFIX ); 
                    addPrefixMapping( publicURI, s.getString() );
                } catch (RDFException ignore) {}
                
                // there may be a language specified for this ontology
                try { 
                    s = root.getProperty( LANGUAGE ); 
                    addLanguageEntry( publicURI, s.getResource().getURI() );
                } catch (RDFException ignore) {}
            }
            else {
                m_log.warn( "Ontology specification node lists no public URI - node ignored");
            }
        }
    }
    
    
    /**
     * <p>
     * Load the document referenced by the given URI into the model.  The cache will be
     * used if permitted by the policy, and the imports of loaded model will be added to
     * the end of the queue. 
     * </p>
     * 
     * @param model The composite model to load into
     * @param importURI The URI of the document to load
     * @param readState Cumulate read state for this operation
     */
    protected void loadImport( OntModel model, String importURI, OntReadState readState ) {
        // add this graph to occurs check list
        model.addLoadedImport( importURI );
        readState.getModel().addLoadedImport( importURI );

        // if we have a cached version get that, otherwise load from the URI but don't do the imports closure
        Graph in = getGraph( importURI );
        
        // if not cached, we must load it from source
        if (in == null) {
            // create a sub ontology model and load it from the source
            // note that we do this to ensure we recursively load imports
            OntModel mIn = ModelFactory.createOntologyModel( model.getProfile().NAMESPACE(), null, this, model.getGraphFactory() );
            mIn.read( importURI, readState );
            
            in = mIn.getGraph();
        }
        
        // we need to add this graph to the union
        // both the top-level source document model, and the local cache model
        if (in instanceof OntologyGraph) {
            // 'in' is composite - so flatten all subgraphs into the main union
            MultiUnion u = ((OntologyGraph) in).getUnion();
            
            model.addSubGraph( u.getBaseGraph() );
            readState.getModel().addSubGraph( u.getBaseGraph() );
            
            for (Iterator i = u.getSubGraphs().iterator(); i.hasNext(); ) {
                Graph sub = (Graph) i.next();
                model.addSubGraph( sub );
                readState.getModel().addSubGraph( sub );
            }
        }
        else {
            model.addSubGraph( in );
            readState.getModel().addSubGraph( in );
        }
    }
    
    
    /**
     * <p>
     * Load into the given model from the given URI, or from a local cache URI if defined.
     * </p>
     * 
     * @param model A model to read statements into
     * @param uri A URI string
     * @param lang The language profile
     * @param gf A graph factory
     * @return A Model containing the statements from the document with the given URI
     */
    protected void read( OntModel model, String uri ) {
        // map to the cache URI if defined
        String resolvableURI = (String) (m_uriMap.containsKey( uri ) ? m_uriMap.get( uri ) : uri);
        
        // make a new model to contain 
        OntModel m = ModelFactory.createOntologyModel( model.getProfile().NAMESPACE(), null, this, model.getGraphFactory() );
        
        //Model m = ModelFactory.createModelForGraph( gf.getGraph() );
        
        // try to load the URI
        try {
            // TODO: should be able to specify uri as the base here
            m.read( resolvableURI );
            
            // success: cache the model against the uri
            if (m_cacheGraphs) {
                m_graphMap.put( uri, m );
            }
        }
        catch (RDFException e) {
            Logger.getLogger( OntDocumentManager.class )
                  .warn( "RDFException while reading model from " + resolvableURI + ", with message: " + e.getMessage(), e );
        }
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
