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

package com.hp.hpl.jena.db;

import java.text.MessageFormat;

import com.hp.hpl.jena.db.impl.DBType;
import com.hp.hpl.jena.shared.JenaException;

public class UnsupportedDatabaseException extends JenaException{
	/**
	 * Creates an exception for an unsupported database type {@code databaseType}.
	 * 
	 * @param databaseType the unsupported database type 
	 * @return the exception
	 */
	public static UnsupportedDatabaseException create(String databaseType) {
		return new UnsupportedDatabaseException(
			MessageFormat.format("Unsupported database type '{0}'. Supported types are: {1}", databaseType, DBType.getSupportedTypesAsString())
		);
	}

	public UnsupportedDatabaseException(String message) {
		super(message);
	}
}
