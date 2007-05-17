/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import static java.lang.String.format;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.graph.GraphSDB;
import com.hp.hpl.jena.sdb.layout1.*;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashDerby;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashHSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashMySQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashPGSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexDerby;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexHSQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexMySQL;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexPGSQL;
import com.hp.hpl.jena.sdb.layout2.hash.StoreTriplesNodesHashSQLServer;
import com.hp.hpl.jena.sdb.layout2.index.StoreTriplesNodesIndexSQLServer;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

/** 
 * @author Andy Seaborne
 * @version $Id: StoreFactory.java,v 1.5 2006/04/22 19:51:12 andy_seaborne Exp $
 */

public class StoreFactory
{
    private static Log log = LogFactory.getLog(StoreFactory.class) ;
    
    static { SDB.init() ; } 

    public static Store create(String filename)
    { return create(null, StoreDesc.read(filename)) ; }
    
    public static Store create(StoreDesc desc)
    { return create(null, desc) ; }
    
    public static Store create(SDBConnection sdb, StoreDesc desc)
    {
        Store store = _create(desc, sdb) ;
        return store ;
    }
    
    public static Graph createGraph(String filename)
    {
        return createGraph(StoreDesc.read(filename)) ;
    }

    public static Graph createGraph(StoreDesc storeDesc)
    {
        return createGraph(create(storeDesc)) ;
    }
    
    public static Graph createGraph(Store store)
    {
        return createGraphSDB(store) ;
    }

    public static GraphSDB createGraphSDB(Store store)
    {
        return new GraphSDB(store) ;
    }

    
    public static Model createModel(String filename)
    {
        return createModel(StoreDesc.read(filename)) ;
    }

    public static Model createModel(StoreDesc storeDesc)
    {
        return createModel(create(storeDesc)) ;
    }

    public static Model createModel(Store store)
    {
        return ModelFactory.createModelForGraph(new GraphSDB(store)) ;
    }

    private static Store _create(StoreDesc desc, SDBConnection sdb)
    {    
        if ( sdb == null ) 
            sdb = SDBFactory.createConnection(desc.connDesc) ;

        if ( desc.getLayout() == null )
        {
            log.warn("Layout is null.") ; 
            throw new SDBException("No such layout") ;
        }
            
        if ( desc.getLayout() == LayoutType.LayoutSimple )
        {
            switch (desc.getDbType())
            {
                case MySQL5:
                    return new StoreSimpleMySQL(sdb, desc.engineType) ;
                case PostgreSQL:
                    return new StoreSimplePGSQL(sdb) ;
                case MySQL41:
                case Oracle10:
                    throw new SDBException("Not supported (yet): "+desc.getLayout().getName()+" : "+desc.getDbType().getName()) ;
                case HSQLDB:
                    return new StoreSimpleHSQL(sdb) ;
                case Derby:
                    return new StoreSimpleDerby(sdb) ;
                case SQLServer:
                    return new StoreSimpleSQLServer(sdb) ;
                default:
                    throw new SDBException(format("Unknown DB type: %s [layout=%s]",
                                                  desc.getDbType().getName(), desc.getLayout().getName())) ;
            }
        }

        if ( desc.getLayout() == LayoutType.LayoutTripleNodesHash )
        {
            switch (desc.getDbType())
            {
                case Derby:
                    return new StoreTriplesNodesHashDerby(sdb) ;
                case MySQL5:
                    return new StoreTriplesNodesHashMySQL(sdb, desc.engineType) ;
                case PostgreSQL:
                    return new StoreTriplesNodesHashPGSQL(sdb) ;
                case HSQLDB:
                    return new StoreTriplesNodesHashHSQL(sdb) ;
                case SQLServer:
                    return new StoreTriplesNodesHashSQLServer(sdb) ;
                case MySQL41:
                case Oracle10:
                default:
                    throw new SDBException(format("Unknown DB type: %s [layout=%s, hash variant]",
                                                  desc.getDbType().getName(), desc.getLayout().getName())) ;
            }
        }
        
        if ( desc.getLayout() == LayoutType.LayoutTripleNodesIndex )
        {
            switch (desc.getDbType())
            {
                case Derby:
                    return new StoreTriplesNodesIndexDerby(sdb) ;
                case MySQL5:
                    return new StoreTriplesNodesIndexMySQL(sdb, desc.engineType) ;
                case PostgreSQL:
                    return new StoreTriplesNodesIndexPGSQL(sdb) ;
                case HSQLDB:
                    return new StoreTriplesNodesIndexHSQL(sdb) ;
                case MySQL41:
                case Oracle10:
                case SQLServer:
                    return new StoreTriplesNodesIndexSQLServer(sdb) ;
                default:
                    throw new SDBException(format("Unknown DB type: %s [layout=%s, index variant]",
                                                  desc.getDbType().getName(), desc.getLayout().getName())) ;
            }
        }
        
        if ( desc.getLayout() == LayoutType.LayoutRDB )
        {
            try { 
                // TODO Cope with no real connection
                IDBConnection conn = new DBConnection(sdb.getSqlConnection(), desc.connDesc.rdbType) ;
                String mName = desc.rdbModelName ;
                ModelRDB modelRDB = null ;
                if ( mName == null || mName.equals("") || mName.equalsIgnoreCase("default") )
                    modelRDB = ModelRDB.open(conn) ;
                else
                    modelRDB = ModelRDB.open(conn, mName) ;
                StoreRDB store = new StoreRDB(modelRDB) ;
                return store ;
            } catch ( RDFRDBException ex)
            {
                throw new SDBException(format("Failed to create ModelRDB store (%s, %s): %s", 
                                              desc.rdbModelName==null?"<default>":desc.rdbModelName,
                                              desc.rdbModelType, ex.getMessage())) ;
            }
        }

        log.warn(format("Can't make (%s, %s)", desc.getLayout().getName(), desc.getDbType())) ; 
        return null ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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
