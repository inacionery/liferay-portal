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

import com.liferay.analytics.client.AnalyticsClient;
import com.liferay.analytics.model.AnalyticsEventsMessage;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Inácio Nery
 */
public class WorkflowClientImpl implements AnalyticsClient {

	public Response sendAnalytics(
		AnalyticsEventsMessage analyticsEventsMessage) {

		WebTarget webTarget = _client.target(_ANALYTICS_GATEWAY_URL);

		return webTarget.request(MediaType.APPLICATION_JSON).post(
			Entity.entity(analyticsEventsMessage, MediaType.APPLICATION_JSON));
	}

	private static final String _ANALYTICS_GATEWAY_URL = System.getProperty(
		"analytics.gateway.url", "http://192.168.108.90:8081/");

	private final Client _client = ClientBuilder.newClient();

}