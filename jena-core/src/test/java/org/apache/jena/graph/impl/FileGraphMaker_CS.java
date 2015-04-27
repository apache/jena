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

package org.apache.jena.graph.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.xenei.junit.contract.Contract;
import org.xenei.junit.contract.ContractImpl;
import org.xenei.junit.contract.ContractSuite;
import org.xenei.junit.contract.IProducer;

@RunWith(ContractSuite.class)
@ContractImpl(FileGraphMaker.class)
public class FileGraphMaker_CS {
	 
	protected IProducer<FileGraphMaker> graphProducer;
	
	public FileGraphMaker_CS() throws IOException {
		graphProducer = new FileGraphMakerProducer();
	}

	@Contract.Inject
	public final IProducer<FileGraphMaker> getCollectionTestProducer() {
		return graphProducer;
	}
	
	private static class FileGraphMakerProducer implements IProducer<FileGraphMaker> {
		private Map<Path,FileGraphMaker> map;
		
		public FileGraphMakerProducer () throws IOException {
			map = new HashMap<Path,FileGraphMaker>();
		}
		
		
		@Override
		public FileGraphMaker newInstance() {
			Path p;
			try {
				p = Files.createTempDirectory("fgm_CS");
			} catch (IOException e) {
				throw new RuntimeException( "Unable to create temp directory", e );
			}
			FileGraphMaker fgm = new FileGraphMaker( p.toString(), true);
			map.put(p, fgm);
			return fgm;
		}

		@Override
		public void cleanUp() {
			for (Path p : map.keySet())
			{
				map.get(p).close();
				p.toFile().delete();
			}
			map.clear();
		}

	};

}
