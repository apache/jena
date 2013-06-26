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

package com.hp.hpl.jena.sparql.modify;

import java.util.List ;

import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;
import com.hp.hpl.jena.update.Update ;
import com.hp.hpl.jena.update.UpdateRequest ;

public class UpdateCompare {
    
    public static boolean isomorphic(UpdateRequest req1, UpdateRequest req2) {
        if ( req1 == req2 )
            return true ;
        if ( ! req1.samePrologue(req2) )
            return false ;
        List<Update> updates1 =  req1.getOperations() ;
        List<Update> updates2 =  req2.getOperations() ;
        if ( updates1.size() != updates2.size() )
            return false ;
         
        NodeIsomorphismMap isomap = new NodeIsomorphismMap() ;
        for ( int i = 0 ; i < updates1.size() ; i++ ) {
            Update upd1 = updates1.get(i) ;
            Update upd2 = updates2.get(i) ;
            if ( !isomorphic(upd1, upd2, isomap) )
                return false ;
        }
        return true ;
    }

    public static boolean isomorphic(Update req1, Update req2) {
        return isomorphic(req1, req2, new NodeIsomorphismMap()) ;
    }
    
    private static boolean isomorphic(Update upd1, Update upd2, NodeIsomorphismMap isomap) {
        return upd1.equalTo(upd2, isomap) ;
    }
}

