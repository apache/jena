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

package com.hp.hpl.jena.sdb.sql;

import com.hp.hpl.jena.sdb.SDBException;

public enum MySQLEngineType
{
    MyISAM    { @Override public String getEngineName() { return "MyISAM" ; } } ,
    Maria     { @Override public String getEngineName() { return "Maria" ; } } ,
    InnoDB    { @Override public String getEngineName() { return "InnoDB" ; } } ,
    Falcon    { @Override public String getEngineName() { return "Falcon" ; } } ,
    Memory    { @Override public String getEngineName() { return "MEMORY" ; } } ,
    BDB       { @Override public String getEngineName() { return "BDB" ; } } ,
    NDB       { @Override public String getEngineName() { return "NDB" ; } } ,
    ;
    abstract public String getEngineName() ;
    
    public static MySQLEngineType convert(String engineName)
    {
        if ( check(engineName, MyISAM) )  return MyISAM ;
        if ( check(engineName, Maria) )   return Maria ;
        if ( check(engineName, InnoDB) )  return InnoDB ;
        if ( check(engineName, Falcon) )  return Falcon ;
        if ( check(engineName, Memory) )  return Memory ;
        if ( check(engineName, BDB) )     return BDB ;
        if ( check(engineName, NDB) )     return NDB ;
        throw new SDBException("Can't turn '"+engineName+"' into an engine type") ; 
    }
    
    private static boolean check(String engineName, MySQLEngineType t)
    { return  engineName.equalsIgnoreCase(t.getEngineName()) ; }
}
