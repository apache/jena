/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */


package jena;

import junit.framework.* ;
import java.io.* ;

import com.hp.hpl.jena.util.* ;
import com.hp.hpl.jena.rdql.* ;
import com.hp.hpl.jena.rdql.test.* ;
import com.hp.hpl.jena.rdf.model.* ;
import com.hp.hpl.jena.vocabulary.ResultSet ;

//import com.hp.hpl.jena.mem.ModelMem ;
//import com.hp.hpl.jena.reasoner.* ;

/** A program to execute queries from the command line.
 *
 *  Queries can specify the source file so this can be used as a simple
 *  script engine for RDQL.
 *
 *  <pre>
 *  Usage: [--xml|--ntriple] [--data URL] [queryString | --query file]") ;
 *     --query file         Read one query from a file
 *     --rdfs               Use an RDFS reasoner around the data
 *     --reasoner URI       Set the reasoner URI explicitly.   
 *     --vocab URL | File   Specify a separate vocabulary (may also be in the data)
 *     --xml                Data source is XML (default)
 *     --ntriple            Data source is n-triple
 *     --data URL | File    Data source (can also be part of query)
 *     --time               Print some time information
 *     --test [file]        Run the test suite
 *     --format FMT         One of text, html, tuples, dump or none
 *     --verbose            Verbose - more messages
 *     --quiet              Quiet - less messages
 * Options for data and format override the query specified source.
 * </pre>
 *
 * @author  Andy Seaborne
 * @version $Id: rdfquery.java,v 1.7 2003-03-19 17:21:29 andy_seaborne Exp $
 */

// To do: formalise the use of variables and separate out the command line processor
// so the "query" routine can be used for testing from other main's.

public class rdfquery
{
    static public boolean displayTime = false ;
    static public int messageLevel = 0 ;
    static public boolean debug   = false ;
    static public boolean dumpModel = false ;

    static final int FMT_NONE    = -1 ;
    static final int FMT_TUPLES  = 0 ;
    static final int FMT_TEXT    = 1 ;
    static final int FMT_HTML    = 2 ;
    static final int FMT_DUMP    = 3 ;

    static public int outputFormat = FMT_TEXT ;
    static String dbUser = "" ;
    static String dbPassword = "" ;
    
    //static final String defaultReasonerURI =  "http://www.hpl.hp.com/semweb/2003/RDFSReasoner1" ;
    //static String reasonerURI = defaultReasonerURI ;
    static String vocabularyURI = null ;
    static Model vocabulary = null ;
    
    static boolean applyRDFS = false ;

