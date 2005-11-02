/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       ian.dickinson@hp.com
 * Package            Jena2
 * Web site           http://jena.sourceforge.net
 * Created            16-Sep-2005
 * Filename           $RCSfile: rdfcat.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2005-11-02 22:31:25 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 *****************************************************************************/

// Package
///////////////
package jena;


// Imports
///////////////

import java.io.OutputStream;
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.*;

import jena.cmdline.*;


/**
 * <p>
 * An RDF utility that takes its name from the Unix utility <em>cat</em>, and
 * is used to generate serialisations of the contents of zero or more
 * input model serialisations.
 * </p>
 * <p>Synopsis:</p>
 * <pre>
 * java jena.rdfcat (options|lang|input)*
 * where options are:
 *   -out N3  (aliases n, n3, ttl)
 *   -out N-TRIPLE  (aliases t, ntriple)
 *   -out RDF/XML  (aliases x, rdf, xml, rdfxml)
 *   -out RDF/XML-ABBREV (default)
 *   -include
 *   -noinclude (default)
 *
 * lang is one of:
 *   -n for n3 input  (aliases -n3, -N3, -ttl)
 *   -x for rdf/xml input  (aliases -rdf, -xml, -rdfxml)
 *   -t for n-triple input  (aliases -ntriple)
 *
 * input is a URL, a filename, or - for the standard input
 * </pre>
 * <p>The input language options set the default language for all subsequent
 * arguments, up to the next lang option. So in the following example, inputs
 * A, B and C are read as N3, while D and stdin are read as RDF/XML:</p>
 * <pre>
 * java jena.rdfcat -n A B C -x - D
 * </pre>
 * <p>If the <code>include</code> option is set, the input files are scanned
 * for <code>rdfs:seeAlso</code> and <code>owl:imports</code> statements, and
 * the objects of these statements are read as well.  By default, <code>include</code>
 * is off.</p>
 * <p>rdfcat uses the Jena {@link com.hp.hpl.jena.util.FileManager FileManager}
 * to resolve input URI's to locations. This allows, for example, <code>http:</code>
 * URI's to be re-directed to local <code>file:</code> locations, to avoid a
 * network transaction.</p>
 * <p>Examples:</p>
 * <pre>
 * Join two RDF/XML files together into a single model in RDF/XML-ABBREV:
 * java jena.rdfcat in1 in2 &gt; out.rdf
 *
 * Convert a single RDF/XML file to N3:
 * java jena.rdfcat in1 -out N3 &gt; out.n3
 *
 * Join two owl files one N3, one XML, and their imports, into a single NTRIPLE file:
 * java jena.rdfcat -out NTRIPLE -include in1.owl -n in2.owl > out.ntriple
 * </pre>
 * <p>Note that, in a difference from the Unix utility <code>cat</code>, the order
 * of input statements is not preserved. The output document is a merge of the
 * input documents, and does not preserve any statement ordering from the input
 * serialisations. Also, duplicate triples will be suppressed.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: rdfcat.java,v 1.7 2005-11-02 22:31:25 ian_dickinson Exp $)
 */
public class rdfcat
{
    // Constants
    //////////////////////////////////

