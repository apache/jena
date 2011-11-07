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

package com.hp.hpl.jena.db.impl;

/**
 * Enumerations of the supported databases
 */
public enum DBType {	
	DERBY("Derby"),
	HSQL("HSQL"),
	HSQLDB("HSQLDB"),
	MSSQL("MsSQL"),
	MYSQL("MySQL"),
	ORACLE_LOB("Oracle_LOB"),
	ORACLE("Oracle"),
	POSTGRESQL("PostgreSQL");
	
	private String name;
	private String driverClassName = null;
	
	/**
	 * Replies the DB type for a given name. 
	 * <p>
	 * {@code name} is matched case insensitive with the names of the available DB types. Replies
	 * null if {@code name} is null or if no DB type exists for the supplied name.
	 * 
	 * @param name the name
	 * @return the DB type or null
	 */
	static public DBType fromName(String name){
		if (name == null) return null;
		name = name.trim();
		for(DBType type: values()) {
			if (type.name.equalsIgnoreCase(name)) return type;
		}
		return null;
	}
	
	DBType(String name) {
		this.name = name;
	}
	
	DBType(String name, String driverClassName) {
		this.name = name;
		this.driverClassName = driverClassName;
	}
	
	public String getDisplayName() {
		return name;
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
	
	/**
	 * Replies the driver class name for this database type
	 * @return the class name
	 * @see IRDBDriver
	 */
	public String getDriverClassName(){
		if (driverClassName != null) return driverClassName;
		return "com.hp.hpl.jena.db.impl.Driver_" + name;		
	}
	
	/**
	 * Replies the names of the supported databases (comma separated list, as string)
	 * 
	 * @return the supported databases
	 */
	static public String getSupportedTypesAsString() {
		StringBuilder sb = new StringBuilder();
		DBType[] types  =values();
		for (int i=0; i< types.length;i++) {			
			sb.append(types[i]);
			if (i < types.length-1) sb.append(",");			
		}
		return sb.toString();
	}
}
