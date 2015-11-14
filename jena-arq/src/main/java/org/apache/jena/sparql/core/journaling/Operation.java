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

package org.apache.jena.sparql.core.journaling;

/**
 * An encapsulation of some operation against a service of type <code>Upon</code> around a datum of type
 * <code>DataType</code>.
 *
 * @param <DataType> the type of data encapsulated
 * @param <ServiceType> the type of service upon which this operation acts
 */
public interface Operation<DataType, ServiceType> {

	/**
	 * @return the data encapsulated in this operation
	 */
	DataType data();

	/**
	 * Execute this operation against a given service
	 *
	 * @param service the service against which to execute
	 */
	void actOn(ServiceType service);

	/**
	 * An invertible {@link Operation}.
	 *
	 * @param <DataType> the type of data encapsulated
	 * @param <ServiceType> the type of service upon which this operation acts
	 * @param <SelfType> this type
	 * @param <InverseType> the type of the inverse operation
	 */
	public static interface InvertibleOperation<DataType, ServiceType, SelfType extends InvertibleOperation<DataType, ServiceType, SelfType, InverseType>, InverseType extends InvertibleOperation<DataType, ServiceType, InverseType, SelfType>>
			extends Operation<DataType, ServiceType> {

		/**
		 * Creates an inverse operation for this data.
		 *
		 * @return the inverse of this operation on the same data and against the same type of service.
		 */
		InverseType inverse();
	}
}
