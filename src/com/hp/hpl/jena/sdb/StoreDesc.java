/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.shared.Env;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.FeatureSet;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sparql.util.GraphUtils;


public class StoreDesc
{
    private static Log log = LogFactory.getLog(StoreDesc.class) ;
    
    public SDBConnectionDesc connDesc   = null ;
    private DatabaseType dbType         = null ;
    private LayoutType layout           = null ;
    private FeatureSet featureSet       = null ;
    
    /** ModelRDB spefic */
    public String rdbModelName          = null ;
    public String rdbModelType          = null ;

    /** MySQL specific */
    public MySQLEngineType engineType   = null ;

    
    public static StoreDesc read(String filename)
    {
        Model m = Env.fileManager().loadModel(filename) ;
        return read(m) ;
    }
    
    public StoreDesc(String layoutName, String dbTypeName)
    {
        this(layoutName, dbTypeName, null) ;
    }
    
    public StoreDesc(String layoutName, String dbTypeName, FeatureSet featureSet)
    {
        this(LayoutType.fetch(layoutName), DatabaseType.fetch(dbTypeName), featureSet) ;
    }
    
    public StoreDesc(LayoutType layout, DatabaseType dbType)
    { this(layout, dbType, null ) ; }


    public StoreDesc(LayoutType layout, DatabaseType dbType, FeatureSet featureSet)
    {
        this.layout = layout ;
        this.dbType = dbType ;
        if ( featureSet == null )
            featureSet = new FeatureSet() ;
        this.featureSet = featureSet ;
    }
    
    public LayoutType getLayout() { return layout ; }
    
    public void setLayout(LayoutType layout)
    {
        this.layout = layout ;
    }

    public static StoreDesc read(Model m)
    {
        Resource r = GraphUtils.getResourceByType(m, AssemblerVocab.StoreAssemblerType) ;
        if ( r == null )
            throw new SDBException("Can't find store description") ;
        return read(r) ;
    }

    public static StoreDesc read(Resource r)
    {
        return (StoreDesc)AssemblerBase.general.open(r) ;
    }

    public DatabaseType getDbType()
    {
        return dbType ;
    }

    public void setDbType(DatabaseType dbType)
    {
        this.dbType = dbType ;
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