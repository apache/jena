/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

/**
 * @author Andy Seaborne
 * @version $Id: TableNodes.java,v 1.2 2006/04/19 17:23:32 andy_seaborne Exp $
 */

public abstract class TableDescNodes
{
    // This is not a TableDesc - that onlt describes tables all of whose columns are Nodes.
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
    
    public abstract String getKeyColName() ;
    public abstract String getIdColName() ; // Maybe null

    public String getHashColName()          { return colHash ; }
    public String getLexColName()           { return colLex ; }
    public String getLangColName()          { return colLang ; }
    public String getTypeColName()          { return colType ; }
    public String getDatatypeColName()      { return colDatatype ; }
}

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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