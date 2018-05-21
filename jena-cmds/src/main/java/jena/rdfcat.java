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

import static org.apache.jena.atlas.logging.LogCtl.setCmdLogging;

import java.io.OutputStream ;
import java.util.* ;
import java.util.function.BiConsumer;

import org.apache.jena.rdf.model.* ;
import org.apache.jena.rdf.model.impl.RDFWriterFImpl ;
import org.apache.jena.shared.NoWriterForLangException ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.util.FileManager ;
import org.apache.jena.util.FileUtils ;
import org.apache.jena.vocabulary.OWL ;
import org.apache.jena.vocabulary.RDFS ;


/**
 * <p>
 * An RDF utility that takes its name from the Unix utility <em>cat</em>, and
 * is used to generate serialisations of the contents of zero or more
 * input model serialisations. <strong>Note</strong> In a change from previous
 * versions, but to ensure compatibility with standard argument handling
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
 * <p>rdfcat uses the Jena {@link org.apache.jena.util.FileManager FileManager}
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
 * java jena.rdfcat -out NTRIPLE -include in1.owl -n in2.owl &gt; out.ntriple
 *
 * Concatenate two N3-serving http URL's as N-TRIPLE
 * java jena.rdfcat -in N3 -out N-TRIPLE http://example.com/a http://example.com/b
 * </pre>
 * <p>Note that, in a difference from the Unix utility <code>cat</code>, the order
 * of input statements is not preserved. The output document is a merge of the
 * input documents, and does not preserve any statement ordering from the input
 * serialisations. Also, duplicate triples will be suppressed.</p>
 */
@Deprecated
public class rdfcat
{
    static { setCmdLogging("jena-log4j.properties") ; }
    
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

    // Allow testing to run silent.
    public static boolean suppressDeprecationBanner = false ;
    
    // Constants
    //////////////////////////////////

    /** Argument setting expected input language to N3 */
    public final ArgDecl IN_N3 = new ArgDecl( true, "n", "n3", "ttl", "N3",
    		(arg,val) -> m_actionQ.add( new ReadAction( val, "N3") ) );

    /** Argument setting expected input language to RDF/XML */
    public final ArgDecl IN_RDF_XML = new ArgDecl( true, "x", "xml", "rdfxml", "rdf",
    		(arg,val) -> m_actionQ.add( new ReadAction( val, "RDF/XML") ) );

    /** Argument setting expected input language to NTRIPLE */
    public final ArgDecl IN_NTRIPLE = new ArgDecl( true, "t", "ntriples", "ntriple", "n-triple", "n-triples",
    		(arg,val) -> m_actionQ.add( new ReadAction( val, "N-TRIPLE" ) ) );    

    /** Argument to set the output language */
    public final ArgDecl OUT_LANG = new ArgDecl( true, "out", (arg,val) -> setOutput( val ) );

    /** Argument to set the default input language */
    public final ArgDecl IN_LANG = new ArgDecl( true, "in", (arg,val) -> expectInput( val ) );

    /** Argument to turn include processing on */
    public final ArgDecl INCLUDE = new ArgDecl( false, "include", (arg,val) -> setInclude( true ) );

    /** Argument to turn include processing off */
    public final ArgDecl NOINCLUDE = new ArgDecl( false, "noinclude", (arg,val) -> setInclude( false ) );

    /** Argument to leave import/seeAlso statements in place in flattened models */
    public final ArgDecl NOFILTER = new ArgDecl( false, "nofilter", (arg,val) -> setRemoveIncludeStatements( false ) );

    /** Argument to show usage */
    public final ArgDecl HELP = new ArgDecl( false, "help", (arg,val) -> usage() );
    public final ArgDecl USAGE = new ArgDecl( false, "usage", (arg,val) -> usage() );

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
    	JenaSystem.init();
        if ( ! suppressDeprecationBanner ) {
            System.err.println("------------------------------------------------------------------");
    		System.err.println("   DEPRECATED: Please use 'riot' instead.");
    		System.err.println("     http://jena.apache.org/documentation/io/#command-line-tools");
            System.err.println("------------------------------------------------------------------");
    		System.err.println() ;
        }
    	
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
    		System.err.println( "------------------------------------" );
    		System.err.println( "DEPRECATED: Please use riot instead." );
    		System.err.println( "------------------------------------\n" );
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
    
