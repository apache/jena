/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            10 Jan 2001
 * Filename           $RCSfile: DAMLLoader.java,v $
 * Revision           $Revision: 1.9 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:05:23 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.vocabulary.*;




/**
 * <p>Obsolete. In Jena 1, this class was used to load
 * DAML ontologies from either input readers or named files, and map the DAML resources
 * to their corresponding DAML abstractions.  Using the new polymorphism support in 
 * Jena 2, this is no longer necessary.  DAMLLoader is kept as the means of controlling
 * some of the behaviours of the DAML model, such as processing imports.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLLoader.java,v 1.9 2005-02-21 12:05:23 andy_seaborne Exp $
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



    // Instance variables
    //////////////////////////////////

    /** A status flag we can use to inform callers what just happened */
    private long m_status = STATUS_OK;

    /** A reference to the storage service this class is using */
    private DAMLModel m_damlModel = null;

    /** List of ontologies that are not automatically fetched when loading containing ontologies */
    private List m_importBlockList = new ArrayList();

    /** Flag used to control the import blocking behaviour */
    private boolean m_useImportBlocking = true;


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
        m_importBlockList.add( DAMLVocabulary.NAMESPACE_DAML_2000_12_URI );
        m_importBlockList.add( DAMLVocabulary.NAMESPACE_DAML_2001_03_URI );
        m_importBlockList.add( RDFS.getURI() );
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
        return m_damlModel.getDocumentManager().getProcessImports();
    }


    /**
     * Set the flag to control whether imported ontologies are to be loaded. Default
     * true.
     *
     * @param loadImports If true, ontologies that are included in this one, via
     *                     the &lt;imports&gt; element are loaded as they are discovered.
     */
    public void setLoadImportedOntologies( boolean loadImports ) {
        m_damlModel.getDocumentManager().setProcessImports( loadImports );
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
        return m_damlModel.hasLoadedImport( uri );
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
     * Clear the status flags. Resets the status to {@link #STATUS_OK}.
     */
    public void resetStatus() {
        m_status = STATUS_OK;
    }



    // Internal implementation methods
    //////////////////////////////////



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}

/*
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 */

