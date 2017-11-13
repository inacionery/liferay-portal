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

package com.liferay.portal.workflow.analytics.internal.util;

import com.liferay.analytics.model.AnalyticsEventsMessage;
import com.liferay.analytics.model.AnalyticsEventsMessage.Builder;
import com.liferay.analytics.model.AnalyticsEventsMessage.Event;

import java.io.IOException;

import java.util.Map;

import javax.servlet.http.HttpServlet;

/**
 * @author Inácio Nery
 */
public class WorkflowAnalyticsUtil extends HttpServlet {

	public static void sendMessage(
			String userId, String eventId, Map<String, String> properties)
		throws IOException {

		AnalyticsEventsMessage.Event.Builder eventBuilder =
			AnalyticsEventsMessage.Event.builder(_applicationId, eventId);

		eventBuilder.properties(properties);

		Event build = eventBuilder.build();

		Builder builder = AnalyticsEventsMessage.builder(
			_analyticsKey, userId).event(build);

		_workflowClientImpl.sendAnalytics(builder.build());
	}

	private static final String _analyticsKey = "";
	private static final String _applicationId =
		"com.liferay.portal.workflow.analytics1.0.0";
	private static final WorkflowClientImpl _workflowClientImpl =
		new WorkflowClientImpl();

}