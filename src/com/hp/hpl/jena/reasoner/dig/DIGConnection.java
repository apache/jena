/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            11-Sep-2003
 * Filename           $RCSfile: DIGConnection.java,v $
 * Revision           $Revision: 1.12 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-03-24 17:33:18 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/


// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;



// Imports
///////////////
import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;

import com.hp.hpl.jena.util.FileUtils;


/**
 * <p>
 * Encapsulates the connection to a DIG reasoner.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGConnection.java,v 1.12 2005-03-24 17:33:18 ian_dickinson Exp $)
 */
public class DIGConnection {
    // Constants
    //////////////////////////////////

    /** Default URL for connecting to a local DIG reasoner on port 8081 */
    public static final String DEFAULT_REASONER_URL = "http://localhost:8081";
    
    /** Namespace for XSI */
    public static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
    

    // Static variables
    //////////////////////////////////

    private static Log log = LogFactory.getLog( DIGConnection.class );
    
    
    // Instance variables
    //////////////////////////////////

    /** The URL to connect to, initialised to the default URL */
    protected String m_extReasonerURL = DEFAULT_REASONER_URL;
    
    /** URI of current KB */
    private String m_kbURI;
    
    /** The XML document builder we are using */
    protected DocumentBuilderFactory m_factory = DocumentBuilderFactory.newInstance();
    
    /** List of most recent warnings */
    private List m_warnings = new ArrayList();
    
    /** Flag to control whether we log incoming and outgoing messages */
    protected boolean m_logCommunications = true;
    
    
    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /**
     * <p>Send a verb to the attached DIG reasoner and answer the result. The verb is encoded as an XML
     * document object.</p>
     * @param digVerb A DIG verb (information request, ask or tell) as an XML document
     * @return The resulting XML document formed from the response from the reasoner
     * @exception DigReasonerException for any errors in XML encoding or HTTP transmission 
     */
    public Document sendDigVerb( Document digVerb, DIGProfile profile ) {
        try {
            // make sure we set the KB uri
            Element verb = digVerb.getDocumentElement();
            if (!verb.hasAttribute( DIGProfile.URI )) {
                verb.setAttribute( DIGProfile.URI, m_kbURI );
            }
            
            // first open the connection
            URL url = new URL( m_extReasonerURL );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // pre-serialise the content so we can set the Content-Length field correctly
            StringWriter out = new StringWriter();
            serialiseDocument( digVerb, out );
            
            conn.setDoOutput( true );
            conn.setRequestMethod( "POST" );
            conn.setRequestProperty( "Content-Length", Integer.toString( out.getBuffer().length() ) );
            
            // different varaints on the protocol make different choices here
            conn.setRequestProperty( "Content-Type", profile.getContentType() );
            
            // log
            logMessage( true, digVerb );
            
            // send
            conn.connect();
            PrintWriter pw = FileUtils.asPrintWriterUTF8( conn.getOutputStream() );
            pw.print( out.getBuffer() );
            pw.flush();
            pw.close();
            
            // and receive
            Document response = getDigResponse( conn );

            // log
            logMessage( false, response );
            
            errorCheck( response );
            return response;
        }
        catch (IOException e) {
            throw new DIGWrappedException( e );
        }
    }


    /**
     * <p>Serialise the given document to the given output writer.</p> 
     * @param doc An XML document to serialise
     * @param out A writer that will consume the seralised form of the document
     */
    public void serialiseDocument( Document doc, Writer out ) {
        try {
            // write the given document to the string buffer
            XMLSerializer serializer = new XMLSerializer ( out, createXMLFormatter( doc ) );
            serializer.asDOMSerializer();
            serializer.serialize( doc );
        }
        catch (IOException e) {
            throw new DIGWrappedException( e );
        }
    }


    /**
     * <p>Bind a DIG KB to this adapter, by requesting a KB URI through the newKB 
     * verb.  If there is already a binding, do nothing unless rebind is true.
     * @param rebind If true, any existing KB will be released before binding 
     * to a new KB
     */
    public void bindKB( boolean rebind, DIGProfile profile ) {
        // delete the old KB
        if (rebind && m_kbURI != null) {
            Document release = createDigVerb( DIGProfile.RELEASEKB, profile );

            Document response = sendDigVerb( release, profile );
            errorCheck( response );

            if (warningCheck(response)) {
                log.warn( "DIG reasoner warning: " + getWarnings().next() );
            }
            m_kbURI = null;
        }
        
        // allocate a new KB
        if (m_kbURI == null) {
            // request a whole new KB
            Document response = sendDigVerb( createDigVerb( DIGProfile.NEWKB, profile ), profile );
            errorCheck( response );
            
            // extract the new KB URI
            Element kb = (Element) response.getDocumentElement()
                                           .getElementsByTagName( DIGProfile.KB )
                                           .item( 0 );
            if (kb == null) {
                throw new DIGReasonerException( "Could not locate DIG KB identifier in return value from newKB" );
            }
            else {
                m_kbURI = kb.getAttribute( DIGProfile.URI );
            }
        }
    }
    

