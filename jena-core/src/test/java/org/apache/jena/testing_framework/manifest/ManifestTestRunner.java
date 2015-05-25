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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class ManifestTestRunner extends BlockJUnit4ClassRunner {

	private ManifestItem manifestItem;

	public ManifestTestRunner(ManifestItem manifestItem,
			Class<? extends ManifestTest> cls) throws InitializationError {
		super(cls);
		this.manifestItem = manifestItem;
	}

	/**
	 * Returns the name that describes {@code method} for {@link Description}s.
	 * Default implementation is the method's name
	 */
	@Override
	protected String testName(FrameworkMethod method) {
		return manifestItem.getTestName();
	}

	/**
	 * Returns the methods that run tests. Default implementation returns all
	 * methods annotated with {@code @Test} on this class and superclasses that
	 * are not overridden.
	 */
	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		FrameworkMethod[] lst = new FrameworkMethod[1];

		try {
			lst[0] = new FrameworkMethod(getTestClass().getJavaClass()
					.getMethod("runTest")) {

				@Override
				public String getName() {
					return manifestItem.getTestName();
				}
			};
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException(e);
		} catch (SecurityException e) {
			throw new IllegalStateException(e);
		}
		return Arrays.asList(lst);
	}

	@Override
	public Description getDescription() {
		return Description.createTestDescription(this.getTestClass()
				.getJavaClass(), manifestItem.getTestName(), new Annotation[0]);
	}

	/**
	 * Returns a new fixture for running a test. Default implementation executes
	 * the test class's no-argument constructor (validation should have ensured
	 * one exists).
	 */
	@Override
	protected Object createTest() throws Exception {
		ManifestTest instance = (ManifestTest) super.createTest();
		instance.setManifestItem(manifestItem);
		return instance;
	}

}
