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

package com.liferay.portal.workflow;

import com.liferay.portal.kernel.messaging.proxy.BaseProxyBean;
import com.liferay.portal.kernel.spring.osgi.OSGiBeanProperties;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionVersion;
import com.liferay.portal.kernel.workflow.WorkflowDefinitionVersionManager;
import com.liferay.portal.kernel.workflow.WorkflowException;

import java.util.List;

/**
 * @author Inácio Nery
 */
@OSGiBeanProperties(
	property = "proxy.bean=true",
	service = WorkflowDefinitionVersionManager.class
)
public class WorkflowDefinitionVersionManagerProxyBean
	extends BaseProxyBean implements WorkflowDefinitionVersionManager {

	@Override
	public WorkflowDefinitionVersion getWorkflowDefinitionVersion(
			long companyId, String name, String version)
		throws WorkflowException {

		throw new UnsupportedOperationException();
	}

	@Override
	public int getWorkflowDefinitionVersionCount(long companyId, String name)
		throws WorkflowException {

		throw new UnsupportedOperationException();
	}

	@Override
	public List<WorkflowDefinitionVersion> getWorkflowDefinitionVersions(
			long companyId, int start, int end,
			OrderByComparator<WorkflowDefinitionVersion> orderByComparator)
		throws WorkflowException {

		throw new UnsupportedOperationException();
	}

	@Override
	public List<WorkflowDefinitionVersion> getWorkflowDefinitionVersions(
			long companyId, String name, int start, int end,
			OrderByComparator<WorkflowDefinitionVersion> orderByComparator)
		throws WorkflowException {

		throw new UnsupportedOperationException();
	}

}