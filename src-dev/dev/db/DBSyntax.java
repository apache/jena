/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.db;

import static java.lang.String.format ;
import static com.hp.hpl.jena.sdb.util.StrUtils.strjoin ;

import java.util.List;

/** Database column descriptions.  This calls contains the SQL (or common) standard forms,
 *  to be overrided by each database type.
 *  
 * @author Andy Seaborne
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

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */