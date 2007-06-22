/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TableDesc
{
    private String tableName ;
    private List<String> columnNames = new ArrayList<String>() ;
    
    public TableDesc(String tableName) { this(tableName, (String[])null) ; } 
    
    public TableDesc(String tableName, String... colNames)
    { 
        this.tableName = tableName ;
        if ( colNames != null )
            // Filter nulls.
            for ( int i = 0 ; i < colNames.length ; i++ )
                if ( colNames[i] != null )
                    columnNames.add(colNames[i]) ;
    }

    public TableDesc(String tableName, List<String> colNames)
    {
        this.tableName = tableName ;
        this.columnNames = colNames ;
    }
    
    public String getTableName()
    { return tableName ; }
    
    public boolean hasColumn(String colName)
    { return columnNames.contains(colName) ; }
   
    public List<String> getColNames() { return columnNames ; }
    
    public int getWidth() { return columnNames.size() ; }
    
    public Iterator<String> colNames()
    { return columnNames.iterator() ; } 
    
    @Override
    public String toString() { return tableName ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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