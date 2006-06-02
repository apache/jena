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

import arq.cmdline.ArgDecl;

import com.hp.hpl.jena.sdb.ModelSDB;
import com.hp.hpl.jena.sdb.SDB;
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

public abstract class CmdStore extends CmdGeneral
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
    protected String layoutName = null ;

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
    
    protected CmdStore(String name, String argv[])
    {
        super(name, argv) ;
        SDB.init() ;

        add(argDeclSDBdesc) ;
        // Connection-level
        add(argDeclJdbcURL);
        add(argDeclJdbcDriver);
        add(argDeclDbHost);
        add(argDeclDbName);
        add(argDeclDbArgs);
        add(argDeclDbType);
        add(argDeclDbUser);
        add(argDeclDbPassword);

        // Store
        add(argDeclLayout) ;
        add(this.argDeclMySQLEngine) ;
    }
    
    @Override 
    public void process()
    {
        try {
            super.process();
        } catch (IllegalArgumentException ex)
        {
            System.err.println(ex.getMessage()) ;
            usage() ;
            System.exit(1) ;
        }
        
       if (contains(argDeclSDBdesc))
        {
            String f = getArg(argDeclSDBdesc).getValue() ;
            try {
                storeDesc = StoreDesc.read(f) ;
            } catch (SDBException ex)
            {
                System.err.println("Failed to read the store description");
                System.err.println(ex.getMessage()) ;
                System.exit(1) ;
            }
            catch (NotFoundException ex)
            {
                System.err.println(f+" : Store description not found");
                System.exit(1) ;
            }
        }
        
        // Overrides.
        if (contains(argDeclDbHost))
            storeDesc.connDesc.host = getArg(argDeclDbHost).getValue();
        
        if (contains(argDeclDbName))
            storeDesc.connDesc.name = getArg(argDeclDbName).getValue();
        
        if (contains(argDeclDbType))
            storeDesc.connDesc.type = getArg(argDeclDbType).getValue();

        if (contains(argDeclDbArgs))
            storeDesc.connDesc.argStr = getArg(argDeclDbArgs).getValue();

        if (contains(argDeclDbUser))
            storeDesc.connDesc.user = getArg(argDeclDbUser).getValue();

        if (contains(argDeclDbPassword))
            storeDesc.connDesc.password = getArg(argDeclDbPassword).getValue();

        if (contains(argDeclLayout))
        {
            layoutName = getArg(argDeclLayout).getValue() ;
            // TODO LayoutEnum
            
            // Crude fixup
            if ( !layoutName.equalsIgnoreCase("layout1") &&
                 !layoutName.equalsIgnoreCase("layout2") )
            {
                System.err.println("Don't recognize layout name '"+layoutName+"'") ;
                System.exit(2) ;
            }
        }

        //storeDesc.connDesc.initJDBC() ;
        
        if ( debug )
        {
            //System.out.println("URL       = " + storeDesc.connDesc.URL);
            System.out.println("Type      = " + storeDesc.connDesc.type);
            System.out.println("Host      = " + storeDesc.connDesc.host);
            System.out.println("Database  = " + storeDesc.connDesc.name);
            System.out.println("User      = " + storeDesc.connDesc.user);
            System.out.println("Password  = " + storeDesc.connDesc.password);
            if ( storeDesc.connDesc.argStr != null )
                System.out.println("Args      = " + storeDesc.connDesc.argStr);
                
            System.out.println("Layout    = " + layoutName) ;
            //System.out.println("Name      = " + argModelName);

            SDBConnection.logSQLExceptions = true ;
            SDBConnection.logSQLStatements = true ;
        }

        // Mandatory arguments
//        if ( argDbURL == null )
//        {
//            System.err.println("Missing a required argument (JDBC URL)");
//            System.exit(9);
//        }

        driverName = storeDesc.connDesc.driver ;
        
        if (contains(argDeclJdbcDriver))
            driverName = getArg(argDeclJdbcDriver).getValue();

        if ( driverName == null )
            driverName = jdbcDrivers.get(storeDesc.connDesc.type.toLowerCase());

        if (driverName == null)
        {
            System.err.println("No known driver: please say which JDBC driver to use");
            System.exit(9);
        }

        JDBC.loadDriver(driverName);
    }
    
    protected Store getStore()
    { 
        if ( store == null )
            store = StoreFactory.create(storeDesc, getConnection()) ;
        return store ; 
    }

    protected DatasetStore getDataset()
    { 
        if ( dataset == null )
            dataset = new DatasetStore(getStore()) ;
        
        return dataset ;
    }
    
    protected ModelSDB getModel()
    {
        if ( model == null )
            model = SDBFactory.connectModel(getStore()) ;
        return model ;
    }
    
    protected GraphSDB getGraph()
    {
        return getModel().getGraphSDB() ;
    }
    
    protected SDBConnection getConnection()
    {
        if ( connection == null )
            connection = SDBFactory.createConnection(storeDesc.connDesc) ;
        return connection ;
    }


    boolean hsqlDetech = false ;
    boolean isHSQL = false ;
    
    protected boolean isHSQL()
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
    
    protected void closedown()
    {
        if ( store != null )
            store.close() ;
    }

    protected void addSpecUsage(List<String> u)
    {
        u.add("--sdb=SDB") ;
        u.add("--dbName=") ;
        u.add("--dbHost=") ;
        u.add("--dbType=") ;
        u.add("--dbArgs=") ;
        u.add("--dbUser=") ;
        u.add("--dbPassword=") ;
        u.add("--layout=") ;
        u.add("--engine=  [MySQL]") ;
        u.add("--jdbc=") ; 
        u.add("--jdbcDriver=") ;
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