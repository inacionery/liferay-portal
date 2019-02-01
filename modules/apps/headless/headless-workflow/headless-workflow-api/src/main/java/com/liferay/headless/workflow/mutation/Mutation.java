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

package com.liferay.headless.workflow.mutation;

import com.liferay.headless.workflow.dto.WorkflowTask;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLInvokeDetached;
import graphql.annotations.annotationTypes.GraphQLName;

import graphql.schema.DataFetchingEnvironment;

import javax.annotation.Generated;

/**
 * To access this query, run:
 *
 *     curl -u your@email.com:yourpassword -D - http://localhost:8080/o/headless-workflow-graphql
 *
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
public interface Mutation {

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowTask postWorkflowTasksAssignToMe(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception;

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowTask postWorkflowTasksAssignToUser(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception;

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowTask postWorkflowTasksChangeTransition(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception;

	@GraphQLField
	@GraphQLInvokeDetached
	public WorkflowTask postWorkflowTasksUpdateDueDate(
			final DataFetchingEnvironment env, @GraphQLName("id") Integer id)
		throws Exception;

}