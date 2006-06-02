/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import static java.lang.String.format;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.layout1.StoreSimpleHSQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesHSQL;
import com.hp.hpl.jena.sdb.layout2.StoreTriplesNodesMySQL;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

/** 
 * @author Andy Seaborne
 * @version $Id: StoreFactory.java,v 1.5 2006/04/22 19:51:12 andy_seaborne Exp $
 */

public class StoreFactory
{
    private static Log log = LogFactory.getLog(StoreFactory.class) ;

//    public static Store create(SDBConnection connection,
//                                StoreName schemaName)
//    { return create(connection, schemaName, false) ; }
//    
//    public static Store create(SDBConnection connection,
//                                StoreName schemaName,
//                                boolean canCreate)
//    { return null; }
    
    public static Store create(StoreDesc desc)
    { return create(desc, null) ; }
    
    public static Store create(StoreDesc desc, SDBConnection sdb)
    {
        if ( sdb == null ) 
            sdb = SDBFactory.createConnection(desc.connDesc) ;
        
        if ( desc.layoutName.equalsIgnoreCase("layout1") )
        {
            switch (desc.dbType)
            {
                case MySQL5:
                    return new StoreTriplesNodesMySQL(sdb, desc.engineType) ;
                case MySQL41:
                case PostgreSQL:
                case Oracle10:
                case SQLServer:
                    throw new SDBException("Not supported (yet): "+desc.layoutName+" : "+desc.dbType.getName()) ;
                case HSQLDB:
                    return new StoreSimpleHSQL(sdb) ;
                default:
                    throw new SDBException(format("Unknown DB type: %s [layout=%s]",
                                                  desc.dbType.getName(), desc.layoutName)) ;
            }
        }

        if ( desc.layoutName.equalsIgnoreCase("layout2") )
        {
            switch (desc.dbType)
            {
                case MySQL5:
                    return new StoreTriplesNodesMySQL(sdb, desc.engineType) ;
                case MySQL41:
                case PostgreSQL:
                case Oracle10:
                case SQLServer:
                    throw new SDBException("Not supported (yet): "+desc.layoutName+" : "+desc.dbType.getName()) ;
                case HSQLDB:
                    return new StoreTriplesNodesHSQL(sdb) ;
                default:
                    throw new SDBException(format("Unknown DB type: %s [layout=%s]",
                                                  desc.dbType.getName(), desc.layoutName)) ;
            }
        }

        log.warn(format("Can't make (%s, %s)", desc.layoutName, desc.connDesc.type)) ; 
        return null ;
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