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
import com.liferay.portal.kernel.json.JSONFactory;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.workflow.analytics.internal.metrics.Event;
import com.liferay.portal.workflow.analytics.internal.metrics.WorkflowAnalyticsEventEntry;
import com.liferay.portal.workflow.analytics.internal.servlet.WorkflowAnalyticsServlet;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
			JSONObject attributes = _jsonFactory.createJSONObject();

			attributes.put(
				"kaleoInstanceId", kaleoInstanceToken.getKaleoInstanceId());
			attributes.put(
				"kaleoInstanceTokenId",
				kaleoInstanceToken.getKaleoInstanceTokenId());
			attributes.put(
				WorkflowConstants.CONTEXT_COMPANY_ID,
				String.valueOf(kaleoInstanceToken.getCompanyId()));
			attributes.put(
				WorkflowConstants.CONTEXT_GROUP_ID,
				String.valueOf(kaleoInstanceToken.getGroupId()));
			attributes.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME,
				kaleoInstanceToken.getClassName());
			attributes.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_PK,
				String.valueOf(kaleoInstanceToken.getClassPK()));
			attributes.put(
				"currentKaleoNodeId",
				String.valueOf(kaleoInstanceToken.getCurrentKaleoNodeId()));
			attributes.put(
				"currentKaleoNodeName",
				kaleoInstanceToken.getCurrentKaleoNodeName());

			KaleoInstance kaleoInstance = kaleoInstanceToken.getKaleoInstance();

			attributes.put(
				"kaleoDefinitionName", kaleoInstance.getKaleoDefinitionName());
			attributes.put(
				"kaleoDefinitionVersion",
				String.valueOf(kaleoInstance.getKaleoDefinitionVersion()));
			attributes.put(
				"kaleoInstanceId", kaleoInstance.getKaleoInstanceId());

			WorkflowAnalyticsServlet.add(
				new WorkflowAnalyticsEventEntry(
					kaleoInstanceToken.getUserId(), attributes,
					Event.KALEO_INSTANCE_TOKEN_CREATE,
					kaleoInstanceToken.getCreateDate()));
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterUpdate(KaleoInstanceToken kaleoInstanceToken)
		throws ModelListenerException {

		try {
			JSONObject attributes = _jsonFactory.createJSONObject();

			attributes.put(
				"kaleoInstanceTokenId",
				kaleoInstanceToken.getKaleoInstanceTokenId());
			attributes.put(
				WorkflowConstants.CONTEXT_COMPANY_ID,
				String.valueOf(kaleoInstanceToken.getCompanyId()));
			attributes.put(
				WorkflowConstants.CONTEXT_GROUP_ID,
				String.valueOf(kaleoInstanceToken.getGroupId()));
			attributes.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_NAME,
				kaleoInstanceToken.getClassName());
			attributes.put(
				WorkflowConstants.CONTEXT_ENTRY_CLASS_PK,
				String.valueOf(kaleoInstanceToken.getClassPK()));
			attributes.put(
				"currentKaleoNodeId",
				String.valueOf(kaleoInstanceToken.getCurrentKaleoNodeId()));
			attributes.put(
				"currentKaleoNodeName",
				kaleoInstanceToken.getCurrentKaleoNodeName());

			KaleoInstance kaleoInstance = kaleoInstanceToken.getKaleoInstance();

			attributes.put(
				"kaleoDefinitionName", kaleoInstance.getKaleoDefinitionName());
			attributes.put(
				"kaleoDefinitionVersion",
				String.valueOf(kaleoInstance.getKaleoDefinitionVersion()));
			attributes.put(
				"kaleoInstanceId", kaleoInstance.getKaleoInstanceId());

			Event event = Event.KALEO_INSTANCE_TOKEN_UPDATE;

			if (kaleoInstanceToken.isCompleted()) {
				event = Event.KALEO_INSTANCE_TOKEN_COMPLETE;
			}

			WorkflowAnalyticsServlet.add(
				new WorkflowAnalyticsEventEntry(
					kaleoInstanceToken.getUserId(), attributes, event,
					kaleoInstanceToken.getModifiedDate()));
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Reference
	private JSONFactory _jsonFactory;

}