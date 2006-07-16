/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.io.IOException;

import arq.cmd.CmdUtils;
import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.CmdArgModule;

import com.hp.hpl.jena.db.impl.Driver_MySQL;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.core.BindingRoot;
import com.hp.hpl.jena.query.engine1.PlanElement;
import com.hp.hpl.jena.query.engine1.PlanFormatter;
import com.hp.hpl.jena.query.engine1.PlanVisitorBase;
import com.hp.hpl.jena.query.engine1.PlanWalker;
import com.hp.hpl.jena.query.engine1.plan.PlanElementExternal;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.core.Block;
import com.hp.hpl.jena.sdb.core.compiler.QueryCompilerBasicPattern;
import com.hp.hpl.jena.sdb.engine.PlanSDB;
import com.hp.hpl.jena.sdb.engine.PlanTranslatorGeneral;
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

public class sdbprint extends CmdArgModule
{
    static { CmdUtils.setLog4j() ; }

    private static ArgDecl argDeclLayout = new ArgDecl(ArgDecl.HasValue, "layout") ;
    private static ArgDecl argDeclQuery   = new ArgDecl(ArgDecl.HasValue,   "query") ;

    // TODO ModLayout when StoreType is two dimensions (DB type, layout) 
    String layoutNameDefault = "layout2" ;

    
    // This command knows how to create queries without needing a store or connection.
    
    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;

    public static void main (String [] argv)
    {
        new sdbprint(argv).mainAndExit() ;
    }
    
    protected sdbprint(String[] args)
    {
        super(args);
        add(argDeclLayout, "layout", "Store layout") ;
        add(argDeclQuery, "query", "The query") ;
    }

    
    public static void compilePrint(String queryString, String layoutName)
    {
        System.err.println("BROKEN - FIX ME") ;
    }
    
    @Override
    protected void exec()
    {
      //---- Query
      
      String queryFile = getValue(argDeclQuery) ;
      String queryString = null ;
      
      if ( getNumPositional() == 0 && queryFile == null )
          cmdError("No query string or query file") ;

      if ( getNumPositional() > 1 )
          cmdError("Only one query string allowed") ;
      
      if ( getNumPositional() == 1 && queryFile != null )
          cmdError("Either query string or query file - not both") ;

      try {
          if ( queryFile != null )
              queryString  = FileUtils.readWholeFileAsUTF8(queryFile) ;
          else
          {
              queryString = getPositionalArg(0) ;
              queryString = indirect(queryString) ;
          }
              
      } catch (IOException ex)
      {
          System.err.println("Failed to read: "+queryFile) ;
          System.err.println(ex.getMessage()) ;
      }

      String layoutName = getValue(argDeclLayout) ;
      if ( layoutName == null )
          layoutName = layoutNameDefault ;

      Query query = QueryFactory.create(queryString) ;
      
      if ( layoutName.equalsIgnoreCase("layout1") ) 
      {
          compilePrint(query, new QueryCompilerSimple()) ;
          throw new TerminationException(0) ;
      }
      
      if ( layoutName.equalsIgnoreCase("modelRDB") || layoutName.equalsIgnoreCase("RDB") ) 
      {
          // Kludge something to work.
          IRDBDriver iDriver = new Driver_MySQL() ;
          compilePrint(query, new QueryCompiler1(new CodecRDB(iDriver))) ;
          throw new TerminationException(0) ;
      }
      
      if ( layoutName.equalsIgnoreCase("layout2") ) 
      {
          compilePrint(query, new QueryCompiler2()) ;
          throw new TerminationException(0) ;
      }
      
      cmdError("Unknown layout name: "+layoutName) ;
  }

    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    private void compilePrint(Query query, QueryCompiler compiler)
    {
        
        if ( ! quiet )
        {
            divider() ;
            query.serialize(System.out, Syntax.syntaxARQ) ;
        }
        
        if ( verbose )
        {
            divider() ;
            query.serialize(System.out, Syntax.syntaxPrefix) ;
        }

        Store store = new StoreBase(null, new PlanTranslatorGeneral(true, true), null, null, compiler) ;
        QueryEngineSDB qe = new QueryEngineSDB(store, query) ;

        if ( verbose )
        {
            
            PlanElement plan = qe.getPlan() ;
            divider() ;
            PlanFormatter.out(System.out, plan) ;
        }

        // Print all SDB things in the plan
        divider() ;
        PlanWalker.walk(qe.getPlan(), new PrintSDBBlocks(store)) ;
    }

    class PrintSDBBlocks extends PlanVisitorBase
    {
        private Store store ;
        String separator = null ;

        PrintSDBBlocks(Store store) { this.store = store ; }
        
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
                    QueryCompilerBasicPattern.printBlock = false ;  // Done earlier.
                    QueryCompilerBasicPattern.printAbstractSQL = true ;
                    QueryCompilerBasicPattern.printDivider = divider ;
                }
                // Mimic what the QueryIterSDB/QueryCompilerBasicPattern does.
                Block block = planSDB.getBlock() ;
                block = block.substitute(new BindingRoot());
                
                String sqlStmt = store.getQueryCompiler().asSQL(block) ;
                System.out.println(sqlStmt) ;
            }
        }
    }


    @Override
    protected String getSummary()
    {
        return "Usage: [--layout schemaName] [--query URL | string ] " ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
    
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