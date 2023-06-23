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

/** A servlet that dumps its request
 */

// Could be neater - much, much neater!

package org.apache.jena.fuseki.ctl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.fuseki.system.FusekiNetLib;

/** Dump the HTTP request */
public class ActionDumpRequest extends HttpServlet {
    public ActionDumpRequest() {}

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPrintInformation(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doPrintInformation(req, resp);
    }

    public void doPrintInformation(HttpServletRequest req, HttpServletResponse resp) {
        try {
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");

            String now = new Date().toString();

            // HEAD
            out.println("<html>");
            out.println("<head>");
            out.println("<Title>Dump @ " + now + "</Title>");
            // Reduce the desire to cache it.
            out.println("<meta CONTENT=now HTTP-EQUIV=expires>");
            out.println("</head>");

            // BODY
            out.println("<body>");
            out.println("<pre>");

            out.println("Dump : " + now);
            out.println();
            out.println("==== Request");
            out.println();
            printRequest(out, req);
            out.println();

            out.println(">>>> Body");
            out.println();
            printBody(out, req);
            out.println("<<<< Body");

//            out.println("==== ServletContext");
//            out.println();
//            printServletContext(out, req);
//            out.println();
//
//            out.println("==== Environment");
//            out.println();
//            printEnvironment(out, req);
//            out.println();

            out.println("</pre>");

            out.println("</body>");
            out.println("</html>");
            out.flush();
        } catch (IOException e) {}
    }

    // ---- Library of things to report on.

    static public void printRequest(PrintWriter pw, HttpServletRequest req) {
        // ----Standard environment
        pw.println("Method:                 " + req.getMethod());
        pw.println("getContentLength:       " + Long.toString(req.getContentLengthLong()));
        pw.println("getContentType:         " + req.getContentType());
        pw.println("getRequestURI:          " + req.getRequestURI());
        pw.println("getRequestURL:          " + req.getRequestURL());
        pw.println("getContextPath:         " + req.getContextPath());
        pw.println("getServletPath:         " + req.getServletPath());
        pw.println("getPathInfo:            " + req.getPathInfo());
        pw.println("getPathTranslated:      " + req.getPathTranslated());
        pw.println("getQueryString:         " + req.getQueryString());
        pw.println("getProtocol:            " + req.getProtocol());
        pw.println("getScheme:              " + req.getScheme());
        pw.println("getServerName:          " + req.getServerName());
        pw.println("getServerPort:          " + req.getServerPort());
        pw.println("getRemoteUser:          " + req.getRemoteUser());
        pw.println("getRemoteAddr:          " + req.getRemoteAddr());
        pw.println("getRemoteHost:          " + req.getRemoteHost());
        pw.println("getRequestedSessionId:  " + req.getRequestedSessionId());
    }

    // ---- Library of things to report on.

    static void printBody(PrintWriter pw, HttpServletRequest req) throws IOException {
        // Destructive read of the request body.
        BufferedReader in = req.getReader();
        while (true) {
            String x = in.readLine();
            if ( x == null )
                break;
            x = x.replaceAll("&", "&amp;");
            x = x.replaceAll("<", "&lt;");
            x = x.replaceAll(">", "&gt;");
            pw.println(x);
        }
    }

    static void printCookies(PrintWriter pw, HttpServletRequest req) {
        Cookie c[] = req.getCookies();
        if ( c == null )
            pw.println("getCookies:            <none>");
        else {
            for ( int i = 0; i < c.length; i++ ) {
                pw.println();
                pw.println("Cookie:        " + c[i].getName());
                pw.println("    value:     " + c[i].getValue());
                pw.println("    version:   " + c[i].getVersion());
                pw.println("    comment:   " + c[i].getComment());
                pw.println("    domain:    " + c[i].getDomain());
                pw.println("    maxAge:    " + c[i].getMaxAge());
                pw.println("    path:      " + c[i].getPath());
                pw.println("    secure:    " + c[i].getSecure());
            }
        }
    }

    static void printHeaders(PrintWriter pw, HttpServletRequest req) {
        Enumeration<String> en = req.getHeaderNames();

        for (; en.hasMoreElements(); ) {
            String name = en.nextElement();
            String value = req.getHeader(name);
            pw.println("Head: " + name + " = " + value);
        }
    }

    // Note that doing this on a form causes the forms content (body) to be read
    // and parsed as form variables.
    static void printParameters(PrintWriter pw, HttpServletRequest req) {
        Enumeration<String> en = req.getParameterNames();
        for (; en.hasMoreElements(); ) {
            String name = en.nextElement();
            String value = req.getParameter(name);
            pw.println("Param: " + name + " = " + value);
        }
    }

    static void printQueryString(PrintWriter pw, HttpServletRequest req) {
        MultiValuedMap<String, String> map = FusekiNetLib.parseQueryString(req);
        for ( String name : map.keys() )
            for ( String value : map.get(name) )
                pw.println("Param: "+name + " = " + value);
    }

    static void printLocales(PrintWriter pw, HttpServletRequest req) {
        Enumeration<Locale> en = req.getLocales();
        for (; en.hasMoreElements(); ) {
            String name = en.nextElement().toString();
            pw.println("Locale: " + name);
        }
        pw.println();
    }

    /**
     * <code>printEnvironment</code>
     *
     * @return String that is the HTML of the System properties as name/value pairs. The
     *         values are with single quotes independent of whether or not the value has
     *         single quotes in it.
     */
    static public String printEnvironment() {
        Properties properties = System.getProperties();
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
            Enumeration<Object> en = properties.keys();
            while (en.hasMoreElements()) {
                String key = en.nextElement().toString();
                pw.println(key + ": '" + properties.getProperty(key) + "'");
            }

            pw.println();
            return sw.toString();
        } catch (IOException e) {
            IO.exception(e);
            return null;
        }
    }

    public String printServletContext() {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
            ServletContext sc = getServletContext();
            pw.println("majorVersion: '" + sc.getMajorVersion() + "'");
            pw.println("minorVersion: '" + sc.getMinorVersion() + "'");
            pw.println("contextName:  '" + sc.getServletContextName() + "'");
            pw.println("servletInfo:  '" + getServletInfo() + "'");
            pw.println("serverInfo:  '" + sc.getServerInfo() + "'");

            {
                Enumeration<String> en = sc.getInitParameterNames();
                if ( en != null ) {
                    pw.println("initParameters: ");
                    while (en.hasMoreElements()) {
                        String key = en.nextElement();
                        pw.println(key + ": '" + sc.getInitParameter(key) + "'");
                    }
                }
            }

            {
                Enumeration<String> en = sc.getAttributeNames();
                if ( en != null ) {
                    pw.println("attributes: ");
                    while (en.hasMoreElements()) {
                        String key = en.nextElement();
                        pw.println(key + ": '" + sc.getAttribute(key) + "'");
                    }
                }
            }
            pw.println();

            return sw.toString();
        } catch (IOException e) {
            IO.exception(e);
            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Dump";
    }
}
