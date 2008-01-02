/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package jena;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import arq.cmd.CmdException;
import arq.cmd.TerminationException;
import jena.cmdline.ArgDecl;
import jena.util.* ;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab;
import com.hp.hpl.jena.util.FileManager;

public class dbquery extends DBcmd
{

    // TODO Rewrite to use assemblers (and merge with arq.query?)
    // Make DBcmd public -- actually, make an extension of CmdLineArgs that adds and handles the extra stuff.
    // Execute an ARQ query against an old-style Jena database
    static private Log log = LogFactory.getLog( dbquery.class );

    public static final String[] usage =
        new String[] {
        "dbquery [--spec spec] | [db_description] [--model name] --query QueryFile" ,
        "  where db_description is" ,
        "    --db JDBC URL --dbType type" ,
        "    --dbUser user --dbPassword password" 
    } ;


    public static void main (String [] argv)
    {
        try {
            main2(argv) ;
        }
        catch (TerminationException ex) { System.exit(ex.getCode()) ; }
        catch (CmdException ex)
        {
            System.err.println(ex.getMessage()) ;
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
        }

    }

    public static void main2(String [] argv)
    {
        dbquery db = new dbquery();
        db.setUsage(usage) ;

        // add any new args
        db.init(argv);
        // do any additional test here

        // Action!
        db.exec();
    }

    private ArgDecl queryDecl = new ArgDecl(true, "query") ; 
    private ArgDecl queryTime = new ArgDecl(false, "time") ;
    private ArgDecl repeatDecl = new ArgDecl(true, "repeat") ;
    private boolean timing = false ;

    public dbquery()
    {
        super("dbquery", false);
        getCommandLine().add(queryDecl) ;
        getCommandLine().add(queryTime) ;
        getCommandLine().add(repeatDecl) ;
    }

    //@Override
    protected void exec0()
    {
        if ( ! getCommandLine().contains(queryDecl) )
        {
            System.err.println("No query") ;
            return ;
        }
        String queryFile = getCommandLine().getValue(queryDecl) ;
        String queryString = FileManager.get().readWholeFileAsUTF8(queryFile) ;
        exec1(queryString) ;
    }

    //@Override
    protected boolean exec1(String arg)
    {
        if ( arg.startsWith("@") )
            arg = FileManager.get().readWholeFileAsUTF8(arg.substring(1)) ;

        boolean timing = false ;
        if ( getCommandLine().contains(queryTime) )
            timing = true ;
//            timing = ( getCommandLine().getArg(queryTime).getValue().equalsIgnoreCase("true") ||
//                getCommandLine().getArg(queryTime).getValue().equalsIgnoreCase("on") ) ;

//      GraphRDB g = ((GraphRDB)getRDBModel().getGraph()) ;
//      System.err.println("Reif: "+g.reificationBehavior()) ;
//      System.err.println("OnlyReified:  "+getRDBModel().getQueryOnlyReified()) ;
//      System.err.println("FullReified:  "+getRDBModel().getQueryFullReified()) ;
//      System.err.println("QueryOnly:    "+getRDBModel().getQueryOnlyAsserted()) ;

//      super.getRDBModel().setDoFastpath(true) ;
//      super.getRDBModel().setQueryOnlyReified(false) ;
//      super.getRDBModel().setQueryFullReified(false) ;
//      super.getRDBModel().setQueryOnlyAsserted(true) ;

        //super.getRDBModel().setDoFastpath(true) ;
        //System.err.println("Fastpath: "+getRDBModel().getDoFastpath()) ;

        
        

        long totalTime = 0 ;
        long firstTime = 0 ;
        ResultsFormat fmt = ResultsFormat.FMT_TEXT ;
        int repeat = 1 ;
        
        if ( getCommandLine().contains(repeatDecl) )
            repeat = Integer.parseInt(getCommandLine().getValue(repeatDecl)) ;

        if ( timing )
            fmt =  ResultsFormat.FMT_NONE ;

        // Compile and execute once on empty model (get classes)
        if ( timing )
        {
            long startTime = System.currentTimeMillis() ;
            Query query = QueryFactory.create(arg) ;
            Model m = ModelFactory.createDefaultModel() ;
            QueryExecution qExec = QueryExecutionFactory.create(query, m) ;
            doQuery(query, qExec, ResultsFormat.FMT_NONE) ;        
            long finishTime = System.currentTimeMillis() ;
            firstTime = (finishTime-startTime) ;
        }

        //Query query = QueryFactory.create(arg) ;
        // Creates model - does JDBC. 
        long startTimeJDBC = System.currentTimeMillis() ;
        getRDBModel() ;
        long jdbcTime = System.currentTimeMillis()-startTimeJDBC ;

        for ( int i = 0 ; i < repeat ; i++ )
        {
            long startTime = System.currentTimeMillis() ;
            //getRDBModel().begin() ;
            // fastpath => one SQL query => autocommit is enough
            Query query = QueryFactory.create(arg) ;
            QueryExecution qExec = QueryExecutionFactory.create(query, super.getRDBModel()) ;
            doQuery(query, qExec, fmt) ;        
            qExec.close() ;
            //getRDBModel().commit() ;
            long finishTime = System.currentTimeMillis() ;
            long time = finishTime-startTime ;
            totalTime += time ;
        }

        if ( timing )
        {
            System.out.println("Query execution time:   "+(totalTime)/(repeat*1000.0)) ;
            System.out.println("Setup:                  "+(firstTime)/1000.0) ;
            System.out.println("JDBC setup:             "+(jdbcTime)/1000.0) ;
        }


        return false ;
    }

