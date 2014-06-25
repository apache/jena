/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.util;

import java.io.* ;
import java.util.* ;

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.impl.IO_Ctl ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.NotFoundException ;
import com.hp.hpl.jena.shared.WrappedIOException ;

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
 
public class FileManager
{
    /** Delimiter between path entries : because URI scheme names use : we only allow ; */
    public static final String PATH_DELIMITER = ";";
    public static final String filePathSeparator = java.io.File.separator ;
    private static Logger log = LoggerFactory.getLogger(FileManager.class) ;

    static FileManager fmInstance = null ;

    static boolean logAllLookups = true ; 
    protected List<Locator> fmHandlers = new ArrayList<>() ;
    protected LocationMapper fmMapper = null ;
    
    // This forces Jena to initialize and wire in RIOT if available.
    // The global FileManager is reset. 
    static { IO_Ctl.init() ; }
    
    /** Get the global file manager.
     * @return the global file manager
     */
    public static FileManager get()
    {
        // Singleton pattern adopted in case we later have several file managers.
        if ( fmInstance == null )
            fmInstance = makeGlobal() ;
        return fmInstance ;
    }
    
    /** Set the global file manager (as returned by get())
     * If called before any call to get(), then the usual default filemanager is not created 
     * @param globalFileManager
     */
    public static void setGlobalFileManager(FileManager globalFileManager)
    {
        fmInstance = globalFileManager ;
    }
    
    /** Create an uninitialized FileManager */
    public FileManager() {}
    
    @Override
    public FileManager clone() { return clone(this) ; } 
 
    // Isolate to help avoid copy errors.
    private static FileManager clone(FileManager filemanager) {
        FileManager newFm = new FileManager() ;
        newFm.fmHandlers.addAll(filemanager.fmHandlers) ;
        newFm.fmMapper = null ;
        if ( filemanager.getLocationMapper() != null )
            newFm.fmMapper = new LocationMapper(filemanager.getLocationMapper()) ;
        newFm.cacheModelLoads = false ;
        newFm.modelCache = null ;
        return newFm ;
    }

    /** Create a "standard" FileManager. */
    public static FileManager makeGlobal()
    {
        FileManager fMgr = new FileManager(LocationMapper.get()) ;
        setStdLocators(fMgr) ;
        return fMgr ;
    }
    
    /** Force a file handler to have the default configuration. */
    public static void setStdLocators(FileManager fMgr)
    {
        fMgr.fmHandlers.clear() ;
        fMgr.addLocatorFile() ;
        fMgr.addLocatorURL() ;
        fMgr.addLocatorClassLoader(fMgr.getClass().getClassLoader()) ;
    }
    /** Create with the given location mapper */
    public FileManager(LocationMapper _mapper)    { setLocationMapper(_mapper) ; }
    
    /** Set the location mapping */
    public void setLocationMapper(LocationMapper _mapper) { fmMapper = _mapper ; }
    
    /** Get the location mapping */
    public LocationMapper getLocationMapper() { return fmMapper ; }
    
    /** Return an iterator over all the handlers */
    public Iterator<Locator> locators() { return fmHandlers.listIterator() ; }

    /** Add a locator to the end of the locators list */ 
    public void addLocator(Locator loc)
    {
        log.debug("Add location: "+loc.getName()) ;
        fmHandlers.add(loc) ; }

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
    public void remove(Locator loc) { fmHandlers.remove(loc) ; }

    // -------- Cache operations
    boolean cacheModelLoads = false ;
    Map<String, Model> modelCache = null ;
    
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
    public boolean isCachingModels() { return cacheModelLoads ; }
    
    /** Read out of the cache - return null if not in the cache */ 
    public Model getFromCache(String filenameOrURI)
    { 
        if ( ! isCachingModels() )
            return null; 
        return modelCache.get(filenameOrURI) ;
    }
    
    public boolean hasCachedModel(String filenameOrURI)
    { 
        if ( ! isCachingModels() )
            return false ; 
        return modelCache.containsKey(filenameOrURI) ;
    }
    
    public void addCacheModel(String uri, Model m)
    { 
        if ( isCachingModels() )
            modelCache.put(uri, m) ;
    }

