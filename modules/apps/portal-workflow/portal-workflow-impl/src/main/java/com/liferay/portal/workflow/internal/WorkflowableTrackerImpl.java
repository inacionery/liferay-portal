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

package com.liferay.portal.workflow.internal;

import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.workflow.Workflowable;
import com.liferay.portal.workflow.WorkflowableTracker;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = WorkflowableTracker.class)
public class WorkflowableTrackerImpl implements WorkflowableTracker {

	@Override
	public List<Workflowable<?>> getScopeableWorkflowables() {
		return ListUtil.fromMapValues(_scopeableWorkflowableMap);
	}

	@Override
	public <T> Workflowable<T> getWorkflowable(String className) {
		return (Workflowable<T>)_workflowableMap.get(className);
	}

	@Override
	public List<Workflowable<?>> getWorkflowables() {
		return ListUtil.fromMapValues(_workflowableMap);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_indexerServiceTracker = new ServiceTracker<>(
			bundleContext, Indexer.class.getName(),
			new ServiceTrackerCustomizer<Workflowable<?>, Workflowable<?>>() {

				@Override
				public Workflowable<?> addingService(
					ServiceReference<Workflowable<?>> serviceReference) {

					Workflowable<?> workflowable = bundleContext.getService(
						serviceReference);

					_workflowableMap.put(
						workflowable.getClassName(), workflowable);

					if (workflowable.isScopeable()) {
						_scopeableWorkflowableMap.put(
							workflowable.getClassName(), workflowable);
					}

					return workflowable;
				}

				@Override
				public void modifiedService(
					ServiceReference<Workflowable<?>> serviceReference,
					Workflowable<?> workflowable) {
				}

				@Override
				public void removedService(
					ServiceReference<Workflowable<?>> serviceReference,
					Workflowable<?> workflowable) {

					_workflowableMap.remove(workflowable.getClassName());

					if (workflowable.isScopeable()) {
						_scopeableWorkflowableMap.remove(
							workflowable.getClassName());
					}

					bundleContext.ungetService(serviceReference);
				}

			});
	}

	@Deactivate
	protected void deactivate() {
		_indexerServiceTracker.close();
	}

	private static final Map<String, Workflowable<?>>
		_scopeableWorkflowableMap = new ConcurrentSkipListMap<>();
	private static final Map<String, Workflowable<?>> _workflowableMap =
		new TreeMap<>();

	private ServiceTracker<Workflowable<?>, Workflowable<?>>
		_indexerServiceTracker;

}