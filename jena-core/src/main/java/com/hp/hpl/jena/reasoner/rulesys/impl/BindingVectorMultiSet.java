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

package com.hp.hpl.jena.reasoner.rulesys.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;

/**
 * A multi set of BindingVector's divided in buckets matching an unique
 * combination of values at given indices managed by RETEQueue
 */
public class BindingVectorMultiSet {

	/**
	 * Inner class used to represent an updatable count.
	 * Formerly enclosed in RETEQueue
	 */
	protected static class Count {
		/** the count */
		int count;

		/** Constructor */
		public Count(int count) {
			this.count = count;
		}

		/** Decrement the count value */
		public void dec() {
			count--;
		}

		/** Access count value */
		public int getCount() {
			return count;
		}

		/** Increment the count value */
		public void inc() {
			count++;
		}

		/** Set the count value */
		public void setCount(int count) {
			this.count = count;
		}
	}

	/** Inner representation */
	protected Map<BindingVector, Map<BindingVector, Count>> data = new HashMap<>();

	/** An array of indices which mark the primary key */
	protected byte[] matchIndices;

	/**
	 * Constructor
	 * 
	 * @param matchIndices
	 *            a set of indices for matching
	 */
	public BindingVectorMultiSet(byte[] matchIndices) {
		this.matchIndices = matchIndices;
	}

	/**
	 * Increase the current quantity of env
	 * 
	 * @param env
	 */
	public void add(BindingVector env) {
		Count c = get(env);
		if (c == null) {
			put(env, new Count(1));
		} else {
			c.inc();
		}
	}

	/**
	 * Get current quantity of BindingVector env
	 * 
	 * @param env
	 * @return
	 */
	protected Count get(BindingVector env) {
		Map<BindingVector, Count> set = getRawSubSet(env);
		return (set == null ? null : set.get(env));
	}

	/**
	 * Create a BindingVector containing only values at matchIndices so it can
	 * be used as key
	 * 
	 * @param env
	 *            BindingVector to find the key for
	 * @return the key BindingVector
	 */
	protected BindingVector getPartialEnv(BindingVector env) {
		Node[] envNodes = env.getEnvironment();

		Node[] partialEnv = new Node[envNodes.length];
		for (byte i : matchIndices) {
			partialEnv[i] = envNodes[i];
		}
		return new BindingVector(partialEnv);
	}

	/**
	 * Get the bucket into which env belongs if it exists
	 * 
	 * @param env
	 * @return
	 */
	protected Map<BindingVector, Count> getRawSubSet(BindingVector env) {
		return data.get(getPartialEnv(env));
	}

	/**
	 * Get an iterator over all BindingVectors currently present which match
	 * with env
	 * 
	 * @param env
	 */
	public Iterator<BindingVector> getSubSet(BindingVector env) {
		Map<BindingVector, Count> rawSubSet = getRawSubSet(env);
		return (rawSubSet == null ? new HashMap<BindingVector, Count>(0)
				: rawSubSet).keySet().iterator();

	}

	/**
	 * Set the quantity of env to a given Count value c
	 * 
	 * @param env
	 * @param c
	 */
	protected void put(BindingVector env, Count c) {
		Map<BindingVector, Count> set = getRawSubSet(env);
		if (set == null) {
			set = new HashMap<>();
			data.put(getPartialEnv(env), set);
		}
		set.put(env, c);

	}

	/**
	 * Copy all item from queue.data into data.
	 * Assumes this and queue share the same matchIndices.
	 * 
	 * @param queue
	 */
	public void putAll(BindingVectorMultiSet queue) {
        for ( BindingVector env : queue.data.keySet() )
        {
            Map<BindingVector, Count> set = getRawSubSet( env );
            if ( set == null )
            {
                set = new HashMap<>();
                data.put( env, set );
            }
            set.putAll( queue.data.get( env ) );
        }
	}

	/**
	 * Decrease the quantity of env
	 * 
	 * @param env
	 */
	public void remove(BindingVector env) {
		BindingVector key = getPartialEnv(env);
		Map<BindingVector, Count> set = data.get(key);
		if (set != null) {
			Count c = set.get(env);
			if (c != null) {
				if (c.getCount() > 1) {
					c.dec();
				} else { // clean up
					set.remove(env);
				}
			}
			if (set.isEmpty()) {
				data.remove(key);
			}
		}

	}

}
