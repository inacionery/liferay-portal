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

package com.liferay.portal.workflow.analytics.internal.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;

import javax.portlet.Portlet;

import org.osgi.service.component.annotations.Component;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.css-class-wrapper=portlet-workflow-health",
		"com.liferay.portlet.display-category=category.analytics",
		"com.liferay.portlet.friendly-url-mapping=workflow_health",
		"com.liferay.portlet.preferences-owned-by-group=true",
		"javax.portlet.display-name=Workflow Health",
		"javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=com_liferay_portal_workflow_task_web_portlet_WorkflowHealthPortlet",
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.supports.mime-type=text/html"
	},
	service = Portlet.class
)
public class WorkflowHealthPortlet extends MVCPortlet {
}