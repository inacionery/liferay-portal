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

import com.liferay.headless.workflow.dto.WorkflowLog;
import com.liferay.headless.workflow.dto.WorkflowTask;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;

import graphql.schema.DataFetchingEnvironment;

import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public class Query {

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowTask> getRolesWorkflowTasksList(
			final DataFetchingEnvironment env,
			@GraphQLName("parent-id") String parentId)
		throws Exception {

		return Collections.emptyList();
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowLog getWorkflowLog(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception {

		return new WorkflowLog();
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowTask getWorkflowTask(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception {

		return new WorkflowTask();
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowTask> getWorkflowTasksList(
			final DataFetchingEnvironment env)
		throws Exception {

		return Collections.emptyList();
	}

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowLog> getWorkflowTasksWorkflowLogsList(
			final DataFetchingEnvironment env,
			@GraphQLName("parent-id") Integer parentId)
		throws Exception {

		return Collections.emptyList();
	}

}