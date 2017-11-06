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

package com.liferay.portal.workflow.analytics.internal.metrics;

import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.workflow.analytics.WorkflowAnalytics;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true, property = {"workflow.analytics.metric=throughput"}
)
public class WorkflowAnalyticsThroughput implements WorkflowAnalytics {

	@Override
	public JSONObject getData(
			List<WorkflowAnalyticsEventEntry> eventEntries,
			HttpServletRequest request)
		throws Exception {

		JSONObject jsonObject = _jsonFactory.createJSONObject();

		Stream<WorkflowAnalyticsEventEntry> stream = eventEntries.stream();

		long companyId = ParamUtil.getLong(
			request, WorkflowConstants.CONTEXT_COMPANY_ID);

		stream = stream.filter(
			event -> event.getCompanyId() == companyId &&
			(event.getEvent().equals(Event.KALEO_INSTANCE_TOKEN_CREATE) ||
			event.getEvent().equals(Event.KALEO_INSTANCE_TOKEN_COMPLETE)));

		Stream<WorkflowAnalyticsKaleoInstanceEvent> kaleoInstanceEventstream =
			stream.map(
				e -> new WorkflowAnalyticsKaleoInstanceEvent(
					e.getKaleoInstanceId(), e.getEvent()));

		Map<Event, Long> map = kaleoInstanceEventstream.collect(
			Collectors.groupingBy(
				WorkflowAnalyticsKaleoInstanceEvent::getEvent,
				Collectors.counting()));

		Long created = map.get(Event.KALEO_INSTANCE_TOKEN_CREATE);
		Long completed = map.get(Event.KALEO_INSTANCE_TOKEN_COMPLETE);

		jsonObject.put("created", created);

		jsonObject.put("completed", completed);

		if (completed != null) {
			jsonObject.put("running", created - completed);
		}
		else {
			jsonObject.put("running", created);
		}

		return jsonObject;
	}

	@Reference
	private JSONFactory _jsonFactory;

}