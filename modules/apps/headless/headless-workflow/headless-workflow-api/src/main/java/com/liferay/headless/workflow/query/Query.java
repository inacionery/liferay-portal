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

package com.liferay.headless.workflow.query;

import com.liferay.headless.workflow.dto.WorkflowLog;
import com.liferay.headless.workflow.dto.WorkflowTask;
import com.liferay.portal.vulcan.context.Pagination;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;

import graphql.schema.DataFetchingEnvironment;

import java.util.List;

import javax.annotation.Generated;

/**
 * To access this resource, run:
 *
 *     curl -u your@email.com:yourpassword -D - http://localhost:8080/o/headless-workflow-graphql
 *
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public interface Query {

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowTask> getRolesWorkflowTasksList(
			final DataFetchingEnvironment env,
			@GraphQLName("parent-id") String parentId,
			@GraphQLName("Pagination") Pagination pagination)
		throws Exception;

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowLog getWorkflowLog(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception;

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowTask getWorkflowTask(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception;

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowTask> getWorkflowTasksList(
			final DataFetchingEnvironment env,
			@GraphQLName("genericparentid") Object genericparentid,
			@GraphQLName("Pagination") Pagination pagination)
		throws Exception;

	@GraphQLField
	@GraphQLInvokeDetached
	public List<WorkflowLog> getWorkflowTasksWorkflowLogsList(
			final DataFetchingEnvironment env,
			@GraphQLName("parent-id") Integer parentId,
			@GraphQLName("Pagination") Pagination pagination)
		throws Exception;

}