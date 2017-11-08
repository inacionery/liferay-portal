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
import com.liferay.portal.workflow.analytics.internal.metrics.Event;
import com.liferay.portal.workflow.analytics.internal.metrics.WorkflowAnalyticsEventEntry;
import com.liferay.portal.workflow.analytics.internal.servlet.WorkflowAnalyticsServlet;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = ModelListener.class)
public class KaleoDefinitionVersionModelListener
	extends BaseModelListener<KaleoDefinitionVersion> {

	@Override
	public void onAfterCreate(KaleoDefinitionVersion kaleoDefinitionVersion)
		throws ModelListenerException {

		try {
			JSONObject attributes = _jsonFactory.createJSONObject();

			attributes.put(
				"kaleoDefinitionVersionId",
				kaleoDefinitionVersion.getKaleoDefinitionVersionId());
			attributes.put("name", kaleoDefinitionVersion.getName());
			attributes.put("version", kaleoDefinitionVersion.getVersion());

			WorkflowAnalyticsServlet.add(
				new WorkflowAnalyticsEventEntry(
					kaleoDefinitionVersion.getUserId(), attributes,
					Event.KALEO_DEFINITION_VERSION_CREATE,
					kaleoDefinitionVersion.getCreateDate()));
		}
		catch (Exception e) {
			throw new ModelListenerException(e);
		}
	}

	@Reference
	private JSONFactory _jsonFactory;

}