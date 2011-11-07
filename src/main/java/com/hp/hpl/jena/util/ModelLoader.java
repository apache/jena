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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;

/** A set of static convenience methods for getting models
 *  The loader will guess the language/type of the model using
 *  {@link #guessLang(String) guessLang}
 *
 * @author Andy Seaborne
 * @version $Id: ModelLoader.java,v 1.1 2009-06-29 08:55:47 castagna Exp $
 */

public class ModelLoader
{
    static Logger log = LoggerFactory.getLogger(ModelLoader.class)  ;
    
    /** @deprecated Use FileUtils.FileUtils.langXML */
    @Deprecated
    public static final String langXML          = FileUtils.langXML ;
    
    /** @deprecated Use FileUtils.langXMLAbbrev */
    @Deprecated
    public static final String langXMLAbbrev    = FileUtils.langXMLAbbrev ;
    
    /** @deprecated Use FileUtils.langNTriple */
    @Deprecated
    public static final String langNTriple      = FileUtils.langNTriple ;
    
    /** @deprecated Use FileUtils.langN3 */
    @Deprecated
    public static final String langN3           = FileUtils.langN3 ;
    // Non-standard
    /** @deprecated Use FileUtils.langBDB */
    @Deprecated
    public static final String langBDB          = FileUtils.langBDB ;
    /** @deprecated Use FileUtils.langSQL */
    @Deprecated
    public static final String langSQL          = FileUtils.langSQL ;

    
    /** Load a model
     *  @deprecated Use FileManager.get().loadModel(urlStr)
     * @param urlStr    The URL or file name of the model
     */

    @Deprecated
    public static Model loadModel(String urlStr) { return loadModel(urlStr, null) ; }

	/** Load a model or attached a persistent store (but not a database).
	 *  @deprecated Use FileManager.get().loadModel(urlStr, lang)
	 * @param urlStr	The URL or file name of the model
	 * @param lang		The language of the data - if null, the system guesses
	 */

    @Deprecated
    public static Model loadModel(String urlStr, String lang)
    {
        try {
    	    return FileManager.get().loadModel(urlStr, lang) ;
        } catch (JenaException ex) { return null ; }
    }

	/** Load a model or attached a persistent store.
     *  Tries to guess syntax type.
     *  Database paramters only needed if its a database.
	 *
	 * @param urlStr		The URL or file name of the model
	 * @param lang			The language of the data - if null, the system guesses
	 * @param dbUser		Database user name (for RDB/JDBC)
	 * @param dbPassword	Database password (for RDB/JDBC)
     * @param modelName     The name of the model 
     * @param dbType        Database type (e.g. MySQL)
     * @param driver        JDBC driver to load.
     * @return Model
	 */


    public static Model loadModel(String urlStr, String lang,
                                  String dbUser, String dbPassword,
                                  String modelName, String dbType, String driver)
    {
        // Wild guess at the language!
        if ( lang == null )
            lang = guessLang(urlStr) ;

        if ( lang == null )
            lang = FileUtils.langXML ;

        if ( lang.equals(FileUtils.langBDB) )
        {
        	// @@ temporarily not supported        	
            LoggerFactory.getLogger(ModelLoader.class).error("Failed to open Berkeley database") ;
            return null ;
/*
            // URL had better be a file!
            if ( basename != null )
                urlStr = basename+File.separator+urlStr ;

            String dirBDB = getDirname(urlStr) ;
            if ( dirBDB == null || dirBDB.length() == 0)
                   dirBDB = "." ;
            urlStr = getBasename(urlStr) ;

            log.debug("BDB: file="+urlStr+", dir="+dirBDB+", basename="+basename) ;

            try {
                Model model = new ModelBdb(new StoreBdbF(dirBDB, urlStr)) ;
                return model ;
            } catch (JenaException rdfEx)
            {
                log.severe("Failed to open Berkeley database", rdfEx) ;
                System.exit(1) ;
            }
            */
        }

        if ( lang.equals(FileUtils.langSQL) )
            return connectToDB(urlStr, dbUser, dbPassword, modelName, dbType, driver) ;

        // Its a files.
		// Language is N3, RDF/XML or N-TRIPLE
        Model m = ModelFactory.createDefaultModel() ;

        m.setReaderClassName(FileUtils.langXML, com.hp.hpl.jena.rdf.arp.JenaReader.class.getName());
        m.setReaderClassName(FileUtils.langXMLAbbrev, com.hp.hpl.jena.rdf.arp.JenaReader.class.getName());

        // Default.
        //m.setReaderClassName(langNTriple, com.hp.hpl.jena.rdf.arp.NTriple.class.getName()) ;

        try {
            FileManager.get().readModel(m, urlStr, null, lang) ;
        } catch (JenaException rdfEx)
        {
            log.warn("Error loading data source", rdfEx);
            return null ;
        }
        return m ;
    }

