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

package com.liferay.headless.workflow.internal.resource.v1_0;

import com.liferay.headless.workflow.dto.v1_0.WorkflowTask;
import com.liferay.headless.workflow.resource.v1_0.WorkflowTaskResource;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.workflow.WorkflowTaskManager;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Javier Gamarra
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/workflow-task.properties",
	scope = ServiceScope.PROTOTYPE, service = WorkflowTaskResource.class
)
public class WorkflowTaskResourceImpl extends BaseWorkflowTaskResourceImpl {

	@Override
	public Page<WorkflowTask> getRolesWorkflowTasksPage(
			Long rolesId, Pagination pagination)
		throws Exception {

		Long companyId = CompanyThreadLocal.getCompanyId();

		return Page.of(
			transform(
				_workflowTaskManager.getWorkflowTasksByRole(
					companyId, rolesId, null, pagination.getStartPosition(),
					pagination.getEndPosition(), null),
				this::_toWorkflowTask),
			pagination,
				_workflowTaskManager.getWorkflowTaskCountByRole(
					companyId, rolesId, null));
	}

	private WorkflowTask _toWorkflowTask(
			com.liferay.portal.kernel.workflow.WorkflowTask workflowTask)
		throws Exception {

		return new WorkflowTask() {
			{
				setCompleted(workflowTask.getCompletionDate() != null);
				setDateCompleted(
					workflowTask.getCompletionDate() != null ?
						workflowTask.getCompletionDate().toString() : "");
				setDateCreated(workflowTask.getCreateDate().toString());
				setDefinitionName(workflowTask.getWorkflowDefinitionName());
				setDescription(workflowTask.getDescription());
				setDueDate(
					workflowTask.getDueDate() != null ?
						workflowTask.getDueDate().toString() : "");
				setId(workflowTask.getWorkflowTaskId());
				setName(workflowTask.getName());
			}
		};
	}

	@Reference
	private WorkflowTaskManager _workflowTaskManager;

}