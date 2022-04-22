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

package org.apache.jena.shex;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.shex.parser.ShExC;
import org.apache.jena.shex.parser.ShExJ;
import org.apache.jena.shex.sys.SysShex;
import org.apache.jena.sys.JenaSystem;

/**
 * <a href="https://shex.io/">ShEx</a>
 * <p>
 *
 * @see ShexValidator
 */
public class Shex {
    static { JenaSystem.init(); }

//    // Node used for a START shape.
//    public static Node startNode = SysShex.startNode;
    /** Node used for FOCUS in a shape map. */
    public static Node FOCUS = SysShex.focusNode;

    /**
     * Parse the string in ShExC syntax to produce a ShEx schema.
     * @param inputStr
     * @return ShexSchema
     */
    public static ShexSchema schemaFromString(String inputStr) {
        return schemaFromString(inputStr, null);
    }

    /**
     * Parse the string in ShExC syntax to produce a ShEx schema.
     * @param inputStr
     * @param baseURI
     * @return ShexSchema
     */
    public static ShexSchema schemaFromString(String inputStr, String baseURI) {
        ShexSchema shapes = ShExC.parse(new StringReader(inputStr), baseURI);
        return shapes;
    }

    /**
     * Read the file to produce a ShEx schema.
     * @param filenameOrURL
     * @return ShexSchema
     */
    public static ShexSchema readSchema(String filenameOrURL) {
        return readSchema(filenameOrURL, null);
    }

    /**
     * Read the file to produce a ShEx schema.
     * @param filenameOrURL
     * @param base
     * @return ShexSchema
     */
    public static ShexSchema readSchema(String filenameOrURL, String base) {
        InputStream input = RDFDataMgr.open(filenameOrURL);
        // Buffering done in ShexParser
//        if ( ! ( input instanceof BufferedInputStream ) )
//            input = new BufferedInputStream(input, 128*1024);
        String parserBase = (base != null) ? base : IRILib.filenameToIRI(filenameOrURL);
        ShexSchema shapes = ShExC.parse(input, IRILib.filenameToIRI(filenameOrURL), parserBase);
        return shapes;
    }

    /** Print shapes - the format details the internal structure */
    public static void printSchema(ShexSchema shexSchema) {
        IndentedWriter iOut = IndentedWriter.clone(IndentedWriter.stdout);
        printSchema(iOut, shexSchema);
    }

    /** Print shapes - the format details the internal structure */
    public static void printSchema(OutputStream outStream, ShexSchema shexSchema) {
        IndentedWriter iOut = new IndentedWriter(outStream);
        printSchema(iOut, shexSchema);
    }

    /** Print shapes - the format details the internal structure */
    public static void printSchema(IndentedWriter iOut, ShexSchema shexSchema) {
        Set<String> visited = new HashSet<>();
        if ( shexSchema.getSource() != null )
            visited.add(shexSchema.getSource());
        printSchema(iOut, shexSchema, visited);
    }

    private static void printSchema(IndentedWriter iOut, ShexSchema shexSchema, Set<String> visited) {
        try {
            boolean havePrinted = false;

            if ( ! shexSchema.getPrefixMap().isEmpty() ) {
                RiotLib.writePrefixes(iOut, shexSchema.getPrefixMap(), true);
                havePrinted = true;
            }

            if ( shexSchema.hasImports() ) {
                if ( havePrinted )
                    iOut.println();
                shexSchema.getImports().forEach(iriStr->{
                    String pname = shexSchema.getPrefixMap().abbreviate(iriStr);
                    if ( pname == null )
                        iOut.printf("IMPORT <%s>\n", iriStr);
                    else
                        iOut.printf("IMPORT %s\n", pname);
                });
                havePrinted = true;
            }

            if ( ! shexSchema.getShapes().isEmpty() ) {
                boolean shapePrinted = false;
                NodeFormatter nFmt = new NodeFormatterTTL(null, shexSchema.getPrefixMap());
                for ( ShexShape shape : shexSchema.getShapes() ) {
                    if ( havePrinted )
                        iOut.println();
                    shape.print(iOut, nFmt);
                    havePrinted = true;
                }
            }

            // Print imports.
            if ( shexSchema.hasImports() ) {
                if ( havePrinted )
                    iOut.println();
                shexSchema.getImports().forEach(iriStr->{
                    if ( visited.contains(iriStr) )
                        return;
                    visited.add(iriStr);
                    String prefix = iOut.getLinePrefix();
                    iOut.println("Import = <"+iriStr+">");
                    iOut.incIndent(4);
                    try {
                        ShexSchema imports = readSchema(iriStr);
                        iOut.setLinePrefix(prefix+"I");
                        printSchema(iOut, imports, visited);
                    } catch (Exception ex) {
                        iOut.println("Failed to read shapes: "+ex.getMessage());
                    } finally {
                        iOut.setLinePrefix(prefix);
                        iOut.decIndent(4);
                    }
                });
                havePrinted = true;
            }
        } finally {
            iOut.flush();
        }
    }

    /**
     * Parse the file to get a ShEx shape map.
     * @param filename
     * @return ShexShapeMap
     */
    public static ShexMap readShapeMap(String filename) {
        return readShapeMap(filename, IRILib.filenameToIRI(filename));
    }

    /**
     * Parse the file to get a ShEx shape map.
     * @param filename
     * @param baseURI
     * @return ShexShapeMap
     */
    public static ShexMap readShapeMap(String filename, String baseURI) {
        InputStream input = IO.openFile(filename);
        return readShapeMap(input, baseURI);
    }

    /**
     * Parse the {@code InputStream} to get a ShEx shape map.
     * @param input
     * @param baseURI
     * @return ShexShapeMap
     */
    public static ShexMap readShapeMap(InputStream input, String baseURI) {
        return ShExC.parseShapeMap(input, baseURI);
    }

    /**
     * Parse a shape map from a {@code StringReader}.
     * @param inputStr
     * @param baseURI
     * @return ShexShapeMap
     */
    public static ShexMap shapeMapFromString(String inputStr, String baseURI) {
        return ShExC.parseShapeMap(new StringReader(inputStr), baseURI);
    }

    /** Read a {@link ShexMap} from a file or URL. */
    public static ShexMap readShapeMapJson(String filenameOrURL) {
        TypedInputStream in = RDFDataMgr.open(filenameOrURL);
        return readShapeMapJson(in.getInputStream());
    }

    /**
     * Parse the {@code InputStream} to get a ShEx shape map from JSON syntax.
     * @param input
     * @return ShexShapeMap
     */
    public static ShexMap readShapeMapJson(InputStream input) {
        return ShExJ.readShapeMapJson(input);
    }
}
