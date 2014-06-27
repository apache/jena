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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.shared.*;

import java.io.*;
import java.util.Locale ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Writes out an XML serialization of a model.
 */
public class NTripleWriter extends Object implements RDFWriter {

    RDFErrorHandler errorHandler = new RDFDefaultErrorHandler();

    protected static Logger logger = LoggerFactory.getLogger( NTripleWriter.class );
    
    public NTripleWriter() {
    }
    @Override
    public void write(Model model, OutputStream out, String base)
         {
        try {
            Writer w;
            try {
                w = new OutputStreamWriter(out, "ascii");
            } catch (UnsupportedEncodingException e) {
                logger.warn( "ASCII is not supported: in NTripleWriter.write", e );
                w = FileUtils.asUTF8(out);
            }
            write(model, w, base);
            w.flush();

        } catch (Exception ioe) {
            errorHandler.error(ioe);
        }
    }
    @Override
    public void write(Model model, Writer writer, String base)
         {
        try {
            PrintWriter pw;
            if (writer instanceof PrintWriter) {
                pw = (PrintWriter) writer;
            } else {
                pw = new PrintWriter(writer);
            }

            StmtIterator iter = model.listStatements();
            Statement stmt = null;

            while (iter.hasNext()) {
                stmt = iter.nextStatement();
                writeResource(stmt.getSubject(), pw);
                pw.print(" ");
                writeResource(stmt.getPredicate(), pw);
                pw.print(" ");
                writeNode(stmt.getObject(), pw);
                pw.println(" .");
            }
            pw.flush();
        } catch (Exception e) {
            errorHandler.error(e);
        }
    }

    /** Set a property to control the writer's behaviour.
     *
     * <p>This writer currently recognises no properties.  Invoking this
     * method always causes an <CODE>UnknownPropertyException</CODE>
     * to be raised.</p>?
     * @param propName The name of the property to be set
     * @param propValue The new value of the property
     * @return the previous value of the property
     */
    @Override
    public Object setProperty(String propName, Object propValue) {
        throw new UnknownPropertyException( propName );
    }

    public void setNsPrefix(String prefix, String ns) {
    }
    
    public String getPrefixFor( String uri )
        { return null; }

    @Override
    public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
        RDFErrorHandler old = this.errorHandler;
        this.errorHandler = errHandler;
        return old;
    }

    public static void write(Model model, PrintWriter writer) {
        StmtIterator iter = model.listStatements();
        Statement stmt = null;

        while (iter.hasNext()) {
            stmt = iter.nextStatement();
            writeResource(stmt.getSubject(), writer);
            writer.print(" ");
            writeResource(stmt.getPredicate(), writer);
            writer.print(" ");
            writeNode(stmt.getObject(), writer);
            writer.println(" .");
        }
    }

    protected static void writeResource(Resource r, PrintWriter writer)
         {
        if (r.isAnon()) {
            writer.print(anonName(r.getId()));
        } else {
            writer.print("<");
            writeURIString(r.getURI(), writer);
            writer.print(">");
        }
    }
    static private boolean okURIChars[] = new boolean[128];
    static {
        for (int i = 32; i < 127; i++)
            okURIChars[i] = true;
        okURIChars['<'] = false;
        okURIChars['>'] = false;
        okURIChars['\\'] = false;

    }
    private static void writeURIString(String s, PrintWriter writer) {

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < okURIChars.length && okURIChars[c]) {
                writer.print(c);
            } else {
                String hexstr = Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
                int pad = 4 - hexstr.length();
                writer.print("\\u");
                for (; pad > 0; pad--)
                    writer.print("0");
                writer.print(hexstr);
            }
        }
    }
    private static void writeString(String s, PrintWriter writer) {

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '"') {
                writer.print('\\');
                writer.print(c);
            } else if (c == '\n') {
                writer.print("\\n");
            } else if (c == '\r') {
                writer.print("\\r");
            } else if (c == '\t') {
                writer.print("\\t");
            } else if (c >= 32 && c < 127) {
                writer.print(c);
            } else {
                String hexstr = Integer.toHexString(c).toUpperCase();
                int pad = 4 - hexstr.length();
                writer.print("\\u");
                for (; pad > 0; pad--)
                    writer.print("0");
                writer.print(hexstr);
            }
        }
    }
    protected static void writeLiteral(Literal l, PrintWriter writer) {
        String s = l.getString();
        /*
        if (l.getWellFormed())
        	writer.print("xml");
        */
        writer.print('"');
        writeString(s, writer);
        writer.print('"');
        String lang = l.getLanguage();
        if (lang != null && !lang.equals(""))
            writer.print("@" + lang);
        String dt = l.getDatatypeURI();
        if (dt != null && !dt.equals(""))
            writer.print("^^<" + dt + ">");
    }

    protected static void writeNode(RDFNode n, PrintWriter writer)
         {
        if (n instanceof Literal) {
            writeLiteral((Literal) n, writer);
        } else {
            writeResource((Resource) n, writer);
        }
    }

    protected static String anonName(AnonId id) {
        String name = "_:A";
        String sid = id.toString();
        for (int i = 0; i < sid.length(); i++) {
            char c = sid.charAt(i);
            if (c == 'X') {
                name = name + "XX";
            } else if (Character.isLetterOrDigit(c)) {
                name = name + c;
            } else {
                name = name + "X" + Integer.toHexString(c) + "X";
            }
        }
        return name;
    }
}
