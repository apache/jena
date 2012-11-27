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

package org.openjena.riot.system;

import static org.openjena.riot.WebContent.* ;

import java.io.IOException ;
import java.io.InputStream ;
import java.net.HttpURLConnection ;
import java.net.URL ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.zip.GZIPInputStream ;
import java.util.zip.InflaterInputStream ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.util.FileUtils ;
import com.hp.hpl.jena.util.TypedStream ;

public class ContentNeg
{
    // Work-in-progress
    // TODO RDFa, GRDDL
    // See ConNeg elsewjere.
    
    static { Log.setLog4j() ; }
    
    public static void main(String ... args) throws Exception
    {
        dwim("http://topbraid.org/examples/kennedys") ;
        dwim("http://dbpedia.org/resource/Fred") ; 
        System.exit(0) ;
    }

    static void dwim(String url) throws Exception
    {
        log.info("URL = "+url) ;
        TypedStream typedStream = negotiateHTTP(url) ; 
        log.info("MIME type:   "+typedStream.getMimeType()) ;
        log.info("Content type: "+typedStream.getCharset()) ; 
        typedStream.getInput().close() ;
    }
    
    final
    public void read(Model model, String url, String baseIRI) 
    {
        
    }

    /*
     * https://sourceforge.net/mailarchive/message.php?msg_id=4B1D5C7F.3040109%40talis.com
     * 
     * Graph I/O
     * https://sourceforge.net/mailarchive/message.php?msg_id=200906111051.44324.chris.dollin%40hp.com
     * https://sourceforge.net/mailarchive/message.php?msg_id=B6CF1054FDC8B845BF93A6645D19BEA3646C65DC72%40GVW1118EXC.americas.hpqcorp.net
        String actual = FileManager.get().mapURI(url) ;
        TypedStream typedStream = FileManager.get().openNoMapOrNull(url) ;
        typedStream.getCharset() ;
        typedStream.getInput() ;
        typedStream.getMimeType() ;
        See AFS/dev.ContentNeg
      */  
    
    /* Ping the semantic web uses:
     * Accept: text/html, html/xml, application/rdf+xml;q=0.9, text/rdf+n3;q=0.9, application/turtle;q=0.9, application/rdf+n3;q=0.9, * /*;q=0.8
     * So: application/rdf+xml;q=0.9, text/rdf+n3;q=0.9, application/turtle;q=0.9, application/rdf+n3;q=0.9, * /*;q=0.8
     */
    /* Tabulator
         application/rdf+xml,
        application/xhtml+xml;q=0.3, text/xml;q=0.2, application/xml;q=0.2, text/html;q=0.3,
        text/plain;q=0.1, text/n3, text/rdf+n3;q=0.5, application/x-turtle;q=0.2, text/turtle;q=1

     */
    
    // Currently Jena - rss is too risky.
    //application/rdf+xml, application/xml; q=0.8, text/xml; q=0.7, application/rss+xml; q=0.3, */*; q=0.2");


    private static Logger log = LoggerFactory.getLogger(ContentNeg.class) ;

    static String InternalNTriples = contentTypeNTriplesAlt ;
    
    // Filename to MIME type by extension.
    static Map<String, String> extToMimeType = new HashMap<String, String>() ;
    // MIME type to reader language.
    static Map<String, String> readers = new HashMap<String, String>() ;
    static { init(); }
    
    // XXX See also Lang and WebContent.
    private static void init()
    {
        extToMimeType.put("n3",     contentTypeN3) ;
        extToMimeType.put("ttl",    contentTypeTurtle) ;
        extToMimeType.put("nt",     InternalNTriples) ;
        extToMimeType.put("rdf",    contentTypeRDFXML) ;
        extToMimeType.put("owl",    contentTypeRDFXML) ;
        extToMimeType.put("xml",    contentTypeRDFXML) ;

        
        readers.put(contentTypeRDFXML,  langRDFXML) ;
        
        readers.put(contentTypeN3,      langN3) ;
        readers.put(contentTypeN3Alt1,  langN3) ;
        readers.put(contentTypeN3Alt2,  langN3) ;
        
        readers.put(contentTypeTurtle,      langTurtle) ;
        readers.put(contentTypeTurtleAlt1,  langTurtle) ;
        readers.put(contentTypeTurtleAlt2,  langTurtle) ;
        
        readers.put("text/plain", "NT") ;           // ??
        readers.put(InternalNTriples, "NT") ;       // Internal name.
        
        readers.put("application/rss+xml", "RDF/XML") ; // And hope it's RSS 1.0
    }
    
