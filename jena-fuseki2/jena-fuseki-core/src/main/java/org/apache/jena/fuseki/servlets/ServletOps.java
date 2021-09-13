/**
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.fuseki.system.UploadDetails;
import org.apache.jena.fuseki.system.UploadDetails.PreState;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.apache.jena.web.HttpSC.Code;

public class ServletOps {

    /** Send an HTTP error response.
     *  Include an optional message in the body (as text/plain), if provided.
     *  Note that we do not set a custom Reason Phrase.
     *  <br/>
     *  HTTPS/2 does not have a "Reason Phrase".
     *
     * @param response
     * @param statusCode
     * @param message
     */
    public static void responseSendError(HttpServletResponse response, int statusCode, String message) {
        response.setStatus(statusCode);
        if ( message != null )
            writeMessagePlainTextError(response, message);
        //response.sendError(statusCode, message);
    }

    /** Send an HTTP response with no body */
    public static void responseSendError(HttpServletResponse response, int statusCode) {
        response.setStatus(statusCode);
    }

    /** Write a plain text body.
     * <p>
     * Use Content-Length so the connection is preserved.
     */
    static void writeMessagePlainText(HttpServletResponse response, String message) {
        if ( message == null )
            return;
        if ( ! message.endsWith("\n") )
            message = message+"\n";
        response.setContentLength(message.length());
        response.setContentType(WebContent.contentTypeTextPlain);
        response.setCharacterEncoding(WebContent.charsetUTF8);
        ServletOps.setNoCache(response);
        try(ServletOutputStream out = response.getOutputStream()){
            out.print(message);
        }
        catch (IOException ex) {
            IO.exception(ex);
        }
    }

     public static void writeMessagePlainTextError(HttpServletResponse response, String message) {
        try { writeMessagePlainText(response, message); }
        catch (RuntimeIOException ex) {}
     }

    public static void successNoContent(HttpAction action) {
        success(action, HttpSC.NO_CONTENT_204);
    }

    public static void success(HttpAction action) {
        success(action, HttpSC.OK_200);
    }

    public static void successCreated(HttpAction action) {
        success(action, HttpSC.CREATED_201);
    }

    // When 404 is no big deal e.g. HEAD
    public static void successNotFound(HttpAction action) {
        success(action, HttpSC.NOT_FOUND_404);
    }

    //
    public static void success(HttpAction action, int httpStatusCode) {
        action.setResponseStatus(httpStatusCode);
    }

    public static void successPage(HttpAction action, String message) {
        try {
            action.setResponseContentType("text/html");
            action.setResponseCharacterEncoding(WebContent.charsetUTF8);
            action.setResponseStatus(HttpSC.OK_200);
            PrintWriter out = action.getResponseWriter();
            out.println("<html>");
            out.println("<head>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Success</h1>");
            if ( message != null ) {
                out.println("<p>");
                out.println(message);
                out.println("</p>");
            }
            out.println("</body>");
            out.println("</html>");
            out.flush();
        } catch (IOException ex) {
            errorOccurred(ex);
        }
    }

    public static void warning(HttpAction action, String string) {
        action.log.warn(string);
    }

    public static void warning(HttpAction action, String string, Throwable thorwable) {
        action.log.warn(string, thorwable);
    }

    public static void errorParseError(RiotParseException ex) {
        error(HttpSC.BAD_REQUEST_400, "Parse Error: "+ex.getMessage());
    }

    public static void errorBadRequest(String string) {
        error(HttpSC.BAD_REQUEST_400, string);
    }

    public static void errorNotFound() {
        errorNotFound(Code.NOT_FOUND.getMessage());
    }

    public static void errorNotFound(String string) {
        error(HttpSC.NOT_FOUND_404, string);
    }

    public static void errorNotImplemented(String msg) {
        error(HttpSC.NOT_IMPLEMENTED_501, msg);
    }

    public static void errorMethodNotAllowed(String method) {
        errorMethodNotAllowed(method, "HTTP method not allowed: " + method);
    }

    public static void errorMethodNotAllowed(String method, String msg) {
        error(HttpSC.METHOD_NOT_ALLOWED_405, msg);
    }

    public static void errorForbidden() {
        error(HttpSC.FORBIDDEN_403, "Forbidden");
    }

    public static void errorForbidden(String msg) {
        if ( msg != null )
            error(HttpSC.FORBIDDEN_403, msg);
        else
            errorForbidden();
    }

    public static void error(int statusCode) {
        throw new ActionErrorException(statusCode, null, null);
    }

    public static void error(int statusCode, String string) {
        throw new ActionErrorException(statusCode, string, null);
    }

    public static void errorOccurred(String message) {
        errorOccurred(message, null);
    }

    public static void errorOccurred(Throwable ex) {
        errorOccurred(null, ex);
    }

    public static void errorOccurred(String message, Throwable ex) {
        if ( message == null )
            System.err.println();
        throw new ActionErrorException(HttpSC.INTERNAL_SERVER_ERROR_500, message, ex);
    }

    public static String formatForLog(String string) {
        if ( string == null )
            return "<null>";
        string = string.replace('\n', ' ');
        string = string.replace('\r', ' ');
        return string;
    }

    public static void setNoCache(HttpAction action) {
        setNoCache(action.getResponse());
    }

    public static void setNoCache(HttpServletResponse response) {
        response.setHeader(HttpNames.hCacheControl, "must-revalidate,no-cache,no-store");
        response.setHeader(HttpNames.hPragma, "no-cache");
    }

    /** response to a upload operation of some kind. */
    public static void uploadResponse(HttpAction action, UploadDetails details) {
        if ( details.getExistedBefore().equals(PreState.ABSENT) )
            ServletOps.successCreated(action);
        else
            ServletOps.success(action); // successNoContent if empty body.
        JsonValue v = details.detailsJson();
        ServletOps.sendJson(action, v);
    }

    /** Send a JSON value as a 200 response.  Null object means no response body and no content-type headers. */
    public static void sendJsonReponse(HttpAction action, JsonValue v) {
        if ( v == null ) {
            ServletOps.success(action);
            //ServletOps.successNoContent(action);
            return;
        }

        ServletOps.success(action);
        sendJson(action, v);
    }

    /** Send a JSON value as a 200 response.  Null object means no response body and no content-type headers. */
    public static void sendJson(HttpAction action, JsonValue jValue) {
        if ( jValue == null )
            return;

        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        JSON.write(bytesOut, jValue);
        byte[] bytes = bytesOut.toByteArray();
        try {
            ServletOutputStream out = action.getResponseOutputStream();
            action.setResponseContentType(WebContent.contentTypeJSON);
            action.setResponseContentLength(bytes.length);
            action.setResponseCharacterEncoding(WebContent.charsetUTF8);
            out.write(bytes);
            out.flush();
        } catch (IOException ex) { ServletOps.errorOccurred(ex); }
    }
}

