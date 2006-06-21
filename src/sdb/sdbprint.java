/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.io.IOException;

import arq.cmd.CmdException;
import arq.cmd.CmdUtils;
import arq.cmd.QCmd;
import arq.cmd.TerminateException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdLineArgs;

import com.hp.hpl.jena.Jena;
import com.hp.hpl.jena.db.impl.Driver_MySQL;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.PlanFormatter;
import com.hp.hpl.jena.query.engine1.PlanVisitorBase;
import com.hp.hpl.jena.query.engine1.PlanWalker;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternal;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.engine.PlanSDB;
import com.hp.hpl.jena.sdb.engine.PlanTranslatorGeneral;
import com.hp.hpl.jena.sdb.engine.QueryCompilerBase;
import com.hp.hpl.jena.sdb.engine.QueryEngineSDB;
import com.hp.hpl.jena.sdb.layout1.CodecRDB;
import com.hp.hpl.jena.sdb.layout1.QueryCompiler1;
import com.hp.hpl.jena.sdb.layout1.QueryCompilerSimple;
import com.hp.hpl.jena.sdb.layout2.QueryCompiler2;
import com.hp.hpl.jena.sdb.store.QueryCompiler;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreBase;
import com.hp.hpl.jena.util.FileUtils;

/**
 * Compile and print the SQL for a SPARQL query.
 * @author Andy Seaborne
 * @version $Id: sdbprint.java,v 1.12 2006/04/24 17:31:26 andy_seaborne Exp $
 */

public class sdbprint // NOT CmdArgsDB
{
    static { CmdUtils.setLog4j() ; }

    
    // This command knows how to create queries without needing a store or connection.
    
    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;

    static boolean verbose = false ;

    // Rough!
    public static void main (String [] argv)
    {
        // TODO To be integrated ...
        try { main2(argv) ; }
        catch (CmdException ex)
        {
            System.err.println(ex.getMessage()) ;
            if ( ex.getCause() != null )
                ex.getCause().printStackTrace(System.err) ;
        }
        catch (TerminateException ex) { System.exit(ex.getCode()) ; }
    }

    public static void main2(String[] args)
    {
        CmdLineArgs cl = new CmdLineArgs(args) ;
        
        ArgDecl helpDecl = new ArgDecl(ArgDecl.NoValue, "h", "help") ;
        cl.add(helpDecl) ;
        
        ArgDecl verboseDecl = new ArgDecl(ArgDecl.NoValue, "v", "verbose") ;
        cl.add(verboseDecl) ;
        
        ArgDecl versionDecl = new ArgDecl(ArgDecl.NoValue, "ver", "version", "V") ;
        cl.add(versionDecl) ;
        
        ArgDecl quietDecl = new ArgDecl(ArgDecl.NoValue, "q", "quiet") ;
        cl.add(quietDecl) ;

        ArgDecl noExecDecl = new ArgDecl(ArgDecl.NoValue, "n", "noExec", "noexec") ;
        cl.add(noExecDecl) ;
        
        ArgDecl layoutDecl = new ArgDecl(ArgDecl.HasValue, "layout") ;
        cl.add(layoutDecl) ;

        ArgDecl queryDecl = new ArgDecl(ArgDecl.HasValue, "query", "file") ;
        cl.add(queryDecl) ;

        //ArgDecl querySyntaxDecl = new ArgDecl(ArgDecl.HasValue, "syntax", "syn", "in") ;
        //cl.add(querySyntaxDecl) ;
        
        String layoutNameDefault = "layout2" ;
        String queryString = null ;
        
        try {
            cl.process() ;
        } catch (IllegalArgumentException ex)
        {
            System.err.println(ex.getMessage()) ;
            usage(System.err) ;
            throw new TerminateException(2) ;
        }        
        
        //---- Basic stuff
        if ( cl.contains(helpDecl) )
        {
            usage(System.out) ;
            throw new TerminateException(0) ;
        }
        
        if ( cl.contains(versionDecl) )
        {
            System.out.println("SDB Version: "+SDB.VERSION+"  ARQ Version: "+ARQ.VERSION+"  Jena: "+Jena.VERSION+"") ;
            throw new TerminateException(0) ;
        }
        
        // Now set all the arguments.
        QCmd qCmd = new QCmd() ;

        // ==== General things
        verbose = cl.contains(verboseDecl) ;
        if ( verbose ) qCmd.setMessageLevel(qCmd.getMessageLevel()+1) ;
        
        boolean quiet = cl.contains(quietDecl) ;
        if ( quiet ) qCmd.setMessageLevel(qCmd.getMessageLevel()-1) ;
        
        
//        if ( cl.contains(querySyntaxDecl) )
//        {
//            // short name
//            String s = cl.getValue(querySyntaxDecl) ;
//            Syntax syn = Syntax.lookup(s) ;
//            if ( syn == null )
//                argError("Unrecognized syntax: "+syn) ;
//        }

        //---- Query
        
        String queryFile = cl.getValue(queryDecl) ;
        
        if ( cl.getNumPositional() == 0 && queryFile == null )
            argError("No query string or query file") ;

        if ( cl.getNumPositional() > 1 )
            argError("Only one query string allowed") ;
        
        if ( cl.getNumPositional() == 1 && queryFile != null )
            argError("Either query string or query file - not both") ;

        try {
            if ( queryFile != null )
                queryString  = FileUtils.readWholeFileAsUTF8(queryFile) ;
            else
            {
                queryString = cl.getPositionalArg(0) ;
                queryString = cl.indirect(queryString) ;
            }
                
        } catch (IOException ex)
        {
            System.err.println("Failed to read: "+queryFile) ;
            System.err.println(ex.getMessage()) ;
        }
 
        String layoutName = cl.getValue(layoutDecl) ;
        if ( layoutName == null )
            layoutName = layoutNameDefault ;

        Query query = QueryFactory.create(queryString) ;
        
        if ( layoutName.equalsIgnoreCase("layout1") ) 
        {
            compilePrint(query, new QueryCompilerSimple()) ;
            throw new TerminateException(0) ;
        }
        
        if ( layoutName.equalsIgnoreCase("modelRDB") ) 
        {
            // Kludge something to work.
            IRDBDriver iDriver = new Driver_MySQL() ;
            compilePrint(query, new QueryCompiler1(new CodecRDB(iDriver))) ;
            throw new TerminateException(0) ;
        }
        
        if ( layoutName.equalsIgnoreCase("layout2") ) 
        {
            compilePrint(query, new QueryCompiler2()) ;
            throw new TerminateException(0) ;
        }
        
        argError("Unknown layout name: "+layoutName) ;
    }
    
