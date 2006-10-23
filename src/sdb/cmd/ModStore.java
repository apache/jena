/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb.cmd;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arq.cmd.TerminationException;
import arq.cmdline.ArgDecl;
import arq.cmdline.ArgModule;
import arq.cmdline.CmdArgModule;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.shared.SDBNotFoundException;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.*;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.util.FileManager;

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

    protected final ArgDecl argDeclDbHost       = new ArgDecl(true, "dbHost", "dbhost");
    protected final ArgDecl argDeclDbName       = new ArgDecl(true, "dbName", "db");
    
    protected final ArgDecl argDeclDbType      = new ArgDecl(true, "dbType", "dbtype");
    protected final ArgDecl argDeclDbArgs      = new ArgDecl(true, "dbArgs", "dbargs");
    
    protected final ArgDecl argDeclDbUser      = new ArgDecl(true, "dbUser", "user");
    protected final ArgDecl argDeclDbPassword  = new ArgDecl(true, "dbPassword", "password", "pw");

    // Store modifiers
    
    protected final ArgDecl argDeclLayout       = new ArgDecl(true, "layout");
    protected final ArgDecl argDeclMySQLEngine  = new ArgDecl(true, "engine");

    // Load some data - useful for in-memory stores.
    // ModData?
    protected final ArgDecl argDeclLoad         = new ArgDecl(true,  "load");
    protected final ArgDecl argDeclFormat       = new ArgDecl(false, "format");

    
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
    boolean connectionAttempted = false ;
    Store store = null ;
    DatasetStore dataset = null ;
    Model model = null ;
    List<String> loadFiles = null ;
    boolean formatFirst = false ; 
    
    public ModStore()
    {
        SDBConnection.logSQLExceptions = true ;
    }
    
    public void registerWith(CmdArgModule cmdLine)
    {
        final boolean AddUsage = false ;
        
        cmdLine.getUsage().startCategory("Store and connection") ;
        
        cmdLine.add(argDeclSDBdesc,
                    "--sdb=<file>", "Store and connection description") ;
        
        cmdLine.add(argDeclLayout,
                    "--layout=NAME", "Database schema") ;
        
        // Connection-level
        cmdLine.add(argDeclJdbcURL);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--jdbc", "JDBC URL") ;
        
        cmdLine.add(argDeclJdbcDriver);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--jdbcDriver=", "JDBC driver class name") ;
        
        cmdLine.add(argDeclDbHost);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--dbHost=", "DB Host") ;

        cmdLine.add(argDeclDbName);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--dbName=", "Database name") ;
        
        cmdLine.add(argDeclDbArgs);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--dbArgs=", "Additional arguments for JDBC URL") ;

        cmdLine.add(argDeclDbArgs);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--dbType=", "Database type") ;

        cmdLine.add(argDeclDbUser);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--dbUser=", "Database user") ;

        cmdLine.add(argDeclDbPassword);
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--dbPassword", "Daatbase user password") ;

        // Store
        cmdLine.add(argDeclMySQLEngine) ;
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--engine=", "MySQL engine type") ;
        
        cmdLine.add(argDeclLoad) ;
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--load=", "Datafile to load (permanent : for in-memory stores only) ") ;
        
        cmdLine.add(argDeclFormat) ;
        if ( AddUsage )
            cmdLine.getUsage().addUsage("--format", "Format first(permanent : for in-memory stores only) ") ;
        
    }
    
    
    @SuppressWarnings("unchecked")
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

        if (cmdLine.contains(argDeclMySQLEngine))
            storeDesc.engineType = MySQLEngineType.convert(cmdLine.getArg(argDeclMySQLEngine).getValue());
        
        if (cmdLine.contains(argDeclLayout))
        {
            String layoutName = cmdLine.getArg(argDeclLayout).getValue() ;
            storeDesc.layout = LayoutType.convert(layoutName) ;

            if ( storeDesc.layout == null )
            {
                System.err.println("Don't recognize layout name '"+layoutName+"'") ;
                throw new TerminationException(2) ;
            }
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
                
            System.out.println("Layout    = " + storeDesc.layout.getName()) ;
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

        try { JDBC.loadDriver(driverName); }
        catch (SDBNotFoundException ex)
        {
            System.err.println("Driver not found: "+driverName);
            throw new TerminationException(9);
        }
        
        // Data stuff
        loadFiles = (List<String>)cmdLine.getValues(argDeclLoad) ;
        formatFirst = cmdLine.contains(argDeclFormat) ;
    }
    
    public Store getStore()
    { 
        if ( store == null )
        {
            store = StoreFactory.create(getConnection(), storeDesc) ;
            if ( formatFirst )
                getStore().getTableFormatter().format() ;
        }
            
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
        {
            dataset = new DatasetStore(getStore()) ;
            initData(dataset.getDefaultModel()) ;
        }
        
        return dataset ;
    }
    
    public Model getModel()
    {
        if ( model == null )
        {
            model = SDBFactory.connectModel(getStore()) ;
            initData(model) ;
        }
        return model ;
    }
    
    private void initData(Model model)
    {
        if ( loadFiles != null )
        {
            
            for ( String s : loadFiles )
                FileManager.get().readModel(model, s) ;
        }
        loadFiles = null ;
    }
    
    public Graph getGraph()
    {
        return getModel().getGraph() ;
    }
    
    public boolean isConnected() { return connection != null ; }
    public boolean hasStore() { return store != null ; }
    
    public SDBConnection getConnection()
    {
        if ( ! isConnected() && ! connectionAttempted )
            try {
                connection = SDBFactory.createConnection(storeDesc.connDesc) ;
            } finally { connectionAttempted = true ; }
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