    /**
     * <p>Check the response from the DIG server to see if there is an error code,
     * and raise an excption if so.</p>
     * @param response The response from the DIG server
     */
    public void errorCheck( Document response ) {
        Element root = response.getDocumentElement();
        NodeList errs = root.getElementsByTagName( DIGProfile.ERROR );
        
        if (errs != null && errs.getLength() > 0) {
            Element error = (Element) errs.item( 0 );
            
            String errCode = error.getAttribute( DIGProfile.CODE );
            int code = (errCode == null || errCode.length() == 0) ? 0 : Integer.parseInt( errCode );
            
            String msgAttr = error.getAttribute( DIGProfile.MESSAGE );
            
            NodeList messages = error.getChildNodes();
            String message = (messages.getLength() > 0) ? ((Text) messages.item( 0 )).getNodeValue().trim() : "(no message)";
            
            throw new DIGErrorResponseException( message, msgAttr, code );
        }
    }
    
    
    /**
     * <p>Append any warning messages from this response to the list of recent warnings,
     * which is first cleared.</p>
     * @param response The response from the DIG server
     * @return True if any warnings were detected.
     */
    public boolean warningCheck( Document response ) {
        Element root = response.getDocumentElement();
        NodeList ok = root.getElementsByTagName( DIGProfile.OK );
        m_warnings.clear();
        
        if (ok != null && ok.getLength() > 0) {
            Element e = (Element) ok.item(0);
            NodeList warnings = e.getElementsByTagName( DIGProfile.WARNING );
            
            if (warnings != null && warnings.getLength() > 0) {
                for (int i = 0;  i < warnings.getLength(); i++) {
                    // append the warning message to the list
                    Element warn = (Element) warnings.item( i );
                    m_warnings.add( warn.getAttribute( DIGProfile.MESSAGE ) );
                }
                
                return true;
            }
        }
        
        return false;
    }


    /**
     * <p>Answer an iterator over the warnings received since the last tell operation</p>
     * @return An iterator over warnings 
     */
    public Iterator getWarnings() {
        return m_warnings.iterator();
    }
    
    
    /**
     * <p>Release this connection back to the connection pool.</p>
     */
    public void release() {
        DIGConnectionPool.getInstance().release( this );
    }
    
    
    /** 
     * <p>Answer the URL of the external reasoner this connection is bound to.</p>
     * @return The current external reasoner URL
     */
    public String getReasonerURL() {
        return m_extReasonerURL;
    }
    
    
    /**
     * <p>Set the URL of the external reasoner with which this connection communicates.</p>
     * @param url The URL of the new external reasoner connection point
     */
    public void setReasonerURL( String url ) {
        m_extReasonerURL = url;
        m_kbURI = null;
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>Answer the XML document object that resulted from the most recent request.</p>
     * @param conn The current HTTP connection
     * @return The response from the DIG reasoner, as an XML object
     * @exception DigReasonerException if the underling connection or XML parser raises
     * an error.
     */
    protected Document getDigResponse( HttpURLConnection conn ) {
        try {
            // check for successful response
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new DIGReasonerException( "DIG reasoner returned failure code " +
                                                conn.getResponseCode() + ": " +
                                                conn.getResponseMessage() );
            }
            
            BufferedInputStream in = new BufferedInputStream( conn.getInputStream() );
            StringBuffer buf = new StringBuffer();
            
            // read the return result into a string buffer before we parse it
            int ch = in.read();
            while (ch > 0) {
                buf.append( (char) ch );
                ch = in.read();
            }
            
            // TODO remove LogFactory.getLog( getClass() ).debug( "Response buffer = " + buf.toString() );
            // now parse into a document
            DocumentBuilder builder = m_factory.newDocumentBuilder();
            return builder.parse( new ByteArrayInputStream( buf.toString().getBytes() ) );
        }
        catch (Exception e) {
            e.printStackTrace( System.err );
            throw new DIGWrappedException( e );
        }
    }


    /**
     * <p>Answer an XML formatter object for the given document
     * @param doc The XML document to be serialised
     * @return An XML formatter object for the document
     */
    protected OutputFormat createXMLFormatter( Document doc ) {
        OutputFormat format = new OutputFormat( doc );
        format.setIndenting( true );
        format.setLineWidth( 0 );             
        format.setPreserveSpace( false );
        
        return format;
    }


    /**
     * <p>Create a DIG verb as an xml element in a new document object.</p>
     * @param verbName The name of the DIG verb, as a string
     * @return An XML DOM element representing the DIG verb
     */
    protected Document createDigVerb( String verbName, DIGProfile profile ) {
        try {
            // initialise the XML DOM tree
            DocumentBuilder builder = m_factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            
            // create the verb as the root element of the XML document
            Element root = doc.createElementNS( profile.getDIGNamespace(), verbName );
            doc.appendChild( root );
            
            // set the standard attributes
            root.setAttribute( "xmlns", profile.getDIGNamespace() );
            root.setAttribute( "xmlns:xsi", XSI );
            root.setAttributeNS( XSI, "xsi:schemaLocation",
                                 profile.getDIGNamespace() + " " + profile.getSchemaLocation() );
            if (m_kbURI != null) {
                root.setAttribute( DIGProfile.URI, m_kbURI );
            }
            
            return doc;
        }
        catch (FactoryConfigurationError e) {
            throw new DIGWrappedException( e );
        }
        catch (ParserConfigurationException e) {
            throw new DIGWrappedException( e );
        }
    }
    

    /**
     * <p>Log the messages going to and from DIG</p>
     * @param outgoing True for send, false for receive
     * @param msg The document sent or received.
     */    
    protected void logMessage( boolean outgoing, Document msg ) {
        if (m_logCommunications) {
            StringWriter out = new StringWriter();
            serialiseDocument( msg, out );
            
            if (log.isDebugEnabled()) {
                log.debug( outgoing ? "Sending to DIG reasoner ..." : "Received from DIG reasoner ..." );
                log.debug( out );
            }
        }
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