    static void usage(java.io.PrintStream out)
    {
        out.println("Usage: [--layout schemaName] [--query URL | string ] ") ;
    }
    
    // Does not return
    static void argError(String s)
    {
        System.err.println("Argument Error: "+s) ;
        //usage(System.err) ;
        throw new TerminateException(3) ;
    }
    
    public static void compilePrint(String queryString, String layoutName)
    {
        System.err.println("BROKEN - FIX ME") ;
    }
    
    private static void compilePrint(Query query, QueryCompiler compiler)
    {
        if ( verbose )
        {
            System.out.println(divider) ;
//            query.serialize(System.out, Syntax.syntaxARQ) ;
//            System.out.println(divider) ;
            query.serialize(System.out, Syntax.syntaxPrefix) ;
            System.out.println(divider) ;
        }

        Store store = new StoreBase(null, new PlanTranslatorGeneral(true, true), null, null, compiler) ;
        QueryEngineSDB qe = new QueryEngineSDB(store, query) ;
        if ( verbose )
        {
            PlanElement plan = qe.getPlan() ;
            PlanFormatter.out(System.out, plan) ;
            System.out.println(divider) ;
        }

        // Print all SDB things in the plan
        PlanWalker.walk(qe.getPlan(), new Printer(store)) ;
        
    }


    static class Printer extends PlanVisitorBase
    {
        private Store store ;
        String separator = null ;

        Printer(Store store) { this.store = store ; }
        
        @Override
        public void visit(PlanElementExternal planElt)
        {
            if ( planElt instanceof PlanSDB )
            {
                if ( separator != null )
                    System.out.println(separator) ;
                separator = divider ;
                PlanSDB planSDB = (PlanSDB)planElt ;
                if ( verbose )
                {
                    QueryCompilerBase.printBlock = false ;  // Done earlier.
                    QueryCompilerBase.printAbstractSQL = false ;
                    QueryCompilerBase.printDivider = divider ;
                }
                
                Block block = planSDB.getBlock() ;
                String sqlStmt = store.getQueryCompiler().asSQL(block) ;
                System.out.println(sqlStmt) ;
            }
        }
    }
    
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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