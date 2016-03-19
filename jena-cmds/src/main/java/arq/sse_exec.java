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

package arq;

import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.sparql.sse.Item ;
import org.apache.jena.sparql.sse.builders.BuilderExec ;
import arq.cmdline.CmdARQ_SSE ;

public class sse_exec extends CmdARQ_SSE
{
    
    public static void main (String... argv)
    {
        new sse_exec(argv).mainRun() ;
    }
    
    public sse_exec(String[] argv)
    {
        super(argv) ;
    }
    
    @Override
    protected String getCommandName() { return Lib.className(this) ; }
    
    @Override
    protected String getSummary() { return getCommandName()+" [--file<file> | string]" ; }

    @Override
    protected void exec(Item item)
    {
        BuilderExec.exec(item) ;
    }
}
