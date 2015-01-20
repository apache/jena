package org.apache.jena.security.graph;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;

public class RecordingGraphListener implements GraphListener
{

	private boolean add;
	private boolean delete;
	private boolean event;

	public boolean isAdd()
	{
		return add;
	}

	public boolean isDelete()
	{
		return delete;
	}

	public boolean isEvent()
	{
		return event;
	}

	@Override
	public void notifyAddArray( final Graph g, final Triple[] triples )
	{
		add = true;
	}

	@Override
	public void notifyAddGraph( final Graph g, final Graph added )
	{
		add = true;
	}

	@Override
	public void notifyAddIterator( final Graph g, final Iterator<Triple> it )
	{
		add = true;
	}

	@Override
	public void notifyAddList( final Graph g, final List<Triple> triples )
	{
		add = true;
	}

	@Override
	public void notifyAddTriple( final Graph g, final Triple t )
	{
		add = true;
	}

	@Override
	public void notifyDeleteArray( final Graph g, final Triple[] triples )
	{
		delete = true;
	}

	@Override
	public void notifyDeleteGraph( final Graph g, final Graph removed )
	{
		delete = true;
	}

	@Override
	public void notifyDeleteIterator( final Graph g,
			final Iterator<Triple> it )
	{
		delete = true;
	}

	@Override
	public void notifyDeleteList( final Graph g, final List<Triple> L )
	{
		delete = true;
	}

	@Override
	public void notifyDeleteTriple( final Graph g, final Triple t )
	{
		delete = true;
	}

	@Override
	public void notifyEvent( final Graph source, final Object value )
	{
		event = true;
	}

	public void reset()
	{
		add = false;
		delete = false;
		event = false;
	}

}