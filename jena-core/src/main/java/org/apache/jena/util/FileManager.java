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

package org.apache.jena.util;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.JenaException;

public interface FileManager {
    
    public static final boolean logAllLookups = true ; 
    public static final String PATH_DELIMITER = ";";
    public static final String filePathSeparator = java.io.File.separator ;

    /** For usage with Jena libraries only */
    public static FileManager getInternal() { return FileManagerImpl.get(); }
    
    /** @deprecated Use {@code StreamManager} if needed; to read RDF files, use {@code RDFDataMgr}. */   
    @Deprecated
    public static FileManager get() { return getInternal(); }
    
    /** @deprecated Use {@code StreamManager.setGlobal} */   
    @Deprecated
    public static void setGlobalFileManager(FileManager fm) { FileManagerImpl.setGlobalFileManager(fm); }
    
    public static FileManager create() { return new FileManagerImpl(); }
    public static FileManager createStd() { return FileManagerImpl.makeStd(); }
    
    public static FileManager create(LocationMapper locMap) { return new FileManagerImpl(locMap); }
    
    FileManager clone();
    
    /** Set the location mapping */
    void setLocationMapper(LocationMapper _mapper);

    /** Get the location mapping */
    LocationMapper getLocationMapper();

    /** Return an iterator over all the handlers */
    Iterator<Locator> locators();

    /** Add a locator to the end of the locators list */
    void addLocator(Locator loc);

    /** Add a file locator */
    void addLocatorFile();

    /** Add a file locator which uses dir as its working directory */
    void addLocatorFile(String dir);

    /** Add a class loader locator */
    void addLocatorClassLoader(ClassLoader cLoad);

    /** Add a URL locator */
    void addLocatorURL();

    /** Add a zip file locator */
    void addLocatorZip(String zfn);

    /** Remove a locator */
    void remove(Locator loc);

    /** Reset the model cache */
    @Deprecated
    void resetCache();

    /** Change the state of model cache : does not clear the cache */
    @Deprecated
    void setModelCaching(boolean state);

    /** return whether caching is on of off */
    @Deprecated
    boolean isCachingModels();

    /** Read out of the cache - return null if not in the cache */
    @Deprecated
    Model getFromCache(String filenameOrURI);

    @Deprecated
    boolean hasCachedModel(String filenameOrURI);

    @Deprecated
    void addCacheModel(String uri, Model m);

    @Deprecated
    void removeCacheModel(String uri);

    /** Load a model from a file (local or remote).
     *  This operation may attempt content negotiation for http URLs.
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
     */
    Model loadModel(String filenameOrURI);

    /** Load a model from a file (local or remote).
     *  URI is the base for reading the model.
     * 
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @param rdfSyntax  RDF Serialization syntax. 
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
     */

    @Deprecated
    Model loadModel(String filenameOrURI, String rdfSyntax);

    /** Load a model from a file (local or remote).
     * 
     *  @param filenameOrURI The filename or a URI (file:, http:)
     *  @param baseURI  Base URI for loading the RDF model.
     *  @param rdfSyntax  RDF Serialization syntax. 
     *  @return a new model
     *  @exception JenaException if there is syntax error in file.
    */
    @Deprecated
    Model loadModel(String filenameOrURI, String baseURI, String rdfSyntax);

    /**
     * Read a file of RDF into a model.  Guesses the syntax of the file based on filename extension, 
     *  defaulting to RDF/XML.
     * @param model
     * @param filenameOrURI
     * @return The model or null, if there was an error.
     *  @exception JenaException if there is syntax error in file.
     */
    Model readModel(Model model, String filenameOrURI);

    /**
     * Read a file of RDF into a model.
     * @param model
     * @param filenameOrURI
     * @param rdfSyntax RDF Serialization syntax.
     * @return The model or null, if there was an error.
     *  @exception JenaException if there is syntax error in file.
     */
    @Deprecated
    Model readModel(Model model, String filenameOrURI, String rdfSyntax);

    /**
     * Read a file of RDF into a model.
     * @param model
     * @param filenameOrURI
     * @param baseURI
     * @param syntax
     * @return The model
     *  @exception JenaException if there is syntax error in file.
     */

    @Deprecated
    Model readModel(Model model, String filenameOrURI, String baseURI, String syntax);

    /** Open a file using the locators of this FileManager */
    InputStream open(String filenameOrURI);

    /** Apply the mapping of a filename or URI */
    String mapURI(String filenameOrURI);

    /** Slurp up a whole file */
    @Deprecated
    String readWholeFileAsUTF8(InputStream in);

    /** Slurp up a whole file: map filename as necessary */
    @Deprecated
    String readWholeFileAsUTF8(String filename);

    /** Open a file using the locators of this FileManager 
     *  but without location mapping */
    InputStream openNoMap(String filenameOrURI);

    /** Open a file using the locators of this FileManager 
     *  but without location mapping.
     *  Return null if not found
     */

    TypedStream openNoMapOrNull(String filenameOrURI);

}
