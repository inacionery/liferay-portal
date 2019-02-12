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

package com.liferay.headless.workflow.internal.query;

import javax.annotation.Generated;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

import com.liferay.headless.workflow.dto.v1_0.WorkflowLog;
import com.liferay.headless.workflow.dto.v1_0.WorkflowTask;
import com.liferay.headless.workflow.resource.v1_0.WorkflowLogResource;
import com.liferay.headless.workflow.resource.v1_0.WorkflowTaskResource;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.Map;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class Query {

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowTask> getRolesWorkflowTasksPage(
			final DataFetchingEnvironment env,
			@GraphQLName("roles-id") Long rolesId,
			@GraphQLName("Pagination") Pagination pagination)
		throws Exception {

		if (pagination == null) {
			Map<String,	Integer> paginationMap = env.getArgument("Pagination");
			
			pagination = Pagination.of(
				paginationMap.get("itemsPerPage"),
				paginationMap.get("pageNumber"));
		}

		Page<WorkflowTask> rolesWorkflowTasksPage = 
			_getWorkflowTaskResource().getRolesWorkflowTasksPage(rolesId, pagination);

		return (List<WorkflowTask>) rolesWorkflowTasksPage.getItems();
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowLog getWorkflowLog(
			final DataFetchingEnvironment env,
			@GraphQLName("workflow-logs-id") Long workflowLogsId)
		throws Exception {

		return _getWorkflowLogResource().getWorkflowLog(workflowLogsId);
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowTask getWorkflowTask(
			final DataFetchingEnvironment env,
			@GraphQLName("workflow-tasks-id") Long workflowTasksId)
		throws Exception {

		return _getWorkflowTaskResource().getWorkflowTask(workflowTasksId);
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowTask> getWorkflowTasksPage(
			final DataFetchingEnvironment env,
			@GraphQLName("Pagination") Pagination pagination)
		throws Exception {

		Page<WorkflowTask> workflowTasksPage = 
			_getWorkflowTaskResource().getWorkflowTasksPage(pagination);
	
		return (List<WorkflowTask>) workflowTasksPage.getItems();
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowLog> getWorkflowTasksWorkflowLogsPage(
			final DataFetchingEnvironment env,
			@GraphQLName("workflow-tasks-id") Long workflowTasksId,
			@GraphQLName("Pagination") Pagination pagination)
		throws Exception {

		Page<WorkflowLog> workflowTasksWorkflowLogsPage = 
			_getWorkflowLogResource().getWorkflowTasksWorkflowLogsPage(workflowTasksId, pagination);
	
		return (List<WorkflowLog>) workflowTasksWorkflowLogsPage.getItems();
	}

	private static WorkflowLogResource _getWorkflowLogResource() {
		return _workflowLogResourceServiceTracker.getService();
	}

	private static WorkflowTaskResource _getWorkflowTaskResource() {
		return _workflowTaskResourceServiceTracker.getService();
	}

	private static final ServiceTracker<WorkflowLogResource, WorkflowLogResource>
		_workflowLogResourceServiceTracker;
	private static final ServiceTracker<WorkflowTaskResource, WorkflowTaskResource>
		_workflowTaskResourceServiceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(Query.class);

		ServiceTracker<WorkflowLogResource, WorkflowLogResource> workflowLogResourceServiceTracker =
			new ServiceTracker<>(
				bundle.getBundleContext(), WorkflowLogResource.class, null);

		workflowLogResourceServiceTracker.open();

		_workflowLogResourceServiceTracker = workflowLogResourceServiceTracker;

		ServiceTracker<WorkflowTaskResource, WorkflowTaskResource> workflowTaskResourceServiceTracker =
			new ServiceTracker<>(
				bundle.getBundleContext(), WorkflowTaskResource.class, null);

		workflowTaskResourceServiceTracker.open();

		_workflowTaskResourceServiceTracker =
			workflowTaskResourceServiceTracker;
	}

}