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

// Package
///////////////
package jena;


// Imports
///////////////

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.RDFWriterFImpl;
import com.hp.hpl.jena.shared.NoWriterForLangException;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.*;

import jena.cmdline.*;


/**
 * <p>
 * An RDF utility that takes its name from the Unix utility <em>cat</em>, and
 * is used to generate serialisations of the contents of zero or more
 * input model serialisations. <strong>Note</strong> In a change from previous
 * versions, but to ensure compatability with standard argument handling
 * practice, the input language options are <em>no longer sticky</em>. In
 * previous versions, <code>rdfcat -n A B C</code> would ensure that A, B
 * and C were all read as N3. From Jena 2.5.2 onwards, this requires:
 * <code>rdfcat -n A -n B -n C</code>, or the use of the <code>-in</code>
 * option.
 * </p>
 * <p>Synopsis:</p>
 * <pre>
 * java jena.rdfcat (options|input)*
 * where options are:
 *   -out N3  (aliases n, n3, ttl)
 *   -out N-TRIPLE  (aliases t, ntriple)
 *   -out RDF/XML  (aliases x, rdf, xml, rdfxml)
 *   -out RDF/XML-ABBREV (default)
 *   -in N3  (aliases n, n3, ttl)
 *   -in N-TRIPLE  (aliases t, ntriple)
 *   -in RDF/XML  (aliases x, rdf, xml, rdfxml)
 *   -include
 *   -noinclude (default)
 *
 * input is one of:
 *   -n &lt;filename&gt; for n3 input  (aliases -n3, -N3, -ttl)
 *   -x &lt;filename&gt; for rdf/xml input  (aliases -rdf, -xml, -rdfxml)
 *   -t &lt;filename&gt; for n-triple input  (aliases -ntriple)
 * or just a URL, a filename, or - for the standard input.
 * </pre>
 * <p>
 * The default
 * input language is RDF/XML, but the reader will try to guess the
 * input language based on the file extension (e.g. N3 for file with a .n3
 * file extension.
 * </p>
 * <p>The input language options set the language for the following file
 * name only. So in the following example, input
 * A is read as N3, inputs B, C and D are read as RDF/XML,
 * while stdin is read as N-TRIPLE:</p>
 * <pre>
 * java jena.rdfcat -n A B C -t - -x D
 * </pre>
 * <p>To change the default input language for all files that do
 * not have a specified language encoding, use the <code>-in</code> option.
 * </p>
 * <p>If the <code>include</code> option is set, the input files are scanned
 * for <code>rdfs:seeAlso</code> and <code>owl:imports</code> statements, and
 * the objects of these statements are read as well.  By default, <code>include</code>
 * is off. If <code>include</code> is turned on, the normal behaviour is for
 * the including statements (e.g <code>owl:imports</code> to be filtered
 * from the output models. To leave such statements in place, use the <code>--nofilter</code>
 * option.</p>
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
 *
 * Concatenate two N3-serving http URL's as N-TRIPLE
 * java jena.rdfcat -in N3 -out N-TRIPLE http://example.com/a http://example.com/b
 * </pre>
 * <p>Note that, in a difference from the Unix utility <code>cat</code>, the order
 * of input statements is not preserved. The output document is a merge of the
 * input documents, and does not preserve any statement ordering from the input
 * serialisations. Also, duplicate triples will be suppressed.</p>
 */
public class rdfcat
{
    static { setLog4jConfiguration() ; }
    
    // Constants
    //////////////////////////////////

