/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            10 Feb 2003
 * Filename           $RCSfile: OntDocumentManager.java,v $
 * Revision           $Revision: 1.49 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-10-04 09:38:31 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import java.io.*;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.util.XMLChar;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.vocabulary.OntDocManagerVocab;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;


/**
 * <p>
 * Provides services for managing ontology documents, including loading imported
 * documents, and locally caching documents from resolvable URL's to improve
 * load performance. This class now delegates some of the responsibility for
 * resolving URI's and caching models to {@link com.hp.hpl.jena.util.FileManager FileManager}.
 * By default, OntDocumentManager will use a copy of the
 * singleton global FileManager. Alternatively, a specific <code>FileManager</code>
 * can be given to a document manager instance to use when resolving URI's and file paths.
 * Note that the default behaviour is to hold a <strong>copy</strong> of the global file
 * manager. In order to ensure that the document manager directly uses the global
 * file manager (e.g. so that document manager sees updates to the location mappings
 * held by the file manager), use the {@link #setFileManager(FileManager)} method. For
 * example:
 * </p>
 * <pre>OntDocumentManager dm = OntDocumentManager.getInstance();
 * dm.setFileManager( FileManager.get() );</pre>
 * <p>Note that in Jena 2.3, we have deprecated the capability of the document manager
 * to store a table of known prefixes, and a table mapping document URI's to ontology language
 * types. <strong>The intention is to remove both of these capabilities from
 * Jena 2.4 onwards</strong>. If this change would be problematic, please send email to the
 * <a href="http://groups.yahoo.com/group/jena-dev">Jena support
 * list</a>.</p>
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntDocumentManager.java,v 1.49 2005-10-04 09:38:31 ian_dickinson Exp $
 */
public class OntDocumentManager
{
    // Constants
    ////////////////////////////////////

	/** The default path for searching for the metadata on locally cached ontologies */
    public static final String DEFAULT_METADATA_PATH = "file:ont-policy.rdf;file:etc/ont-policy.rdf";

    /** Namespace for ontology metadata resources and properties */
    public static final String NS = "http://jena.hpl.hp.com/schemas/2003/03/ont-manager#";

    /** The anchor char is added to the end of namespace prefix expansions */
    public static final String ANCHOR = "#";

    /** rdf:type for ontology specification nodes in meta-data file */
    public static final Resource ONTOLOGY_SPEC = OntDocManagerVocab.OntologySpec;

    /** Represents the public URI of an ontology; also used to derive the namespace */
    public static final Property PUBLIC_URI = OntDocManagerVocab.publicURI;

    /** Represents the alternative local copy of the public ontology; assumed to be resolvable, hence URL not URI */
    public static final Property ALT_URL = OntDocManagerVocab.altURL;

    /** Represents the standard prefix for this namespace */
    public static final Property PREFIX = OntDocManagerVocab.prefix;

    /** Represents the ontology language used to encode the ontology */
    public static final Property LANGUAGE = OntDocManagerVocab.language;

    /** rdf:type for document manager policy nodes */
    public static final Resource DOC_MGR_POLICY = OntDocManagerVocab.DocumentManagerPolicy;

    /** Defines boolean policy choice of caching loaded models */
    public static final Property CACHE_MODELS = OntDocManagerVocab.cacheModels;

    /** Defines boolean policy choice of loading the imports closure */
    public static final Property PROCESS_IMPORTS = OntDocManagerVocab.processImports;

    /** Specifies the URI of an ontology that we do not want to import, even if processImports is true. */
    public static final Property IGNORE_IMPORT = OntDocManagerVocab.ignoreImport;

    /** The policy property for including the pre-declared namespace prefixes in a model. */
    public static final Property USE_DECLARED_NS_PREFIXES = OntDocManagerVocab.useDeclaredNsPrefixes;


    // Static variables
    //////////////////////////////////

    /** Default document manager instance */
    private static OntDocumentManager s_instance = null;

    /** Log for this class */
    private static Log log = LogFactory.getLog( OntDocumentManager.class );


    // Instance variables
    //////////////////////////////////

