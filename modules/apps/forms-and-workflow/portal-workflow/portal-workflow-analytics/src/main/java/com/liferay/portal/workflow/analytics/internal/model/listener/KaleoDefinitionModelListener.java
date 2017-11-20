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
import com.liferay.portal.workflow.kaleo.model.KaleoDefinition;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = ModelListener.class)
public class KaleoDefinitionModelListener
	extends BaseModelListener<KaleoDefinition> {

	@Override
	public void onAfterCreate(KaleoDefinition kaleoDefinition)
		throws ModelListenerException {

		try {
			Map<String, String> properties = new HashMap<>();

			properties.put(
			    "active", String.valueOf(kaleoDefinition.getActive()));
			properties.put(
				"date", String.valueOf(kaleoDefinition.getCreateDate()));
			properties.put(
				"kaleoDefinitionId",
				String.valueOf(kaleoDefinition.getKaleoDefinitionId()));
			properties.put("name", kaleoDefinition.getName());
			properties.put(
				"userId", String.valueOf(kaleoDefinition.getUserId()));
			properties.put(
				"version", String.valueOf(kaleoDefinition.getVersion()));

			WorkflowAnalyticsUtil.sendMessage(
				String.valueOf(kaleoDefinition.getUserId()),
				Event.KALEO_DEFINITION_CREATE.name(), properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Override
	public void onAfterUpdate(KaleoDefinition kaleoDefinition)
		throws ModelListenerException {

		try {
			Map<String, String> properties = new HashMap<>();

	        properties.put(
	            "active", String.valueOf(kaleoDefinition.getActive()));
			properties.put(
				"date", String.valueOf(kaleoDefinition.getCreateDate()));
			properties.put(
				"kaleoDefinitionId",
				String.valueOf(kaleoDefinition.getKaleoDefinitionId()));
			properties.put("name", kaleoDefinition.getName());
			properties.put(
				"userId", String.valueOf(kaleoDefinition.getUserId()));
			properties.put(
				"version", String.valueOf(kaleoDefinition.getVersion()));

			WorkflowAnalyticsUtil.sendMessage(
				String.valueOf(kaleoDefinition.getUserId()),
				Event.KALEO_DEFINITION_UPDATE.name(), properties);
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

}