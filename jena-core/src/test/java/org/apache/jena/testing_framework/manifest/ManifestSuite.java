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

package org.apache.jena.testing_framework.manifest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.shared.JenaException;

/**
 * Class that runs the Manifest annotated tests.
 * 
 * Used with <code>@RunWith( ManifestSuite.class )</code> this class loads a 
 * manifest test suite file and adds the tests.
 * <p>
 * Tests annotated with <code>@RunWith( ManifestSuite.class )</code> must
 * have a <code>ManifestFile</code> annotation specifying the path to the manifest file
 * </p>
 */
public class ManifestSuite extends ParentRunner<Runner> {
	private static final Logger LOG = LoggerFactory
			.getLogger(ManifestSuite.class);
	private final List<Runner> fRunners;
	private final ManifestItemHandler itemHandler;
	private final ManifestFile mf;

	/**
	 * Called reflectively on classes annotated with
	 * <code>@RunWith(Suite.class)</code>
	 * 
	 * @param cls
	 *            the root class
	 * @param builder
	 *            builds runners for classes in the suite
	 * @throws Throwable
	 */
	public ManifestSuite(Class<? extends ManifestItemHandler> cls,
			RunnerBuilder builder) throws Throwable {
		super(cls);

		List<Throwable> errors = new ArrayList<>();

		mf = cls.getAnnotation(ManifestFile.class);
		if (mf == null) {
			throw new IllegalStateException(
					"ManifestSuite requries ManifestFile annotation");
		}
		itemHandler = cls.getConstructor().newInstance();

		Runner[] runner = new Runner[1];
		try {
			runner[0] = oneManifest(new Manifest(mf.value()),
					new ArrayList<Runner>());
		} catch (JenaException ex) {
			runner[0] = new ErrorReportingRunner(null, ex);
		}

		if (!errors.isEmpty()) {
			throw new InitializationError(errors);
		}
		fRunners = Collections.unmodifiableList(Arrays.asList(runner));
	}

	private Runner oneManifest(final Manifest manifest, List<Runner> r) {

		// Recurse
		for (Iterator<String> iter = manifest.includedManifests(); iter
				.hasNext();) {
			try {
				r.add(oneManifest(new Manifest(iter.next()),
						new ArrayList<Runner>()));
			} catch (JenaException ex) {
				r.add(new ErrorReportingRunner(null, ex));
			}
		}
		itemHandler.setTestRunnerList(r);
		manifest.apply(itemHandler);
		try {
			return new Suite((Class<?>) null, r) {

				@Override
				protected String getName() {
					return manifest.getName();
				}

			};
		} catch (InitializationError e) {
			return new ErrorReportingRunner(null, e);
		}
	}

	@Override
	protected List<Runner> getChildren() {
		return fRunners;
	}

	@Override
	protected Description describeChild(Runner child) {
		return child.getDescription();
	}

	@Override
	protected void runChild(Runner child, RunNotifier notifier) {
		child.run(notifier);
	}

	/**
	 * Returns a name used to describe this Runner
	 */
	@Override
	protected String getName() {
		return String.format("%s - %s", super.getName(), mf.value());
	}
}
