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

package org.apache.jena.sdb;

import org.apache.jena.assembler.assemblers.AssemblerBase ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.Resource ;
import org.apache.jena.sdb.assembler.AssemblerVocab ;
import org.apache.jena.sdb.shared.Env ;
import org.apache.jena.sdb.sql.MySQLEngineType ;
import org.apache.jena.sdb.sql.SAPStorageType ;
import org.apache.jena.sdb.sql.SDBConnectionDesc ;
import org.apache.jena.sdb.store.DatabaseType ;
import org.apache.jena.sdb.store.FeatureSet ;
import org.apache.jena.sdb.store.LayoutType ;
import org.apache.jena.sparql.util.graph.GraphUtils ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        Model m = Env.fileManager().loadModelInternal(filename) ;
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
