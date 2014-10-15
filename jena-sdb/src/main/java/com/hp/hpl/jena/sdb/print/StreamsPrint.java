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

package com.hp.hpl.jena.sdb.print;

import static org.apache.jena.atlas.iterator.Iter.apply ;
import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.atlas.iterator.ActionPrint ;


public class StreamsPrint
{
    // ---- Common operations 
    
    public static <T extends Printable> String printString(Iterable<? extends T> struct)
    {
        return printString(struct, " ") ;
    }
    
    public static <T extends Printable> String printString(Iterable<? extends T> struct, String sep)
    {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        apply(struct, new ActionPrint<T>(b, sep)) ;
        return b.asString() ; 
    }
    
    public static <T extends Printable> void print(IndentedWriter out, Iterable<? extends T> struct)
    {
        apply(struct, new ActionPrint<T>(out)) ;
    }

    public static <T extends Printable> void print(IndentedWriter out, 
                                                   Iterable<? extends T> struct,
                                                   String sep)
    {
        apply(struct, new ActionPrint<T>(out, sep)) ;
    }

}