    public void doQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        if ( query.isSelectType() )
            doSelectQuery(query, qe, outputFormat) ;
        if ( query.isDescribeType() )
            doDescribeQuery(query, qe, outputFormat) ;
        if ( query.isConstructType() )
            doConstructQuery(query, qe, outputFormat) ;
        if ( query.isAskType() )
            doAskQuery(query, qe, outputFormat) ;
        qe.close() ;
    }

    void doSelectQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        ResultSet results = qe.execSelect() ;

        // Force query to execute - until now it has merely been set up.
        results = ResultSetFactory.makeRewindable(results) ;

        boolean done = false ;

        // The non-display forms / uses a ResultSetFormatter 

        if ( outputFormat.equals(ResultsFormat.FMT_NONE) ||
            outputFormat.equals(ResultsFormat.FMT_COUNT) )
        {
            int count = ResultSetFormatter.consume(results) ;
            if ( outputFormat.equals(ResultsFormat.FMT_COUNT) )
            {
                System.out.println("Count = "+count) ;
            }
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_RDF) ||
            outputFormat.equals(ResultsFormat.FMT_RDF_N3) )
        {
            Model m = ResultSetFormatter.toModel(results) ;
            m.setNsPrefixes(query.getPrefixMapping()) ;
            RDFWriter rdfw = m.getWriter("TURTLE") ;
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI()) ;
            rdfw.write(m, System.out, null) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_XML) )
        {
            ResultSetFormatter.outputAsXML(System.out, results) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT))
        {
            ResultSetFormatter.out(System.out, results, query.getPrefixMapping()) ;
            done = true ;
        }
        if ( ! done )
            log.warn("Unknown format request: "+outputFormat) ;

        System.out.flush() ;
    }

    void doDescribeQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        Model r = qe.execDescribe() ;
        writeModel(query, r, outputFormat) ;
    }


    void doConstructQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        Model r = qe.execConstruct() ;
        writeModel(query, r, outputFormat) ;
    }

    void writeModel(Query query, Model model, ResultsFormat outputFormat)
    {
        if ( outputFormat.equals(ResultsFormat.FMT_NONE) )
            return ;

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT))
        {
            String qType = "" ;
            if ( query.isDescribeType() ) qType = "DESCRIBE" ;
            if ( query.isConstructType() ) qType = "CONSTRUCT" ;

            System.out.println("# ======== "+qType+" results ") ;
            model.write(System.out, "N3", null) ; // Base is meaningless
            System.out.println("# ======== ") ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_XML) )
        {
            model.write(System.out, "RDF/XML-ABBREV", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) )
        {
            model.write(System.out, "N3", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_NT) )
        {
            model.write(System.out, "N_TRIPLES", null) ;
            return ;
        }

        System.err.println("Unknown format: "+outputFormat.getSymbol()) ;
    }

    void doAskQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        boolean b = qe.execAsk() ;

        if ( outputFormat.equals(ResultsFormat.FMT_RS_XML) )
        {
            ResultSetFormatter.outputAsXML(System.out, b) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) || 
            outputFormat.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            ResultSetFormatter.outputAsRDF(System.out, "TURTLE", b) ;
            System.out.flush() ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) )
        {
            //ResultSetFormatter.out(System.out, b) ;
            System.out.println("Ask => "+(b?"Yes":"No")) ;
            return ;
        }

        System.err.println("Unknown format: "+outputFormat.getSymbol()) ;
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
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