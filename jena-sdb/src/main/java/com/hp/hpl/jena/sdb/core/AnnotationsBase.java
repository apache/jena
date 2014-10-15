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

import java.util.ArrayList;
import java.util.List;

public class AnnotationsBase implements Annotations
{
    //---- Annotations
    List<String> annotations = null ;
    @Override
    public List<String> getNotes()
    {
        initAnnotations() ;
        return annotations ;
    }

    @Override
    public boolean hasNotes()
    { return annotations != null && annotations.size() > 0 ; }
    
    @Override
    public boolean hasOneNote()
    { return annotations != null && annotations.size() == 1 ; }
    
    @Override
    public void addNote(String s)
    {
        initAnnotations() ;
        annotations.add(s) ;
    }
    
    @Override
    public void addNotes(List<String> annotations)
    {
        initAnnotations() ;
        this.annotations.addAll(annotations) ;
    }

    private void initAnnotations()
    {
        if ( annotations == null )
            annotations = new ArrayList<String>() ;
    }
    //---- Annotations
    

}
