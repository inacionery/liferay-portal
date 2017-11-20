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

package com.liferay.portal.workflow.analytics.internal.metrics;

/**
 * @author Inácio Nery
 */
public enum Event {

	KALEO_DEFINITION_CREATE, KALEO_DEFINITION_UPDATE,
	KALEO_DEFINITION_VERSION_CREATE, KALEO_INSTANCE_CREATE,
	KALEO_INSTANCE_UPDATE, KALEO_INSTANCE_COMPLETE, KALEO_INSTANCE_TOKEN_CREATE,
	KALEO_INSTANCE_TOKEN_COMPLETE, KALEO_INSTANCE_TOKEN_UPDATE,
	KALEO_TASK_CREATE, KALEO_TASK_ASSIGNMENT_INSTANCE_CREATE,
	KALEO_TASK_ASSIGNMENT_INSTANCE_DELETE
}