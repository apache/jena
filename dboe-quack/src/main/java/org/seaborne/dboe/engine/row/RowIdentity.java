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

package org.seaborne.dboe.engine.row;

import java.util.Collection ;
import java.util.Collections ;

import org.seaborne.dboe.engine.Row ;

import com.hp.hpl.jena.sparql.core.Var ;

/* A row of zero variables */ 
public class RowIdentity<X> implements Row<X>  {
    
    @SuppressWarnings("rawtypes")
    private static Row instance       = new  RowIdentity<>() ;
    
    @SuppressWarnings({"unchecked", "cast"})
    public static <Z> Row<Z> identityRow() { return (Row<Z>)instance ; }
    
    private RowIdentity() {}
    
    @Override
    public X get(Var key)           { return null ; }

    @Override
    public boolean contains(Var v)  { return false ; }

    @Override
    public Collection<Var> vars() { return Collections.emptySet() ; }

    @Override public boolean isEmpty() { return true ; }
    @Override public boolean isIdentity() { return true ; }
}