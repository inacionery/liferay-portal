/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.kaleo.runtime.integration.impl.internal.test;

import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.proxy.ProxyModeThreadLocal;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.callback.BaseTestCallback;
import com.liferay.portal.kernel.test.rule.callback.SynchronousDestinationTestCallback.SyncHandler;
import com.liferay.portal.workflow.kaleo.runtime.constants.KaleoRuntimeDestinationNames;
import com.liferay.registry.Filter;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;
import com.liferay.registry.dependency.ServiceDependencyManager;

import org.junit.runner.Description;

/**
 * @author Inácio Nery
 */
public class GraphWalkerTestCallback
	extends BaseTestCallback<SyncHandler, SyncHandler> {

	public static final GraphWalkerTestCallback INSTANCE =
		new GraphWalkerTestCallback();

	@Override
	public SyncHandler beforeClass(Description description) {
		Class<?> testClass = description.getTestClass();

		Sync sync = testClass.getAnnotation(Sync.class);

		if (sync != null) {
			return _createSyncHandler(sync);
		}

		return null;
	}

	private GraphWalkerTestCallback() {
	}

	private SyncHandler _createSyncHandler(Sync sync) {
		SyncHandler syncHandler = new SyncHandler();

		syncHandler.setForceSync(ProxyModeThreadLocal.isForceSync());
		syncHandler.setSync(sync);

		syncHandler.enableSync();

		ServiceDependencyManager serviceDependencyManager =
			new ServiceDependencyManager();

		Filter kaleoGraphWalkerFilter = _registerDestinationFilter(
			KaleoRuntimeDestinationNames.KALEO_GRAPH_WALKER);

		serviceDependencyManager.registerDependencies(kaleoGraphWalkerFilter);

		serviceDependencyManager.waitForDependencies();

		ProxyModeThreadLocal.setForceSync(true);

		syncHandler.replaceDestination(
			KaleoRuntimeDestinationNames.KALEO_GRAPH_WALKER);

		return syncHandler;
	}

	private Filter _registerDestinationFilter(String destinationName) {
		Registry registry = RegistryUtil.getRegistry();

		return registry.getFilter(
			"(&(destination.name=" + destinationName + ")(objectClass=" +
				Destination.class.getName() + "))");
	}

}