    /** The search path for metadata */
    protected String m_searchPath = DEFAULT_METADATA_PATH;

    /** FileManager instance that provides location resolution service - defaults to global instance */
    protected FileManager m_fileMgr = null;

    /** Flag to indicate we're using a copy of the global file manager */
    protected boolean m_usingGlobalFileMgr = false;

    /** Mapping of public public URI's to language resources */
    protected Map m_languageMap = new HashMap();

    /** Flag: process the imports closure */
    protected boolean m_processImports = true;

    /** List of URI's that will be ignored when doing imports processing */
    protected Set m_ignoreImports = new HashSet();

    /** Default prefix mapping to use to seed all models */
    protected PrefixMapping m_prefixMap = new PrefixMappingImpl();

    /** Flag to control whether we include the standard prefixes in generated models - default true. */
    protected boolean m_useDeclaredPrefixes = true;

    /** The URL of the policy file that was loaded, or null if no external policy file has yet been loaded */
    protected String m_policyURL = null;


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
     * null to prevent loading of any initial ontology metadata. The path is a series
     * of URL's, separated by a semi-colon (;).
     */
    public OntDocumentManager( String path ) {
        this( null, path );
    }


    /**
     * <p>
     * Initialise a document manager by with the given FileManager, and
     * then searching the given path for ontology
     * metadata about known ontologies cached locally.
     * </p>
     *
     * @param path The search path to search for initial metadata
     * @see #OntDocumentManager(String)
     */
    public OntDocumentManager( FileManager fileMgr, String path ) {
        setFileManager( fileMgr );
        setDefaults();
        m_searchPath = (path == null) ? "" : path;
        initialiseMetadata( m_searchPath );
    }


    /**
     * <p>Initialise a document manager with the given configuration model. This model
     * is used in place of any model that might be
     * found by searching the meta-data search path. Uses the default file manager,
     * i.e. a copy of the global file manager.</p>
     * @param config An RDF model containing configuration information for this document manager.
     */
    public OntDocumentManager( Model config ) {
        this( null, config );
    }


    /**
     * <p>Initialise a document manager with the given configuration model. This model
     * is used in place of any model that might be
     * found by searching the meta-data search path.</p>
     * @param fileMgr A file manager to use when locating source files for ontologies
     * @param config An RDF model containing configuration information for this document manager.
     */
    public OntDocumentManager( FileManager fileMgr, Model config ) {
        // we don't need to reset first since this is a new doc mgr
        setFileManager( fileMgr );
        setDefaults();
        configure( config, false );
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
        if (s_instance == null) {
            s_instance = new OntDocumentManager();
        }
        return s_instance;
    }


    /**
     * <p>Answer the file manager instance being used by this document manager.</p>
     * @return This object's file manager
     */
    public FileManager getFileManager() {
        return m_fileMgr;
    }


    /**
     * <p>Set the file manager used by this ODM instance to <strong>a
     * copy</strong> of the global file manager (and, by extension, the
     * global location mapper).</p>
     */
    public void setFileManager() {
        setFileManager( new FileManager( FileManager.get() ) );
        m_usingGlobalFileMgr = true;
    }

    /**
     * <p>Set the file manager used by this ODM instance to <strong>a
     * copy</strong> of the global file manager (and, by extension, the
     * global location mapper).</p>
     * @param fileMgr The new file manager
     */
    public void setFileManager( FileManager fileMgr ) {
        if (fileMgr == null) {
            // use default fm
            setFileManager();
        }
        else {
            m_fileMgr = fileMgr;
            m_usingGlobalFileMgr = false;
        }
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
        if (replace) {
            reset();
        }
        m_searchPath = path;
        m_policyURL = null;
        initialiseMetadata( path );
    }


    /**
     * <p>Configure this document manager using the given configuration information, after
     * first resetting the model back to all default values.</p>
     * @param config Document manager configuration as an RDF model
     * @see #configure( Model, boolean )
     */
    public void configure( Model config ) {
        configure( config, true );
    }