    /** Argument setting expected input language to N3 */
    public final ArgDecl IN_N3 = new ArgDecl( false, "n", "n3", "ttl", "N3",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    expectInput("N3");
            }} );

    /** Argument setting expected input language to RDF/XML */
    public final ArgDecl IN_RDF_XML = new ArgDecl( false, "x", "xml", "rdfxml", "rdf",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    expectInput("RDF/XML");
            }} );

    /** Argument setting expected input language to NTRIPLE */
    public final ArgDecl IN_NTRIPLE = new ArgDecl( false, "t", "ntriples", "ntriple", "n-triple", "n-triples",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    expectInput("N-TRIPLE");
            }} );

    /** Argument to set the output language */
    public final ArgDecl OUT_LANG = new ArgDecl( true, "out",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    setOutput( val );
            }} );

    /** Argument to turn include processing on */
    public final ArgDecl INCLUDE = new ArgDecl( false, "include",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    setInclude( true );
            }} );

    /** Argument to turn include processing off */
    public final ArgDecl NOINCLUDE = new ArgDecl( false, "noinclude",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    setInclude( false );
            }} );

    /** Argument to show usage */
    public final ArgDecl HELP = new ArgDecl( false, "help",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    usage();
            }} );
    public final ArgDecl USAGE = new ArgDecl( false, "usage",
            new ArgHandler() {
                public void action( String arg, String val ) throws IllegalArgumentException {
                    usage();
            }} );


    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    /** The command line processor that handles the arguments */
    protected CommandLine m_cmdLine = new RCCommandLine().add( IN_N3 )
                                                         .add( IN_NTRIPLE )
                                                         .add( IN_RDF_XML )
                                                         .add( OUT_LANG )
                                                         .add( INCLUDE )
                                                         .add( NOINCLUDE )
                                                         .add( HELP )
                                                         .add( USAGE );

    /** The merged model containing all of the inputs */
    protected Model m_model = ModelFactory.createDefaultModel();

    /** The output format to write to, defaults to RDF/XML-ABBREV */
    protected String m_outputFormat = "RDF/XML-ABBREV";

    /** The input format we're expecting for the next URL to be read - defaults to RDF/XML */
    protected String m_inputFormat = "RDF/XML";

    /** Flag to indicate whether we include owl:imports and rdfs:seeAlso */
    protected boolean m_include = false;

    /** List of URL's that have been loaded already, occurs check */
    protected Set m_seen = new HashSet();


    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public static void main( String[] args ) {
        new rdfcat().go( args );
    }

    // Internal implementation methods
    //////////////////////////////////

    /* main loop */
    protected void go( String[] args ) {
        m_cmdLine.process( args );
        m_model.write( getOutputStream(), m_outputFormat );
    }

    /** Set the input language of next and subsequent reads */
    protected void expectInput( String lang ) {
        m_inputFormat = lang;
    }

    /** Set the language to write the output model in */
    protected void setOutput( String lang ) {
        if ("RDF/XML".equalsIgnoreCase( lang ) ||
            "x".equalsIgnoreCase( lang) ||
            "xml".equalsIgnoreCase( lang) ||
            "rdf".equalsIgnoreCase( lang) ||
        "rdfxml".equalsIgnoreCase( lang ))
        {
            m_outputFormat = "RDF/XML";
        }
        else if ("RDF/XML-ABBREV".equalsIgnoreCase( lang ) ||
                 "abbrev".equalsIgnoreCase( lang ))
        {
            m_outputFormat = "RDF/XML-ABBREV";
        }
        else if ("N3".equalsIgnoreCase( lang ) ||
                 "n".equalsIgnoreCase( lang ) ||
                 "ttl".equalsIgnoreCase( lang ))
        {
            m_outputFormat = "N3";
        }
        else if ("N-TRIPLE".equalsIgnoreCase( lang ) ||
                 "ntriples".equalsIgnoreCase( lang ) ||
                 "ntriple".equalsIgnoreCase( lang ) ||
                 "t".equalsIgnoreCase( lang ))
        {
            m_outputFormat = "N-TRIPLE";
        }
        else {
            throw new IllegalArgumentException( lang + " is not recognised as a legal output format" );
        }
    }

    /** Set the flag to include owl:imports and rdf:seeAlso files in the output, default off */
    protected void setInclude( boolean incl ) {
        m_include = incl;
    }

    /* Take the string as an input file or URI, and
     * try to read using the current default input syntax.
     */
    protected void readInput( String inputName ) {
        List queue = new ArrayList();
        queue.add( inputName );

        while (!queue.isEmpty()) {
            String in = (String) queue.remove( 0 );

            if (!m_seen.contains( in )) {
                m_seen.add( in );
                Model inModel = ModelFactory.createDefaultModel();

                // check for stdin
                if (inputName.equals( "-" )) {
                    inModel.read( System.in, null, m_inputFormat );
                }
                else {
                    // lang from extension overrides default set on command line
                    String lang = FileUtils.guessLang( in, m_inputFormat );
                    FileManager.get().readModel( inModel, in, lang );
                }

                // check for anything more that we need to read
                if (m_include) {
                    addIncludes( inModel, queue );
                }

                // merge the models
                m_model.add( inModel );
                m_model.setNsPrefixes( inModel );
            }
        }
    }

    /** Return the stream to which the output is written, defaults to stdout */
    protected OutputStream getOutputStream() {
        return System.out;
    }

    /** Add any additional models to include given the rdfs:seeAlso and
     * owl:imports statements in the given model
     */
    protected void addIncludes( Model inModel, List queue ) {
        // first collect any rdfs:seeAlso statements
        StmtIterator i = inModel.listStatements( null, RDFS.seeAlso, (RDFNode) null );
        while (i.hasNext()) {
            queue.add( getURL( i.nextStatement().getObject() ));
        }

        // then any owl:imports
        i = inModel.listStatements( null, OWL.imports, (RDFNode) null );
        while (i.hasNext()) {
            queue.add( getURL( i.nextStatement().getResource() ) );
        }
    }

    protected void usage() {
        System.err.println( "Usage: java jena.rdfcat (option|input)*" );
        System.err.println( "Concatenates the contents of zero or more input RDF documents." );
        System.err.println( "Options: -out N3 | N-TRIPLE | RDF/XML | RDF/XML-ABBREV" );
        System.err.println( "         -n  expect subsequent inputs in N3 syntax" );
        System.err.println( "         -x  expect subsequent inputs in RDF/XML syntax" );
        System.err.println( "         -t  expect subsequent inputs in N-TRIPLE syntax" );
        System.err.println( "         -[no]include  include rdfs:seeAlso and owl;imports" );
        System.err.println( "input can be filename, URL, or - for stdin" );
        System.err.println( "Recognised aliases for -n are: -n3 -ttl or -N3" );
        System.err.println( "Recognised aliases for -x are: -xml -rdf or -rdfxml" );
        System.err.println( "Recognised aliases for -t are: -ntriple" );
        System.err.println( "Output format aliases: x, xml or rdf for RDF/XML, n, n3 or ttl for N3, t or ntriple for N-TRIPLE" );


        System.exit(0);
    }

    /** Answer a URL string from a resource or literal */
    protected String getURL( RDFNode n ) {
        return n.isLiteral() ? ((Literal) n).getLexicalForm() : ((Resource) n).getURI();
    }


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Local extension to CommandLine to handle mixed arguments and values */
    protected class RCCommandLine
        extends CommandLine
    {
        /** Don't stop processing args on the first non-arg */
        public boolean endProcessing( String argStr ) {
            return false;
        }

        /** Handle an unrecognised argument by assuming it's a URI to read */
        public void handleUnrecognizedArg( String argStr ) {
            if (argStr.equals("-") || !argStr.startsWith( "-" )) {
                readInput( argStr );
            }
            else {
                System.err.println( "Unrecognised argument: " + argStr );
                usage();
            }
        }
    }
}


/*
 *  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 *
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
 */
