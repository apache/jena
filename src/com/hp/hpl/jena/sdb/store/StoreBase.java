/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.sql.SQLUtils;


public class StoreBase 
    extends SDBConnectionHolder
    implements Store
{
    protected PlanTranslator planTranslator ;
    protected StoreLoader loader ;
    protected StoreFormatter formatter ;
    protected QueryCompiler compiler ;
    protected SQLGenerator sqlGenerator ;
    protected StoreCustomizer customizer ;
    protected StoreConfig configuration ;
    
    public StoreBase(SDBConnection connection, 
                     PlanTranslator planTranslator,
                     StoreLoader loader ,
                     StoreFormatter formatter ,
                     QueryCompiler compiler ,
                     SQLGenerator sqlGenerator ,
                     StoreCustomizer customizer)
    {
        super(connection) ;
        this.loader = loader ;
        this.formatter = formatter ;
        this.compiler = compiler ;
        this.planTranslator = planTranslator ;
        if ( sqlGenerator == null )
            sqlGenerator = new GenerateSQL() ;
        this.sqlGenerator = sqlGenerator ;
        if ( customizer == null )
            customizer = new StoreCustomizerBase() ;
        setCustomizer(customizer) ;
        configuration = new StoreConfig(connection()) ;
    }
    
    public SDBConnection   getConnection()           {  return connection() ; }
    
    public PlanTranslator  getPlanTranslator()       { return planTranslator ; }
    
    public QueryCompiler   getQueryCompiler()        { return compiler ; }

    public SQLGenerator    getSQLGenerator()         { return sqlGenerator ; }

    public StoreFormatter  getTableFormatter()       { return formatter ; }

    public StoreLoader     getLoader()               { return loader ; }

    public StoreConfig     getConfiguration()        { return configuration ; }

    // Note -- this does not close the JDBC connection, which may be shared.
    // See also StoreBaseHSQL
    public void close()                              { }

    
    public StoreCustomizer getCustomizer()           { return customizer ; }
    public void setCustomizer(StoreCustomizer customizer)       { this.customizer = customizer ; }
    
    /** Default implementation: get size of Triples table **/
    public int getSize()
    {
    	return SQLUtils.getTableSize(getConnection().getSqlConnection(), "Triples");
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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