    /**
     * <p>Configure this document manager according to the configuration options
     * supplied by the given configuration model. If <code>reset</code> is true, the
     * document manager is first reset back to all default values.</p>
     * @param config Document manager configuration as an RDF model
     * @param reset If true, reset the document manager to default values, before
     * attempting to configure the document manager using the given model.
     * @see #reset
     */
    public void configure( Model config, boolean reset ) {
        if (reset) {
            reset( false );
        }

        processMetadata( config );
    }


    /**
     * <p>Reset all state in this document manager back to the default
     * values it would have had when the object was created. Optionally
     * reload the profile metadata from the search path. <strong>Note</strong>
     * that the metadata search path is not changed by this reset.</p>
     * @param reload If true, reload the configuration file from the
     * search path.
     */
    public void reset( boolean reload ) {
        // first check if we are using the global file manager, or a local one
        if (m_usingGlobalFileMgr) {
            // we can do a general reset by throwing away the old FM and creating a new one
            setFileManager();
        }
        else {
            // not using the global default FM, so we reset to best effort
            getFileManager().resetCache();
        }

        setDefaults();

        m_languageMap.clear();
        m_ignoreImports.clear();

        // copy the standard prefixes
        m_prefixMap = new PrefixMappingImpl();
        m_prefixMap.setNsPrefixes( PrefixMapping.Standard );

        if (reload) {
            initialiseMetadata( m_searchPath );
        }
    }

    /**
     * <p>Reset all state in this document manager back to the default
     * values it would have had when the object was created. This does
     * <strong>not</strong> reload the configuration information from
     * the search path.  Note also that the metadata search path is one
     * of the values that is reset back to its default value.</p>
     * @see #reset( boolean )
     */
    public void reset() {
        reset( false );
    }


