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

import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.JenaException ;

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

 
    /** Guess the language/type of model data. Updated by Chris, hived off the
     * model-suffix part to FileUtils as part of unifying it with similar code in FileGraph.
     * 
     * <ul>
     * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
     * <li> If the URI ends ".nt", it is assumed to be N-Triples</li>
     * <li> If the URI ends ".ttl", it is assumed to be Turtle</li>
     * <li> If the URI ends ".owl", it is assumed to be RDF/XML</li>
     * </ul>
     * @deprecated Use FileUtils.guessLang
     * @param urlStr    URL to base the guess on
     * @param defaultLang Default guess
     * @return String   Guessed syntax - or the default supplied
     */

    @Deprecated
    public static String guessLang( String urlStr, String defaultLang )
    {
        return FileUtils.guessLang( urlStr, defaultLang );
    }
    
	/** Guess the language/type of model data
     *  
     * 
	 * <ul>
     * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
     * <li> If the URI ends ".nt", it is assumed to be N-Triples</li>
     * <li> If the URI ends ".ttl", it is assumed to be Turtle</li>
     * <li> If the URI ends ".owl", it is assumed to be RDF/XML</li>
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
