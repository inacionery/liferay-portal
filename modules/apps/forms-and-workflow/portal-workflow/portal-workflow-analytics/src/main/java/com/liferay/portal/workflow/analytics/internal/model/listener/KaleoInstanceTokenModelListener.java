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
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.workflow.analytics.internal.metrics.Event;
import com.liferay.portal.workflow.analytics.internal.util.WorkflowAnalyticsUtil;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;

import java.time.Duration;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = ModelListener.class)
public class KaleoInstanceTokenModelListener
	extends BaseModelListener<KaleoInstanceToken> {

	@Override
	public void onAfterCreate(KaleoInstanceToken kaleoInstanceToken)
		throws ModelListenerException {

		try {
			Map<String, String> properties = new HashMap<>();

			properties.put(
				"kaleoInstanceId",
				String.valueOf(kaleoInstanceToken.getKaleoInstanceId()));
			properties.put(
				"kaleoInstanceTokenId",
				String.valueOf(kaleoInstanceToken.getKaleoInstanceTokenId()));
			properties.put(
				WorkflowConstants.CONTEXT_COMPANY_ID,
				String.valueOf(kaleoInstanceToken.getCompanyId()));
			properties.put(
				WorkflowConstants.CONTEXT_GROUP_ID,
				String.valueOf(kaleoInstanceToken.getGroupId()));
			properties.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME,
				kaleoInstanceToken.getClassName());
			properties.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_PK,
				String.valueOf(kaleoInstanceToken.getClassPK()));
			properties.put(
				"currentKaleoNodeId",
				String.valueOf(kaleoInstanceToken.getCurrentKaleoNodeId()));
			properties.put(
				"currentKaleoNodeName",
				kaleoInstanceToken.getCurrentKaleoNodeName());
			properties.put(
				"date", String.valueOf(kaleoInstanceToken.getCreateDate()));
			properties.put(
				"userId", String.valueOf(kaleoInstanceToken.getUserId()));

			WorkflowAnalyticsUtil.sendMessage(
				String.valueOf(kaleoInstanceToken.getUserId()),
				Event.KALEO_INSTANCE_TOKEN_CREATE.name(), properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterUpdate(KaleoInstanceToken kaleoInstanceToken)
		throws ModelListenerException {

		try {
			Map<String, String> properties = new HashMap<>();

			properties.put(
				"kaleoInstanceId",
				String.valueOf(kaleoInstanceToken.getKaleoInstanceId()));
			properties.put(
				"kaleoInstanceTokenId",
				String.valueOf(kaleoInstanceToken.getKaleoInstanceTokenId()));
			properties.put(
				WorkflowConstants.CONTEXT_COMPANY_ID,
				String.valueOf(kaleoInstanceToken.getCompanyId()));
			properties.put(
				WorkflowConstants.CONTEXT_GROUP_ID,
				String.valueOf(kaleoInstanceToken.getGroupId()));
			properties.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME,
				kaleoInstanceToken.getClassName());
			properties.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_PK,
				String.valueOf(kaleoInstanceToken.getClassPK()));
			properties.put(
				"currentKaleoNodeId",
				String.valueOf(kaleoInstanceToken.getCurrentKaleoNodeId()));
			properties.put(
				"currentKaleoNodeName",
				kaleoInstanceToken.getCurrentKaleoNodeName());
			properties.put(
				"date", String.valueOf(kaleoInstanceToken.getModifiedDate()));
			properties.put(
				"userId", String.valueOf(kaleoInstanceToken.getUserId()));

			Event event = Event.KALEO_INSTANCE_TOKEN_UPDATE;

			if (kaleoInstanceToken.isCompleted()) {
				event = Event.KALEO_INSTANCE_TOKEN_COMPLETE;

				Duration duration = Duration.between(
					kaleoInstanceToken.getCreateDate().toInstant(),
					kaleoInstanceToken.getCompletionDate().toInstant());

				properties.put(
					"duration", String.valueOf(duration.getSeconds()));
			}

			WorkflowAnalyticsUtil.sendMessage(
				String.valueOf(kaleoInstanceToken.getUserId()), event.name(),
				properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

}