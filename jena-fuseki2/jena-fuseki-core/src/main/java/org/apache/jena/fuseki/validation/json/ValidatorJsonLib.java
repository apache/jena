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

package org.apache.jena.fuseki.validation.json;

import static org.apache.jena.riot.WebContent.charsetUTF8;
import static org.apache.jena.riot.WebContent.contentTypeJSON;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/** Validation operations for JSON output */
public abstract class ValidatorJsonLib
{
    private static Logger vLog = Fuseki.validationLog;
    public static final String jErrors          = "errors";
    public static final String jWarnings        = "warning";

    public static final String jParseError      = "parse-error";
    public static final String jParseErrorLine  = "parse-error-line";
    public static final String jParseErrorCol   = "parse-error-column";

    public static final String respService      = "X-Service";

    protected static AtomicLong counter = new AtomicLong(0);

    static void setHeaders(HttpServletResponse httpResponse) {
        httpResponse.setCharacterEncoding(charsetUTF8);
        httpResponse.setContentType(contentTypeJSON);
        httpResponse.setHeader(respService, "Jena Fuseki Validator : http://jena.apache.org/");
    }

    static String getArg(ValidationAction action, String paramName) {
        String arg = getArgOrNull(action, paramName);
        if ( arg == null ) {
            ServletOps.error(HttpSC.BAD_REQUEST_400, "No parameter given: " + paramName);
            return null;
        }
        return arg;
    }

    static String getArgOrNull(ValidationAction action, String paramName) {
        String[] args = getArgs(action, paramName);

        if ( args == null || args.length == 0 )
            return null;

        if ( args.length > 1 ) {
            ServletOps.error(HttpSC.BAD_REQUEST_400, "Too many ("+args.length+") parameter values: "+paramName);
            return null;
        }

        return args[0];
    }

    static String[] getArgs(ValidationAction action, String paramName) {
        String[] args = action.request.getParameterValues(paramName);
        if ( args == null || args.length == 0 )
            return null;
        return args;
    }
}

