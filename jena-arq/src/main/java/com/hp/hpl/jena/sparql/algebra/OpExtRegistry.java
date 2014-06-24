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

package com.hp.hpl.jena.sparql.algebra;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.op.OpExt ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderOp ;

/** Manage extension algebra operations */
public class OpExtRegistry
{
    // Known extensions.
    static Map<String, OpExtBuilder> extensions = new HashMap<>() ;
    
    // Wire in (ext NAME ...) form
    static { BuilderOp.add(Tags.tagExt, new BuildExtExt()) ; }
    
    public static void register(OpExtBuilder builder)
    {
        extensions.put(builder.getTagName(), builder) ;

        if ( BuilderOp.contains(builder.getTagName()) )
            throw new ARQException("Tag '"+builder.getTagName()+"' already defined") ;
        BuilderOp.add(builder.getTagName(), new BuildExt2()) ;
    }
    
    
    public static void unregister(String subtag)
    {
        extensions.remove(subtag) ;
    }
    
    public static OpExtBuilder builder(String tag) { return extensions.get(tag) ; }

    public static Op buildExt(String tag, ItemList args)
    {
        OpExtBuilder b = builder(tag) ;
        OpExt ext = b.make(args) ;  // Arguments 2 onwards
        return ext ;
    }
    
    // (ext NAME ...) form
    static public class BuildExtExt implements BuilderOp.Build 
    { 
        @Override
        public Op make(ItemList list)
        {
            // 0 is the "ext"
            String subtag = list.get(1).getSymbol() ;
            list = list.sublist(2) ;
            return buildExt(subtag, list) ; 
        }
    }
    
    // (NAME ...) form
    static public class BuildExt2 implements BuilderOp.Build 
    { 
        @Override
        public Op make(ItemList list)
        {
            String subtag = list.get(0).getSymbol() ;
            list = list.sublist(1) ;
            return buildExt(subtag, list) ; 
        }
    }
    
    
}
