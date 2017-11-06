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

import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Date;

/**
 * @author Inácio Nery
 */
public class WorkflowAnalyticsEventEntry
	implements Comparable<WorkflowAnalyticsEventEntry> {

	public WorkflowAnalyticsEventEntry(
		long userId, JSONObject attributes, Event event, Date date) {

		_userId = userId;
		_attributes = attributes;
		_event = event;
		_date = date;
	}

	@Override
	public int compareTo(WorkflowAnalyticsEventEntry other) {
		return getDate().compareTo(other.getDate());
	}

	public JSONObject getAttributes() {
		return _attributes;
	}

	public long getCompanyId() {
		return _attributes.getLong(WorkflowConstants.CONTEXT_COMPANY_ID);
	}

	public Date getDate() {
		return _date;
	}

	public Event getEvent() {
		return _event;
	}

	public long getKaleoInstanceId() {
		return _attributes.getLong("kaleoInstanceId");
	}

	public long getUserId() {
		return _userId;
	}

	private final JSONObject _attributes;
	private final Date _date;
	private final Event _event;
	private final long _userId;

}