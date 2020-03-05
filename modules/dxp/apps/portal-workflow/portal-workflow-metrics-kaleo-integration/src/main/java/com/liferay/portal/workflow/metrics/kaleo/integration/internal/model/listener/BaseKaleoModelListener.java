/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 *
 *
 *
 */

package com.liferay.portal.workflow.metrics.kaleo.integration.internal.model.listener;

import com.liferay.portal.kernel.model.BaseModel;
import com.liferay.portal.kernel.model.BaseModelListener;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinition;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoDefinitionVersionLocalService;

import org.osgi.service.component.annotations.Reference;

/**
 * @author Rafael Praxedes
 */
public abstract class BaseKaleoModelListener<T extends BaseModel<T>>
	extends BaseModelListener<T> {

	protected KaleoDefinition getKaleoDefinition(
		long kaleoDefinitionVersionId) {

		KaleoDefinitionVersion kaleoDefinitionVersion =
			getKaleoDefinitionVersion(kaleoDefinitionVersionId);

		if (kaleoDefinitionVersion != null) {
			ServiceContext serviceContext = new ServiceContext();

			serviceContext.setCompanyId(kaleoDefinitionVersion.getCompanyId());

			return kaleoDefinitionLocalService.fetchKaleoDefinition(
				kaleoDefinitionVersion.getName(), serviceContext);
		}

		return null;
	}

	protected KaleoDefinitionVersion getKaleoDefinitionVersion(
		long kaleoDefinitionVersionId) {

		return kaleoDefinitionVersionLocalService.fetchKaleoDefinitionVersion(
			kaleoDefinitionVersionId);
	}

	@Reference
	protected KaleoDefinitionLocalService kaleoDefinitionLocalService;

	@Reference
	protected KaleoDefinitionVersionLocalService
		kaleoDefinitionVersionLocalService;

}