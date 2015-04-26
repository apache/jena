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

import java.util.List ;

import org.apache.jena.atlas.lib.Tuple ;
import org.seaborne.dboe.engine.PhysicalPlan ;
import org.seaborne.dboe.engine.Slot ;

import org.apache.jena.tdb.store.NodeId ;

public interface Planner
{
    /** Decide an execution plan for a pattern of tuples of slots of NodeId,
     *  given a tuple table (which must be "appropiate" for the pattern,
     *  including right width, so tuple isze = tuple yable width).  
     *  All decisions on which graph to access, union graphs etc have alrady been done
     *  before calling this.  
     */
    
    public PhysicalPlan<NodeId> generatePlan(List<Tuple<Slot<NodeId>>> tuples) ;
}
