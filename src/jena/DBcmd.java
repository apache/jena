/*
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */

 
package jena;
import jena.cmdline.* ;
import com.hp.hpl.jena.db.* ;
//import com.hp.hpl.jena.rdf.model.* ;
import java.util.* ;
 
/** Framework for the database commands.
 * 
 * @author Andy Seaborne
 * @version $Id: DBcmd.java,v 1.2 2003-12-04 10:16:35 andy_seaborne Exp $
 */ 
 
abstract class DBcmd
{
    // Standardised names.
    protected final ArgDecl argDeclDbURL       = new ArgDecl(true, "db");
    protected final ArgDecl argDeclDbType      = new ArgDecl(true, "dbType");
    protected final ArgDecl argDeclDbUser      = new ArgDecl(true, "dbUser", "user");
    protected final ArgDecl argDeclDbPassword  = new ArgDecl(true, "dbPassword", "password", "pw");
    protected final ArgDecl argDeclModelName   = new ArgDecl(true, "model");
    protected final ArgDecl argDeclDbDriver    = new ArgDecl(true, "driver");

    protected final ArgDecl argDeclVerbose     = new ArgDecl(false, "v", "verbose");
    protected boolean verbose = false ;

    protected final ArgDecl argDeclDebug       = new ArgDecl(false, "debug");
    protected boolean debug = false ;

    protected final ArgDecl argDeclHelp        = new ArgDecl(false, "help", "h");


    // The values of these arguments
    protected String argDbURL = null;
    protected String argDbType = null;
    protected String argDbUser = null;
    protected String argDbPassword = null;
    protected String argModelName = null;

    // DB types to driver
    static Map drivers = new HashMap();
    static {
        drivers.put("mysql",       "com.mysql.jdbc.Driver");
        drivers.put("postgresql",  "org.postgresql.Driver");
        drivers.put("postgres",    "org.postgresql.Driver");
    }

    boolean takesPositionalArgs = false ;
    String cmdName = "DB" ;
    CommandLine cmdLine = new CommandLine();
    private IDBConnection jdbcConnection = null;
    private ModelRDB dbModel = null ;
    
    private String [] usage = new String[]{ "Complain to jena-dev: someone forgot the usage string" } ;

    DBcmd(String n, boolean posArgs)
    {
        cmdName = n ;
        takesPositionalArgs = posArgs ;
        cmdLine.add(argDeclDbURL);
        cmdLine.add(argDeclDbType);
        cmdLine.add(argDeclDbUser);
        cmdLine.add(argDeclDbPassword);
        cmdLine.add(argDeclModelName);
        cmdLine.add(argDeclVerbose) ;
        cmdLine.add(argDeclDebug) ;
        cmdLine.add(argDeclHelp) ;
    }

