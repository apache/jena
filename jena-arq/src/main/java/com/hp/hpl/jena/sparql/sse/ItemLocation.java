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

package com.hp.hpl.jena.sparql.sse;

public class ItemLocation
{
    static final int noLine = -1 ;
    static final int noColumn = -1 ;
    int line = noLine ;
    int column = noColumn ;
    
    String appearance ;
    
    protected ItemLocation(int line, int column)
    {
        this.line = line ;
        this.column = column ;
    }

    public boolean hasLocation()
    { return line != noLine && column != noColumn ; }
    
    public int getColumn()              { return column ; }
    public int getLine()                { return line ; }

    public String location() { return "["+getLine()+","+getColumn()+"]" ; }
    
    @Override
    public String toString() { return location() ; }
}
