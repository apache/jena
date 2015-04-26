/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.engine.tdb;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.* ;
import org.seaborne.dboe.engine.explain.Explain2 ;

import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;

/** Debugging wrapper for an AccessorTDB */
public class AccessorTDBDebug extends AccessorTDB {

    private final AccessorTDB other ;
    private final String label ;
    
    public AccessorTDBDebug(String label, AccessorTDB other) {
        super(null) ;
        this.other = other ;
        this.label = label ; 
    }

    @Override
    public StorageTDB getDB() {
        //Explain2.explain(Quack.quackExec, label+"getDB()");
        return other.getDB() ;
    }

    @Override
    public NodeTable getNodeTable() {
        //Explain2.explain(Quack.quackExec, label+"getNodeTable()");
        return other.getNodeTable() ;
    }
    
    @Override
    public Iterator<Tuple<NodeId>> accessTuples(Tuple<NodeId> pattern) {
        Explain2.explain(Quack.quackExec, label+"accessTuples("+pattern+")");
        return other.accessTuples(pattern) ;
    }
    @Override
    public Iterator<Row<NodeId>> accessRows(Tuple<Slot<NodeId>> pattern) {
        Explain2.explain(Quack.quackExec, label+"accessRows("+pattern+")");
        return other.accessRows(pattern) ;
    }
    @Override
    public Iterator<Row<NodeId>> fetch(PredicateObjectList<NodeId> accessList) {
        Explain2.explain(Quack.quackExec, label+"fetch("+accessList+")");
        return other.fetch(accessList) ;
    }
}

