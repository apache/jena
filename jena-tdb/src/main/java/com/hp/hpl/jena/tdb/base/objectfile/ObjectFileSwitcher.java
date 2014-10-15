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

package com.hp.hpl.jena.tdb.base.objectfile;


/** 
 * An ObjectFile is an append-read file, that is you can append data
 * to the stream or read any block.
 */

public class ObjectFileSwitcher extends ObjectFileWrapper
{
    protected final ObjectFile objFile1 ;
    protected final ObjectFile objFile2 ;

    public ObjectFileSwitcher(ObjectFile objFile1, ObjectFile objFile2)
    {
        super(objFile1) ;
        this.objFile1 = objFile1 ;
        this.objFile2 = objFile2 ;
    }
    
    public final void switchover()
    {
        if ( super.other == objFile1 )
            super.other = objFile2 ;
        else
            super.other = objFile1 ;
    }
}
