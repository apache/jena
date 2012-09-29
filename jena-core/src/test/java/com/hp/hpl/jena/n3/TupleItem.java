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

package com.hp.hpl.jena.n3;

/**
 * The unit found in a line of a tuple.
 * Can be a string (quoted, possibly with the datatype, or unquoted) or a URI.
 */
class TupleItem
{
    public static final int URI      = 0 ;
    public static final int STRING   = 1 ;
    public static final int UNKNOWN  = 2 ;
    public static final int UNQUOTED = 3 ;
    public static final int ANON     = 4 ;

    String rep ;
    String datatype ;
    String asFound ;
    int itemType ;

    TupleItem(String value, String valAsFound, int type, String dt)
    {
        rep = value ;
        asFound = valAsFound ;
        itemType = type ;
        datatype = dt ;
    }

    public int getType() { return itemType ; }

    public boolean isURI()       { return itemType == URI ; }
    public boolean isString()    { return itemType == STRING ; }
    public boolean isUnknown()   { return itemType == UNKNOWN ; }
    public boolean isUnquoted()  { return itemType == UNQUOTED ; }
    public boolean isAnon()      { return itemType == ANON ; }

    public String get() { return rep ; }
    public String getDT() { return datatype ;
    }
    public String asQuotedString() { return asFound ; }
    @Override
    public String toString() { return rep ; }
}
