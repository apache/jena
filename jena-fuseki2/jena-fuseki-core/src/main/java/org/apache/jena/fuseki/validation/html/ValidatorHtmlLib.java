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

package org.apache.jena.fuseki.validation.html;

import java.io.IOException;
import java.util.function.Consumer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ServletBase;
import org.slf4j.Logger;

/** Lib of validators returning HTML or text */
public class ValidatorHtmlLib {
    public static final String jErrors         = "errors";
    public static final String jWarnings       = "warning";

    public static final String jParseError     = "parse-error";
    public static final String jParseErrorLine = "parse-error-line";
    public static final String jParseErrorCol  = "parse-error-column";

    public static Logger       serviceLog      = Fuseki.requestLog;

    // Validator service result page is at "$/validate/data" etc so CSS is:
    public static final String cssFile         = "../../css/fuseki.css";
    public static final String respService     = "X-Service";

    private ValidatorHtmlLib() {}

    public static void output(ServletOutputStream outStream, Consumer<IndentedLineBuffer> content, boolean lineNumbers) throws IOException {
        startFixed(outStream);
        IndentedLineBuffer out = new IndentedLineBuffer(lineNumbers);
        content.accept(out);
        out.flush();
        String x = htmlQuote(out.asString());
        byte b[] = x.getBytes("UTF-8");
        outStream.write(b);
        finishFixed(outStream);
    }

    public static void setHeaders(HttpServletResponse httpResponse) {
        ServletBase.setCommonHeaders(httpResponse);
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("text/html");
        httpResponse.setHeader(respService, "Fuseki/ARQ SPARQL Query Validator: http://jena.apache.org/");
    }

    public static String htmlQuote(String str) {
        StringBuilder sBuff = new StringBuilder();
        for ( int i = 0; i < str.length(); i++ ) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<' :
                    sBuff.append("&lt;");
                    break;
                case '>' :
                    sBuff.append("&gt;");
                    break;
                case '&' :
                    sBuff.append("&amp;");
                    break;
                default :
                    // Work around Eclipe bug with StringBuffer.append(char)
                    // try { sBuff.append(ch); } catch (Exception ex) {}
                    sBuff.append(ch);
                    break;
            }
        }
        return sBuff.toString();
    }

    public static void startFixed(ServletOutputStream outStream) throws IOException {
        outStream.println("<pre class=\"box\">");
    }

    public static void columns(String prefix, ServletOutputStream outStream) throws IOException {
        outStream.print(prefix);
        outStream.println("         1         2         3         4         5         6         7");
        outStream.print(prefix);
        outStream.println("12345678901234567890123456789012345678901234567890123456789012345678901234567890");
    }

    public static void finishFixed(ServletOutputStream outStream) throws IOException {
        outStream.println("</pre>");
    }

    public static void printHead(ServletOutputStream outStream, String title) throws IOException {
        outStream.println("<head>");
        outStream.println(" <title>" + title + "</title>");
        outStream.println("   <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
        if ( cssFile != null )
            outStream.println("   <link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssFile + "\" />");
        outStream.println("</head>");
    }
}
