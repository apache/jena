/*
 *  (c) Copyright Hewlett-Packard Company 2002
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * $Id: schemagen.java,v 1.9 2003-04-14 15:11:00 chris-dollin Exp $
 */

package jena;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.mem.ModelMem;

import java.net.URL;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/** A program to read in an RDF schema and generate a corresponding Jena
 *  constant schema class.
 *
 *  <p>This program will read an RDF schema and generate the source for
 *     a Jena Vocabulary class for that schema.</p>
 *
 *  <pre>java jena.schemagen name schemaURIRef input output [lang]
 *
 *       name is the vocabulary name e.g. RDF or RDFS
 *       schemaURIRef is the URI ref for the schema being processed
 *       input can be a file name or a URI
 *       output must be a file name or '-' for standard out
 *       lang is the language of the input and defaults to RDF/XML.
 *  </pre>
 *
 *  <p>This program will make feeble attempt to convert names to legal java
 *     names, i.e. convert '-' and '.' characters to '_' characters.  The
 *     user may have to correct the output if more exotic character sequences
 *     are used, or this fixup leads to name clashes.</p>
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.9 $ $Date: 2003-04-14 15:11:00 $
 */
public class schemagen extends java.lang.Object {

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {

        if (args.length < 4 || args.length > 5) {
            usage();
            System.exit(-1);
        }

        String name = args[0];
        String schemaURIRef = args[1];
        String input = args[2];
        String output = args[3];
        String lang = "RDF/XML";
        if (args.length > 4) {
            lang = args[4];
        }

        try {
            Model schema = new ModelMem();

            read(schema, input, lang);

            PrintStream out = null;
            if (output.equals("-")) {
                out = System.out;
            } else {
                out = new PrintStream(new FileOutputStream(output));
            }

            renderVocabularyClass(name, schemaURIRef, schema, out);

        } catch (Exception e) {
            System.err.println("Unhandled exception:");
            System.err.println("    " + e.toString());
            System.exit(-1);
        }
    }

    protected static void usage() {
        System.err.println("usage:");
        System.err.println(
            "    java jena.schemagen name schemaURIRef input output [lang]");
        System.err.println();
        System.err.println("    name is the name of the vocabulary");
        System.err.println("         It may be simple, e.g. RDF, or it may" +
                                    " be fully qualified");
        System.err.println("    input can be URL's or filenames");
        System.err.println("    lang can take values");
        System.err.println("      RDF/XML");
        System.err.println("      N-TRIPLE");
        System.err.println("    lang defaults to RDF/XML");
        System.err.println();
    }

    protected static void read(Model model, String in, String lang)
        throws RDFException, java.io.FileNotFoundException {
        try {
            URL url = new URL(in);
            model.read(in, lang);
        } catch (java.net.MalformedURLException e) {
            model.read(new FileReader(in), "", lang);
        }
    }

    protected static void renderVocabularyClass(
        String name,
        String uriRef,
        Model schema,
        PrintStream out)
        throws RDFException {
        Set classNames = listNames(uriRef, schema, RDFS.Class);
        Set propertyNames = listNames(uriRef, schema, RDF.Property);
        renderPreamble(name, uriRef, out);
        renderDeclarations(classNames, "Resource", out);
        renderDeclarations(propertyNames, "Property", out);
        renderInitializer(classNames, propertyNames, out);
        renderPostamble(out);
    }

    protected static Set listNames(String uriRef, Model schema, Resource type)
        throws RDFException {

        Set result = new HashSet();

        // extract all the resources of the given type in the schema
        StmtIterator iter =
            schema.listStatements( null, RDF.type, type );
        // for each one
        while (iter.hasNext()) {
            Resource r = iter.nextStatement().getSubject();
            // ignore if bNode
            if (!r.isAnon()) {
                // get the URI and check if it matches the vocabulary
                String s = r.getURI();
                if (s.startsWith(uriRef)) {
                    // add the name to the set
                    result.add(s.substring(uriRef.length()));
                }
            }
        }

        return result;
    }

    protected static void renderDeclarations(
        Set names,
        String type,
        PrintStream out)
        throws RDFException {
        Iterator iter = names.iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            String jname = makeJavaLegalId(name);
            out.println(
                "           static String n" + jname + " = \"" + name + "\";");
            out.println("    public static " + type + " " + jname + ";");
        }
    }

    protected static void renderInitializer(
        Set classNames,
        Set propertyNames,
        PrintStream out)
        throws RDFException {
        out.println();
        out.println("    static {");
        out.println("        try {");
        renderTypedInitializer(classNames, "Resource", out);
        renderTypedInitializer(propertyNames, "Property", out);
        out.println("        } catch (Exception e) {");
        out.println("            ErrorHelper.logInternalError(\"RDF\", 1, e);");
        out.println("        }");
        out.println("    }");
    }

    protected static void renderTypedInitializer(
        Set names,
        String type,
        PrintStream out) {
        Iterator iter = names.iterator();
        while (iter.hasNext()) {
            String jname = makeJavaLegalId((String) iter.next());
            out.println(
                "            "
                    + jname
                    + " = ResourceFactory.create"
                    + type
                    + "(uri + n"
                    + jname
                    + ");");
        }
    }

    protected static void renderPreamble(
        String name,
        String uriRef,
        PrintStream out) {
        
        // compute the package name
        String packageName;
        if (name.indexOf('.') == -1) {
            packageName = "com.hp.hpl.jena.vocabulary";
        } else {
            packageName = name.substring(0, name.lastIndexOf('.'));
            name = name.substring(name.lastIndexOf('.') + 1);
        }
        
        out.println(
            "/* Vocabulary Class generated by Jena vocabulary generator");
        out.println(" *");
        out.println(" * On: " + (new Date()).toString());
        out.println(
            " * Version $" + "Id" + "$"); // the line split up deliberately
        out.println(" */");
        out.println("package " + packageName + ";");
        out.println();

        out.println("import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;");
        out.println("import com.hp.hpl.jena.rdf.model.Model;");
        out.println("import com.hp.hpl.jena.rdf.model.Resource;");
        out.println("import com.hp.hpl.jena.rdf.model.ResourceFactory;");
        out.println("import com.hp.hpl.jena.rdf.model.Property;");
        out.println("import com.hp.hpl.jena.rdf.model.RDFException;");
        out.println();

        out.println(
            "/** " + name + " vocabulary class for namespace " + uriRef);
        out.println(" */");
        out.println("public class " + name + " {");
        out.println();

        out.println(
            "    protected static final String uri =\"" + uriRef + "\";");
        out.println();

        out.println("    /** returns the URI for this schema");
        out.println("     * @return the URI for this schema");
        out.println("     */");
        out.println("    public static String getURI() {");
        out.println("          return uri;");
        out.println("    }");
    }

    protected static void renderPostamble(PrintStream out) {
        out.println("}");
    }

    protected static String makeJavaLegalId(String name) {
        // this code is imperfect.  It switch '-' and '.'
        // chars to "_" which are legal in java.
        // the user may have to fix up the output themselves
        // if this doesn't work.

        name = Util.replace(name, "-", "_");
        return Util.replace(name, ".", "_");
    }

}