    /**
     * Command line argument processing based on a trigger model.
     * An action is called whenever an argument is encountered. Example:
     * <CODE>
     * public static void main (String[] args)
     * {
     *  CommandLine cl = new CommandLine() ;
     *  cl.add(false, "verbose")
     *    .add(true, "--file") ;
     *  cl.process(args) ;
     *
     *  for ( Iterator iter = cl.args() ; iter.hasNext() ; )
     *  ...
     * }
     * </CODE>
     * A gloabl hook is provided to inspect arguments just before the
     * action.  Tracing is enabled by setting this to a suitable function
     * such as that provided by trace():
     * <CODE>
     *  cl.setHook(cl.trace()) ;
     * </CODE>
     *
     * <ul>
     * <li>Neutral as to whether options have - or --</li>
     * <li>Does not allow multiple single letter options to be concatenated.</li>
     * <li>Options may be ended with - or --</li>
     * <li>Arguments with values can use "="</li>
     * </ul>
     */


    static class CommandLine
    {
        /* Extra processor called before the registered one when set.
         * Used for tracing.
         */
        protected BiConsumer<String,String> argHook = null ;
        protected String usage = null ;
        protected Map<String, ArgDecl> argMap = new HashMap<>() ;
        protected Map<String, Arg> args = new HashMap<>() ;
        //protected boolean ignoreUnknown = false ;

        // Rest of the items found on the command line
        String indirectionMarker = "@" ;
        protected boolean allowItemIndirect = false ;   // Allow @ to mean contents of file
        boolean ignoreIndirectionMarker = false ;       // Allow comand line items to have leading @ but strip it.
        protected List<String> items = new ArrayList<>() ;


        /** Creates new CommandLine */
        public CommandLine()
        {
        }

        /** Set the global argument handler.  Called on every valid argument.
         * @param argHandler Handler
         */
        public void setHook(BiConsumer<String, String> argHandler) { argHook = argHandler ; }

        public void setUsage(String usageMessage) { usage = usageMessage ; }

        public boolean hasArgs() { return args.size() > 0 ; }
        public boolean hasItems() { return items.size() > 0 ; }

        public Iterator<Arg> args() { return args.values().iterator() ; }
//        public Map args() { return args ; }
//        public List items() { return items ; }

        public int numArgs() { return args.size() ; }
        public int numItems() { return items.size() ; }
        public void pushItem(String s) { items.add(s) ; }

        public boolean isIndirectItem(int i)
        { return allowItemIndirect && items.get(i).startsWith(indirectionMarker) ; }

        public String getItem(int i)
        {
            return getItem(i, allowItemIndirect) ;
        }

        public String getItem(int i, boolean withIndirect)
        {
            if ( i < 0 || i >= items.size() )
                return null ;


            String item = items.get(i) ;

            if ( withIndirect && item.startsWith(indirectionMarker) )
            {
                item = item.substring(1) ;
                try { item = FileUtils.readWholeFileAsUTF8(item) ; }
                catch (Exception ex)
                { throw new IllegalArgumentException("Failed to read '"+item+"': "+ex.getMessage()) ; }
            }
            return item ;
        }


