/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.shared.Env;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SAPStorageType;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.FeatureSet;
import com.hp.hpl.jena.sdb.store.LayoutType;


public class StoreDesc
{
    private static Logger log = LoggerFactory.getLogger(StoreDesc.class) ;
    
    public SDBConnectionDesc connDesc   = null ;
    private DatabaseType dbType         = null ;
    private LayoutType layout           = null ;
    private FeatureSet featureSet       = null ;
    
    /** MySQL specific */
    public MySQLEngineType engineType   = null ;
    
    /** SAP specific */
    public SAPStorageType storageType   = null ;
    
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
        // Does not mind store descriptions or dataset descriptions
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
