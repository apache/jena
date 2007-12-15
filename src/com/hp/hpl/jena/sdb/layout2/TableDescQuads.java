/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import com.hp.hpl.jena.sdb.store.TableDesc;

/**
 * @author Andy Seaborne
 */

public class TableDescQuads extends TableDesc
{
    protected static final String graphCol      = "g" ;
    protected static final String subjectCol    = "s" ;
    protected static final String predicateCol  = "p" ;
    protected static final String objectCol     = "o" ;
    private static final String tableName     = "Quads" ;
    
    private final String _graphCol ;
    private final String _subjectCol;
    private final String _predicateCol ;
    private final String _objectCol ;
    private final String _tableName ;
    
    public static String name() { return tableName ; }

    public TableDescQuads()
    { this(tableName, graphCol, subjectCol, predicateCol, objectCol) ; }

    protected TableDescQuads(String tName, String gCol, String sCol, String pCol, String oCol)
    { 
        super(tName, gCol, sCol, pCol, oCol) ;
        _tableName = tName ;
        _graphCol = gCol ;
        _subjectCol = sCol ;
        _predicateCol = pCol ;
        _objectCol = oCol ;
    }
    
    public String getGraphColName()     { return _graphCol ; }
    public String getSubjectColName()   { return _subjectCol ; }
    public String getPredicateColName() { return _predicateCol ; }
    public String getObjectColName()    { return _objectCol ; }
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