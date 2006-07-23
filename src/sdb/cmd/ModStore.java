/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.ArgModule;
import arq.cmdline.CmdArgModule;

import com.hp.hpl.jena.sdb.ModelSDB;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.graph.GraphSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.store.StoreFactory;
import com.hp.hpl.jena.shared.NotFoundException;

/** construction of a store from a store description,
 * possibly modified by command line arguments.
 * @author Andy Seaborne
 * @version $Id: CmdStore.java,v 1.1 2006/04/22 19:51:12 andy_seaborne Exp $
 */ 

public class ModStore implements ArgModule 
{
    // -------- This ...
    protected final ArgDecl argDeclSDBdesc       = new ArgDecl(true, "sdb");
    
    // ---- modified by these .... makes a connection description
    protected final ArgDecl argDeclJdbcURL      = new ArgDecl(true, "jdbc");
    protected final ArgDecl argDeclJdbcDriver   = new ArgDecl(true, "jdbcDriver", "jdbcdriver");

    protected final ArgDecl argDeclDbHost       = new ArgDecl(true, "dbHost");
    protected final ArgDecl argDeclDbName       = new ArgDecl(true, "dbName");
    
    protected final ArgDecl argDeclDbType      = new ArgDecl(true, "dbType", "dbtype");
    protected final ArgDecl argDeclDbArgs      = new ArgDecl(true, "dbArgs", "dbargs");
    
    protected final ArgDecl argDeclDbUser      = new ArgDecl(true, "dbUser", "user");
    protected final ArgDecl argDeclDbPassword  = new ArgDecl(true, "dbPassword", "password", "pw");

    // Store modifiers
    
    protected final ArgDecl argDeclLayout       = new ArgDecl(true, "layout");
    protected final ArgDecl argDeclMySQLEngine  = new ArgDecl(true, "engine");
    
    protected String driverName = null;      // JDBC class name
    //protected String argDriverTypeName = null;  // Jena driver name
    //protected String argModelName = null;
    //protected String layoutName = null ;

    // DB types to JDBC driver name (some common choices)
    private static Map<String,String> jdbcDrivers = new HashMap<String,String>();
    static {
        jdbcDrivers.put("mysql",       "com.mysql.jdbc.Driver");
        jdbcDrivers.put("mssql",       "com.microsoft.jdbc.sqlserver.SQLServerDriver") ;
        jdbcDrivers.put("postgres",    "org.postgresql.Driver");
        jdbcDrivers.put("postgresql",  "org.postgresql.Driver");
        jdbcDrivers.put("hsqldb:file", "org.hsqldb.jdbcDriver");
        jdbcDrivers.put("hsqldb:mem",  "org.hsqldb.jdbcDriver");
    }
    
    // DB types to name Jena uses internally
//    private static Map<String,String> jenaDriverName = new HashMap<String,String>();
//    static {
//        jenaDriverName.put("mssql",       "MsSQL");
//        jenaDriverName.put("mysql",       "MySQL");
//        jenaDriverName.put("postgresql",  "PostgreSQL");
//        jenaDriverName.put("postgres",    "PostgreSQL");
//        jenaDriverName.put("oracle",      "Oracle");
//    }

    StoreDesc storeDesc = null ;
    SDBConnection connection = null ;
    Store store = null ;
    DatasetStore dataset = null ;
    ModelSDB model = null ;
    
    public ModStore()
    {
        SDBConnection.logSQLExceptions = true ;
    }
    
    public void registerWith(CmdArgModule cmdLine)
    {
        cmdLine.getUsage().startCategory("Store and connection") ;
        
        cmdLine.add(argDeclSDBdesc,
                    "--sdb=<file>", "Store and connection description") ;
        
        cmdLine.add(argDeclLayout,
                    "--layout=NAME", "Database schema") ;
        
//        // Connection-level
//        cmdLine.add(argDeclJdbcURL);
//        cmdLine.addArgUsage("--jdbc", "JDBC URL") ;
//        
//        cmdLine.add(argDeclJdbcDriver);
//        cmdLine.addArgUsage("--jdbcDriver=", "JDBC driver class name") ;
//        
//        cmdLine.add(argDeclDbHost);
//        cmdLine.addArgUsage("--dbHost=", "DB Host") ;
//
//        cmdLine.add(argDeclDbName);
//        cmdLine.addArgUsage("--dbName=", "Database name") ;
//        
//        cmdLine.add(argDeclDbArgs);
//        cmdLine.addArgUsage("--dbArgs=", "Additional arguments for JDBC URL") ;
//
//        cmdLine.add(argDeclDbType);
//        cmdLine.addArgUsage("--dbType=", "Database type") ;
//
//        cmdLine.add(argDeclDbUser);
//        cmdLine.addArgUsage("--dbUser=", "Database user") ;
//
//        cmdLine.add(argDeclDbPassword);
//        cmdLine.addArgUsage("--dbPassword", "Daatbase user password") ;
//
//        // Store
//        cmdLine.add(argDeclMySQLEngine) ;
//        cmdLine.addArgUsage("--engine=", "MySQL engine type") ;
    }
    