    /** Argument setting expected input language to N3 */
    public final ArgDecl IN_N3 = new ArgDecl( true, "n", "n3", "ttl", "N3",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    m_actionQ.add( new ReadAction( val, "N3") );
            }} );

    /** Argument setting expected input language to RDF/XML */
    public final ArgDecl IN_RDF_XML = new ArgDecl( true, "x", "xml", "rdfxml", "rdf",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    m_actionQ.add( new ReadAction( val, "RDF/XML") );
            }} );

    /** Argument setting expected input language to NTRIPLE */
    public final ArgDecl IN_NTRIPLE = new ArgDecl( true, "t", "ntriples", "ntriple", "n-triple", "n-triples",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    m_actionQ.add( new ReadAction( val, "N-TRIPLE" ) );
            }} );

    /** Argument to set the output language */
    public final ArgDecl OUT_LANG = new ArgDecl( true, "out",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    setOutput( val );
            }} );

    /** Argument to set the default input language */
    public final ArgDecl IN_LANG = new ArgDecl( true, "in",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    expectInput( val );
            }} );

    /** Argument to turn include processing on */
    public final ArgDecl INCLUDE = new ArgDecl( false, "include",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    setInclude( true );
            }} );

    /** Argument to turn include processing off */
    public final ArgDecl NOINCLUDE = new ArgDecl( false, "noinclude",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    setInclude( false );
            }} );

    /** Argument to leave import/seeAlso statements in place in flattened models */
    public final ArgDecl NOFILTER = new ArgDecl( false, "nofilter",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    setRemoveIncludeStatements( false );
            }} );

    /** Argument to show usage */
    public final ArgDecl HELP = new ArgDecl( false, "help",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    usage();
            }} );
    public final ArgDecl USAGE = new ArgDecl( false, "usage",
            new ArgHandler() {
                @Override
                public void action( String arg, String val ) throws IllegalArgumentException {
                    usage();
            }} );


    // Static variables
    //////////////////////////////////

    static private Logger log = LoggerFactory.getLogger( rdfcat.class );

    // Instance variables
    //////////////////////////////////

    /** The command line processor that handles the arguments */
    protected CommandLine m_cmdLine = new RCCommandLine().add( IN_N3 )
                                                         .add( IN_NTRIPLE )
                                                         .add( IN_RDF_XML )
                                                         .add( OUT_LANG )
                                                         .add( IN_LANG )
                                                         .add( INCLUDE )
                                                         .add( NOINCLUDE )
                                                         .add( NOFILTER )
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
    protected Set<String> m_seen = new HashSet<>();

    /** Flag to control whether import/include statements are filtered from merged models */
    protected boolean m_removeIncludeStatements = true;

    /** Action queue */
    protected List<RCAction> m_actionQ = new ArrayList<>();


    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    public static void main( String... args ) {
        new rdfcat().go( args );
    }

    // Internal implementation methods
    //////////////////////////////////

    /* main loop */
    protected void go( String[] args ) {
        // ensure we use the new RIOT parser subsystem
        enableRIOTParser();

        m_cmdLine.process( args );

        // process any stored items
        for (int i = 0; i < m_cmdLine.numItems(); i++) {
            m_actionQ.add(  new ReadAction( m_cmdLine.getItem( i ), getExpectedInput() ) );
        }
        for ( RCAction aM_actionQ : m_actionQ )
        {
            aM_actionQ.run( this );
        }

        // generate the output
        m_model.write( getOutputStream(), m_outputFormat );
    }

    /** Set the input language of next and subsequent reads */
    protected void expectInput( String lang ) {
        m_inputFormat = lang;
    }

    /** Answer the currently expected input format */
    protected String getExpectedInput() {
        return m_inputFormat;
    }

    /** Set the language to write the output model in */
    protected void setOutput( String lang ) {
        m_outputFormat = getCheckedLanguage( lang );
    }

    /**
         Answer the full, checked, language name expanded from <code>shortName</code>.
        The shortName is expanded according to the table of abbreviations [below].
        It is then checked against RDFWriterFImpl's writer table [this is hacky but
        at the moment it's the most available interface] and the NoWriter exception
        trapped and replaced by the original IllegalArgument exception.
    */
    public static String getCheckedLanguage( String shortLang )
        {
        String fullLang = unabbreviate.get( shortLang );
        String tryLang = (fullLang == null ? shortLang : fullLang);
        try { new RDFWriterFImpl().getWriter( tryLang ); }
        catch (NoWriterForLangException e)
            { throw new IllegalArgumentException( "'" + shortLang + "' is not recognised as a legal output format" ); }
        return tryLang;
        }

    /**
        Map from abbreviated names to full names.
    */
    public static Map<String,String> unabbreviate = makeUnabbreviateMap();

    /**
        Construct the canonical abbreviation map.
    */
    protected static Map<String,String> makeUnabbreviateMap()
        {
        Map<String,String> result = new HashMap<>();
        result.put( "x", "RDF/XML" );
        result.put( "rdf", "RDF/XML" );
        result.put( "rdfxml", "RDF/XML" );
        result.put( "xml", "RDF/XML" );
        result.put( "n3", "N3" );
        result.put( "n", "N3" );
        result.put( "ttl", "N3" );
        result.put( "ntriples", "N-TRIPLE" );
        result.put( "ntriple", "N-TRIPLE" );
        result.put( "t", "N-TRIPLE" );
        result.put( "owl", "RDF/XML-ABBREV" );
        result.put( "abbrev", "RDF/XML-ABBREV" );
        return result;
        }

    /** Set the flag to include owl:imports and rdf:seeAlso files in the output, default off */
    protected void setInclude( boolean incl ) {
        m_include = incl;
    }

    /** Set the flag to leave owl:imports and rdfs:seeAlso statements in place, rather than filter them */
    protected void setRemoveIncludeStatements( boolean f ) {
        m_removeIncludeStatements = f;
    }

    /* Take the string as an input file or URI, and
     * try to read using the current default input syntax.
     */
    protected void readInput( String inputName ) {
        List<IncludeQueueEntry> queue = new ArrayList<>();
        queue.add( new IncludeQueueEntry( inputName, null ) );

        while (!queue.isEmpty()) {
            IncludeQueueEntry entry = queue.remove( 0 );
            String in = entry.m_includeURI;

            if (!m_seen.contains( in )) {
                m_seen.add( in );
                Model inModel = ModelFactory.createDefaultModel();

                // check for stdin
                if (in.equals( "-" )) {
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

                // do we remove the include statement?
                if (m_removeIncludeStatements && entry.m_includeStmt != null) {
                    m_model.remove( entry.m_includeStmt );
                }
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
    protected void addIncludes( Model inModel, List<IncludeQueueEntry> queue ) {
        // first collect any rdfs:seeAlso statements
        StmtIterator i = inModel.listStatements( null, RDFS.seeAlso, (RDFNode) null );
        while (i.hasNext()) {
            Statement s = i.nextStatement();
            queue.add( new IncludeQueueEntry( getURL( s.getObject() ), s ) );
        }

        // then any owl:imports
        i = inModel.listStatements( null, OWL.imports, (RDFNode) null );
        while (i.hasNext()) {
            Statement s = i.nextStatement();
            queue.add( new IncludeQueueEntry( getURL( s.getResource() ), s ) );
        }
    }

    protected void usage() {
        System.err.println( "Usage: java jena.rdfcat (option|input)*" );
        System.err.println( "Concatenates the contents of zero or more input RDF documents." );
        System.err.println( "Options: -out N3 | N-TRIPLE | RDF/XML | RDF/XML-ABBREV" );
        System.err.println( "         -n  expect subsequent inputs in N3 syntax" );
        System.err.println( "         -x  expect subsequent inputs in RDF/XML syntax" );
        System.err.println( "         -t  expect subsequent inputs in N-TRIPLE syntax" );
        System.err.println( "         -[no]include  include rdfs:seeAlso and owl:imports" );
        System.err.println( "input can be filename, URL, or - for stdin" );
        System.err.println( "Recognised aliases for -n are: -n3 -ttl or -N3" );
        System.err.println( "Recognised aliases for -x are: -xml -rdf or -rdfxml" );
        System.err.println( "Recognised aliases for -t are: -ntriple" );
        System.err.println( "Output format aliases: x, xml or rdf for RDF/XML, n, n3 or ttl for N3, t or ntriple for N-TRIPLE" );
        System.err.println( "See the Javadoc for jena.rdfcat for additional details." );


        System.exit(0);
    }

    /** Answer a URL string from a resource or literal */
    protected String getURL( RDFNode n ) {
        return n.isLiteral() ? ((Literal) n).getLexicalForm() : ((Resource) n).getURI();
    }

    /**
     * Enable the new RIOT parser subsystem if it is available
     */
    private void enableRIOTParser() {
        try {
            Class<?> sysRIOT = Class.forName( "org.openjena.riot.SysRIOT" );
            Method initMethod = sysRIOT.getMethod( "init" );
            initMethod.invoke( null );
        }
        catch (ClassNotFoundException e) {
            // log if we're in debug mode, but otherwise ignore
//            log.debug( "Did not initialise RIOT parser: " +  e.getMessage(), e );
        }
        catch (SecurityException e) {
            // log if we're in debug mode, but otherwise ignore
            log.debug( "Did not initialise RIOT parser: " +  e.getMessage(), e );
        }
        catch (NoSuchMethodException e) {
            // log if we're in debug mode, but otherwise ignore
            log.debug( "Did not initialise RIOT parser: " +  e.getMessage(), e );
        }
        catch (IllegalArgumentException e) {
            // log if we're in debug mode, but otherwise ignore
            log.debug( "Did not initialise RIOT parser: " +  e.getMessage(), e );
        }
        catch (IllegalAccessException e) {
            // log if we're in debug mode, but otherwise ignore
            log.debug( "Did not initialise RIOT parser: " +  e.getMessage(), e );
        }
        catch (InvocationTargetException e) {
            // log if we're in debug mode, but otherwise ignore
            log.debug( "Did not initialise RIOT parser: " +  e.getMessage(), e );
        }
    }

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /** Local extension to CommandLine to handle mixed arguments and values */
    protected class RCCommandLine
        extends CommandLine
    {
        /** Don't stop processing args on the first non-arg */
        public boolean xendProcessing( String argStr ) {
            return false;
        }

        /** Handle an unrecognised argument by assuming it's a URI to read */
        @Override
        public void handleUnrecognizedArg( String argStr ) {
            if (argStr.equals("-") || !argStr.startsWith( "-" )) {
                // queue this action for reading later
                m_actionQ.add( new ReadAction( argStr, getExpectedInput() ) );
            }
            else {
                System.err.println( "Unrecognised argument: " + argStr );
                usage();
            }
        }

        /** Hook to test whether this argument should be processed further
         */
        @Override
        public boolean ignoreArgument( String argStr ) {
            return !argStr.startsWith("-") || argStr.length() == 1;
        }

        /** Answer an iterator over the non-arg items from the command line */
        public Iterator<String> getItems() {
            return items.iterator();
        }
    }

    /** Queue entry that contains both a URI to be included, and a statement that may be removed */
    protected class IncludeQueueEntry
    {
        protected String m_includeURI;
        protected Statement m_includeStmt;
        protected IncludeQueueEntry( String includeURI, Statement includeStmt ) {
            m_includeURI = includeURI;
            m_includeStmt = includeStmt;
        }
    }

    /** Simple action object for local processing queue */
    protected interface RCAction {
        public void run( rdfcat rc );
    }

    /** Action to set the output format */
    protected class ReadAction
        implements RCAction
    {
        private String m_lang;
        private String m_uri;
        protected ReadAction( String uri, String lang ) {
            m_lang = lang;
            m_uri = uri;
        }

        /** perform the action of reading a uri */
        @Override
        public void run( rdfcat rc ) {
            String l = rc.getExpectedInput();
            if (m_lang != null) {
                // if an input lang was given, use that
                rc.expectInput( m_lang );
            }
            rc.readInput( m_uri );

            // put the lang back to default
            rc.expectInput( l );
        }
    }
}
