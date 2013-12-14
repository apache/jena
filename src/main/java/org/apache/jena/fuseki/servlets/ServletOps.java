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

import java.io.IOException ;
import java.io.PrintWriter ;

import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.web.HttpSC ;

public class ServletOps {

    public static void responseSendError(HttpServletResponse response, int statusCode, String message) {
        try {
            response.sendError(statusCode, message) ;
        } catch (IOException ex) {
            errorOccurred(ex) ;
        } catch (IllegalStateException ex) {}
    }

    public static void responseSendError(HttpServletResponse response, int statusCode) {
        try {
            response.sendError(statusCode) ;
        } catch (IOException ex) {
            errorOccurred(ex) ;
        }
    }

    public static void successNoContent(HttpAction action) {
        success(action, HttpSC.NO_CONTENT_204) ;
    }

    public static void success(HttpAction action) {
        success(action, HttpSC.OK_200) ;
    }

    public static void successCreated(HttpAction action) {
        success(action, HttpSC.CREATED_201) ;
    }

    // When 404 is no big deal e.g. HEAD
    public static void successNotFound(HttpAction action) {
        success(action, HttpSC.NOT_FOUND_404) ;
    }

    //
    public static void success(HttpAction action, int httpStatusCode) {
        action.response.setStatus(httpStatusCode) ;
    }

    public static void successPage(HttpAction action, String message) {
        try {
            action.response.setContentType("text/html") ;
            action.response.setStatus(HttpSC.OK_200) ;
            PrintWriter out = action.response.getWriter() ;
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("</head>") ;
            out.println("<body>") ;
            out.println("<h1>Success</h1>") ;
            if ( message != null ) {
                out.println("<p>") ;
                out.println(message) ;
                out.println("</p>") ;
            }
            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
        } catch (IOException ex) {
            errorOccurred(ex) ;
        }
    }

    public static void warning(HttpAction action, String string) {
        action.log.warn(string) ;
    }

    public static void warning(HttpAction action, String string, Throwable thorwable) {
        action.log.warn(string, thorwable) ;
    }

    public static void errorBadRequest(String string) {
        error(HttpSC.BAD_REQUEST_400, string) ;
    }

    public static void errorNotFound(String string) {
        error(HttpSC.NOT_FOUND_404, string) ;
    }

    public static void errorNotImplemented(String msg) {
        error(HttpSC.NOT_IMPLEMENTED_501, msg) ;
    }

    public static void errorMethodNotAllowed(String method) {
        error(HttpSC.METHOD_NOT_ALLOWED_405, "HTTP method not allowed: " + method) ;
    }

    public static void errorForbidden(String msg) {
        if ( msg != null )
            error(HttpSC.FORBIDDEN_403, msg) ;
        else
            error(HttpSC.FORBIDDEN_403, "Forbidden") ;
    }

    public static void error(int statusCode) {
        throw new ActionErrorException(null, null, statusCode) ;
    }

    public static void error(int statusCode, String string) {
        throw new ActionErrorException(null, string, statusCode) ;
    }

    public static void errorOccurred(String message) {
        errorOccurred(message, null) ;
    }

    public static void errorOccurred(Throwable ex) {
        errorOccurred(null, ex) ;
    }

    public static void errorOccurred(String message, Throwable ex) {
        throw new ActionErrorException(ex, message, HttpSC.INTERNAL_SERVER_ERROR_500) ;
    }

    public static String formatForLog(String string) {
        string = string.replace('\n', ' ') ;
        string = string.replace('\r', ' ') ;
        return string ;
    }

}