    static String acceptHeaderValue = StrUtils.strjoin(",",
                                  "application/rdf+xml",
                                  "application/turtle;q=0.9",
                                  "application/x-turtle;q=0.9",
                                  "text/n3;q=0.8",
                                  "text/turtle;q=0.8",
                                  "text/rdf+n3;q=0.7",
                                  "application/xml;q=0.5",
                                  "text/xml;q=0.5",
                                  "text/plain;q=0.4",     // N-triples
                                  "*/*;q=0.2") ;          // Hope.
                      
    
    public static TypedStream negotiateFilename(final String filename) throws IOException
    {
        if ( filename.startsWith("file:") )
        {}
        
        InputStream in = IO.openFile(filename) ;
        if ( in == null )
            return null ; 

        // Need to think out some pragmatics here:
        // Use of URL file extension 
        // If text/plain and ".nt" ==> N-Triples
        // but many files are text/plain (incorrectly) so if text/plain try harder.
        
        String fn = filename ;
        boolean isGZipped = false ;
        if ( filename.endsWith(".gz") )
        {
            isGZipped = true ;
            int x = filename.length() ;
            fn = filename.substring(x-3) ;
        }
        
        String suffix = FileUtils.getFilenameExt(fn) ;
        String mimeType = extToMimeType.get(suffix) ;
        return new TypedStream(in, mimeType, null) ;
    }
    
    public static String guessMIMETypeFromFilename(final String filename)
    {
        String fn = filename ;
        if ( filename.endsWith(".gz") )
        {
            int x = filename.length() ;
            fn = filename.substring(x-3) ;
        }
        
        String suffix = FileUtils.getFilenameExt(fn) ;
        String mimeType = extToMimeType.get(suffix) ;
        
        if ( mimeType == null )
            mimeType = contentTypeRDFXML ;
        else
            mimeType = mimeType.toLowerCase() ;
        return mimeType ;
    }
    
    public static TypedStream negotiateHTTP(String url) throws IOException
    {
        if ( ! url.startsWith("http://") )
        {
            // Not a URL for us.
            return null ;
        }
        
        HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
        
        // Accept-Encoding: gzip,deflate
        // HTTP-Range 14 ....
        
        HttpURLConnection.setFollowRedirects(true);
        con.setRequestProperty("Accept", acceptHeaderValue);
        con.setRequestProperty("Accept-Encoding", "gzip,deflate") ;
        // We need to handle this?
        con.setRequestProperty("Connection", "keep-alive");
        // Don't set charset.
        con.connect();
        
        String contentEncoding = con.getContentEncoding() ;
        if ( log.isDebugEnabled() )
            log.debug("Content-Encoding: " + contentEncoding) ;

        InputStream stream = con.getInputStream() ;
        
        if ( contentEncoding != null )
        {
            if ( contentEncoding.equalsIgnoreCase("deflate") ) 
                stream = new InflaterInputStream(stream) ;
            else if ( contentEncoding.equalsIgnoreCase("gzip") ) 
                stream = new GZIPInputStream(stream) ;
            else
                Log.warn(ContentNeg.class, "Unsupported ContentEncoding: "+contentEncoding) ;
        }
         
        String x = con.getContentType() ; 
        String contentType = null ;
        String charset = null ;
        
        if ( x.contains(";") )
        {
            String[] xx = x.split("\\s*;\\s*") ;
            contentType = xx[0] ;
            charset = xx[1] ;
        }
        else
            contentType = x ;

        if ( log.isDebugEnabled() )
            log.debug(contentType+" ;; "+charset) ;
        
        if ( charset != null )
        {
            int i = charset.indexOf("charset=") ;
            if ( i == 0 )
                charset = charset.substring("charset=".length()) ;
        }
        //Charset cs = Charset.forName(charset) ;
        
        if ( contentType != null )
            contentType = contentType.toLowerCase() ;
        
        if ( contentTypeTextPlain.equals(contentType) )
        {
            // MUST be .nt or .nt.gz
            // Too many RDF/XMl files are served as text/plain.
            if (! ( url.endsWith(".nt") || url.endsWith(".nt.gz") ) )
                contentType = null ;
        }

        if ( contentType == null )
            contentType = contentTypeRDFXML ;
        
        return new TypedStream(stream, contentType, charset) ;
    }
}
