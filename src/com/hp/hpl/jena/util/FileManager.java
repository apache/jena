/*
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.* ;
import java.util.* ;

import org.apache.commons.logging.*; 
//import javax.servlet.* ;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.shared.*;

/** FileManager
 * 
 * A FileManager provides access to named file-like resources by opening
 * InputStreams to things in the filing system, by URL (http: and file:) amd
 * found by the classloader.  It can also load RDF data from such a system
 * resource into an existing model or create a new (Memory-based) model.
 * There is a global FileManager which provide uniform access to system
 * resources: applications may also create specialised FileManagers.
 * 
 * A FileManager contains a list of location functions to try: the global
 * FileManger has one {@link LocatorFile}, one {@link LocatorClassLoader} and
 * one {@link LocatorURL}
 * 
 * A FileManager works in conjunction with a LocationMapper.
 * A {@link LocationMapper} is a set of alternative locations for system
 * resources and a set of alternative prefix locations.  For example, a local
 * copy of a common RDF dataset may be used whenever the usual URL is used by
 * the application.
 *
 * The FileManager also supports the idea of "current directory".  This only
 * applies to the LocatorFile and not, for example, items found by a classloader.
 *
 * 
 * @see LocationMapper
 * 
 * @author     Andy Seaborne
 * @version    $Id: FileManager.java,v 1.14 2005-02-15 16:06:38 chris-dollin Exp $
 */
 
public class FileManager
{
    /** Delimiter between path entries : because URI scheme names use : we only allow ; */
    public static final String PATH_DELIMITER = ";";
    public static final String filePathSeparator = java.io.File.separator ;
    static Log log = LogFactory.getLog(FileManager.class) ;

    static FileManager instance = null ;

    static boolean logAllLookups = true ; 
    List handlers = new ArrayList() ;
    LocationMapper mapper = null ;
    boolean cacheModelLoads = false ;
    Map modelCache = null ;
    
    
    /** Get the global file manager.
     * @return the global file manager
     */
    public static FileManager get()
    {
        // Singleton pattern adopted in case we later have several file managers.
        if ( instance == null )
            instance = makeGlobal() ;
        return instance ;
    }
    
    /** Create an uninitialized FileManager */
    public FileManager() {}

    /** Create a standard FileManager. */
    private static FileManager makeGlobal()
    {
        FileManager fMgr = new FileManager(LocationMapper.get()) ;
        fMgr.addLocatorFile() ;
        fMgr.addLocatorURL() ;
        fMgr.addLocatorSystemClassLoader() ;
        return fMgr ;
    }
    
    /** Create with the given location mapper */
    public FileManager(LocationMapper _mapper)
    {
        setMapper(_mapper) ;
    }

    /** Set the location mapping */
    public void setMapper(LocationMapper _mapper)
    {
        mapper = _mapper ;
    }
    
    
    /** Return an iterator over all the handlers */
    public Iterator locators() { return handlers.listIterator() ; }

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
    
    /** Add the system class loader */ 
    public void addLocatorSystemClassLoader()
    {
        addLocatorClassLoader(ClassLoader.getSystemClassLoader()) ;
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

    /** Reset the model cache */
    public void resetCache()
    {
        if ( modelCache != null )
        {
            for ( Iterator iter = modelCache.keySet().iterator() ; iter.hasNext() ; )
            {
                String name = (String)iter.next() ;
                Model m = (Model)modelCache.remove(name) ;
                if ( m != null )
                    m.close() ;
            }
            modelCache = new HashMap() ;
        }
    }
    
    /** Change the state of model cache : does not clear the cache */ 
    
    public void setModelCaching(boolean state)
    {
        cacheModelLoads = state ;
        if ( cacheModelLoads && modelCache == null )
            modelCache = new HashMap() ; 
    }
    
    /** Load a model from a file (local or remote).
     *  Guesses the syntax of the file based on filename extension, 
     *  defaulting to RDF/XML.
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
     */

    public Model loadModel(String filenameOrURI)
    { return loadModel(filenameOrURI, null) ; }

    /** Load a model from a file (local or remote).
     *  URI is teh base for reading the model.
     * 
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @param rdfSyntax  RDF Serialization syntax. 
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
     */

    public Model loadModel(String filenameOrURI, String rdfSyntax)
    {
        return loadModel(filenameOrURI, null, rdfSyntax) ;
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
        if ( modelCache != null && modelCache.containsKey(filenameOrURI) )
        {
            log.debug("Model cache hit: "+filenameOrURI) ;
            return (Model)modelCache.get(filenameOrURI) ;
        }
        
        Model m = ModelFactory.createDefaultModel() ;
        readModel(m, filenameOrURI, baseURI, rdfSyntax) ;
        if ( this.cacheModelLoads )
        {
            if ( modelCache == null )
                modelCache = new HashMap() ;
            modelCache.put(filenameOrURI, m) ;
        }
        return m ;
    }
    
    /**
     * Read a file of RDF into a model.
     * @param model
     * @param filenameOrURI
     * @return The model or null, if there was an error.
     *  @exception JenaException if there is syntax error in file.
     */    

    public Model readModel(Model model, String filenameOrURI)
    { return readModel(model, filenameOrURI, null); }
    
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
        return readModel(model, filenameOrURI, null, rdfSyntax);
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
        if ( baseURI == null )
            baseURI = chooseBaseURI(filenameOrURI) ;

        String mappedURI = remap(filenameOrURI) ;

        if ( syntax == null )
        {
            syntax = FileUtils.guessLang(mappedURI) ;
            if ( syntax == null || syntax.equals("") )
                syntax = FileUtils.langXML ;
            if ( log.isDebugEnabled() ) 
                log.debug("Syntax guess: "+syntax);
        }

        InputStream in = openNoMap(mappedURI) ;
        if ( in == null )
        {
            if ( log.isTraceEnabled() )
                log.trace("Failed to locate '"+mappedURI+"'") ;
            throw new NotFoundException("Not found: "+filenameOrURI) ;
        }
        model.read(in, baseURI, syntax) ;
        return model ;
    }

    // replace with RelURI.resolve()
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
        
        String uri = remap(filenameOrURI) ;
        
        return openNoMap(uri) ;
    }

    /** Apply the mapping of a filename or URI */
    
    public String remap(String filenameOrURI)
    {
        if ( mapper == null )
            return filenameOrURI ; 
            
        String uri = mapper.altMapping(filenameOrURI, null) ;

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
        try {
            Reader r = FileUtils.asBufferedUTF8(in) ;
            StringWriter sw = new StringWriter(1024);
            char buff[] = new char[1024];
            while (r.ready()) {
                int l = r.read(buff);
                if (l <= 0)
                    break;
                sw.write(buff, 0, l);
            }
            r.close();
            sw.close();
            return sw.toString();
        } catch (IOException ex)
        {
            throw new WrappedIOException(ex) ;
        }
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
        for ( Iterator iter = handlers.iterator() ; iter.hasNext() ; )
        {
            Locator loc = (Locator)iter.next() ;
            InputStream in = loc.open(filenameOrURI) ;
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



/*
 *  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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
 
