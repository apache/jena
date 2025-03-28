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

import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.finishFixed;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.printHead;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.setHeaders;
import static org.apache.jena.fuseki.validation.html.ValidatorHtmlLib.startFixed;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.langtag.LangTag;
import org.apache.jena.langtag.LangTagException;
import org.apache.jena.langtag.SysLangTag;
import org.apache.jena.shared.JenaException;

public class LangTagValidatorHTML
{
    private LangTagValidatorHTML()
    { }

    static final String paramLang      = "lang";
    static final String paramLangTag   = "langtag";

    public static void executeHTML(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            String[] args1 = httpRequest.getParameterValues(paramLang);
            String[] args2 = httpRequest.getParameterValues(paramLangTag);

            if ( args1 == null && args2 == null )
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No ?lang= parameter");


            List<String> args = new ArrayList<>();
            if ( args1 != null )
                for ( String a : args1 ) args.add(a);
            if ( args2 != null )
                for ( String a : args2 ) args.add(a);

            if ( args.size() == 0 )
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No language tags");

            ServletOutputStream outStream = httpResponse.getOutputStream();
            PrintStream stdout = System.out;
            PrintStream stderr = System.err;
            System.setOut(new PrintStream(outStream));
            System.setErr(new PrintStream(outStream));

            setHeaders(httpResponse);

            outStream.println("<html>");
            printHead(outStream, "Apache Jena LangTag");
            outStream.println("<body>");

            outStream.println("<h1>LangTag Report</h1>");
            startFixed(outStream);

            try {
                boolean first = true;

                for ( String languageTag : args ) {
                    if ( !first )
                        System.out.println();
                    first = false;
                    outStream.println(String.format("%-16s %s", "Input:    ", languageTag));

                    if ( languageTag.isEmpty() ) {
                        outStream.println("Empty string for language tag");
                        continue;
                    }
                    if ( languageTag.isBlank() ) {
                        outStream.println("Blank string for language tag");
                        continue;
                    }
                    if ( languageTag.contains(" ") || languageTag.contains("\t") || languageTag.contains("\n") || languageTag.contains("\r") ) {
                        outStream.println("Language tag contains white space");
                        continue;
                    }
                    if ( languageTag.contains("--") ) {
                        outStream.println("Illgeal language tag. String contains '--'");
                        continue;
                    }

                    try {
                        LangTag langTag = SysLangTag.create(languageTag);
                        outStream.println(String.format("%-16s %s", "Formatted:", langTag.str()));
                        print(outStream, "Formatted:",   langTag.str(),           true);
                        print(outStream, "Language:",    langTag.getLanguage(),   true);
                        print(outStream, "Script:",      langTag.getScript(),     true);
                        print(outStream, "Region:",      langTag.getRegion(),     true);
                        print(outStream, "Variant:",     langTag.getVariant(),    false);
                        print(outStream, "Extension:",   langTag.getExtension(),  false);
                        print(outStream, "Private Use:", langTag.getPrivateUse(), false);
                    } catch (JenaException | LangTagException ex) {
                        outStream.println("Bad language tag: "+ex.getMessage());
                    }
                }
            } finally {
                finishFixed(outStream);
                System.out.flush();
                System.err.flush();
                System.setOut(stdout);
                System.setErr(stdout);
            }

            outStream.println("</body>");
            outStream.println("</html>");
        } catch (IOException ex) {}
    }

    private static void print(ServletOutputStream outStream, String label, String value, boolean always) throws IOException {
        if ( value == null ) {
            if ( ! always )
                return;
            value = "-";
        }
        outStream.print(String.format("  %-14s %s\n", label, value));
    }
}
