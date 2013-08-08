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

package com.hp.hpl.jena.sdb.assembler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SAPStorageType;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.store.Feature;
import com.hp.hpl.jena.sdb.store.FeatureSet;

public class StoreDescAssembler extends AssemblerBase implements Assembler
{
    static { SDB.init() ; }
    
    private static Logger log = LoggerFactory.getLogger(StoreDescAssembler.class) ;
    
    @Override
    public StoreDesc open(Assembler a, Resource root, Mode mode)
    {
        SDBConnectionDesc sdbConnDesc = null ;
        Resource c = GraphUtils.getResourceValue(root, AssemblerVocab.pConnection) ;
        if ( c != null )
            sdbConnDesc = (SDBConnectionDesc)a.open(c) ;
        
        String layoutName = GraphUtils.getStringValue(root, AssemblerVocab.pLayout) ;
        String dbType = chooseDBType(root, sdbConnDesc) ;
        
        // Features
        List<Resource> x = GraphUtils.multiValueResource(root, AssemblerVocab.featureProperty) ;
        FeatureSet fSet = new FeatureSet() ;
        for ( Resource r : x )
        {
            String n = GraphUtils.getStringValue(r, AssemblerVocab.featureNameProperty) ;
            String v = GraphUtils.getStringValue(r, AssemblerVocab.featureValueProperty) ;
            Feature f = new Feature(new Feature.Name(n), v) ;
            fSet.addFeature(f) ;
        }
        
        StoreDesc storeDesc = new StoreDesc(layoutName, dbType, fSet) ; 
        storeDesc.connDesc = sdbConnDesc ;

        // MySQL specials
        String engineName = GraphUtils.getStringValue(root, AssemblerVocab.pMySQLEngine) ;
        storeDesc.engineType = null ;
        if ( engineName != null )
            try { storeDesc.engineType= MySQLEngineType.convert(engineName) ; }
            catch (SDBException ex) {}

        // SAP specials
        String storageType = GraphUtils.getStringValue(root, AssemblerVocab.pStorageType) ;
        if ( storageType != null )
            try { storeDesc.storageType= SAPStorageType.convert(storageType) ; }
            catch (SDBException ex) {}
            
        return storeDesc ;
    }
    
    private String chooseDBType(Resource root, SDBConnectionDesc sdbConnDesc)
    {
        // --- DB Type
        // Two places the dbType can be (it's needed twice)
        // The connection description and the store description
        // Propagate one ot the other.
        // If specified twice, make sure they are the same.
        String dbTypeConn = (sdbConnDesc != null) ? sdbConnDesc.getType() : null ;
        String dbType     = GraphUtils.getStringValue(root, AssemblerVocab.pSDBtype) ;

        if ( dbTypeConn != null && dbType != null )
        {
            if ( ! dbTypeConn.equals(dbType) )
            {
                String $ = String.format(
                  "Connection-specified DB type and store description dbtype are different : %s %s", dbTypeConn, dbType ) ; 
                log.warn($) ;
            }
        }
        
        else if ( dbType != null )
        {
            if ( sdbConnDesc != null )
                sdbConnDesc.setType(dbType) ;
        }
        else if ( dbTypeConn != null )
            dbType = dbTypeConn ;
        else
        {
            // Both null.
            log.warn("Failed to determine the database type (not in store description, no connection description)") ;
            throw new SDBException("No database type found") ;
        }
        return dbType ;
    }
    
}
