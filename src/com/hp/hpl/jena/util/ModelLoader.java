/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.util;

import java.io.* ;
import java.net.* ;

import org.apache.log4j.*;

import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.mem.* ;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.db.* ;

/** A set of static convenience methods for getting models
 *  The loader will guess the language/type of the model using
 *  {@link #guessLang(String) guessLang}
 *
 * @author Andy Seaborne
 * @version $Id: ModelLoader.java,v 1.12 2003-08-07 11:24:15 andy_seaborne Exp $
 */

public class ModelLoader
{
    static Logger logger = Logger.getLogger(ModelLoader.class.getName()) ;

    public static final String langXML         = "RDF/XML" ;
    public static final String langXMLAbbrev   = "RDF/XML-ABBREV" ;
    public static final String langNTriple     = "N-TRIPLE" ;
    public static final String langN3          = "N3" ;

    // Non-standard
    public static final String langBDB          = "RDF/BDB" ;
    public static final String langSQL          = "RDF/SQL" ;

    public static String defaultLanguage = langXML ;
    public static String basename = null ;
    public static boolean useARP = true ;

    /** Load a model
     *
     * @param urlStr    The URL or file name of the model
     */

    public static Model loadModel(String urlStr) { return loadModel(urlStr, null) ; }

	/** Load a model or attached a persistent store (but not a database).
	 *
	 * @param urlStr	The URL or file name of the model
	 * @param lang		The language of the data - if null, the system guesses
	 */

    public static Model loadModel(String urlStr, String lang)
    {
    	return loadModel(urlStr, lang, null, null, null, null, null) ;
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
            lang = defaultLanguage ;

        if ( lang.equals(langBDB) )
        {
        	// @@ temporarily not supported        	
            logger.fatal("Failed to open Berkeley database") ;
            System.exit(1) ;
/*
            // URL had better be a file!
            if ( basename != null )
                urlStr = basename+File.separator+urlStr ;

            String dirBDB = getDirname(urlStr) ;
            if ( dirBDB == null || dirBDB.length() == 0)
                   dirBDB = "." ;
            urlStr = getBasename(urlStr) ;

            logger.debug("BDB: file="+urlStr+", dir="+dirBDB+", basename="+basename) ;

            try {
                Model model = new ModelBdb(new StoreBdbF(dirBDB, urlStr)) ;
                return model ;
            } catch (JenaException rdfEx)
            {
                logger.severe("Failed to open Berkeley database", rdfEx) ;
                System.exit(1) ;
            }
            */
        }

        if ( lang.equals(langSQL) )
            return connectToDB(urlStr, dbUser, dbPassword, modelName, dbType, driver) ;

        // Its a files.
		// Language is N3, RDF/XML or N-TRIPLE
        Model m = new ModelMem() ;

        m.setReaderClassName(langXML, com.hp.hpl.jena.rdf.arp.JenaReader.class.getName());
        m.setReaderClassName(langXMLAbbrev, com.hp.hpl.jena.rdf.arp.JenaReader.class.getName());

        // Default.
        //m.setReaderClassName(langNTriple, com.hp.hpl.jena.rdf.arp.NTriple.class.getName()) ;

        try {
            loadModel(m, urlStr, lang) ;
        } catch (JenaException rdfEx)
        {
            logger.warn("Error loading data source", rdfEx);
            return null ;
        }
        catch (FileNotFoundException e)
        {
            logger.warn("No such data source: "+urlStr);
            return null ;
        }
        return m ;
    }

    /** Load a model from a file into a model.
     * @param model   Model to read into
     * @param urlStr  URL (or filename) to read from
     * @param lang    Null mean guess based on the URI String
     * @return Returns the model passed in.
     * @throws java.io.FileNotFoundException
     */


