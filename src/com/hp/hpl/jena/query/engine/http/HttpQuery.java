/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.query.engine.http;

import java.net.* ;
import java.io.* ;
import java.util.* ;

import org.apache.commons.logging.* ;

import com.hp.hpl.jena.query.util.Convert;
import com.hp.hpl.jena.shared.* ;

/** Create an execution object for performing a query on a model
 *  over HTTP.  This is the main protocol engine for HTTP query.
 *  There are higher level classes for doing a query and presenting
 *  the results in an API fashion. 
 * 
 *  If the query string is large, then HTTP POST is used.

 * @author  Andy Seaborne
 * @version $Id: HttpQuery.java,v 1.13 2007/01/14 19:11:50 andy_seaborne Exp $
 */
public class HttpQuery extends Params
{
    static final Log log = LogFactory.getLog(HttpQuery.class.getName()) ;
    
    /** The definition of "large" queries */
    // Not final so that other code can change it.
    static public /*final*/ int urlLimit = 2*1024 ;
    
    String serviceURL ;
    
    String contentTypeResult = HttpParams.contentTypeResultsXML ;
    HttpURLConnection httpConnection = null ;
    
    // An object indicate no value associated with parameter name 
    final static Object noValue = new Object() ;
    
    int responseCode = 0;
    String responseMessage = null ;
    boolean forcePOST = false ;
    String queryString = null ;
    
    static final String ENC_UTF8 = "UTF-8" ;
    
    /** Create a execution object for a whole model GET
     * @param serviceURL     The model
     */
    
    public HttpQuery(String serviceURL)
    {
        init(serviceURL) ;
    }
        

    /** Create a execution object for a whole model GET
     * @param url           The model
     */
    
    public HttpQuery(URL url)
    {
        init(url.toString()) ;
    }
        

    private void init(String serviceURL)
    {
        if ( log.isTraceEnabled())
            log.trace("URL: "+serviceURL) ;

        if ( serviceURL.indexOf('?') >= 0 )
            throw new QueryExceptionHTTP(-1, "URL already has a query string ("+serviceURL+")") ;
        
        this.serviceURL = serviceURL ;
    }

    
    private String getQueryString()
    {
        if ( queryString == null )
            queryString = super.httpString() ;
        return queryString ;
    }

    public HttpURLConnection getConnection() { return httpConnection ; }
    
    /** Set the content type (Accept header) for the results
     */
    
    public void setAccept(String contentType)
    {
        contentTypeResult = contentType ;
    }
    
    /** Return whether this request will go by GET or POST
     *  @return boolean
     */
    public boolean usesPOST()
    {
        if ( forcePOST )
            return true ;
        String s = getQueryString() ;
        
        return serviceURL.length()+s.length() >= urlLimit ;
    }

    /** Force the use of HTTP POST for the query operation
     */

    public void setForcePOST()
    {
        forcePOST = true ;
    }

    /** Execute the operation
     * @return Model    The resulting model
     * @throws QueryExceptionHTTP
     */
    public InputStream exec() throws QueryExceptionHTTP
    {
        try {
            if (usesPOST())
                return execPost();
            return execGet();
        } catch (QueryExceptionHTTP httpEx)
        {
            log.trace("Exception in exec", httpEx);
            throw httpEx;
        }
        catch (JenaException jEx)
        {
            log.trace("JenaException in exec", jEx);
            throw jEx ;
        }
    }
     

    private InputStream execGet() throws QueryExceptionHTTP
    {
        URL target = null ;
        String qs = getQueryString() ;
        try {
            if ( count() == 0 )
                target = new URL(serviceURL) ; 
            else
                target = new URL(serviceURL+"?"+qs) ;
        }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: "+malEx) ; }
        log.trace("GET "+target.toExternalForm()) ;
        
        try
        {
            httpConnection = (HttpURLConnection) target.openConnection();
            httpConnection.setRequestProperty("Accept", contentTypeResult) ;
            // By default, following 3xx redirects is true
            //conn.setFollowRedirects(true) ;

            httpConnection.setDoInput(true);
            httpConnection.connect();
            try
            {
                return execCommon();
            }
            catch (QueryExceptionHTTP qEx)
            {
                // Back-off and try POST if something complain about long URIs
                // Broken 
                if (qEx.getResponseCode() == 414 /*HttpServletResponse.SC_REQUEST_URI_TOO_LONG*/ )
                    return execPost();
                throw qEx;
            }
        }
        catch (java.net.ConnectException connEx)
        {
            throw new QueryExceptionHTTP(QueryExceptionHTTP.NoServer, "Failed to connect to remote server");
        }

        catch (IOException ioEx)
        {
            throw new QueryExceptionHTTP(ioEx);
        }
    }
    
    // Better (now) - turn into an HttpExec and use that engine  
    
    private InputStream execPost() throws QueryExceptionHTTP
    {
        URL target = null;
        try { target = new URL(serviceURL); }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: " + malEx); }
        log.trace("POST "+target.toExternalForm()) ;
        
        try
        {
            httpConnection= (HttpURLConnection) target.openConnection();
            httpConnection.setRequestMethod("POST") ;
            httpConnection.setRequestProperty("Accept", contentTypeResult) ;
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") ;
            
            httpConnection.setDoOutput(true) ;
            OutputStream out = httpConnection.getOutputStream() ;
            for ( Iterator iter = pairs().listIterator() ; iter.hasNext() ; )
            {
                Pair p = (Pair)iter.next() ;
                out.write(p.getName().getBytes()) ;
                out.write('=') ;
                String x = p.getValue() ;
                x = Convert.encWWWForm(x) ;
                out.write(x.getBytes()) ;
                out.write('&') ;
            }
            out.flush() ;
            httpConnection.connect() ;
            return execCommon() ;
        }
        catch (JenaException rdfEx)
        {
            throw new QueryExceptionHTTP(-1, "Failed to create RDF request");
        }
        catch (java.net.ConnectException connEx)
        {
            throw new QueryExceptionHTTP(-1, "Failed to connect to remote server");
        }

        catch (IOException ioEx)
        {
            throw new QueryExceptionHTTP(ioEx);
        }
    }
    
    private InputStream execCommon() throws QueryExceptionHTTP
    {
        try {        
            responseCode = httpConnection.getResponseCode() ;
            responseMessage = Convert.decWWWForm(httpConnection.getResponseMessage()) ;
            
            // 1xx: Informational 
            // 2xx: Success 
            // 3xx: Redirection 
            // 4xx: Client Error 
            // 5xx: Server Error 
            
            if ( 300 <= responseCode && responseCode < 400 )
            {
                
                throw new QueryExceptionHTTP(responseCode, responseMessage) ;
            }
            
            // Other 400 and 500 - errors 
            
            if ( responseCode >= 400 )
            {
                throw new QueryExceptionHTTP(responseCode, responseMessage) ;
            }
  
            // Request suceeded
            InputStream in = httpConnection.getInputStream() ;
            
            return in ;
        }
        catch (IOException ioEx)
        {
            throw new QueryExceptionHTTP(ioEx) ;
        } 
        catch (JenaException rdfEx)
        {
            throw new QueryExceptionHTTP(rdfEx) ;
        }
    }
    
    public String toString()
    {
        String s = httpString() ;
        if ( s != null || s.length() > 0 )
            return serviceURL+"?"+httpString() ;
        return serviceURL ;
    }
}

/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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