    protected void init(String[] args)
    {
        try {
            cmdLine.process(args);
        } catch (IllegalArgumentException ex)
        {
            usage() ;
            System.exit(1) ;
        }
        
        if ( cmdLine.contains(argDeclHelp) )
        {
            usage() ;
            System.exit(0) ;
        }
        
        verbose = cmdLine.contains(argDeclVerbose) ;
        debug = cmdLine.contains(argDeclDebug) ;
        if ( debug )
            verbose = true ;
        
        if (cmdLine.contains(argDeclDbURL))
            argDbURL = cmdLine.getArg(argDeclDbURL).getValue();

        if (cmdLine.contains(argDeclDbType))
            argDbType = cmdLine.getArg(argDeclDbType).getValue();

        if (cmdLine.contains(argDeclDbUser))
            argDbUser = cmdLine.getArg(argDeclDbUser).getValue();

        if (cmdLine.contains(argDeclDbPassword))
            argDbPassword = cmdLine.getArg(argDeclDbPassword).getValue();

        if (cmdLine.contains(argDeclModelName))
            argModelName = cmdLine.getArg(argDeclModelName).getValue();

        if ( verbose )
        {
            System.out.println("URL       = " + argDbURL);
            System.out.println("User      = " + argDbUser);
            System.out.println("Password  = " + argDbPassword);
            System.out.println("Type      = " + argDbType);
            System.out.println("Name      = " + argModelName);
        }

        // Mandatory arguments
        if (argDbURL == null || argDbType == null || argDbUser == null || argDbPassword == null)
        {
            System.err.println("Missing a required argument (need JDBC URL, user, password and DB type)");
            System.exit(9);
        }

        if ( ! takesPositionalArgs && cmdLine.items().size() != 0 )
        {
            System.err.println(cmdName+": No positional arguments allowed") ;
            usage() ;
            System.exit(9) ;
        }
        
        if ( takesPositionalArgs && cmdLine.items().size() == 0 )
        {
            System.err.println(cmdName+": Positional argument required") ;
            usage() ;
            System.exit(9) ;
        }

        String driverClass = (String)drivers.get(argDbType);
        if (cmdLine.contains(argDeclDbDriver))
            driverClass = cmdLine.getArg(argDeclDbDriver).getValue();

        if (driverClass == null)
        {
            System.err.println("No driver: please say which JDBC driver to use");
            System.exit(9);
        }

        try
        {
            Class.forName(driverClass).newInstance();
        }
        catch (Exception ex)
        {
            System.err.println("Couldn't load the driver class: " + driverClass);
            System.err.println("" + ex);
            System.exit(9);
        }

    }
    
    protected ModelRDB getRDBModel() 
    {
        if ( dbModel == null )
        {
            if ( argModelName == null )
                dbModel = ModelRDB.open(getConnection()) ;
            else
                dbModel = ModelRDB.open(getConnection(), argModelName) ;
        }
        return dbModel ;   
    }


    protected IDBConnection getConnection()
    {
        if ( jdbcConnection == null )
        {
            try {
                jdbcConnection = new DBConnection(argDbURL, argDbUser, argDbPassword, argDbType);
            } catch (Exception ex)
            {
                System.out.println("Exception making connection: "+ex.getMessage()) ;
                System.exit(9) ;
            }
        }
        return jdbcConnection ;
    }


    protected void exec()
    {
        if ( cmdLine.items().size() == 0 )
        {
            exec0() ;
            return ;
        }
        
        
        boolean inTransaction = false ;
        try
        {
            if ( getRDBModel().supportsTransactions() )
            {
                inTransaction = true ;
                getRDBModel().begin() ;
            }

            for ( Iterator iter = cmdLine.items().iterator() ; iter.hasNext() ; )
            {
                String arg = (String)iter.next() ;
                boolean contTrans = false ;
                try {
                    contTrans = exec1(arg) ;
                }
                catch (Exception ex)
                {
                    System.out.println(ex.getMessage()) ;
                    ex.printStackTrace(System.out) ;
                    if ( inTransaction )
                    {
                        getRDBModel().abort() ;
                        inTransaction = false ;
                    }
                    dbModel.close() ;
                    dbModel = null ;
                    System.exit(9);
                }
                                        
                    
                if ( !contTrans && inTransaction )
                {
                    getRDBModel().commit() ;
                    if ( iter.hasNext() )
                    {
                        inTransaction = true ;
                        getRDBModel().begin() ; 
                    } 
                }
            }
        }            
        finally
        {
            if ( inTransaction )
                getRDBModel().commit() ;
        }
        
        dbModel.close() ;
        dbModel = null ;
    }

    /** Called if there are no psoitional arguments
     */     
    abstract void exec0() ;
    
    /** Called for each postional argument, inside a transaction.
     *  Return true to continue this transaction, false to end it and start a new
     *  one if there are any more args 
     */     
    abstract boolean exec1(String arg) ;
    
    
    
    protected void setUsage(String a)
    {
        String[] aa = new String[]{a} ;
        setUsage(aa) ;
    } 


    /** Usage message: one line per entry*/
    
    protected void setUsage(String[] a)
    {
        usage = a ;
    }
    
    protected void usage()
    {
        for ( int i = 0 ; i < usage.length ; i++ )
        {
            System.err.println(usage[i]) ;
        }
    }
}

/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
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
