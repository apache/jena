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

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/** An implementation of Bag
 */
public class BagImpl extends ContainerImpl implements Bag {
    
    @SuppressWarnings("hiding")
    final static public Implementation factory = new Implementation() {
        @Override
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        @Override
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new BagImpl(n,eg);
        }
    };
        
    /** Creates new BagMem */
    public BagImpl( ModelCom model )  {
        super(model);
    }
    
    public BagImpl( String uri, ModelCom model )  {
        super(uri, model);
    }
    
    public BagImpl( Resource r, ModelCom m )  {
        super( r, m );
    }
    
    public BagImpl( Node n, EnhGraph g ) {
        super(n,g);
    }
    

}
