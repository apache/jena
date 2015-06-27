/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.permissions.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.rdf.model.RDFList ;
import org.apache.jena.vocabulary.RDF ;

public class RDFListIterator implements Iterator<RDFList>
{
	private RDFList current;
	private Boolean found;

	public RDFListIterator( final RDFList start )
	{
		this.current = start;
	}

	private boolean endOfList()
	{
		return current.equals(RDF.nil);
	}

	@Override
	public boolean hasNext()
	{
		if ((found == null) && !endOfList())
		{
			found = !endOfList();
		}
		return found == null ? false : found;
	}

	private void incrementCurrent()
	{
		if (!endOfList())
		{
			current = current.getRequiredProperty(RDF.rest).getResource()
					.as(RDFList.class);
		}
	}

	@Override
	public RDFList next()
	{
		if (hasNext())
		{
			found = null;
			final RDFList retval = current;
			incrementCurrent();
			return retval;
		}
		throw new NoSuchElementException();
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

}
