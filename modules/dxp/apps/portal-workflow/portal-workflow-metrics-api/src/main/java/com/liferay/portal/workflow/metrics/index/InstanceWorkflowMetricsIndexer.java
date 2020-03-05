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

package com.liferay.portal.workflow.metrics.index;

import com.liferay.portal.search.document.Document;

import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author Rafael Praxedes
 */
public interface InstanceWorkflowMetricsIndexer {

	public Document add(
		long companyId, long instanceId, Map<Locale, String> assetTitleMap,
		Map<Locale, String> assetTypeMap, String className, long classPK,
		Date completionDate, Date createDate, Date modifiedDate, long duration,
		long processId, String processVersion, long userId, String userName);

	public Document add(
		long companyId, long instanceId, Map<Locale, String> assetTitleMap,
		Map<Locale, String> assetTypeMap, String className, long classPK,
		Date createDate, Date modifiedDate, long processId,
		String processVersion, long userId, String userName);

	public Document complete(
		long companyId, long instanceId, Date completionDate, Date modifiedDate, long duration);

	public void delete(long companyId, long instanceId);

	public Document update(
		long companyId, long instanceId, Map<Locale, String> assetTitleMap,
		Map<Locale, String> assetTypeMap, Date modifiedDate);

}