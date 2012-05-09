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

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.ontology.impl.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;


/**
 * <p>
 * Provides a means to map between the URI's that represent ontology languages
 * and their language profiles.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:ian_dickinson@users.sourceforge.net" >email</a>)
 * @version CVS $Id: ProfileRegistry.java,v 1.2 2009-10-06 13:04:34 ian_dickinson Exp $
 */
public class ProfileRegistry {
    // Constants
    //////////////////////////////////

    /** The URI that maps to the language profile for OWL-Full */
    public static final String OWL_LANG = OWL.FULL_LANG.getURI();

    /** The URI that maps to the language profile for OWL-DL */
    public static final String OWL_DL_LANG = OWL.DL_LANG.getURI();

    /** The URI that maps to the language profile for OWL-Lite */
    public static final String OWL_LITE_LANG = OWL.LITE_LANG.getURI();

    /** The URI that maps to the language profile for DAML+OIL */
    public static final String DAML_LANG = DAMLVocabulary.NAMESPACE_DAML_2001_03_URI;

    /** The URI that maps to the language profile for RDFS */
    public static final String RDFS_LANG = RDFS.getURI();


    // Static variables
    //////////////////////////////////

    private static Object[][] s_initData = new Object[][] {
        {OWL_LANG,      new OWLProfile()},
        {OWL_DL_LANG,   new OWLDLProfile()},
        {OWL_LITE_LANG, new OWLLiteProfile()},
        {DAML_LANG,     new DAML_OILProfile()},
        {RDFS_LANG,     new RDFSProfile()}
    };


    /** Singleton instance */
    private static ProfileRegistry s_instance = new ProfileRegistry();


    // Instance variables
    //////////////////////////////////

    /** Maps from public URI's to language profiles */
    private Map<String,Profile> m_registry = new HashMap<String, Profile>();


    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Singleton constructor
     * </p>
     */
    private ProfileRegistry() {
        for (int i = 0;  i < s_initData.length;  i++) {
            registerProfile( (String) s_initData[i][0], (Profile) s_initData[i][1] );
        }
    }


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the singleton instance
     * </p>
     *
     * @return The singleton registry
     */
    public static ProfileRegistry getInstance() {
        return s_instance;
    }


    /**
     * <p>
     * Add a language profile with the given URI key
     * </p>
     *
     * @param uri The URI denoting the language
     * @param profile The language profile for the language
     */
    public void registerProfile( String uri, Profile profile ) {
        m_registry.put( uri, profile );
    }


    /**
     * <p>
     * Answer the language profile for the given language URI, or null if not known.
     * </p>
     *
     * @param uri A URI denoting an ontology language
     * @return An ontology language profile for that language
     */
    public Profile getProfile( String uri ) {
        return m_registry.get( uri );
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
