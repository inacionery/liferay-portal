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

import jodd.http.HttpRequest;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(immediate = true)
public class WorkflowClientImpl {

	public void sendAnalytics(AnalyticsEventsMessage analyticsEventsMessage)
		throws Exception {

		String map = _jsonObjectMapper.map(analyticsEventsMessage);
		
		HttpRequest.post("http://192.168.108.90:8081/").body(map).send();
	}

	private final AnalyticsEventsMessageJSONObjectMapper _jsonObjectMapper =
		new AnalyticsEventsMessageJSONObjectMapper();

}