    public static Model loadModel(Model model, String urlStr, String lang)
        throws java.io.FileNotFoundException
    {
        // Wild guess at the language!
        // Yes - repeated from above.
        // System.err.println( "[" + urlStr + "]" );
        if ( lang == null )
            lang = guessLang(urlStr) ;

        if ( lang.equals(langBDB) || lang.equals(langSQL) )
        {
            logger.fatal("Can't load data into existing model from a persistent database") ;
            return null ;
        }

        String base = "file://unknown.net/" ;
        Reader dataReader = null ;
        try {
            URL url = new URL(urlStr);
            dataReader = new BufferedReader(new InputStreamReader(url.openStream())) ;
            base = urlStr ;
        }
        catch (java.net.MalformedURLException e)
        {
            // Try as a file.
            String filename = urlStr ;
            File file = ( basename != null ) ? new File(basename, filename) : new File(filename) ;
            // Unfortunately Xerces objects to hybrid file, URLs with \ in them, for a base name.
            base = ("file:///"+file.getAbsolutePath()).replace('\\','/') ;
            // System.err.println( "| file = " + filename + " & basename = " + basename );
            FileReader fr = tryFile( basename, filename ); // was  new FileReader(filename) ; // was (file)
            dataReader = new BufferedReader(fr) ;
        }
        catch (java.io.IOException ioEx)
        {
            logger.fatal("IOException: "+ioEx) ;
            return null ;
        }
        //model.read(urlStr, base, lang) ;
        RDFReader rdfReader = model.getReader(lang) ;
        if ( rdfReader instanceof com.hp.hpl.jena.rdf.arp.JenaReader )
            rdfReader.setProperty("error-mode", "lax") ;
        rdfReader.read(model, dataReader, base) ;

        try { dataReader.close() ; }
        catch (IOException ioEx)
        { logger.warn("IOException closing reader", ioEx) ; }

        return model ;
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
        logger.debug("SQL: file="+urlStr) ;
        //System.out.println("SQL: file="+urlStr) ;
        try { 
            if ( driverName != null )
                Class.forName(driverName).newInstance();
        } catch (Exception ex) {}

//        System.out.println("URL        = "+urlStr) ;
//        System.out.println("dbName     = "+modelName) ;
//        System.out.println("dbUser     = "+dbUser) ;
//        System.out.println("dbType     = "+dbType) ;
//        System.out.println("dbPassword = "+dbPassword) ;
//        System.out.println("driver     = "+driverName) ;
            
        try {
            IDBConnection conn = 
                ModelFactory.createSimpleRDBConnection(urlStr, dbUser, dbPassword, dbType) ;
            return ModelRDB.open(conn, modelName) ;
        } catch (JenaException rdfEx)
        {
            logger.error("Failed to open SQL database: ModelLoader.connectToDB", rdfEx) ;
            throw rdfEx ;
        }
    }

    private static FileReader tryFile( String baseName, String fileName ) throws FileNotFoundException
        {
        try { return new FileReader( fileName ); }
        catch (FileNotFoundException e)
            {
            // System.err.println( "| could not read " + fileName + "; trying " + new File( baseName, fileName ) );
            try { return new FileReader( new File( baseName, fileName ) ); }
            catch (FileNotFoundException e2)
                {
                // System.err.println( "| that didn't work either, alas" );
                throw e2;
                }
            }
        }

	/** Guess the language/type of model data
	 * <ul>
	 * <li> If the URI of the model starts jdbc: it is assumed to be an RDB model</li>
	 * <li> If the URI ends ".rdf", it is assumed to be RDF/XML</li>
	 * <li> If the URI end .nt, it is assumed to be N-Triples</li>
	 * <li> If the URI end .bdbd, it is assumed to be BerkleyDB model</li>
	 * </ul>
	 */


    public static String guessLang(String urlStr)
    {
        String lang = null ;

        if ( urlStr.startsWith("jdbc:") || urlStr.startsWith("JDBC:") )
            return langSQL ;

        String ext = getFilenameExt(urlStr) ;

        if ( ext != null && ext.length() > 0 )
        {
            // Types that can be detected from file extensions
            if ( ext.equalsIgnoreCase("rdf") )
                lang = langXML ;
            else if ( ext.equalsIgnoreCase("nt") )
                lang = langNTriple ;
            else if ( ext.equalsIgnoreCase("n3") )
                lang = langN3 ;
            else if ( ext.equalsIgnoreCase("bdb") )
                lang = langBDB ;
            // But not ..
            //else if ( ext.equalsIgnoreCase("rdb") )
            //    lang = langSQL ;
            // else no idea.
        }
        return lang ;
    }

	/** Sets the directory used in
	 * resolving URIs that are raw file names (no file:)
	 * This is a global change when the ModelLoader is used.
	 */

    public static void setFileBase(String _basename) { basename = _basename ; } 

    private static String getFilenameExt(String filename)
    {
        // Works on URLs

        int iSep = 0 ;      // Last separator: either / or \ (covers all OSes?)
        int iExt = 0 ;      // File extension

        iSep = filename.lastIndexOf('/') ;
        int iTmp = filename.lastIndexOf('\\') ;  // NB \ is not an escape character in URLs
        if ( iTmp > iSep ) iSep = iTmp ;

        iExt = filename.lastIndexOf('.') ;
        if ( iExt > iSep )
        {
            String ext = filename.substring(iExt+1).toLowerCase() ;
            return ext ;
        }
        return "" ;
    }

    private static String getDirname(String filename)
    {
        File f = new File(filename) ;
        return f.getParent() ;
    }

    private static String getBasename(String filename)
    {
        File f = new File(filename) ;
        return f.getName() ;
    }
}


/*
 *  (c) Copyright Hewlett-Packard Company 2002
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
