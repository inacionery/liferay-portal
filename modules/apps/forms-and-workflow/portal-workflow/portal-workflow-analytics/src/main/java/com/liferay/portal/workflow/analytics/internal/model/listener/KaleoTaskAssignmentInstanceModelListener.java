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

package com.liferay.portal.workflow.analytics.internal.model.listener;

import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.workflow.analytics.internal.metrics.Event;
import com.liferay.portal.workflow.analytics.internal.util.WorkflowAnalyticsUtil;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;

import java.time.Duration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = ModelListener.class)
public class KaleoTaskAssignmentInstanceModelListener
	extends BaseModelListener<KaleoTaskAssignmentInstance> {

	@Override
	public void onAfterCreate(
			KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance)
		throws ModelListenerException {

		try {
			Map<String, String> properties = new HashMap<>();

			properties.put(
				"assigneeClassName",
				kaleoTaskAssignmentInstance.getAssigneeClassName());
			properties.put(
				"assigneeClassPK",
				String.valueOf(
					kaleoTaskAssignmentInstance.getAssigneeClassPK()));
			properties.put(
				"date",
				String.valueOf(kaleoTaskAssignmentInstance.getCreateDate()));
			properties.put(
				"kaleoTaskAssignmentInstanceId",
				String.valueOf(
					kaleoTaskAssignmentInstance.
						getKaleoTaskAssignmentInstanceId()));
			properties.put(
				"kaleoTaskId",
				String.valueOf(kaleoTaskAssignmentInstance.getKaleoTaskId()));
			properties.put(
				"userId",
				String.valueOf(kaleoTaskAssignmentInstance.getUserId()));

			WorkflowAnalyticsUtil.sendMessage(
				String.valueOf(kaleoTaskAssignmentInstance.getUserId()),
				Event.KALEO_TASK_ASSIGNMENT_INSTANCE_CREATE.name(), properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterRemove(
			KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance)
		throws ModelListenerException {

		try {
			Map<String, String> properties = new HashMap<>();

			properties.put(
				"assigneeClassName",
				kaleoTaskAssignmentInstance.getAssigneeClassName());
			properties.put(
				"assigneeClassPK",
				String.valueOf(
					kaleoTaskAssignmentInstance.getAssigneeClassPK()));
			properties.put("date", String.valueOf(new Date()));
			properties.put(
				"kaleoTaskAssignmentInstanceId",
				String.valueOf(
					kaleoTaskAssignmentInstance.
						getKaleoTaskAssignmentInstanceId()));
			properties.put(
				"kaleoTaskId",
				String.valueOf(kaleoTaskAssignmentInstance.getKaleoTaskId()));
			properties.put(
				"userId",
				String.valueOf(kaleoTaskAssignmentInstance.getUserId()));

			Duration duration = Duration.between(
				kaleoTaskAssignmentInstance.getCreateDate().toInstant(),
				new Date().toInstant());

			properties.put("duration", String.valueOf(duration.getSeconds()));

			WorkflowAnalyticsUtil.sendMessage(
				String.valueOf(kaleoTaskAssignmentInstance.getUserId()),
				Event.KALEO_TASK_ASSIGNMENT_INSTANCE_DELETE.name(), properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

}