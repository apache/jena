/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import arq.cmd.CmdUtils;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;

import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.sdb.Access;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.layout1.StoreRDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.sdb.util.StrUtils;

import java.sql.* ;

public class RunRDB
{
    static { CmdUtils.setLog4j() ; CmdUtils.setN3Params() ; }

    private static void jdbcGetMetadata()
    {
        String jdbcURL = "jdbc:mysql://localhost/jenatest" ;
        String user = null ;
        String password = null ;
        JDBC.loadDriver("com.mysql.jdbc.Driver") ;

        if ( user == null )
            user = Access.getUser() ;
        if ( password == null )
            password = Access.getPassword() ;

        try
        {
            Connection sqlConnection = DriverManager.getConnection(jdbcURL, user, password) ;
            DatabaseMetaData dbmd = sqlConnection.getMetaData() ;
            sqlConnection.close() ;
        } catch (SQLException e)
        {
            //exception("SDBConnection",e ) ;
            throw new SDBException("SQL Exception while connecting to database: "+jdbcURL+" : "+e.getMessage()) ;
        }

    }

    @SuppressWarnings("deprecation")
    public static void dwim(String[]argv)
    {
        // Crate a model - normal way
        try
        {
            String className = "com.mysql.jdbc.Driver";         // path of driver class
            //Class.forName (className);                          // Load the Driver
            String DB_URL =     "jdbc:mysql://localhost/test";  // URL of database 
            String DB_USER =   "????";                          // database user id
            String DB_PASSWD = "????";                          // database password
            String DB =        "MySQL";                         // database type

            IDBConnection conn = new DBConnection ( DB_URL, DB_USER, DB_PASSWD, DB );
            ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;
            Model m = maker.createDefaultModel() ;
            
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1) ;
        }
        
        // Description for ModelRDB -- need to incorporate elsewhere
        // Use the 
        SDB.init() ;

        String f = "sdb.ttl" ; 
        SDBConnectionDesc desc = SDBConnectionDesc.read(f) ;
        desc.name =     "jenatest" ;

        desc.user =     Access.getUser() ;
        if ( desc.user == null ) desc.user = "user" ;

        desc.password = Access.getPassword() ;
        if ( desc.password == null ) desc.password = "password" ;

        desc.initJDBC() ;
        SDBConnection sdb = new SDBConnection(desc) ;

        JDBC.loadDriver(desc.driver) ;
        //DBConnection c ;
        IDBConnection conn = new DBConnection(sdb.getSqlConnection(), desc.type);

        ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;

        ModelRDB modelRDB = ModelRDB.open(conn) ;
        StoreRDB store = new StoreRDB(modelRDB) ;
        //Model model = SDBFactory.connectModel(store) ;
        Dataset dataset = new DatasetStore(store) ;

        SDBConnection.logSQLStatements = true ;
        String queryStr = StrUtils.strjoinNL(
            "PREFIX : <http://example/>" ,
            "SELECT * { :x :p ?a . ?a :q1 ?b OPTIONAL { ?b ?q ?v }}") ;
        
        Query query = QueryFactory.create(queryStr) ;
        System.out.println(query.serialize()) ;
        
        {
            QueryExecution qExec = QueryExecutionFactory.create(query, dataset) ;
            ResultSetFormatter.out(System.out, qExec.execSelect()) ;
            qExec.close();
        }
        {
            QueryExecution qExec = QueryExecutionFactory.create(query, modelRDB) ;
            ResultSetFormatter.out(System.out, qExec.execSelect()) ;
            qExec.close();
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