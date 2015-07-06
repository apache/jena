/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package org.apache.jena.testing_framework;

import org.xenei.junit.contract.IProducer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * An abstract implementation of the IProducer<RDFNode> interface.
 * 
 * This class handles tracking of the created graphs and closing them. It also
 * provides a callback for the implementing class to perform extra cleanup when
 * the graph is closed.
 * 
 */
public interface INodeProducer<T extends RDFNode> extends IProducer<T> {

	abstract public T newInstance(String uri);

	abstract public Model getModel();

}
