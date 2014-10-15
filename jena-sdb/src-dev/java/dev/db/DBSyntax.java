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

package dev.db;

import static java.lang.String.format ;
import static org.apache.jena.atlas.lib.StrUtils.strjoin ;

import java.util.List;

/** Database column descriptions.  This calls contains the SQL (or common) standard forms,
 *  to be overrided by each database type.
 */ 
public class DBSyntax
{
    // -----------------

    // Column type names.
    public String integer32()           { return "int" ; }
    public String integer64()           { return "bigint" ; }
    
    // national character set (UTF-8) 
    
    public String varchar(int n)        { return varchar(Integer.toString(n)) ; }
    
    public String varchar(String str)   { return "nvarchar("+str+")" ; }
    
//    public String varchar(String str, String dftValue)
//    { return  defaultValue(varchar(str), SQLUtils.quoteStr(dftValue)) ; }
    
    public String text()                { return "ntext" ; }

    public String primaryKey(List<String> colNames)
    { return format("PRIMARY KEY(%s))", strjoin(", ", colNames)) ; }
    
    public String primaryKey(String... colNames)                
    { return format("PRIMARY KEY(%s))", strjoin(", ", colNames)) ; }

    public String index(String indexName, String tableName, String... colNames)
    {
        return format("CREATE INDEX %s ON %s (%s)",
                      indexName, tableName, strjoin(", ", colNames)) ;
    }
    
    public String dropIndex(String indexName, String tableName)
    {
        return format("DROP INDEX %s", indexName, tableName);
    }
    
    public String truncate(String tableName)
    { return "DELETE FROM "+tableName ; }
    // -----------------
    
    public static String notNull(String x)
    { return allowNull(x, false); }
    
    public static String allowNull(String x, boolean nullable)
    {
        if ( ! nullable )
            x = x+" NOT NULL" ;
        return x ;
    }


    public static String defaultValue(String x, String dftValue)
    {
        if ( dftValue != null )
            x = x+" DEFAULT "+dftValue ;
        return x ;
    }

    public static String col(String name, String type, boolean nullable)
    {
        String x = name+" "+type ;
        x = allowNull(x, nullable) ;
        return x ;
        
    }
}
