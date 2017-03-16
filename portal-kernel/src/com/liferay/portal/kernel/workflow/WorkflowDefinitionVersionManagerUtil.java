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

package com.liferay.portal.kernel.workflow;

import com.liferay.portal.kernel.security.pacl.permission.PortalRuntimePermission;
import com.liferay.portal.kernel.util.OrderByComparator;

import java.util.List;

/**
 * @author Inácio Nery
 */
public class WorkflowDefinitionVersionManagerUtil {

	public static WorkflowDefinitionVersion getWorkflowDefinitionVersion(
			long companyId, String name, String version)
		throws WorkflowException {

		return getWorkflowDefinitionVersionManager().
			getWorkflowDefinitionVersion(companyId, name, version);
	}

	public static int getWorkflowDefinitionVersionCount(
			long companyId, String name)
		throws WorkflowException {

		return getWorkflowDefinitionVersionManager().
			getWorkflowDefinitionVersionCount(companyId, name);
	}

	public static WorkflowDefinitionVersionManager
		getWorkflowDefinitionVersionManager() {

		PortalRuntimePermission.checkGetBeanProperty(
			WorkflowDefinitionVersionManagerUtil.class);

		return _workflowDefinitionVersionManager;
	}

	public static List<WorkflowDefinitionVersion> getWorkflowDefinitionVersions(
			long companyId, int start, int end,
			OrderByComparator<WorkflowDefinitionVersion> orderByComparator)
		throws WorkflowException {

		return getWorkflowDefinitionVersionManager().
			getWorkflowDefinitionVersions(
				companyId, start, end, orderByComparator);
	}

	public static List<WorkflowDefinitionVersion> getWorkflowDefinitionVersions(
			long companyId, String name, int start, int end,
			OrderByComparator<WorkflowDefinitionVersion> orderByComparator)
		throws WorkflowException {

		return getWorkflowDefinitionVersionManager().
			getWorkflowDefinitionVersions(
				companyId, name, start, end, orderByComparator);
	}

	public void setWorkflowDefinitionVersionManager(
		WorkflowDefinitionVersionManager workflowDefinitionVersionManager) {

		PortalRuntimePermission.checkSetBeanProperty(getClass());

		_workflowDefinitionVersionManager = workflowDefinitionVersionManager;
	}

	private static WorkflowDefinitionVersionManager
		_workflowDefinitionVersionManager;

}