        /** Process a set of command line arguments.
         * @param argv The words of the command line.
         * @throws IllegalArgumentException Throw when something is wrong (no value found, action fails).
         */
        public void process(String[] argv) throws java.lang.IllegalArgumentException
        {
            List<String> argList = new ArrayList<>() ;
            argList.addAll(Arrays.asList(argv)) ;

            int i = 0 ;
            for ( ; i < argList.size() ; i++ )
            {
                String argStr = argList.get(i) ;
                if (endProcessing(argStr))
                    break ;
                
                if ( ignoreArgument(argStr) )
                    continue ;

                // If the flag has a "=" or :, it is long form --arg=value.
                // Split and insert the arg
                int j1 = argStr.indexOf('=') ;
                int j2 = argStr.indexOf(':') ;
                int j = Integer.MAX_VALUE ;

                if ( j1 > 0 && j1 < j )
                    j = j1 ;
                if ( j2 > 0 && j2 < j )
                    j = j2 ;

                if ( j != Integer.MAX_VALUE )
                {
                    String a2 = argStr.substring(j+1) ;
                    argList.add(i+1,a2) ;
                    argStr = argStr.substring(0,j) ;
                }

                argStr = ArgDecl.canonicalForm(argStr) ;
                String val = null ;

                if ( argMap.containsKey(argStr) )
                {
                    if ( ! args.containsKey(argStr))
                        args.put(argStr, new Arg(argStr)) ;

                    Arg arg = args.get(argStr) ;
                    ArgDecl argDecl = argMap.get(argStr) ;

                    if ( argDecl.takesValue() )
                    {
                        if ( i == (argList.size()-1) )
                            throw new IllegalArgumentException("No value for argument: "+arg.getName()) ;
                        i++ ;
                        val = argList.get(i) ;
                        arg.setValue(val) ;
                        arg.addValue(val) ;
                    }

                    // Global hook
                    if ( argHook != null )
                        argHook.accept(argStr, val) ;

                    argDecl.trigger(arg) ;
                }
                else
                    handleUnrecognizedArg( argList.get(i) );
//                    if ( ! getIgnoreUnknown() )
//                        // Not recognized
//                        throw new IllegalArgumentException("Unknown argument: "+argStr) ;
            }

            // Remainder.
            if ( i < argList.size() )
            {
                if ( argList.get(i).equals("-") || argList.get(i).equals("--") )
                    i++ ;
                for ( ; i < argList.size() ; i++ )
                {
                    String item = argList.get(i) ;
                    items.add(item) ;
                }
            }
        }

        /** Hook to test whether this argument should be processed further
         */
        public boolean ignoreArgument( String argStr )
        { return false ; }
        
        /** Answer true if this argument terminates argument processing for the rest
         * of the command line. Default is to stop just before the first arg that
         * does not start with "-", or is "-" or "--".
         */
        public boolean endProcessing( String argStr )
        {
            return ! argStr.startsWith("-") || argStr.equals("--") || argStr.equals("-");
        }

        /**
         * Handle an unrecognised argument; default is to throw an exception
         * @param argStr The string image of the unrecognised argument
         */
        public void handleUnrecognizedArg( String argStr ) {
            throw new IllegalArgumentException("Unknown argument: "+argStr) ;
        }


        /** Test whether an argument was seen.
         */

        public boolean contains(ArgDecl argDecl) { return getArg(argDecl) != null ; }

        /** Test whether an argument was seen.
         */

        public boolean contains(String s) { return getArg(s) != null ; }


        /** Test whether the command line had a particular argument
         *
         * @param argName
         */
        public boolean hasArg(String argName) { return getArg(argName) != null ; }

        /** Test whether the command line had a particular argument
         *
         * @param argDecl
         */

        public boolean hasArg(ArgDecl argDecl) { return getArg(argDecl) != null ; }


        /** Get the argument associated with the argument declaration.
         *  Actually returns the LAST one seen
         *  @param argDecl Argument declaration to find
         *  @return Last argument that matched.
         */

        public Arg getArg(ArgDecl argDecl)
        {
            Arg arg = null ;
            for ( Arg a : args.values() )
            {
                if ( argDecl.matches( a ) )
                {
                    arg = a;
                }
            }
            return arg ;
        }

        /** Get the argument associated with the arguement name.
         *  Actually returns the LAST one seen
         *  @param arg Argument declaration to find
         *  @return Arg - Last argument that matched.
         */

        public Arg getArg(String arg)
        {
            arg = ArgDecl.canonicalForm(arg) ;
            return args.get(arg) ;
        }

        /**
         * Returns the value (a string) for an argument with a value -
         * returns null for no argument and no value.
         * @param argDecl
         * @return String
         */
        public String getValue(ArgDecl argDecl)
        {
            Arg arg = getArg(argDecl) ;
            if ( arg == null )
                return null ;
            if ( arg.hasValue())
                return arg.getValue() ;
            return null ;
        }

        /**
         * Returns the value (a string) for an argument with a value -
         * returns null for no argument and no value.
         * @param argName
         * @return String
         */
        public String getValue(String argName)
        {
            Arg arg = getArg(argName) ;
            if ( arg == null )
                return null ;
            return arg.getValue() ;
        }

        /**
         * Returns all the values (0 or more strings) for an argument.
         * @param argDecl
         * @return List
         */
        public List<String> getValues(ArgDecl argDecl)
        {
            Arg arg = getArg(argDecl) ;
            if ( arg == null )
                return null ;
            return arg.getValues() ;
        }

