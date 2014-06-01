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

package org.apache.jena.riot.adapters;

import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;

public class RDFReaderRIOT_CSV implements RDFReader{

	private RDFReader reader ;
	public RDFReaderRIOT_CSV(){
		reader = new RDFReaderRIOT("CSV");
	}

	@Override
	public void read(Model model, Reader r, String base) {
		reader.read(model, r, base);
		
	}

	@Override
	public void read(Model model, InputStream r, String base) {
		reader.read(model, r, base);
		
	}

	@Override
	public void read(Model model, String url) {
		reader.read(model, url);
		
	}

	@Override
	public Object setProperty(String propName, Object propValue) {
		return reader.setProperty(propName, propValue);
	}

	@Override
	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
		return reader.setErrorHandler(errHandler);
	}
}
