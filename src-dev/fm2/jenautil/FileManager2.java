/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package fm2.jenautil;

import java.io.File ;
import java.io.IOException ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;

import org.openjena.atlas.web.TypedStream ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.NotFoundException ;
import com.hp.hpl.jena.util.FileUtils ;

import fm2.atlas.LocationMapper ;
import fm2.atlas.Locator ;
import fm2.atlas.LocatorClassLoader ;
import fm2.atlas.LocatorFile ;
import fm2.atlas.LocatorURL ;
import fm2.atlas.LocatorZip ;
import fm2.atlas.StreamManager ;

/** FileManager
 * 
 * A FileManager provides access to named file-like resources by opening
 * InputStreams to things in the filing system, by URL (http: and file:) and
 * found by the classloader.  It can also load RDF data from such a system
 * resource into an existing model or create a new (Memory-based) model.
 * There is a global FileManager which provide uniform access to system
 * resources: applications may also create specialised FileManagers.
 * 
 * A FileManager contains a list of location functions to try: the global
 * FileManger has one {@link LocatorFile}, one {@link LocatorClassLoader} and
 * one {@link LocatorURL}
 * 
 * Main operations:
 *  * <ul>
 * <li>loadModel, readModel : URI to model</li>
 * <li>open, openNoMap : URI to input stream</li>
 * <li>mapURI : map URI to another by {@link LocationMapper}</li> 
 * </ul>
 * 
 * Utilities:
 * <ul>
 * <li>readWholeFileAsUTF8</li>
 * <li>optional caching of models<li>
 * </ul>
 * 
 * A FileManager works in conjunction with a LocationMapper.
 * A {@link LocationMapper} is a set of alternative locations for system
 * resources and a set of alternative prefix locations.  For example, a local
 * copy of a common RDF dataset may be used whenever the usual URL is used by
 * the application.
 *
 * The {@link LocatorFile} also supports the idea of "current directory".
 * 
 * @see LocationMapper
 * @see FileUtils
 */
 
public class FileManager2 extends StreamManager
{
    /** Delimiter between path entries : because URI scheme names use : we only allow ; */
    public static final String PATH_DELIMITER = ";";
    public static final String filePathSeparator = java.io.File.separator ;
    private static Logger log = LoggerFactory.getLogger(FileManager2.class) ;

    static FileManager2 instance = null ;

    boolean cacheModelLoads = false ;
    Map<String, Model> modelCache = null ;
    
    /** Get the global file manager.
     * @return the global file manager
     */
    public static FileManager2 get()
    {
        // Singleton pattern adopted in case we later have several file managers.
        if ( instance == null )
            instance = makeGlobal() ;
        return instance ;
    }
    
    /** Set the global file manager (as returned by get())
     * If called before any call to get(), then the usual default filemanager is not created 
     * @param globalFileManager
     */
    public static void setGlobalFileManager(FileManager2 globalFileManager)
    {
        instance = globalFileManager ;
    }
    
    /** Create an uninitialized FileManager */
    public FileManager2() {}
    
    /** Create a new file manager that is a deep copy another.
     *  Location mapper and locators chain are copied (the locators are not cloned).
     *  The model cache is not copied and is initially set to not cache.
     * @param filemanager
     */
    public FileManager2(FileManager2 filemanager)
    {
        handlers.addAll(filemanager.handlers) ;
        mapper = null ;
        if ( filemanager.getLocationMapper() != null )
            mapper = new LocationMapper(filemanager.getLocationMapper()) ;
        cacheModelLoads = false ;
        modelCache = null ;
    }

    /** Create a "standard" FileManager. */
    public static FileManager2 makeGlobal()
    {
        FileManager2 fMgr = new FileManager2(JenaIOEnvironment.get()) ;
        setStdLocators(fMgr) ;
        return fMgr ;
    }
    
    /** Force a file handler to have the default configuration. */
    public static void setStdLocators(FileManager2 fMgr)
    {
        fMgr.handlers.clear() ;
        fMgr.addLocatorFile() ;
        fMgr.addLocatorURL() ;
        fMgr.addLocatorClassLoader(fMgr.getClass().getClassLoader()) ;
    }
    /** Create with the given location mapper */
    public FileManager2(LocationMapper _mapper)    { setLocationMapper(_mapper) ; }

    /** @deprecated Use setLocationMapper */
    @Deprecated
    public void setMapper(LocationMapper _mapper) { setLocationMapper(_mapper) ; }
    
    
    /** Set the location mapping */
    public void setLocationMapper(LocationMapper _mapper) { mapper = _mapper ; }
    
