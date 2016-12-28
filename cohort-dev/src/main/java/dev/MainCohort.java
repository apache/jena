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

package dev;

import org.apache.jena.atlas.logging.LogCtl ;
import org.seaborne.tdb2.store.NodeId;
import org.seaborne.tdb2.store.NodeIdFactory;
import org.seaborne.tdb2.store.NodeIdTypes;

public class MainCohort {
    static { LogCtl.setLog4j() ; }
    
    public static void main(String... args) {
        NodeId nid = NodeIdFactory.createValue(NodeIdTypes.XSD_INTEGER, 1);
        System.out.println("Node id = "+nid);
        
        byte[] b = new byte[8];
        
        NodeIdFactory.set(nid, b);
        NodeId nid1 = NodeIdFactory.get(b);
        System.out.println("Node id = "+nid1);
    }
}


