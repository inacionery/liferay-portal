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

package com.liferay.portal.workflow.analytics.internal.servlet;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.workflow.analytics.WorkflowAnalytics;
import com.liferay.portal.workflow.analytics.WorkflowAnalyticsTracker;
import com.liferay.portal.workflow.analytics.internal.metrics.WorkflowAnalyticsEventEntry;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true,
	property = {
		"osgi.http.whiteboard.context.path=/portal-workflow-analytics-event",
		"osgi.http.whiteboard.servlet.name=com.liferay.portal.workflow.analytics.internal.servlet.WorkflowAnalyticsServlet",
		"osgi.http.whiteboard.servlet.pattern=/portal-workflow-analytics-event/*"
	},
	service = Servlet.class
)
public class WorkflowAnalyticsServlet extends HttpServlet {

	public static void add(
		WorkflowAnalyticsEventEntry workflowAnalyticsEventEntry) {

		_workflowAnalyticsEventEntries.add(workflowAnalyticsEventEntry);
	}

	@Override
	protected void doGet(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {

		try {
			WorkflowAnalytics workflowAnalytics =
				_workflowAnalyticsTracker.getMetric(
					ParamUtil.getString(request, "metric"));

			if (workflowAnalytics == null) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);

				return;
			}

			JSONObject jsonObject = workflowAnalytics.getData(
				_workflowAnalyticsEventEntries, request);
			
			response.setContentType(ContentTypes.APPLICATION_JSON);
	        response.setStatus(HttpServletResponse.SC_OK);

			ServletResponseUtil.write(response, jsonObject.toJSONString());
		}
		catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);

			return;
		}
	}

	private static final List<WorkflowAnalyticsEventEntry>
		_workflowAnalyticsEventEntries = new ArrayList<>();

	@Reference
	private JSONFactory _jsonFactory;

	@Reference
	private WorkflowAnalyticsTracker _workflowAnalyticsTracker;

}