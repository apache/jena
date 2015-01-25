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

package dev.binding;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Operations on Bindings */ 
public class BindingLib {
    // See BindingUtils

    public static boolean equals(Binding bind1, Binding bind2)
    {
        if ( bind1 == bind2 ) return true ;

        // Same variables?
        
        if ( bind1.size() != bind2.size() )
            return false ;

        for ( Iterator<Var> iter1 = bind1.vars() ; iter1.hasNext() ; )
        {
            Var var = iter1.next() ; 
            Node node1 = bind1.get(var) ;
            Node node2 = bind2.get(var) ;
            if ( ! Lib.equal(node1, node2) )
                return false ;
        }
        
        // No need to check the other way round as the sizes matched. 
        return true ;
    }
    
}

