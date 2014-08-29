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

/*
 * AResource.java
 *
 * Created on June 26, 2001, 9:26 AM
 */

package com.hp.hpl.jena.rdfxml.xmlinput.impl;

import com.hp.hpl.jena.rdfxml.xmlinput.AResource ;

/** A resource from the input file.
 */
public interface AResourceInternal extends AResource, ANode {
    /**
	 * Only for blank nodes - non blank need not support.
	 *
	 */
	 void setHasBeenUsed();
	/**
	 * Only for blank nodes - non blank need not support.
	 *
	 */
	 boolean getHasBeenUsed();
}