    public void removeCacheModel(String uri)
    { 
        if ( isCachingModels() )
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
        if ( hasCachedModel(filenameOrURI) )
        {
            if ( log.isDebugEnabled() )
                log.debug("Model cache hit: "+filenameOrURI) ;
            return getFromCache(filenameOrURI) ;
        }

        Model m = ModelFactory.createDefaultModel() ;
        readModelWorker(m, filenameOrURI, baseURI, rdfSyntax) ;
        
        if ( isCachingModels() )
            addCacheModel(filenameOrURI, m) ;
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
    
    protected Model readModelWorker(Model model, String filenameOrURI, String baseURI, String syntax)
    {
        // Doesn't call open() - we want to make the syntax guess based on the mapped URI.
        String mappedURI = mapURI(filenameOrURI) ;

        if ( log.isDebugEnabled() && ! mappedURI.equals(filenameOrURI) )
            log.debug("Map: "+filenameOrURI+" => "+mappedURI) ;

        if ( syntax == null && baseURI == null && mappedURI.startsWith( "http:" ) )
        {
            syntax = FileUtils.guessLang(mappedURI) ;
            // Content negotation in next version (FileManager2) 
            model.read(mappedURI, syntax) ;
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
        if ( in.getMimeType() != null )
        {
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
    
    /** Open a file using the locators of this FileManager */
    public InputStream open(String filenameOrURI)
    {
        if ( log.isDebugEnabled())
            log.debug("open("+filenameOrURI+")") ;
        
        String uri = mapURI(filenameOrURI) ;
        
        if ( log.isDebugEnabled() && ! uri.equals(filenameOrURI) )
            log.debug("open: mapped to "+uri) ;
        
        return openNoMap(uri) ;
    }


    /** Apply the mapping of a filename or URI */
    public String mapURI(String filenameOrURI)
    {
        if ( fmMapper == null )
            return filenameOrURI ; 
            
        String uri = fmMapper.altMapping(filenameOrURI, null) ;

        if ( uri == null )
        {
            if ( FileManager.logAllLookups && log.isDebugEnabled() )
                log.debug("Not mapped: "+filenameOrURI) ;
            uri = filenameOrURI ;
        }
        else
        {
            if ( log.isDebugEnabled() )
                log.debug("Mapped: "+filenameOrURI+" => "+uri) ;
        }
        return uri ;
    }
    
    /** Slurp up a whole file */
    public String readWholeFileAsUTF8(InputStream in)
    {
        try (Reader r = FileUtils.asBufferedUTF8(in); StringWriter sw = new StringWriter(1024)) {
            char buff[] = new char[1024] ;
            while (true) {
                int l = r.read(buff) ;
                if ( l <= 0 )
                    break ;
                sw.write(buff, 0, l) ;
            }
            return sw.toString() ;
        } catch (IOException ex)
        { throw new WrappedIOException(ex) ; }
    }
    
    /** Slurp up a whole file: map filename as necessary */
    public String readWholeFileAsUTF8(String filename)
    {
        InputStream in = open(filename) ;
        if ( in == null )
            throw new NotFoundException("File not found: "+filename) ;
        return readWholeFileAsUTF8(in) ;
    }
        
    /** Open a file using the locators of this FileManager 
     *  but without location mapping */ 
    public InputStream openNoMap(String filenameOrURI)
    {
        TypedStream in = openNoMapOrNull(filenameOrURI) ;
        if ( in == null )
            return null ;
//        if ( in == null )
//            throw new NotFoundException(filenameOrURI) ;
        return in.getInput() ;
    }
    
    /** Open a file using the locators of this FileManager 
     *  but without location mapping.
     *  Return null if not found
     */ 
    
    public TypedStream openNoMapOrNull(String filenameOrURI)
    {
        for (Locator loc : fmHandlers)
        {
            TypedStream in = loc.open(filenameOrURI) ;
            if ( in != null )
            {
                if ( log.isDebugEnabled() )
                    log.debug("Found: "+filenameOrURI+" ("+loc.getName()+")") ;
                return in ;
            }
        }
        return null; 
    }
}
