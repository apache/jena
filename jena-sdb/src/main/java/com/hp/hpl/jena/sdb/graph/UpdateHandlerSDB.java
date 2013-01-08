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

package com.hp.hpl.jena.sdb.graph;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;

/**
 * This is very dumb, but ensures we bulk update when we can
 */

@SuppressWarnings("deprecation")
public class UpdateHandlerSDB extends SimpleBulkUpdateHandler {
	
	public UpdateHandlerSDB(GraphSDB graph)
	{
		super(graph);
	}

	@Override
    public void add(Triple[] arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.add(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
    public void add(List<Triple> arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.add(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
    public void add(Iterator<Triple> arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.add(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
     	public void add(Graph arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.add(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
	public void add(Graph arg0, boolean arg1) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.add(arg0, arg1); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
	public void delete(Triple[] arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.delete(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
	public void delete(List<Triple> arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.delete(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
	public void delete(Iterator<Triple> arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.delete(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

    @Override
	public void delete(Graph arg0) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.delete(arg0); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

	@Override
    public void delete(Graph arg0, boolean arg1) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.delete(arg0, arg1); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

	@Override
    public void remove(Node arg0, Node arg1, Node arg2) {
		try { ((GraphSDB) graph).startBulkUpdate(); super.remove(arg0, arg1, arg2); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}

	@Override
    public void removeAll() {
		notifyRemoveAll();
		try { ((GraphSDB) graph).startBulkUpdate(); ((GraphSDB) graph).deleteAll(); }
		finally { ((GraphSDB) graph).finishBulkUpdate(); }
	}
}
