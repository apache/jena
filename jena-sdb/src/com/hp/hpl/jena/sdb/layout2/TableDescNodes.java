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

package com.hp.hpl.jena.sdb.layout2;

public abstract class TableDescNodes
{
    // This is not a TableDesc - that only describes tables all of whose columns are Nodes.
    // TODO The formatters know the column names as well - refactor
    
    // This table is different in Hash and Index versions
    
    protected static final String tableName        = "Nodes" ;
    public static String name()                 { return tableName ; } 
    
    protected static final String colId            = "id" ;
    protected static final String colHash          = "hash" ;
    protected static final String colLex           = "lex" ;
    protected static final String colLang          = "lang" ;
    protected static final String colDatatype      = "datatype" ;
    protected static final String colType          = "type" ;

    public static final int DatatypeUriLength           = 200 ;
    
    public TableDescNodes() {}
    
    public String getTableName()            { return tableName ; }
    
    // Details of the column that indexes nodes. 
    // The SQL type of the primary key column is the column type for all
    // tuple tables including triples and quads.
    
    public abstract String getNodeRefTypeString() ;         // As a string.
    public abstract int getNodeRefTypeNum() ;               // As the java.sql.type number
    // The name of the column which is the primary key of the node table. 
    public abstract String getNodeRefColName() ;
    
    // The name of the id column (may be null if there isn't one) 
    public abstract String getIdColName() ;

    public String getHashColName()          { return colHash ; }
    public String getLexColName()           { return colLex ; }
    public String getLangColName()          { return colLang ; }
    public String getTypeColName()          { return colType ; }
    public String getDatatypeColName()      { return colDatatype ; }
}
