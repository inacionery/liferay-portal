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

import com.liferay.portal.kernel.util.HashUtil;

import java.util.Objects;

/**
 * @author Inácio Nery
 */
public class WorkflowAnalyticsKaleoInstanceEvent {

	public WorkflowAnalyticsKaleoInstanceEvent(
		long kaleoInstanceId, Event event) {

		_kaleoInstanceId = kaleoInstanceId;
		_event = event;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof WorkflowAnalyticsKaleoInstanceEvent)) {
			return false;
		}

		WorkflowAnalyticsKaleoInstanceEvent userEvent =
			(WorkflowAnalyticsKaleoInstanceEvent)obj;

		if (Objects.equals(_kaleoInstanceId, userEvent._kaleoInstanceId) &&
			Objects.equals(_event, userEvent._event)) {

			return true;
		}

		return false;
	}

	public Event getEvent() {
		return _event;
	}

	public long getKaleoInstanceId() {
		return _kaleoInstanceId;
	}

	@Override
	public int hashCode() {
		int hash = HashUtil.hash(0, _kaleoInstanceId);

		return HashUtil.hash(hash, _event);
	}

	private final Event _event;
	private final long _kaleoInstanceId;

}