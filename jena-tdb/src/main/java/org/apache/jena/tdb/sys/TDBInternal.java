/**
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

package org.apache.jena.tdb.sys;

import java.io.File ;
import java.io.FileFilter ;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDBException ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.setup.StoreParamsConst ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;
import org.apache.jena.tdb.transaction.DatasetGraphTransaction ;
import org.apache.jena.tdb.transaction.TransactionManager ;

/** A collection of helpers to abstract away from internal details of TDB. 
 * Use with care.
 */
public class TDBInternal
{
    /**
     * Return true if this is a TDB1 backed DatasetGraph. 
     */
    public static boolean isTDB1(DatasetGraph dsg) {
        return ( dsg instanceof DatasetGraphTransaction );
    }

    /**
     * Return the NodeId for a node. Returns NodeId.NodeDoesNotExist when the
     * node is not found. Returns null when not a TDB-backed dataset.
     */
    public static NodeId getNodeId(Dataset ds, Node node) {
        return getNodeId(ds.asDatasetGraph(), node) ;
    }

    /**
     * Return the NodeId for a node. Returns NodeId.NodeDoesNotExist when the
     * node is not found. Returns null when not a TDB-backed dataset.
     */
    public static NodeId getNodeId(DatasetGraph ds, Node node) {
        DatasetGraphTDB dsg = getDatasetGraphTDB(ds) ;
        return getNodeId(dsg, node) ;
    }

    /**
     * Return the NodeId for a node. Returns NodeId.NodeDoesNotExist when the
     * node is not found. Returns null when not a TDB-backed dataset.
     */
    public static NodeId getNodeId(DatasetGraphTDB dsg, Node node) {
        if ( dsg == null )
            return null ;
        NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable() ;
        NodeId nodeId = nodeTable.getNodeIdForNode(node) ;
        return nodeId ;
    }

    /**
     * Return the node for a NodeId (if any). Returns null if the NodeId does
     * not exist in the dataset.
     */
    public static Node getNode(Dataset ds, NodeId nodeId) {
        return getNode(ds.asDatasetGraph(), nodeId) ;
    }

    /**
     * Return the node for a NodeId (if any). Returns null if the NodeId does
     * not exist in the dataset.
     */
    public static Node getNode(DatasetGraph ds, NodeId nodeId) {
        DatasetGraphTDB dsg = getDatasetGraphTDB(ds) ;
        return getNode(dsg, nodeId) ;
    }

    /**
     * Return the node for a NodeId (if any). Returns null if the NodeId does
     * not exist in the dataset.
     */
    public static Node getNode(DatasetGraphTDB dsg, NodeId nodeId) {
        if ( dsg == null )
            return null ;
        NodeTable nodeTable = dsg.getQuadTable().getNodeTupleTable().getNodeTable() ;
        Node node = nodeTable.getNodeForNodeId(nodeId) ;
        return node ;
    }

    /**
     * Return the DatasetGraphTDB for a Dataset, or null.
     */
    public static DatasetGraphTDB getDatasetGraphTDB(Dataset ds) {
        return getDatasetGraphTDB(ds.asDatasetGraph()) ;
    }

    /**
     * Return the DatasetGraphTDB for a DatasetGraph, or null.
     * May not be up-to-date.
     */
    public static DatasetGraphTDB getDatasetGraphTDB(DatasetGraph dsg) {
        if ( dsg instanceof DatasetGraphTransaction ) {
                // Latest. Must be inside a transaction or completely non-transactional.
                // See getBaseDatasetGraphTDB
                return ((DatasetGraphTransaction)dsg).getDatasetGraphToQuery() ;
        }
        if ( dsg instanceof DatasetGraphTDB )
            return (DatasetGraphTDB)dsg ;

        return null ;
    }

    /**
     * Return a DatasetGraphTDB that uses the raw storage for tables.
     * Use with great care.
     */
    public static DatasetGraphTDB getBaseDatasetGraphTDB(DatasetGraph dsg) {
        return getStoreConnection(dsg).getBaseDataset() ;
    }

    /** Return the TransactionManager of this DatasetGraphTransaction */ 
    public static TransactionManager getTransactionManager(DatasetGraph dsg) {
        return getStoreConnection(dsg).getTransactionManager() ;
    }

    /* Use with great care */ 
    public static StoreConnection getStoreConnection(DatasetGraph dsg) {
        if ( dsg instanceof DatasetGraphTransaction )
            return ((DatasetGraphTransaction)dsg).getStoreConnection() ;
        throw new TDBException("Not a suitable TDB-backed DatasetGraph: " + Lib.classShortName(dsg.getClass())) ;
    }
    
    /** Look at a directory and see if it is a new area */
    public static boolean isNewDatabaseArea(Location location) {
        StoreConnection sConn = StoreConnection.getExisting(location) ;
        if ( sConn != null )
            // Already has a StoreConnection
            return false ;
        if ( location.isMem() )
            return true ;
        File d = new File(location.getDirectoryPath()) ;
        if ( !d.exists() )
            return true ;
        FileFilter ff = fileFilterNewDB ;
        File[] entries = d.listFiles(ff) ;
        return entries.length == 0 ;
    }

    static FileFilter fileFilterNewDB = 
        (pathname) -> {
            String fn = pathname.getName() ;
            if ( fn.equals(".") || fn.equals("..") )
                return false ;
            if ( pathname.isDirectory() )
                return true ;

            if ( fn.equals(StoreParamsConst.TDB_CONFIG_FILE) )
                return false ;
            return true ;
        } ;

}
