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

package org.apache.jena.fuseki.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.servlets.HttpAction;

/** Create standard request logs (NCSA etc) */
public class RequestLog {
    /*
      http://httpd.apache.org/docs/current/mod/mod_log_config.html
Common Log Format (CLF)
    "%h %l %u %t \"%r\" %>s %b"
Common Log Format with Virtual Host
    "%v %h %l %u %t \"%r\" %>s %b"
NCSA extended/combined log format
    "%h %l %u %t \"%r\" %>s %b \"%{Referer}i\" \"%{User-agent}i\""
     */
    /*
        %l -- Identity or -
        %u -- Remote user or -
        %t -- Timestamp
        "%r" -- Request line
        %>s -- Final request status
        %b -- Size in bytes
        Headers.
        %{}i for request header.
        %{}o for response header.
      */

    private static DateFormat dateFormatter;
    static {
        dateFormatter = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     *  NCSA combined log format
     *
     * LogFormat "%{X-Forwarded-For}i %l %u %t \"%r\" %&gt;s %b  \"%{Referer}i\" \"%{User-Agent}i\"" combinedfwd
     * xxx.xxx.xxx.xxx - - [01/Feb/2014:03:19:09 +0000] "GET / HTTP/1.1" 200 6190  "-" "check_http/v1.4.16 (nagios-plugins 1.4.16)"
     */
    public static String combinedNCSA(HttpAction action) {
        HttpServletRequest request = action.request;
        HttpServletResponse response = action.response;
        return combinedNCSA(request, response);
    }

    public static String combinedNCSA(HttpServletRequest request, HttpServletResponse response) {
        StringBuilder builder = new StringBuilder();
        // Remote
        String remote = get(request, "X-Forwarded-For", request.getRemoteAddr());
        builder.append(remote);
        builder.append(" ");

        // %l %u : User identity (unrelaible)
        builder.append("- - ");

        // %t
        // Expensive?
        builder.append("[");
        // Better?
        builder.append(dateFormatter.format(new Date()));
        builder.append("] ");

        // "%r"
        builder.append("\"");
        builder.append(request.getMethod());
        builder.append(" ");
        // No query string - they are long and logged readably elsewhere
        builder.append(request.getRequestURI());
        builder.append("\"");
        //%>s -- Final request status
        builder.append(" ");
        builder.append(response.getStatus());

        //%b -- Size in bytes
        builder.append(" ");
        //String size = getField()
        String size = get(response, "Content-Length", "-");
        builder.append(size);

        // "%{Referer}i"
        builder.append(" \"");
        builder.append(get(request, "Referer", ""));
        builder.append("\"");
        // "%{User-Agent}i"
        builder.append(" \"");
        builder.append(get(request, "User-Agent", ""));
        builder.append("\"");

        return builder.toString();
    }

    private static String get(HttpServletRequest request, String name, String dft) {
        String x = get(request, name);
        if ( x == null )
            x = dft;
        return x;
    }

    private static String get(HttpServletRequest request, String name) {
        Enumeration<String> en = request.getHeaders(name);
        if ( ! en.hasMoreElements() ) return null;
        String x = en.nextElement();
        if ( en.hasMoreElements() ) {
            Log.warn(RequestLog.class, "Multiple request header values");
        }
        return x;
    }

    private static String get(HttpServletResponse response, String name, String dft) {
        String x = get(response, name);
        if ( x == null )
            x = dft;
        return x;
    }


    private static String get(HttpServletResponse response, String name) {
        Collection<String> en = response.getHeaders(name);
        if ( en.isEmpty() )return null;
        if ( en.size() != 1 ) Log.warn(RequestLog.class, "Multiple response header values");
        return response.getHeader(name);
    }

}
