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

package com.hp.hpl.jena.sparql.modify.request;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.out.SinkQuadBracedOutput ;

import com.hp.hpl.jena.sparql.serializer.SerializationContext;

public class UpdateDataWriter extends SinkQuadBracedOutput
{
    /**
     * The mode an UpdateDataWriter is in.
     */
    public enum UpdateMode
    {
        INSERT,
        DELETE,
    }
    
    private final UpdateMode mode;
    
    public UpdateDataWriter(UpdateMode mode, IndentedWriter out, SerializationContext sCxt)
    {
        super(out, sCxt);
        this.mode = mode;
    }
    
    public UpdateMode getMode()
    {
        return mode;
    }
    
    @Override
    public void open()
    {
        out.ensureStartOfLine();
        out.print(mode.toString());
        out.print(" DATA ");
        super.open();
    }
}
