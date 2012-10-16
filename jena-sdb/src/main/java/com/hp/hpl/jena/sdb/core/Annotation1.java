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

package com.hp.hpl.jena.sdb.core;

import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;

/** Collect some notes into a single annoation.
 */
public class Annotation1
{
    private StringBuilder annotation = new StringBuilder() ;
    private String separator ;

    public Annotation1(String separator)
    { this.separator = separator ; }

    public Annotation1()
    { this(" ") ; }
        
    public Annotation1(boolean withCommas)
    { this(withCommas? ", " : " ") ; }
    
    public void addAnnotation(String note)
    {
        if ( annotation.length() > 0 )
            annotation.append(separator) ;
        annotation.append(note) ;
    }
    
    public void setAnnotation(SqlNode node)
    {
        if ( annotation.length() > 0 )
            node.addNote(annotation.toString()) ;
    }
    

}
