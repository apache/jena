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

package com.hp.hpl.jena.sparql.engine.optimizer.reorder;

import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.sse.Item ;

public class PatternElements
{
    public static final Item ANY       = Item.createSymbol("ANY") ;
    public static final Item VAR       = Item.createSymbol("VAR") ;
    public static final Item TERM      = Item.createSymbol("TERM") ;
    public static final Item URI       = Item.createSymbol("URI") ;
    public static final Item BNODE     = Item.createSymbol("BNODE") ;
    public static final Item LITERAL   = Item.createSymbol("LITERAL") ;
    
    public static boolean isSet(Item item)
    {
        if ( item.isNode() )
        {
            if ( item.getNode().isConcrete() ) return true ;
        }
        if (item.equals(TERM) ) return true ;
        if (item.equals(URI) ) return true ;
        if (item.equals(BNODE) ) return true ;
        if (item.equals(LITERAL) ) return true ;
        return false ;
    }
    
    public static boolean isAny(Item item)         { return item.equals(ANY) ; }
    public static boolean isAnyTerm(Item item)     { return item.equals(TERM) ; }
    public static boolean isAnyURI(Item item)      { return item.equals(URI) ; }
    public static boolean isAnyLiteral(Item item)  { return item.equals(LITERAL) ; }
    public static boolean isAnyBNode(Item item)    { return item.equals(BNODE) ; }
    public static boolean isAnyVar(Item item)      { return item.equals(VAR) ; }
    public static boolean isVar(Item item)         { return Var.isVar(item.getNode()) ; }
    

}
