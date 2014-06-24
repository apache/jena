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

package com.hp.hpl.jena.rdf.model.test.helpers;

import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
/**
 * Class to listen to model changes and record them for testing.
 */
public class RecordingModelListener implements ModelChangedListener
{
	List<Object> history = new ArrayList<>();

	@Override
	public void addedStatement( final Statement s )
	{
		record("add", s);
	}

	@Override
	public void addedStatements( final List<Statement> statements )
	{
		record("addList", statements);
	}

	@Override
	public void addedStatements( final Model m )
	{
		record("addModel", m);
	}

	@Override
	public void addedStatements( final Statement[] statements )
	{
		record("add[]", Arrays.asList(statements));
	}

	@Override
	public void addedStatements( final StmtIterator statements )
	{
		record("addIterator", GraphTestBase.iteratorToList(statements));
	}

	public void assertHas( final List<?> things )
	{
		if (has(things) == false)
		{
			Assert.fail("expected " + things + " but got " + history);
		}
	}

	public void assertHas( final Object[] things )
	{
		if (has(things) == false)
		{
			Assert.fail("expected " + Arrays.asList(things) + " but got "
					+ history);
		}
	}

	public void assertHasEnd( final Object[] end )
	{
		final List<Object> L = Arrays.asList(end);
		if (hasEnd(L) == false)
		{
			Assert.fail("expected " + L + " at the end of " + history);
		}
	}

	public void assertHasStart( final Object[] start )
	{
		final List<Object> L = Arrays.asList(start);
		if (hasStart(L) == false)
		{
			Assert.fail("expected " + L + " at the beginning of " + history);
		}
	}

	@SuppressWarnings("unchecked")
    public static boolean checkEquality( final Object o1, final Object o2)
	{
		if (o1 == o2)
		{
			return true;
		}
		if (o1.getClass().isArray() && o2.getClass().isArray())
		{
			final Object[] o1a = (Object[])o1;
			final Object[] o2a = (Object[])o2;

			if (o1a.length == o2a.length)
			{
				for (int i=0;i<o1a.length;i++)
				{
					if (!checkEquality( o1a[i], o2a[i]))
					{
						return false;
					}
				}
				return true;
			}
			return false;
		}
		else if ( (o1 instanceof Collection<?>) && (o2 instanceof Collection<?>) )
		{
			return checkEquality( ((Collection<Object>)o1).toArray(), ((Collection<Object>)o2).toArray() );

		}
		else if ((o1 instanceof Model) && (o2 instanceof Model))
		{
			return checkEquality( ((Model)o1).listStatements().toList(), ((Model)o2).listStatements().toList());

		}
		else if ((o1 instanceof Statement) && (o2 instanceof Statement))
		{
			return checkEquality( ((Statement)o1).asTriple(), ((Statement)o2).asTriple());

		}
		else
		{
			return o1.equals(o2);
		}
	}

	public void clear()
	{
		history.clear();
	}

	public boolean has( final List<?> things )
	{
		return has(things.toArray());
	}

	public boolean has( final Object[] things )
	{
		return checkEquality(history.toArray(), things);
	}

	public boolean hasEnd( final List<Object> L )
	{
		return (L.size() <= history.size())
				&& checkEquality(L, history.subList(history.size() - L.size(),
						history.size()));
	}

	public boolean hasStart( final List<Object> L )
	{
		return (L.size() <= history.size())
				&& checkEquality(L, history.subList(0, L.size()));
	}

	@Override
	public void notifyEvent( final Model m, final Object event )
	{
		record("someEvent", m, event);
	}

	protected void record( final String tag, final Object info )
	{
		history.add(tag);
		history.add(info);
	}

	protected void record( final String tag, final Object x, final Object y )
	{
		history.add(tag);
		history.add(x);
		history.add(y);
	}

	@Override
	public void removedStatement( final Statement s )
	{
		record("remove", s);
	}

	@Override
	public void removedStatements( final List<Statement> statements )
	{
		record("removeList", statements);
	}

	@Override
	public void removedStatements( final Model m )
	{
		record("removeModel", m);
	}

	@Override
	public void removedStatements( final Statement[] statements )
	{
		record("remove[]", Arrays.asList(statements));
	}

	@Override
	public void removedStatements( final StmtIterator statements )
	{
		record("removeIterator", GraphTestBase.iteratorToList(statements));
	}

}