        /**
         * Returns all the values (0 or more strings) for an argument.
         * @param argName
         * @return List
         */
        public List<String> getValues(String argName)
        {
            Arg arg = getArg(argName) ;
            if ( arg == null )
                return null ;
            return arg.getValues() ;
        }



        /** Add an argument to those to be accepted on the command line.
         * @param argName Name
         * @param hasValue True if the command takes a (string) value
         * @return The CommandLine processor object
         */

        public CommandLine add(String argName, boolean hasValue)
        {
            return add(new ArgDecl(hasValue, argName)) ;
        }

        /** Add an argument to those to be accepted on the command line.
         *  Argument order reflects ArgDecl.
         * @param hasValue True if the command takes a (string) value
         * @param argName Name
         * @return The CommandLine processor object
         */

        public CommandLine add(boolean hasValue, String argName)
        {
            return add(new ArgDecl(hasValue, argName)) ;
        }

        /** Add an argument object
         * @param arg Argument to add
         * @return The CommandLine processor object
         */

        public CommandLine add(ArgDecl arg)
        {
            for ( Iterator<String> iter = arg.names() ; iter.hasNext() ; )
                argMap.put(iter.next(), arg) ;
            return this ;
        }

//        public boolean getIgnoreUnknown() { return ignoreUnknown ; }
//        public void setIgnoreUnknown(boolean ign) { ignoreUnknown = ign ; }

        /**
         * @return Returns whether items starting "@" have the value of named file.
         */
        public boolean allowItemIndirect()
        {
            return allowItemIndirect ;
        }

        /**
         * @param allowItemIndirect Set whether items starting "@" have the value of named file.

         */
        public void setAllowItemIndirect(boolean allowItemIndirect)
        {
            this.allowItemIndirect = allowItemIndirect ;
        }

        /**
         * @return Returns the ignoreIndirectionMarker.
         */
        public boolean isIgnoreIndirectionMarker()
        {
            return ignoreIndirectionMarker ;
        }

        /**
         * @return Returns the indirectionMarker.
         */
        public String getIndirectionMarker()
        {
            return indirectionMarker ;
        }

        /**
         * @param indirectionMarker The indirectionMarker to set.
         */
        public void setIndirectionMarker(String indirectionMarker)
        {
            this.indirectionMarker = indirectionMarker ;
        }

        /**
         * @param ignoreIndirectionMarker The ignoreIndirectionMarker to set.
         */
        public void setIgnoreIndirectionMarker(boolean ignoreIndirectionMarker)
        {
            this.ignoreIndirectionMarker = ignoreIndirectionMarker ;
        }

	    	public BiConsumer<String, String> trace() {
	    		return (arg, val) -> {
	    			System.err.println("Seen: " + arg + (val != null ? " = " + val : ""));
	    		};
	    	}

    }
    
    /** A command line argument that has been foundspecification.
	 */
	static class Arg
	{
	    String name ;
	    String value ;
	    List<String> values = new ArrayList<>() ;
	    
	    Arg() { name = null ; value = null ; }
	    
	    Arg(String _name) { this() ; setName(_name) ; }
	    
	    Arg(String _name, String _value) { this() ; setName(_name) ; setValue(_value) ; }
	    
	    void setName(String n) { name = n ; }
	    
	    void setValue(String v) { value = v ; }
	    void addValue(String v) { values.add(v) ; }
	    
	    public String getName() { return name ; }
	    public String getValue() { return value; }
	    public List<String> getValues() { return values; }
	    
	    public boolean hasValue() { return value != null ; }
	    
	    public boolean matches(ArgDecl decl)
	    {
	        return decl.getNames().contains(name) ;
	    }
	    
	}
    
    /** A command line argument specification.
	 */
	static class ArgDecl
	{
	    boolean takesValue ;
	    Set<String> names = new HashSet<>() ;
	    boolean takesArg = false ;
		List<BiConsumer<String, String>> argHooks = new ArrayList<>() ;
	    public static final boolean HasValue = true ;
	    public static final boolean NoValue = false ;

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     */