    public static void main (String [] argv)
    {
        //Log.getInstance().setConsoleHandler();
        Log.getInstance().setHandler(new PlainLogHandler());

        if ( argv.length == 0 )
        {
            usage() ;
            System.exit(0) ;
        }

        String dataURL = null ;
        String language = null ;
        String queryFile = null ;
        displayTime = false ;

        // Flag processing.
        // --X and -X are equivalent.
        // Flags:
        // --ntriple URL    Get/read in a model in n-triple syntax
        // --xml URL        Get/read in a model in XML syntax

        int argi = 0 ;
        for ( ; argi < argv.length ; argi++ )
        {
            String arg = argv[argi] ;

            if ( ! arg.startsWith("-") )
                break ;

            // Canonical form: --LONG
            if ( !arg.startsWith("--") )
                arg = "-"+arg ;

            if ( arg.equalsIgnoreCase("--test") )
            {
                argi++ ;
                if ( argi == argv.length )
                {
                    // No control file.
                    allTests() ;
                    System.exit(0) ;
                }
                else
                {
                    QueryTestScripts.doTests(argv[argi], (messageLevel>0), displayTime) ;
                    System.exit(0) ;
                }

                continue ;
            }

            if ( arg.equalsIgnoreCase("--help") || arg.equalsIgnoreCase("--h") )
            {
                usage() ;
                System.exit(0) ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--debug") )
            {
                debug = true ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--quiet") || arg.equalsIgnoreCase("--q") )
            {
                messageLevel -- ;
                continue ;
            }
            
            if ( arg.equalsIgnoreCase("--verbose") || arg.equalsIgnoreCase("--v") )
            {
                messageLevel ++ ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--format") )
            {
                argi ++ ;
                if ( argi == argv.length )
                {
                    System.err.println("Error: no output format given" );
                    System.exit(1) ;
                }

                arg = argv[argi] ;
                if ( arg.equalsIgnoreCase("none") )
                    outputFormat = FMT_NONE ;
                else if ( arg.equalsIgnoreCase("tuples") )
                    outputFormat = FMT_TUPLES ;
                else if (arg.equalsIgnoreCase("tuple") )
                    outputFormat = FMT_TUPLES ;
                else if (arg.equalsIgnoreCase("text") )
                    outputFormat = FMT_TEXT ;
                else if (arg.equalsIgnoreCase("html") )
                    outputFormat = FMT_HTML ;
                else if (arg.equalsIgnoreCase("dump") )
                    outputFormat = FMT_DUMP ;
                else
                {
                    System.err.println("Unrecognized output format: "+arg) ;
                    System.exit(1) ;
                }
                continue ;
            }

            if ( arg.equalsIgnoreCase("--vocabulary") ||
                 arg.equalsIgnoreCase("--vocab") )
            {
                argi++ ;
                if ( argi == argv.length )
                {
                    System.err.println("Error: no vocabulary specified");
                    System.exit(1) ;
                }
                vocabularyURI = argv[argi] ;
                continue ; 
            }

            
            if ( arg.equalsIgnoreCase("--rdfs"))
            {
                applyRDFS = true ;
                continue ;
            }
            
            if ( arg.equalsIgnoreCase("--time") )
            {
                displayTime = true ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--noarp") )
            {
                ModelLoader.useARP = false ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--xml" ) )
            {
                language = ModelLoader.langXML ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--ntriple" ) )
            {
                language = ModelLoader.langNTriple ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--n3" ) )
            {
                language = ModelLoader.langN3 ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--bdb" ) )
            {
                language = ModelLoader.langBDB ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--user" ) )
            {
                argi++ ;
                if ( argi == argv.length )
                {
                    System.err.println("Error: no user name  specified");
                    System.exit(1) ;
                }
                dbUser = argv[argi] ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--password" ) )
            {
                argi++ ;
                if ( argi == argv.length )
                {
                    System.err.println("Error: no password  specified");
                    System.exit(1) ;
                }
                dbPassword = argv[argi] ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--data") )
            {
                if ( dataURL != null )
                {
                    System.err.println("Error: source already specified");
                    System.exit(1) ;
                }

                argi++ ;
                if ( argi == argv.length )
                {
                    System.err.println("Error: no data file specified");
                    System.exit(1) ;
                }
                dataURL = argv[argi] ;
                continue ;
            }

            if ( arg.equalsIgnoreCase("--query") )
            {
                argi++ ;
                if ( argi == argv.length )
                {
                    System.err.println("Error: no query file specified");
                    System.exit(1) ;
                }

                queryFile = argv[argi] ;
                // read whole file later.
                continue ;
            }

            // Unrecognized argument
            System.err.println("Unrecognized option: "+arg) ;
            usage() ;
            System.exit(1);
        }

        if ( messageLevel >= 3 )
            dumpModel = true ;
        
        if ( debug )
        {
            Log.getInstance().setLevel(Log.DEBUG);
            Log.debug("Debug on");
        }

        String queryString = null ;

        if ( queryFile != null )
        {
            try {
                queryString = FileUtils.readWholeFile(queryFile) ;
            } catch (Exception e)
            {
                System.err.println("Error: failed to read file: "+e) ;
                System.exit(1) ;
            }
        }
        else
        {
            if ( argi >= argv.length )
            {
                System.err.println("Error: No query supplied") ;
                System.exit(1) ;
            }
            queryString = argv[argi] ;
        }

        query(queryString, dataURL, language) ;

    }

    static void allTests()
    {
        // This should load all the built in tests.
        // It does not load the external test scripts.
        
        TestSuite ts = new TestSuite("RDQL") ;
        ts.addTest(TestExpressions.suite()) ;
        //ts.addTest(QueryTestScripts.suite()) ;
        ts.addTest(QueryTestProgrammatic.suite()) ;
        junit.textui.TestRunner.run(ts) ;
    }


    // Execute one query, with stats etc., print results
    static public void query(String s, String dataURL, String language)
    {
        try {
        boolean doBlank = false ;

        if ( messageLevel >= 2 )
        {
            System.out.println("Query:") ;
            System.out.println(s) ;
            if ( ! s.endsWith("\n") )
                System.out.println() ;
            doBlank = true ;
        }

        long startTime = System.currentTimeMillis();
        long loadTime = -1 ;

        Query query = new Query(s) ;
        if ( displayTime )
            // Do again after classloading has occured.
            query = new Query(s) ;

        if ( messageLevel > 0 )
        {
            System.out.println("Parsed query:") ;
            String tmp = query.toString() ;
            System.out.print(tmp) ;
            if ( ! tmp.endsWith("\n") )
                System.out.println() ;
            doBlank = true ;
        }

        if ( dataURL == null && query.getSourceURL() == null )
        {
            System.err.println("RDQL: no data source");
            return ;
        }

        if ( dataURL != null )
        {
            long startLoadTime = System.currentTimeMillis();
            query.setSource(ModelLoader.loadModel(dataURL, language, dbUser, dbPassword)) ;
            Model m = query.getSource() ;
            // ------------
            
            if ( applyRDFS )
            {
                Model model = null ;
                if ( vocabularyURI != null )
                {
                    vocabulary = ModelLoader.loadModel(vocabularyURI, null) ;
                    model = ModelFactory.createRDFSModel(m, vocabulary) ;
                }
                else
                {
                    model = ModelFactory.createRDFSModel(m) ;
                }
                query.setSource(model) ;
            }
            loadTime = System.currentTimeMillis() - startLoadTime ;
            query.loadTime = loadTime ;
        }

        QueryExecution qe = new QueryEngine(query) ;
        qe.init() ;
        if ( dumpModel )
        {
            try {
                if ( doBlank )
                    System.out.println() ;
                doBlank = true ;
                Model model = query.getSource() ;
                RDFWriter w = model.getWriter("N-TRIPLE") ;
                PrintWriter pw = new PrintWriter(System.out) ;
                pw.println("# Model --------------------------------------------------------------------------------") ;
                w.write(model, pw, "http://unset/") ;
                pw.println("# Model --------------------------------------------------------------------------------") ;
                pw.flush() ;
            } catch (RDFException refEx) { Log.severe("rdfquery: Failed to write model") ; System.exit(1) ; }
        }
        QueryResults results = qe.exec() ;
        QueryResultsFormatter fmt = new QueryResultsFormatter(results) ;

        if ( outputFormat == FMT_NONE)
            fmt.consume() ;
        else
        {
            if ( doBlank ) System.out.println() ;
            
            if ( outputFormat == FMT_DUMP )
            {
                Model m = fmt.toModel() ;
                RDFWriter rdfw = m.getWriter("N3") ; 
                rdfw.setNsPrefix("rs", ResultSet.getURI()) ;
                rdfw.write(m, System.out, null) ; 
            }
            else
            {
            
                PrintWriter pw = new PrintWriter(System.out) ;
                switch(outputFormat)
                {
                    case FMT_TEXT:   fmt.printAll(pw) ; break ;
                    case FMT_HTML:   fmt.printHTML(pw) ; break ;
                    case FMT_TUPLES: fmt.dump(pw, true) ; break ;
                    default: break ; 
                }
                pw.flush() ;
            }
            doBlank = true ;
        }

        fmt.close() ;
        results.close() ;

        long finishTime = System.currentTimeMillis();
        long totalTime = finishTime-startTime ;

        if ( messageLevel > 0 )
        {
            if ( doBlank ) System.out.println() ;
            System.out.println("Results: "+fmt.numRows()) ;
            doBlank = true ;
        }

        if ( displayTime )
        {
            if ( doBlank ) System.out.println() ;
            System.out.println("Query parse:     "+formatlong(query.parseTime)   +" ms") ;
            System.out.println("Query build:     "+formatlong(query.buildTime)   +" ms") ;
            System.out.println("Data load time:  "+formatlong(query.loadTime)    +" ms") ;
            System.out.println("Query execute:   "+formatlong(query.executeTime) +" ms") ;
            System.out.println("Query misc:      "+formatlong(totalTime-query.parseTime-query.buildTime-query.loadTime-query.executeTime)+" ms") ;
            System.out.println("Query total:     "+formatlong(totalTime)         +" ms") ;
            doBlank = true ;
        }
        
        if ( query.getSource() != null )
            query.getSource().close() ;
        /*
        PrintWriter pw = new PrintWriter(System.out) ;
        resultsIter.print(pw) ;
        pw.flush() ;
        */
        } catch (QueryException qEx)
        {
            System.err.println(qEx.getMessage()) ;
            System.exit(9) ;
        }
    }

    static String formatlong(long x)
    {
        StringBuffer sbuff = new StringBuffer() ;
        sbuff.append(Long.toString(x)) ;
        for ( int i = sbuff.length() ; i < 4 ; i++ ) sbuff.append(" ") ;
        return sbuff.toString() ;
    }

    static void usage()
    {
        System.out.println("Usage: [--rdfs] [--data URL] [queryString | --query file]") ;
        System.out.println("   --query file         Read one query from a file") ;
        System.out.println("   --rdfs               Use an RDFS reasoner around the data") ;
        //System.out.println("   --reasoner URI       Set the reasoner URI explicitly.") ;   
        System.out.println("   --vocab URL | File   Specify a separate vocabulary (may also be in the data)") ;
        System.out.println("   --xml                Data source is XML (default)") ;
        System.out.println("   --ntriple            Data source is n-triple") ;
        System.out.println("   --n3                 Data source is N3") ;
        System.out.println("   --data URL           Data source (can also be part of query)") ;
        System.out.println("   --time               Print some time information") ;
        System.out.println("   --test [file]        Run the test suite") ;
        System.out.println("   --format FMT         One of text, html, tuples, dump or none") ;
        System.out.println("   --verbose            Verbose - more messages") ;
        System.out.println("   --quiet              Quiet - less messages") ;
    }
 }

/*
 *  (c) Copyright Hewlett-Packard Company 2001
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
