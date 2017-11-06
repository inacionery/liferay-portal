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

package com.liferay.portal.workflow.analytics.internal;

import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMap;
import com.liferay.osgi.service.tracker.collections.map.ServiceTrackerMapFactory;
import com.liferay.portal.workflow.analytics.WorkflowAnalytics;
import com.liferay.portal.workflow.analytics.WorkflowAnalyticsTracker;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Inácio Nery
 */
@Component(immediate = true)
public class WorkflowAnalyticsTrackerImpl implements WorkflowAnalyticsTracker {

	public WorkflowAnalytics getMetric(String metric) {
		return _workflowAnalyticsTrackerMap.getService(metric);
	}

	@Activate
	protected void activate(BundleContext bundleContext) {
		_workflowAnalyticsTrackerMap =
			ServiceTrackerMapFactory.openSingleValueMap(
				bundleContext, WorkflowAnalytics.class,
				"workflow.analytics.metric");
	}

	@Deactivate
	protected void deactivate() {
		_workflowAnalyticsTrackerMap.close();
	}

	private ServiceTrackerMap<String, WorkflowAnalytics>
		_workflowAnalyticsTrackerMap;

}