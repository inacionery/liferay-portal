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

package com.liferay.headless.workflow.dto;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;

import javax.annotation.Generated;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Javier Gamarra
 * @generated
 */
@Generated("")
@GraphQLName("WorkflowTask")
@XmlRootElement(name = "WorkflowTask")
public class WorkflowTask {

	public Boolean getCompleted() {
		return _completed;
	}

	public String getDateCompleted() {
		return _dateCompleted;
	}

	public String getDateCreated() {
		return _dateCreated;
	}

	public String getDefinitionName() {
		return _definitionName;
	}

	public String getDescription() {
		return _description;
	}

	public String getDueDate() {
		return _dueDate;
	}

	public Integer getId() {
		return _id;
	}

	public WorkflowLog getLogs() {
		return _logs;
	}

	public String getName() {
		return _name;
	}

	public Object getObject() {
		return _object;
	}

	public String getSelf() {
		return _self;
	}

	public String[] getTransitions() {
		return _transitions;
	}

	public void setCompleted(Boolean completed) {
		_completed = completed;
	}

	public void setDateCompleted(String dateCompleted) {
		_dateCompleted = dateCompleted;
	}

	public void setDateCreated(String dateCreated) {
		_dateCreated = dateCreated;
	}

	public void setDefinitionName(String definitionName) {
		_definitionName = definitionName;
	}

	public void setDescription(String description) {
		_description = description;
	}

	public void setDueDate(String dueDate) {
		_dueDate = dueDate;
	}

	public void setId(Integer id) {
		_id = id;
	}

	public void setLogs(WorkflowLog logs) {
		_logs = logs;
	}

	public void setName(String name) {
		_name = name;
	}

	public void setObject(Object object) {
		_object = object;
	}

	public void setSelf(String self) {
		_self = self;
	}

	public void setTransitions(String[] transitions) {
		_transitions = transitions;
	}

	@GraphQLField
	private Boolean _completed;

	@GraphQLField
	private String _dateCompleted;

	@GraphQLField
	private String _dateCreated;

	@GraphQLField
	private String _definitionName;

	@GraphQLField
	private String _description;

	@GraphQLField
	private String _dueDate;

	@GraphQLField
	private Integer _id;

	@GraphQLField
	private WorkflowLog _logs;

	@GraphQLField
	private String _name;

	@GraphQLField
	private Object _object;

	@GraphQLField
	private String _self;

	@GraphQLField
	private String[] _transitions;

}