    /**
     * <p>
     * Answer an iterator over the ontology document URI's that this document manager
     * knows to re-direct to other URL's. <strong>Note</strong> that being in this
     * iteration does <em>not</em> mean that a document with the given name is in
     * the set of cached models held by this document manager.
     * </p>
     *
     * @return An Iterator ranging over the public URI strings for the known
     * document URI's.
     */
    public Iterator listDocuments() {
        return getFileManager().getLocationMapper().listAltEntries();
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
    public String doAltURLMapping( String uri ) {
        return getFileManager().mapURI( uri );
    }


    /**
     * <p>
     * Answer the representation of the ontology document with the given URI, if known.
     * </p>
     *
     * @param uri The ontology document to lookup
     * @return The URI of the representation language, or null.
     * @deprecated Language determination via the ODM will be removed from Jena 2.4 onwards
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
     * @deprecated Prefix management via the ODM is very likely to be removed from Jena 2.4 onwards
     */
    public String getPrefixForURI( String uri ) {
        return m_prefixMap.getNsURIPrefix( uri );
    }


    /**
     * <p>
     * Answer the base URI for qnames with the given prefix, if known.
     * </p>
     *
     * @param prefix A prefix string
     * @return The basename that the prefix expands to, or null
     * @deprecated Prefix management via the ODM is very likely to be removed from Jena 2.4 onwards
     */
    public String getURIForPrefix( String prefix ) {
        return m_prefixMap.getNsPrefixURI( prefix );
    }


    /**
     * <p>
     * Answer the cached model corresponding to the given document, if known.
     * </p>
     *
     * @param uri The ontology document to lookup
     * @return The model for the document, or null if the model is not known.
     * @see #getOntology
     */
    public Model getModel( String uri ) {
        return getFileManager().getFromCache( uri );
    }


    /**
     * <p>Answer true if, according to the policy expressed by this document manager, newly
     * generated ontology models should include the pre-declared namespace prefixes.
     * </p>
     *
     * @return True if pre-declared prefixes should be added to the models
     * @deprecated Prefix management via the ODM is very likely to be removed from Jena 2.4 onwards
     */
    public boolean useDeclaredPrefixes() {
        return m_useDeclaredPrefixes;
    }

    /**
     * <p>Set the flag that determines whether pre-declared namespace prefixes will be added to newly
     * generated ontology models.</p>
     *
     * @param useDeclaredPrefixes If true, new models will include the pre-declared prefixes set held
     * by this document manager.
     * @deprecated Prefix management via the ODM is very likely to be removed from Jena 2.4 onwards
     */
    public void setUseDeclaredPrefixes( boolean useDeclaredPrefixes ) {
        m_useDeclaredPrefixes = useDeclaredPrefixes;
    }

    /**
     * <p>Answer the namespace prefix map that contains the shared prefixes managed by this
     * document manager.</p>
     *
     * @return The namespace prefix mapping
     * @deprecated Prefix management via the ODM is very likely to be removed from Jena 2.4 onwards
     */
    public PrefixMapping getDeclaredPrefixMapping() {
        return m_prefixMap;
    }


    /**
     * <p>
     * Add a prefix mapping between the given public base URI and the
     * given prefix.
     * </p>
     *
     * @param uri The base URI that <code>prefix</code> expands to
     * @param prefix A qname prefix
     * @deprecated Prefix management via the ODM is very likely to be removed from Jena 2.4 onwards
     */
    public void addPrefixMapping( String uri, String prefix ) {
        m_prefixMap.setNsPrefix( prefix, uri );
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
        getFileManager().getLocationMapper().addAltEntry( docURI, locationURL );
    }


    /**
     * <p>
     * Add an entry that <code>model</code> is the appropriate model to use
     * for the given ontology document. Will not replace any existing
     * model that is cached for this URI (see
     * {@link #addModel(String, Model, boolean)} for an alternative
     * that can replace existing models).
     * </p>
     *
     * @param docURI The public URI of the ontology document
     * @param model A model containing the triples from the document
     */
    public void addModel( String docURI, Model model ) {
        addModel( docURI, model, false );
    }


    /**
     * <p>
     * Add an entry that <code>model</code> is the appropriate model to use
     * for the given ontology document
     * </p>
     *
     * @param docURI The public URI of the ontology document
     * @param model A model containing the triples from the document
     * @param replace If true, replace any existing entry with this one.
     */
    public void addModel( String docURI, Model model, boolean replace ) {
        if (getFileManager().getCachingModels() &&
            (replace || !getFileManager().hasCachedModel( docURI )))
        {
            getFileManager().addCacheModel( docURI, model );
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
     * @deprecated Language determination via the ODM will be removed from Jena 2.4 onwards
     */
    public void addLanguageEntry( String docURI, String language ) {
        m_languageMap.put( docURI, language );
    }


    /**
     * <p>
     * Remove all managed entries for the given document.  Note does not side-effect
     * the prefixes table: this will have to be done separately.
     * </p>
     *
     * @param docURI The public URI for an ontology document
     */
    public void forget( String docURI ) {
        getFileManager().getLocationMapper().removeAltEntry( docURI );
        getFileManager().removeCacheModel( docURI );
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
     * @param spec Specifies the structure of the ontology model to create
     * @return An ontology model containing the statements from the ontology document.
     * @see #getModel
     */
    public OntModel getOntology( String uri, OntModelSpec spec ) {
        // ensure consistency of document managers (to allow access to cached documents)
        OntModelSpec _spec = spec;
        if (_spec.getDocumentManager() != this) {
            _spec = new OntModelSpec( spec );
            _spec.setDocumentManager( this );
        }

        // cached already?
        if (getFileManager().hasCachedModel( uri )) {
            Model cached = getFileManager().getFromCache( uri );
            if (cached instanceof OntModel) {
                return (OntModel) cached;
            }
            else {
                return ModelFactory.createOntologyModel( _spec, cached );
            }
        }
        else {
            OntModel m = ModelFactory.createOntologyModel( _spec, null );
            read( m, uri, true );

            // cache this model for future re-use
            getFileManager().addCacheModel( uri, m );
            return m;
        }
    }


    /**
     * <p>
     * Answer the policy flag indicating whether the imports statements of
     * loaded ontologies will be processed to build a union of s.
     * </p>
     *
     * @return True if imported models will be included in a loaded model
     */
    public boolean getProcessImports() {
        return m_processImports;
    }


    /**
     * <p>
     * Answer true if the models loaded by this document manager from a given
     * URI will be cached, so that they can be re-used in other compound
     * ontology models.
     * </p>
     *
     * @return If true, a cache is maintained of loaded models from their URI's.
     */
    public boolean getCacheModels() {
        return getFileManager().getCachingModels();
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
    public void setCacheModels( boolean cacheModels ) {
        getFileManager().setModelCaching( cacheModels );
    }

    /**
     * <p>Add the given URI to the set of URI's we ignore in imports statements</p>
     * @param uri A URI to ignore when importing
     */
    public void addIgnoreImport( String uri ) {
        m_ignoreImports.add( uri );
    }

    /**
     * <p>Remove the given URI from the set of URI's we ignore in imports statements</p>
     * @param uri A URI to ignore no longer when importing
     */
    public void removeIgnoreImport( String uri ) {
        m_ignoreImports.remove( uri );
    }

    /**
     * <p>Answer an iterator over the set of URI's we're ignoring</p>
     * @return An iterator over ignored imports
     */
    public Iterator listIgnoredImports() {
        return m_ignoreImports.iterator();
    }

    /**
     * <p>Answer true if the given URI is one that will be ignored during imports </p>
     * @param uri A URI to test
     * @return True if uri will be ignored as an import
     */
    public boolean ignoringImport( String uri ) {
        return m_ignoreImports.contains( uri );
    }

    /**
     * <p>
     * Remove all entries from the model cache
     * </p>
     */
    public void clearCache() {
        getFileManager().resetCache();
    }


    /**
     * <p>
     * Inspect the statements in the graph expressed by the given model, and load
     * into the model any imported documents.  Imports statements are recognised according
     * to the model's language profile.  An occurs check allows cycles of importing
     * safely.  This method will do nothing if the {@linkplain #getProcessImports policy}
     * for this manager is not to process imports.  If the {@linkplain #getCacheModels cache policy}
     * for this doc manager allows, models will be cached by URI and re-used where possible.
     * </p>
     *
     * @param model An ontology model whose imports are to be loaded.
     */
    public void loadImports( OntModel model ) {
        if (m_processImports) {
            List readQueue = new ArrayList();

            // add the imported statements from the given model to the processing queue
            queueImports( model, readQueue, model.getProfile() );
            loadImports( model, readQueue );
        }
    }


    /**
     * <p>Add the given model from the given URI as an import to the given model.  Any models imported by the given
     * URI will also be imported.</p>
     *
     * @param model A model to import into
     * @param uri The URI of a document to import
     */
    public void loadImport( OntModel model, String uri ) {
        if (m_processImports) {
            List readQueue = new ArrayList();
            readQueue.add( uri );
            loadImports( model, readQueue );
        }
    }


    /**
     * <p>Remove from the given model the import denoted by the given URI.</p>
     *
     * @param model A model
     * @param uri The URI of a document to no longer import
     */
    public void unloadImport( OntModel model, String uri ) {
        if (m_processImports) {
            List unloadQueue = new ArrayList();
            unloadQueue.add( uri );
            unloadImports( model, unloadQueue );
        }
    }


    /**
     * <p>Answer the URL of the most recently loaded policy URL, or null
     * if no document manager policy has yet been loaded since the metadata
     * search path was last set.</p>
     * @return The most recently loaded policy URL or null.
     */
    public String getLoadedPolicyURL() {
        return m_policyURL;
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Load all of the imports in the queue</p>
     * @param model The model to load the imports into
     * @param readQueue The queue of imports to load
     */
    protected void loadImports( OntModel model, List readQueue ) {
        while (!readQueue.isEmpty()) {
            // we process the import statements as a FIFO queue
            String importURI = (String) readQueue.remove( 0 );

            if (!model.hasLoadedImport( importURI )  &&  !ignoringImport( importURI )) {
                // this file has not been processed yet
                loadImport( model, importURI, readQueue );
            }
        }
    }


    /**
     * <p>Unload all of the imports in the queue</p>
     * @param model The model to unload the imports from
     * @param unloadQueue The queue of imports to unload
     */
    protected void unloadImports( OntModel model, List unloadQueue ) {
        while (!unloadQueue.isEmpty()) {
            // we process the import statements as a FIFO queue
            String importURI = (String) unloadQueue.remove( 0 );

            if (model.hasLoadedImport( importURI )) {
                // this import has not been unloaded yet

                // look up the cached model - if we can't find it, we can't unload the import
                Model importModel = getModel( importURI );
                if (importModel != null) {
                    List imports = new ArrayList();

                    // collect a list of the imports from the model that is scheduled for removal
                    for (StmtIterator i = importModel.listStatements( null, model.getProfile().IMPORTS(), (RDFNode) null ); i.hasNext(); ) {
                        imports.add( i.nextStatement().getResource().getURI() );
                    }

                    // now remove the sub-model
                    model.removeSubModel( importModel, false );
                    model.removeLoadedImport( importURI );

                    // check the list of imports of the model we have removed - if they are not
                    // imported by other imports that remain, we should remove them as well
                    for (StmtIterator i = model.listStatements( null, model.getProfile().IMPORTS(), (RDFNode) null ); i.hasNext(); ) {
                        imports.remove( i.nextStatement().getResource().getURI() );
                    }

                    // any imports that remain are scheduled for removal
                    unloadQueue.addAll( imports );
                }
            }
        }

        model.rebind();
    }


    /**
     * <p>Add the ontologies imported by the given model to the end of the queue.</p>
     */
    protected void queueImports( Model model, List readQueue, Profile profile ) {
        if (model instanceof OntModel) {
            // add the imported ontologies to the queue
            readQueue.addAll( ((OntModel) model).listImportedOntologyURIs() );
        }
        else {
            // we have to do the query manually
            StmtIterator i = model.listStatements( null, profile.IMPORTS(), (RDFNode) null );

            while (i.hasNext()) {
                // read the next import statement and add to the queue
                readQueue.add( i.nextStatement().getResource().getURI() );
            }
        }
    }


    /**
     * <p>
     * Initialise the mappings for uri's and prefixes by loading metadata
     * from an RDF model.
     * </p>
     *
     * @param path The URI path to search for a loadable model
     */
    protected void initialiseMetadata( String path ) {
        // search the path for metadata about locally cached models
        Model metadata = findMetadata( path );

        if (metadata != null) {
            processMetadata( metadata );
        }
    }


    /**
     * <p>
     * Search the given path for a resolvable URI, from which we load a model
     * (assuming RDF/XML).
     * </p>
     *
     * @param configPath The path to search
     * @return A model loaded by resolving an entry on the path, or null if
     * no path entries succeed.
     */
    protected Model findMetadata( String configPath ) {
        if (configPath == null) {
            return null;
        }

        // Make a temporary file manager to look for the metadata file
        FileManager fm = new FileManager();
        fm.addLocatorFile();
        fm.addLocatorClassLoader( fm.getClass().getClassLoader() );

        try {
            String uri = null ;
            InputStream in = null ;

            StringTokenizer pathElems = new StringTokenizer( configPath, FileManager.PATH_DELIMITER );
            while (in == null && pathElems.hasMoreTokens()) {
                uri = pathElems.nextToken();
                in = fm.openNoMap( uri );
            }

            if (in != null) {
                String syntax = FileUtils.guessLang(uri);
                Model model = ModelFactory.createDefaultModel() ;
                model.read( in, uri, syntax );
                m_policyURL = uri;
                return model;
            }
        }
        catch (JenaException e) {
            log.warn( "Exception while reading configuration: " + e.getMessage(), e);
        }

        return null;
    }


    /**
     * <p>
     * Load the ontology specification metadata from the model into the local
     * mapping tables.
     * </p>
     *
     * @param metadata A model containing metadata about ontologies.
     */
    protected void processMetadata( Model metadata ) {
        // there may be configuration for the location mapper in the ODM metadata file
        getFileManager().getLocationMapper().processConfig( metadata );

        // first we process the general policy statements for this document manager
        for (ResIterator i = metadata.listSubjectsWithProperty( RDF.type, DOC_MGR_POLICY ); i.hasNext(); ) {
            Resource policy = i.nextResource();

            // iterate over each policy statement
            for (StmtIterator j = policy.listProperties();  j.hasNext(); ) {
                Statement s = j.nextStatement();
                Property pred = s.getPredicate();

                if (pred.equals( CACHE_MODELS )) {
                    setCacheModels( s.getBoolean() );
                }
                else if (pred.equals( PROCESS_IMPORTS )) {
                    setProcessImports( s.getBoolean() );
                }
                else if (pred.equals( IGNORE_IMPORT )) {
                    addIgnoreImport( s.getResource().getURI() );
                }
                else if (pred.equals( USE_DECLARED_NS_PREFIXES )) {
                    setUseDeclaredPrefixes( s.getBoolean() );
                }
            }
        }

        // then we look up individual meta-data for particular ontologies
        for (ResIterator i = metadata.listSubjectsWithProperty( RDF.type, ONTOLOGY_SPEC ); i.hasNext(); ) {
            Resource root = i.nextResource();

            Statement s = root.getProperty( PUBLIC_URI );
            if (s != null) {
                // this will be the key in the mappings
                String publicURI = s.getResource().getURI();

                // there may be a cached copy for this ontology
                s = root.getProperty( ALT_URL );
                if (s != null) addAltEntry( publicURI, s.getResource().getURI() );

                // there may be a standard prefix for this ontology
                s = root.getProperty( PREFIX );
                if (s != null) {
                    // if the namespace doesn't end with a suitable split point character, add a #
                    boolean endWithNCNameCh = XMLChar.isNCName( publicURI.charAt( publicURI.length() - 1 ) );
                    String prefixExpansion = endWithNCNameCh ? (publicURI + ANCHOR) : publicURI;

                    addPrefixMapping( prefixExpansion, s.getString() );
                }

                // there may be a language specified for this ontology
                s = root.getProperty( LANGUAGE );
                if (s != null) addLanguageEntry( publicURI, s.getResource().getURI() );
            }
            else {
                log.warn( "Ontology specification node lists no public URI - node ignored");
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
     * @param readQueue Cumulative read queue for this operation
     */
    protected void loadImport( OntModel model, String importURI, List readQueue ) {
        if (m_processImports) {
            log.debug( "OntDocumentManager loading " + importURI );

            // add this model to occurs check list
            model.addLoadedImport( importURI );

            // if we have a cached version get that, otherwise load from the URI but don't do the imports closure
            Model in = getModel( importURI );

            // if not cached, we must load it from source
            if (in == null) {
                // create a sub ontology model and load it from the source
                // note that we do this to ensure we recursively load imports
                ModelMaker maker = model.getSpecification().getImportModelMaker();
                boolean loaded = maker.hasModel( importURI );

                in = maker.openModel( importURI );

                // if the graph was already in existence, we don't need to read the contents (we assume)!
                if (!loaded) {
                    read( in, importURI, true );
                }
            }

            // we trap the case of importing ourself (which may happen via an indirect imports chain)
            if (in != model) {
                // queue the imports from the input model on the end of the read queue
                queueImports( in, readQueue, model.getProfile() );

                // add to the imports union graph, but don't do the rebind yet
                model.addSubModel( in, false );

                // we also cache the model if we haven't seen it before (and caching is on)
                addModel( importURI, in );
            }
        }
    }


    /**
     * <p>
     * Load into the given model from the given URI, or from a local cache URI if defined.
     * </p>
     *
     * @param model A model to read statements into
     * @param uri A URI string
     * @param warn If true, warn on RDF exception
     * @return True if the uri was read successfully
     */
    protected boolean read( Model model, String uri, boolean warn ) {
        boolean success = false;
        try {
            getFileManager().readModel( model, uri );
            success = true;
        }
        catch (Exception e) {
            log.warn( "An error occurred while attempting to read from " + uri + ". Msg was '" + e.getMessage() + "'.", e );
        }
        return success;
    }


    /**
     * <p>Set the default option settings.</p>
     */
    protected void setDefaults() {
        setCacheModels( true );
        setUseDeclaredPrefixes( true );
        setProcessImports( true );
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
