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

import com.hp.hpl.jena.sdb.store.TableDesc;

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
