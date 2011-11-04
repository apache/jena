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

package dev;

import java.nio.ByteBuffer;
import java.util.Iterator;

import org.openjena.atlas.lib.Pair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.file.Location;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.Index;
import com.hp.hpl.jena.tdb.index.IndexBuilder;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTableNative2;
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class NodeTableNative2Run {

	public static void main(String[] args) {
		Location location = new Location("/tmp/tdb");
		RecordFactory nodeTableRecordFactory = new RecordFactory(SystemTDB.SizeOfNodeId, SystemTDB.SizeOfNodeId) ;
		Index nodeToIdIndex = IndexBuilder.createIndex(new FileSet(location, Names.indexNode2Id), nodeTableRecordFactory) ;
		ObjectFile objects = FileFactory.createObjectFileDisk(Names.indexId2Node) ;
		NodeTable nt = new NodeTableNative2(nodeToIdIndex, objects);
		NodeId nodeId = nt.getAllocateNodeId(Node.createLiteral("Foo"));
		nt.sync();
		
		System.out.println(nodeId);
		
		Iterator<Pair<Long, ByteBuffer>> iter = objects.all() ;
		while ( iter.hasNext() ) {
			System.out.println(iter.next()) ;
		}
		
		Iterator<Record> iter2 = nodeToIdIndex.iterator() ;
		while ( iter2.hasNext() ) {
			System.out.println(iter2.next()) ;
		}
	}

}

