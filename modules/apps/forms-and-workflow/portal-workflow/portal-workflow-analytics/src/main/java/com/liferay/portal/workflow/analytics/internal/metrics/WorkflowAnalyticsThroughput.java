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

import java.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

		stream = stream.sorted();

		Map<Long, Map<Long, List<WorkflowAnalyticsEventEntry>>> map =
			stream.collect(
				Collectors.groupingBy(
					WorkflowAnalyticsEventEntry::getKaleoDefinitionId,
					Collectors.groupingBy(
						WorkflowAnalyticsEventEntry::getKaleoInstanceId)));

		Set<Entry<Long, Map<Long, List<WorkflowAnalyticsEventEntry>>>>
			entrySet = map.entrySet();

		Stream<Entry<Long, Map<Long, List<WorkflowAnalyticsEventEntry>>>>
			entrySetStream = entrySet.stream();

		List<Map<Long, Integer>> timeSpentPerProcess = entrySetStream.map(
			this::map
		).collect(
			Collectors.toList()
		);

		Stream<Map<Long, Integer>> timeSpentPerProcessStream =
			timeSpentPerProcess.stream();

		Map<Object, Double> timeSpentPerProcessMap =
			timeSpentPerProcessStream.flatMap(
				mapper -> mapper.entrySet().stream()
			).collect(
				Collectors.groupingBy(
					Map.Entry::getKey,
					Collectors.averagingInt(Map.Entry::getValue))
			);

		for (Map.Entry<Object, Double> entry :
				timeSpentPerProcessMap.entrySet()) {

			jsonObject.put(
				entry.getKey().toString(), entry.getValue().intValue());
		}

		return jsonObject;
	}

	protected Map<Long, Integer> map(
		Map.Entry<Long, Map<Long, List<WorkflowAnalyticsEventEntry>>> entry) {

		Map<Long, Integer> processDurationMap = new HashMap<>();

		Map<Long, List<WorkflowAnalyticsEventEntry>> value = entry.getValue();

		for (Map.Entry<Long, List<WorkflowAnalyticsEventEntry>> valueEntry :
				value.entrySet()) {

			List<WorkflowAnalyticsEventEntry> events = valueEntry.getValue();

			int total = 0;

			for (int i = 0; i < events.size(); i++) {
				int createdIndex = i;
				int closedIndex = i + 1 < events.size() ? i + 1 : i;

				if (closedIndex != createdIndex) {
					WorkflowAnalyticsEventEntry createdEvent = events.get(
						createdIndex);
					WorkflowAnalyticsEventEntry closedEvent = events.get(
						closedIndex);

					Duration duration = Duration.between(
						createdEvent.getDate().toInstant(),
						closedEvent.getDate().toInstant());

					total += duration.getSeconds();

					i++;
				}
			}

			processDurationMap.put(entry.getKey(), total);
		}

		return processDurationMap;
	}

	@Reference
	private JSONFactory _jsonFactory;

}