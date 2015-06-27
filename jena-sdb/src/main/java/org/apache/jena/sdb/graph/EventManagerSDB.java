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

package org.apache.jena.sdb.graph;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphEvents ;
import org.apache.jena.graph.impl.SimpleEventManager ;

public class EventManagerSDB extends SimpleEventManager {
	
	public EventManagerSDB() {
		super();
	}
	
	/*
	 * Override NotifyEvent to catch start and endRead
	 */
	@Override
	public void notifyEvent(Graph arg0, Object arg1)
    {
	    if ( arg0 instanceof GraphSDB) {
    		if (arg1.equals(GraphEvents.startRead) )
    			((GraphSDB) arg0).startBulkUpdate() ;
    		if (arg1.equals(GraphEvents.finishRead) )
                ((GraphSDB) arg0).finishBulkUpdate() ;
	    } else
	        Log.warn(this, "Non GraphSDB passed to EventManagerSDB.notifyEvent");
	    
		super.notifyEvent(arg0, arg1) ;
	}
}
