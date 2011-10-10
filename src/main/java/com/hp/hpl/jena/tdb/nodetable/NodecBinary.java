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

package com.hp.hpl.jena.tdb.nodetable;

import java.nio.ByteBuffer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.PrefixMapping;

public class NodecBinary implements Nodec
{
    private static byte codeNull        = 0 ;
    private static byte codeURI         = 1 ;
    private static byte codeBNode       = 2 ;
    private static byte codeLiteral     = 3 ;
    private static byte codeLiteralLang = 4 ;
    private static byte codeLiteralDT   = 5 ;
    
    @Override
    public int maxSize(Node node)
    {
        return 0 ;
    }
    
    @Override
    public int encode(Node node, ByteBuffer bb, PrefixMapping pmap)
    {
        return 0 ;
    }

    @Override
    public Node decode(ByteBuffer bb, PrefixMapping pmap)
    {
        return null ;
    }
    
    private void encode(String s, ByteBuffer bb, int idx)
    {
    }
}
