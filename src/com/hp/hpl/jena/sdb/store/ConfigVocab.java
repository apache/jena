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

package com.hp.hpl.jena.sdb.store;

//import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.util.Vocab;


public class ConfigVocab
{
    public final static Resource typeConfig             = Vocab.resource(SDB.namespace, "Config") ;
//    public final static Property featureProperty        = Vocab.property(SDB.namespace, "feature") ;
//    public final static Property featureNameProperty    = Vocab.property(SDB.namespace, "name") ;
//    public final static Property featureValueProperty   = Vocab.property(SDB.namespace, "value") ;
}
