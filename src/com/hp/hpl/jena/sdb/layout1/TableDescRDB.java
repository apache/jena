/**
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

package com.hp.hpl.jena.sdb.layout1;

import com.hp.hpl.jena.sdb.layout2.TableDescTriples;

public class TableDescRDB extends TableDescTriples
{
    private static String tableName = "jena_g1t1_stmt" ;
    public static String name() { return tableName ; } 
    
    public TableDescRDB()
    { super(tableName, "Subj", "Prop", "Obj") ; }
    
    @Override
    public String getGraphColName()     { return "GraphID" ; }
}
