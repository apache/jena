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

package com.hp.hpl.jena.n3.turtle;

import com.hp.hpl.jena.graph.Triple;


public class TurtleEventDump implements TurtleEventHandler
{
    
    @Override
    public void triple(int line, int col, Triple triple)
    {
        System.out.print(mark(line, col)) ;
        System.out.print(" ") ;
        System.out.println(triple.toString()) ;
    }
    
    @Override
    public void startFormula(int line, int col)
    {
        System.out.print(mark(line, col)) ;
        System.out.println("{") ;
    }
    
    @Override
    public void endFormula(int line, int col)
    {
        System.out.print(mark(line, col)) ;
        System.out.println("}") ;
    }

    private String mark(int line, int col) { return "["+line+", "+col+"]" ; }

    @Override
    public void prefix(int line, int col, String prefix, String iri)
    { 
        System.out.print(mark(line, col)) ;
        System.out.print(" @prefix ") ;
        System.out.println(prefix+": => "+iri) ;
    }
}