    /** Get the location mapping */
    public LocationMapper getLocationMapper() { return mapper ; }
    
    /** Return an iterator over all the handlers */
    public Iterator<Locator> locators() { return handlers.listIterator() ; }

    /** Add a locator to the end of the locators list */ 
    public void addLocator(Locator loc)
    {
        log.debug("Add location: "+loc.getName()) ;
        handlers.add(loc) ; }

    /** Add a file locator */ 
    public void addLocatorFile() { addLocatorFile(null) ; } 

    /** Add a file locator which uses dir as its working directory */ 
    public void addLocatorFile(String dir)
    {
        LocatorFile fLoc = new LocatorFile(dir) ;
        addLocator(fLoc) ;
    }
    
    /** Add a class loader locator */ 
    public void addLocatorClassLoader(ClassLoader cLoad)
    {
        LocatorClassLoader cLoc = new LocatorClassLoader(cLoad) ;
        addLocator(cLoc) ;
    }

    /** Add a URL locator */
    public void addLocatorURL()
    {
        Locator loc = new LocatorURL() ;
        addLocator(loc) ;
    }

    /** Add a zip file locator */
    public void addLocatorZip(String zfn)
    {
        Locator loc = new LocatorZip(zfn) ;
        addLocator(loc) ;
    }

    
    /** Remove a locator */ 
    public void remove(Locator loc) { handlers.remove(loc) ; }

    // -------- Cache operations
    
    /** Reset the model cache */
    public void resetCache()
    {
        if ( modelCache != null )
            modelCache.clear() ;
    }
    
    /** Change the state of model cache : does not clear the cache */ 
    
    public void setModelCaching(boolean state)
    {
        cacheModelLoads = state ;
        if ( cacheModelLoads && modelCache == null )
            modelCache = new HashMap<String, Model>() ;
    }
    
    /** return whether caching is on of off */
    public boolean getCachingModels() { return cacheModelLoads ; }
    
    /** Read out of the cache - return null if not in the cache */ 
    public Model getFromCache(String filenameOrURI)
    { 
        if ( ! getCachingModels() )
            return null; 
        return modelCache.get(filenameOrURI) ;
    }
    
    public boolean hasCachedModel(String filenameOrURI)
    { 
        if ( ! getCachingModels() )
            return false ; 
        return modelCache.containsKey(filenameOrURI) ;
    }
    
    public void addCacheModel(String uri, Model m)
    { 
        if ( getCachingModels() )
            modelCache.put(uri, m) ;
    }

    public void removeCacheModel(String uri)
    { 
        if ( getCachingModels() )
            modelCache.remove(uri) ;
    }

    // -------- Cache operations (end)

    /** Load a model from a file (local or remote).
     *  Guesses the syntax of the file based on filename extension, 
     *  defaulting to RDF/XML.
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
     */

    public Model loadModel(String filenameOrURI)
    { 
        if ( log.isDebugEnabled() )
            log.debug("loadModel("+filenameOrURI+")") ;
        
        return loadModelWorker(filenameOrURI, null, null) ;
    }


    /** Load a model from a file (local or remote).
     *  URI is the base for reading the model.
     * 
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @param rdfSyntax  RDF Serialization syntax. 
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
     */

    public Model loadModel(String filenameOrURI, String rdfSyntax)
    {
        if ( log.isDebugEnabled() )
            log.debug("loadModel("+filenameOrURI+", "+rdfSyntax+")") ;
        return loadModelWorker(filenameOrURI, null, rdfSyntax) ;
    }
    
    /** Load a model from a file (local or remote).
     * 
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @param baseURI  Base URI for loading the RDF model.
     *  @param rdfSyntax  RDF Serialization syntax. 
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
    */


    public Model loadModel(String filenameOrURI, String baseURI, String rdfSyntax)
    {
        if ( log.isDebugEnabled() )
            log.debug("loadModel("+filenameOrURI+", "+baseURI+", "+rdfSyntax+")") ;

        return loadModelWorker(filenameOrURI, baseURI, rdfSyntax) ;
    }

    private Model loadModelWorker(String filenameOrURI, String baseURI, String rdfSyntax)
    {
        // Better: if ( hasCachedModel(filenameOrURI) ) return getFromCache(filenameOrURI) ;  
        if ( modelCache != null && modelCache.containsKey(filenameOrURI) )
        {
            if ( log.isDebugEnabled() )
                log.debug("Model cache hit: "+filenameOrURI) ;
            return modelCache.get(filenameOrURI) ;
        }

        Model m = ModelFactory.createDefaultModel() ;
        readModelWorker(m, filenameOrURI, baseURI, rdfSyntax) ;
        
        if ( this.cacheModelLoads )
            modelCache.put(filenameOrURI, m) ;
        return m ;
    }
    