    public void processArgs(CmdArgModule cmdLine)
    {
        if (! cmdLine.contains(argDeclSDBdesc))
        {
            System.err.println("No store description");
            throw new TerminationException(1);
        }
        
        String f = cmdLine.getArg(argDeclSDBdesc).getValue() ;
        try {
            storeDesc = StoreDesc.read(f) ;
        } catch (SDBException ex)
        {
            System.err.println("Failed to read the store description");
            System.err.println(ex.getMessage()) ;
            throw new TerminationException(1) ;
        }
        catch (NotFoundException ex)
        {
            System.err.println(f+" : Store description not found");
            throw new TerminationException(1) ;
        }
        
        // Overrides.
        if (cmdLine.contains(argDeclDbHost))
            storeDesc.connDesc.host = cmdLine.getArg(argDeclDbHost).getValue();
        
        if (cmdLine.contains(argDeclDbName))
            storeDesc.connDesc.name = cmdLine.getArg(argDeclDbName).getValue();
        
        if (cmdLine.contains(argDeclDbType))
            storeDesc.connDesc.type = cmdLine.getArg(argDeclDbType).getValue();

        if (cmdLine.contains(argDeclDbArgs))
            storeDesc.connDesc.argStr = cmdLine.getArg(argDeclDbArgs).getValue();

        if (cmdLine.contains(argDeclDbUser))
            storeDesc.connDesc.user = cmdLine.getArg(argDeclDbUser).getValue();

        if (cmdLine.contains(argDeclDbPassword))
            storeDesc.connDesc.password = cmdLine.getArg(argDeclDbPassword).getValue();

        if (cmdLine.contains(argDeclLayout))
        {
            String layoutName = cmdLine.getArg(argDeclLayout).getValue() ;
            // TODO LayoutEnum
            
            // Crude fixup
            if ( !layoutName.equalsIgnoreCase("layout1") &&
                 !layoutName.equalsIgnoreCase("layout2") )
            {
                System.err.println("Don't recognize layout name '"+layoutName+"'") ;
                throw new TerminationException(2) ;
            }
            storeDesc.layoutName = layoutName ;
        }

        //storeDesc.connDesc.initJDBC() ;
        
        if ( false )
        {
            //System.out.println("URL       = " + storeDesc.connDesc.URL);
            System.out.println("Type      = " + storeDesc.connDesc.type);
            System.out.println("Host      = " + storeDesc.connDesc.host);
            System.out.println("Database  = " + storeDesc.connDesc.name);
            System.out.println("User      = " + storeDesc.connDesc.user);
            System.out.println("Password  = " + storeDesc.connDesc.password);
            if ( storeDesc.connDesc.argStr != null )
                System.out.println("Args      = " + storeDesc.connDesc.argStr);
                
            System.out.println("Layout    = " + storeDesc.layoutName) ;
            //System.out.println("Name      = " + argModelName);

            SDBConnection.logSQLExceptions = true ;
            SDBConnection.logSQLStatements = true ;
        }

        // Mandatory arguments
//        if ( argDbURL == null )
//        {
//            System.err.println("Missing a required argument (JDBC URL)");
//            throw new TerminationException(9);
//        }

       
        driverName = storeDesc.connDesc.driver ;
        
        if (cmdLine.contains(argDeclJdbcDriver))
            driverName = cmdLine.getArg(argDeclJdbcDriver).getValue();

        if ( driverName == null )
            driverName = jdbcDrivers.get(storeDesc.connDesc.type.toLowerCase());

        if (driverName == null)
        {
            System.err.println("No known driver: please say which JDBC driver to use");
            throw new TerminationException(9);
        }

        JDBC.loadDriver(driverName);
    }
    
    public Store getStore()
    { 
        if ( store == null )
            store = StoreFactory.create(storeDesc, getConnection()) ;
        return store ; 
    }

    public StoreDesc getStoreDesc()
    {
        return storeDesc ;
    }

    public void setDbName(String dbName)
    {
        // used by truncate and format.
        storeDesc.connDesc.name = dbName ;
    }
    
    
    public DatasetStore getDataset()
    { 
        if ( dataset == null )
            dataset = new DatasetStore(getStore()) ;
        
        return dataset ;
    }
    
    public ModelSDB getModel()
    {
        if ( model == null )
            model = SDBFactory.connectModel(getStore()) ;
        return model ;
    }
    
    public GraphSDB getGraph()
    {
        return getModel().getGraphSDB() ;
    }
    
    public SDBConnection getConnection()
    {
        if ( connection == null )
            connection = SDBFactory.createConnection(storeDesc.connDesc) ;
        return connection ;
    }

    boolean hsqlDetech = false ;
    boolean isHSQL = false ;
    
    public boolean isHSQL()
    {
        if ( !hsqlDetech )
        {
            try {
                isHSQL = getConnection().getSqlConnection().getMetaData().getDatabaseProductName().contains("HSQL") ;
            } catch (SQLException ex)
            { throw new SDBExceptionSQL(ex) ; }
        }
        return isHSQL ;
    }
    
    public void closedown()
    {
        if ( store != null )
            store.close() ;
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