    /** Load a model from a file into a model.
     * @deprecated Use FileManager.get().readModel(model, urlStr) instead
     * @param model   Model to read into
     * @param urlStr  URL (or filename) to read from
     * @return Returns the model passed in.
     */

    @Deprecated
    public static Model loadModel(Model model, String urlStr)
    {
        return loadModel(model, urlStr, null) ;
    }
    /** Load a model from a file into a model.
     * @deprecated Use FileManager.get().readModel(model, urlStr, lang) instead
     * @param model   Model to read into
     * @param urlStr  URL (or filename) to read from
     * @param lang    Null mean guess based on the URI String
     * @return Returns the model passed in.
     */

    @Deprecated
    public static Model loadModel(Model model, String urlStr, String lang)
    {
        try {
            return FileManager.get().readModel(model, urlStr, null, lang) ;
        }
        catch (Exception e)
        {
            LoggerFactory.getLogger(ModelLoader.class).warn("No such data source: "+urlStr);
            return null ;
        }
    }

    /**
     * Connect to a database.
     * @param urlStr
     * @param dbUser
     * @param dbPassword
     * @param dbType
     * @param driverName   Load this driver (if not null)
     * @return Model
     */

    public static Model connectToDB(String urlStr,
                                    String dbUser, String dbPassword,
                                    String modelName,
                                    String dbType, String driverName)
    {
        // Fragment ID is the model name.
        try { 
            if ( driverName != null )
                Class.forName(driverName).newInstance();
        } catch (Exception ex) {}

        try {
            IDBConnection conn = 
                ModelFactory.createSimpleRDBConnection(urlStr, dbUser, dbPassword, dbType) ;
            return ModelRDB.open(conn, modelName) ;
        } catch (JenaException rdfEx)
        {
            LoggerFactory.getLogger(ModelLoader.class).error("Failed to open SQL database: ModelLoader.connectToDB", rdfEx) ;
            throw rdfEx ;
        }
    }
    /** Guess the language/type of model data. Updated by Chris, hived off the
     * model-suffix part to FileUtils as part of unifying it with similar code in FileGraph.
     * 
     * <ul>
     * <li> If the URI of the model starts jdbc: it is assumed to be an RDB model</li>
     * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
     * <li> If the URI end .nt, it is assumed to be N-Triples</li>
     * <li> If the URI end .bdb, it is assumed to be BerkeleyDB model [suppressed at present]</li>
     * </ul>
     * @deprecated Use FileUtils.guessLang
     * @param urlStr    URL to base the guess on
     * @param defaultLang Default guess
     * @return String   Guessed syntax - or the default supplied
     */

    @Deprecated
    public static String guessLang( String urlStr, String defaultLang )
    {
        if ( urlStr.startsWith("jdbc:") || urlStr.startsWith("JDBC:") )
            return FileUtils.langSQL ;
        else
        	return FileUtils.guessLang( urlStr, defaultLang );
    }
    
	/** Guess the language/type of model data
     *  
     * 
	 * <ul>
	 * <li> If the URI of the model starts jdbc: it is assumed to be an RDB model</li>
	 * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
	 * <li> If the URI end .nt, it is assumed to be N-Triples</li>
	 * <li> If the URI end .bdb, it is assumed to be BerkeleyDB model</li>
	 * </ul>
     * @deprecated Use FileUtils.guessLang
     * @param urlStr    URL to base the guess on
     * @return String   Guessed syntax - null for no guess.
	 */

    @Deprecated
    public static String guessLang(String urlStr)
    {
        return FileUtils.guessLang(urlStr) ;
    }
}