	    public ArgDecl(boolean hasValue)
	    {
	        takesValue = hasValue ;
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name      Name of argument
	     */

	    public ArgDecl(boolean hasValue, String name)
	    {
	        this(hasValue) ;
	        addName(name) ;
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name      Name of argument
	     * @param handler   BiConsumer<String, String>
	     */

	    public ArgDecl(boolean hasValue, String name, BiConsumer<String, String> handler)
	    {
	        this(hasValue) ;
	        addName(name) ;
	        addHook( handler );
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name1      Name of argument
	     * @param name2      Name of argument
	     */

	    public ArgDecl(boolean hasValue, String name1, String name2)
	    {
	        this(hasValue) ;
	        addName(name1) ;
	        addName(name2) ;
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name1      Name of argument
	     * @param name2      Name of argument
	     * @param handler   BiConsumer<String, String>
	     */

	    public ArgDecl(boolean hasValue, String name1, String name2, BiConsumer<String, String> handler)
	    {
	        this(hasValue) ;
	        addName(name1) ;
	        addName(name2) ;
	        addHook( handler );
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name1      Name of argument
	     * @param name2      Name of argument
	     * @param name3      Name of argument
	     */

	    public ArgDecl(boolean hasValue, String name1, String name2, String name3)
	    {
	        this(hasValue) ;
	        addName(name1) ;
	        addName(name2) ;
	        addName(name3) ;
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name1      Name of argument
	     * @param name2      Name of argument
	     * @param name3      Name of argument
	     * @param handler   BiConsumer<String, String>
	     */

	    public ArgDecl(boolean hasValue, String name1, String name2, String name3, BiConsumer<String, String> handler)
	    {
	        this(hasValue) ;
	        addName(name1) ;
	        addName(name2) ;
	        addName(name3) ;
	        addHook( handler );
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name1      Name of argument
	     * @param name2      Name of argument
	     * @param name3      Name of argument
	     * @param name4      Name of argument
	     */

	    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4)
	    {
	        this(hasValue) ;
	        addName(name1) ;
	        addName(name2) ;
	        addName(name3) ;
	        addName(name4) ;
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name1      Name of argument
	     * @param name2      Name of argument
	     * @param name3      Name of argument
	     * @param name4      Name of argument
	     * @param handler    BiConsumer<String, String>
	     */

	    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4, BiConsumer<String, String> handler)
	    {
	        this(hasValue) ;
	        addName(name1) ;
	        addName(name2) ;
	        addName(name3) ;
	        addName(name4) ;
	        addHook( handler );
	    }

	    /** Create a declaration for a command argument.
	     *
	     * @param hasValue  Does it take a value or not?
	     * @param name1      Name of argument
	     * @param name2      Name of argument
	     * @param name3      Name of argument
	     * @param name4      Name of argument
	     * @param name5      Name of argument
	     * @param handler    BiConsumer<String, String>
	     */

	    public ArgDecl(boolean hasValue, String name1, String name2, String name3, String name4, String name5, BiConsumer<String, String> handler)
	    {
	        this(hasValue) ;
	        addName(name1) ;
	        addName(name2) ;
	        addName(name3) ;
	        addName(name4) ;
	        addName(name5) ;
	        addHook( handler );
	    }

	    public void addName(String name)
	    {
	        name = canonicalForm(name) ;
	        names.add(name) ;
	    }

	    public Set<String> getNames() { return names ; }
	    public Iterator<String> names() { return names.iterator() ; }

	    // Callback model

	    public void addHook(BiConsumer<String, String> argHandler)
	    {
	        argHooks.add(argHandler) ;
	    }

	    protected void trigger(Arg arg)
	    {
			argHooks.forEach(action -> action.accept( arg.getName(), arg.getValue() ));
	    }

	    public boolean takesValue() { return takesValue ; }

	    public boolean matches(Arg a)
	    {
	        for ( String n : names )
	        {
	            if ( a.getName().equals( n ) )
	            {
	                return true;
	            }
	        }
	        return false ;
	    }

	    public boolean matches(String arg)
	    {
	        arg = canonicalForm(arg) ;
	        return names.contains(arg) ;
	    }

	    static String canonicalForm(String str)
	    {
	        if ( str.startsWith("--") )
	            return str.substring(2) ;

	        if ( str.startsWith("-") )
	            return str.substring(1) ;

	        return str ;
	    }
	}
}