    /**
     * Read a file of RDF into a model.  Guesses the syntax of the file based on filename extension, 
     *  defaulting to RDF/XML.
     * @param model
     * @param filenameOrURI
     * @return The model or null, if there was an error.
     *  @exception JenaException if there is syntax error in file.
     */    

    public Model readModel(Model model, String filenameOrURI)
    {
        if ( log.isDebugEnabled() )
            log.debug("readModel(model,"+filenameOrURI+")") ;
        return readModel(model, filenameOrURI, null);
    }
    
    /**
     * Read a file of RDF into a model.
     * @param model
     * @param filenameOrURI
     * @param rdfSyntax RDF Serialization syntax.
     * @return The model or null, if there was an error.
     *  @exception JenaException if there is syntax error in file.
     */    

    public Model readModel(Model model, String filenameOrURI, String rdfSyntax)
    {
        if ( log.isDebugEnabled() )
            log.debug("readModel(model,"+filenameOrURI+", "+rdfSyntax+")") ;
        return readModelWorker(model, filenameOrURI, null, rdfSyntax);
    }

    /**
     * Read a file of RDF into a model.
     * @param model
     * @param filenameOrURI
     * @param baseURI
     * @param syntax
     * @return The model
     *  @exception JenaException if there is syntax error in file.
     */    

    public Model readModel(Model model, String filenameOrURI, String baseURI, String syntax)
    {
        
        if ( log.isDebugEnabled() )
            log.debug("readModel(model,"+filenameOrURI+", "+baseURI+", "+syntax+")") ;
        return readModelWorker(model, filenameOrURI, baseURI, syntax) ;
    }
    
    private Model readModelWorker(Model model, String filenameOrURI, String baseURI, String syntax)
    {
        // Doesn't call open() - we want to make the synatx guess based on the mapped URI.
        String mappedURI = mapURI(filenameOrURI) ;

        if ( log.isDebugEnabled() && ! mappedURI.equals(filenameOrURI) )
            log.debug("Map: "+filenameOrURI+" => "+mappedURI) ;

        if ( syntax == null && baseURI == null && mappedURI.startsWith( "http:" ) )
        {
            // No syntax, no baseURI, HTTP URL ==> use content negotiation
            model.read(mappedURI) ;
            return model ;
        }
        
        if ( syntax == null )
        {
            syntax = FileUtils.guessLang(mappedURI) ;
            if ( syntax == null || syntax.equals("") )
                syntax = FileUtils.langXML ;
            if ( log.isDebugEnabled() ) 
                log.debug("Syntax guess: "+syntax);
        }

        if ( baseURI == null )
            baseURI = chooseBaseURI(filenameOrURI) ;

        TypedStream in = openNoMapOrNull(mappedURI) ;
        if ( in == null )
        {
            if ( log.isDebugEnabled() )
                log.debug("Failed to locate '"+mappedURI+"'") ;
            throw new NotFoundException("Not found: "+filenameOrURI) ;
        }
        if ( in.getMediaType() != null )
        {
            // XXX
            //syntax
        }
        model.read(in.getInput(), baseURI, syntax) ;
        try { in.getInput().close(); } catch (IOException ex) {}
        return model ;
    }

    private static String chooseBaseURI(String baseURI)
    {
        String scheme = FileUtils.getScheme(baseURI) ;
        
        if ( scheme != null )
        {
            if ( scheme.equals("file") )
            {
                if ( ! baseURI.startsWith("file:///") )
                {
                    try {
                        // Fix up file URIs.  Yuk.
                        String tmp = baseURI.substring("file:".length()) ;
                        File f = new File(tmp) ;
                        baseURI = "file:///"+f.getCanonicalPath() ;
                        baseURI = baseURI.replace('\\','/') ;

//                        baseURI = baseURI.replace(" ","%20");
//                        baseURI = baseURI.replace("~","%7E");
                        // Convert to URI.  Except that it removes ///
                        // Could do that and fix up (again)
                        //java.net.URL u = new java.net.URL(baseURI) ;
                        //baseURI = u.toExternalForm() ;
                    } catch (Exception ex) {}
                }
            }
            return baseURI ;
        }
            
        if ( baseURI.startsWith("/") )
            return "file://"+baseURI ;
        return "file:"+baseURI ;
    }
    
    /** @deprecated Use mapURI */
    @Deprecated
    public String remap(String filenameOrURI)
    { return mapURI(filenameOrURI) ; }
}

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 */
 
