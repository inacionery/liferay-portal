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
import com.liferay.portal.workflow.kaleo.model.KaleoTask;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = ModelListener.class)
public class KaleoTaskModelListener extends BaseModelListener<KaleoTask> {

	@Override
	public void onAfterCreate(KaleoTask kaleoTask)
		throws ModelListenerException {

		try {
			Map<String, String> properties = new HashMap<>();

			properties.put("date", String.valueOf(kaleoTask.getCreateDate()));
			properties.put(
				"kaleoTaskId", String.valueOf(kaleoTask.getKaleoTaskId()));
			properties.put("name", kaleoTask.getName());
			properties.put("userId", String.valueOf(kaleoTask.getUserId()));

			WorkflowAnalyticsUtil.sendMessage(
				String.valueOf(kaleoTask.getUserId()),
				Event.KALEO_TASK_CREATE.name(), properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

}