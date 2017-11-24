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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format ;

import java.io.InputStream ;
import java.io.PrintWriter ;
import java.util.zip.GZIPInputStream ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.commons.fileupload.FileItemIterator ;
import org.apache.commons.fileupload.FileItemStream ;
import org.apache.commons.fileupload.servlet.ServletFileUpload ;
import org.apache.commons.fileupload.util.Streams ;
import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.lang.StreamRDFCounting ;
import org.apache.jena.riot.system.IRIResolver ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.web.HttpSC ;

public class SPARQL_Upload extends ActionService
{
    public SPARQL_Upload() {
        super() ;
    }

    // Methods to respond to.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response) ;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        setCommonHeadersForOptions(response) ;
        response.setHeader(HttpNames.hAllow, "OPTIONS,POST") ;
        response.setHeader(HttpNames.hContentLengh, "0") ;
    }

    @Override
    protected void validate(HttpAction action)
    {}

    @Override
    protected void perform(HttpAction action) {
        // Only allows one file in the upload.
        boolean isMultipart = ServletFileUpload.isMultipartContent(action.request);
        if ( ! isMultipart )
            ServletOps.error(HttpSC.BAD_REQUEST_400 , "Not a file upload") ;

        long count = upload(action, Fuseki.BaseUpload) ;
        ServletOps.success(action) ;
        try {
            action.response.setContentType("text/html") ;
            action.response.setStatus(HttpSC.OK_200);
            PrintWriter out = action.response.getWriter() ;
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("</head>") ;
            out.println("<body>") ;
            out.println("<h1>Success</h1>");
            out.println("<p>") ;
            out.println("Triples = "+count + "\n");
            out.println("<p>") ;
            out.println("</p>") ;
            out.println("<button onclick=\"timeFunction()\">Back to Fuseki</button>");
            out.println("</p>") ;
            out.println("<script type=\"text/javascript\">");
            out.println("function timeFunction(){");
            out.println("window.location.href = \"/fuseki.html\";}");
            out.println("</script>");
            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
            ServletOps.success(action) ;
        }
        catch (Exception ex) { ServletOps.errorOccurred(ex) ; }
    }

    // Also used by SPARQL_REST
    static public long upload(HttpAction action, String base) {
        if ( action.isTransactional() )
            return uploadTxn(action, base) ;
        else
            return uploadNonTxn(action, base) ;
    }

    /** Non-transaction - buffer to a temporary graph so that parse errors
     * are caught before inserting any data.
     */
    private static long uploadNonTxn(HttpAction action, String base) {
        UploadDetails upload = uploadWorker(action, base) ;
        String graphName = upload.graphName ;
        DatasetGraph dataTmp = upload.data ;
        long count = upload.count ;

        if ( graphName == null )
            action.log.info(format("[%d] Upload: %d Quads(s)",action.id, count)) ;
        else
            action.log.info(format("[%d] Upload: Graph: %s, %d triple(s)", action.id, graphName,  count)) ;

        Node gn = null ;
        if ( graphName != null ) {
            gn = graphName.equals(HttpNames.valueDefault)
                ? Quad.defaultGraphNodeGenerated
                : NodeFactory.createURI(graphName) ;
        }

        action.beginWrite() ;
        try {
            if ( gn != null )
                FusekiLib.addDataInto(dataTmp.getDefaultGraph(), action.getActiveDSG(), gn) ;
            else
                FusekiLib.addDataInto(dataTmp, action.getActiveDSG()) ;

            action.commit() ;
            return count ;
        } catch (RuntimeException ex)
        {
            // If anything went wrong, try to backout.
            try { action.abort() ; } catch (Exception ex2) {}
            ServletOps.errorOccurred(ex.getMessage()) ;
            return -1 ;
        }
        finally { action.endWrite() ; }
    }

     /** Transactional - we'd like data to go straight to the destination, with an abort on parse error.
      * But file upload with a name means that the name can be after the data
      * (it is in the Fuseki default pages).
      * Use Graph Store protocol for bulk uploads.
      * (It would be possible to process the incoming stream and see the graph name first.)
      */
     private static long uploadTxn(HttpAction action, String base) {
         // We can't do better than the non-transaction approach.
         return uploadNonTxn(action, base) ;
     }

     static class UploadDetails {
         final String graphName  ;
         final DatasetGraph data ;
         final long count ;
         UploadDetails(String gn, DatasetGraph dsg, long parserCount) {
             this.graphName = gn ;
             this.data = dsg ;
             this.count = parserCount ;
         }
     }

    /** Process an HTTP file upload of RDF with additional name field for the graph name.
     *  We can't stream straight into a dataset because the graph name can be after the data.
     *  @return graph name and count
     */

    // ?? Combine with Upload.fileUploadWorker
    // Difference is the handling of names for graphs.
    static private UploadDetails uploadWorker(HttpAction action, String base) {
        DatasetGraph dsgTmp = DatasetGraphFactory.create() ;
        ServletFileUpload upload = new ServletFileUpload() ;
        String graphName = null ;
        boolean isQuads = false ;
        long count = -1 ;

        String name = null ;
        ContentType ct = null ;
        Lang lang = null ;

        try {
            FileItemIterator iter = upload.getItemIterator(action.request) ;
            while (iter.hasNext()) {
                FileItemStream item = iter.next() ;
                String fieldName = item.getFieldName() ;
                InputStream stream = item.openStream() ;
                if ( item.isFormField() ) {
                    // Graph name.
                    String value = Streams.asString(stream, "UTF-8") ;
                    if ( fieldName.equals(HttpNames.paramGraph) ) {
                        graphName = value ;
                        if ( graphName != null && !graphName.equals("") && !graphName.equals(HttpNames.valueDefault) ) {
                            IRI iri = IRIResolver.parseIRI(value) ;
                            if ( iri.hasViolation(false) )
                                ServletOps.errorBadRequest("Bad IRI: " + graphName) ;
                            if ( iri.getScheme() == null )
                                ServletOps.errorBadRequest("Bad IRI: no IRI scheme name: " + graphName) ;
                            if ( iri.getScheme().equalsIgnoreCase("http") || iri.getScheme().equalsIgnoreCase("https") ) {
                                // Redundant??
                                if ( iri.getRawHost() == null )
                                    ServletOps.errorBadRequest("Bad IRI: no host name: " + graphName) ;
                                if ( iri.getRawPath() == null || iri.getRawPath().length() == 0 )
                                    ServletOps.errorBadRequest("Bad IRI: no path: " + graphName) ;
                                if ( iri.getRawPath().charAt(0) != '/' )
                                    ServletOps.errorBadRequest("Bad IRI: Path does not start '/': " + graphName) ;
                            }
                        }
                    } else if ( fieldName.equals(HttpNames.paramDefaultGraphURI) )
                        graphName = null ;
                    else
                        // Add file type?
                        action.log.info(format("[%d] Upload: Field=%s ignored", action.id, fieldName)) ;
                } else {
                    // Process the input stream
                    name = item.getName() ;
                    if ( name == null || name.equals("") || name.equals("UNSET FILE NAME") )
                        ServletOps.errorBadRequest("No name for content - can't determine RDF syntax") ;

                    String contentTypeHeader = item.getContentType() ;
                    ct = ContentType.create(contentTypeHeader) ;

                    lang = RDFLanguages.contentTypeToLang(ct.getContentType()) ;
                    if ( lang == null ) {
                        lang = RDFLanguages.filenameToLang(name) ;

                        // JENA-600 filenameToLang() strips off certain
                        // extensions such as .gz and
                        // we need to ensure that if there was a .gz extension
                        // present we wrap the stream accordingly
                        if ( name.endsWith(".gz") )
                            stream = new GZIPInputStream(stream) ;
                    }

                    if ( lang == null )
                        // Desperate.
                        lang = RDFLanguages.RDFXML ;

                    isQuads = RDFLanguages.isQuads(lang) ;

                    action.log.info(format("[%d] Upload: Filename: %s, Content-Type=%s, Charset=%s => %s", action.id, name,
                                           ct.getContentType(), ct.getCharset(), lang.getName())) ;

                    StreamRDF x = StreamRDFLib.dataset(dsgTmp) ;
                    StreamRDFCounting dest = StreamRDFLib.count(x) ;
                    ActionLib.parse(action, dest, stream, lang, base) ;
                    count = dest.count() ;
                }
            }

            if ( graphName == null || graphName.equals("") )
                graphName = HttpNames.valueDefault ;
            if ( isQuads )
                graphName = null ;
            return new UploadDetails(graphName, dsgTmp, count) ;
        }
        catch (ActionErrorException ex) { throw ex ; }
        catch (Exception ex)            { ServletOps.errorOccurred(ex) ; return null ; }
    }
}
