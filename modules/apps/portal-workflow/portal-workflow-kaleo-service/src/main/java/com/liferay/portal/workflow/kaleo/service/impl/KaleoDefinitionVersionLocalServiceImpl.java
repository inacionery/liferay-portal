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

package com.liferay.portal.workflow.kaleo.service.impl;

import com.liferay.exportimport.kernel.staging.Staging;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.GroupBy;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.workflow.exception.IncompleteWorkflowInstancesException;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.service.KaleoConditionLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoNodeLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTransitionLocalService;
import com.liferay.portal.workflow.kaleo.service.base.KaleoDefinitionVersionLocalServiceBaseImpl;
import com.liferay.portal.workflow.kaleo.util.comparator.KaleoDefinitionVersionIdComparator;

import java.io.Serializable;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	property = "model.class.name=com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion",
	service = AopService.class
)
public class KaleoDefinitionVersionLocalServiceImpl
	extends KaleoDefinitionVersionLocalServiceBaseImpl {

	@Override
	public KaleoDefinitionVersion addKaleoDefinitionVersion(
			String name, String title, String description, String content,
			String version, ServiceContext serviceContext)
		throws PortalException {

		// Kaleo definition version

		Date createDate = serviceContext.getCreateDate(new Date());
		Date modifiedDate = serviceContext.getModifiedDate(new Date());
		User user = userLocalService.getUser(serviceContext.getGuestOrUserId());

		long kaleoDefinitionVersionId = counterLocalService.increment();

		KaleoDefinitionVersion kaleoDefinitionVersion =
			kaleoDefinitionVersionPersistence.create(kaleoDefinitionVersionId);

		long groupId = _staging.getLiveGroupId(
			serviceContext.getScopeGroupId());

		kaleoDefinitionVersion.setGroupId(groupId);

		kaleoDefinitionVersion.setCompanyId(user.getCompanyId());
		kaleoDefinitionVersion.setUserId(user.getUserId());
		kaleoDefinitionVersion.setUserName(user.getFullName());
		kaleoDefinitionVersion.setCreateDate(createDate);
		kaleoDefinitionVersion.setModifiedDate(modifiedDate);
		kaleoDefinitionVersion.setName(name);
		kaleoDefinitionVersion.setTitle(title);
		kaleoDefinitionVersion.setDescription(description);
		kaleoDefinitionVersion.setContent(content);
		kaleoDefinitionVersion.setVersion(version);

		int status = GetterUtil.getInteger(
			serviceContext.getAttribute("status"),
			WorkflowConstants.STATUS_APPROVED);

		kaleoDefinitionVersion.setStatus(status);

		kaleoDefinitionVersion.setStatusByUserId(user.getUserId());
		kaleoDefinitionVersion.setStatusByUserName(user.getFullName());
		kaleoDefinitionVersion.setStatusDate(modifiedDate);

		kaleoDefinitionVersionPersistence.update(kaleoDefinitionVersion);

		return kaleoDefinitionVersion;
	}

	@Override
	public KaleoDefinitionVersion deleteKaleoDefinitionVersion(
			KaleoDefinitionVersion kaleoDefinitionVersion)
		throws PortalException {

		// Kaleo definition version

		int kaleoInstancesCount =
			_kaleoInstanceLocalService.getKaleoInstancesCount(
				kaleoDefinitionVersion.getKaleoDefinitionVersionId(), false);

		if (kaleoInstancesCount > 0) {
			throw new IncompleteWorkflowInstancesException(kaleoInstancesCount);
		}

		kaleoDefinitionVersionPersistence.remove(kaleoDefinitionVersion);

		// Kaleo condition

		_kaleoConditionLocalService.deleteKaleoDefinitionVersionKaleoCondition(
			kaleoDefinitionVersion.getKaleoDefinitionVersionId());

		// Kaleo instances

		_kaleoInstanceLocalService.deleteKaleoDefinitionVersionKaleoInstances(
			kaleoDefinitionVersion.getKaleoDefinitionVersionId());

		// Kaleo nodes

		_kaleoNodeLocalService.deleteKaleoDefinitionVersionKaleoNodes(
			kaleoDefinitionVersion.getKaleoDefinitionVersionId());

		// Kaleo tasks

		_kaleoTaskLocalService.deleteKaleoDefinitionVersionKaleoTasks(
			kaleoDefinitionVersion.getKaleoDefinitionVersionId());

		// Kaleo transitions

		_kaleoTransitionLocalService.
			deleteKaleoDefinitionVersionKaleoTransitions(
				kaleoDefinitionVersion.getKaleoDefinitionVersionId());

		return kaleoDefinitionVersion;
	}

	@Override
	public void deleteKaleoDefinitionVersion(
			long companyId, String name, String version)
		throws PortalException {

		KaleoDefinitionVersion kaleoDefinitionVersion =
			getKaleoDefinitionVersion(companyId, name, version);

		deleteKaleoDefinitionVersion(kaleoDefinitionVersion);
	}

	@Override
	public void deleteKaleoDefinitionVersions(
			List<KaleoDefinitionVersion> kaleoDefinitionVersions)
		throws PortalException {

		for (KaleoDefinitionVersion kaleoDefinitionVersion :
				kaleoDefinitionVersions) {

			deleteKaleoDefinitionVersion(kaleoDefinitionVersion);
		}
	}

	@Override
	public void deleteKaleoDefinitionVersions(long companyId, String name)
		throws PortalException {

		List<KaleoDefinitionVersion> kaleoDefinitionVersions =
			getKaleoDefinitionVersions(companyId, name);

		deleteKaleoDefinitionVersions(kaleoDefinitionVersions);
	}

	@Override
	public KaleoDefinitionVersion fetchKaleoDefinitionVersion(
		long companyId, String name, String version) {

		return kaleoDefinitionVersionPersistence.fetchByC_N_V(
			companyId, name, version);
	}

	@Override
	public KaleoDefinitionVersion fetchLatestKaleoDefinitionVersion(
			long companyId, String name)
		throws PortalException {

		return kaleoDefinitionVersionPersistence.fetchByC_N_First(
			companyId, name, new KaleoDefinitionVersionIdComparator(false));
	}

	@Override
	public KaleoDefinitionVersion fetchLatestKaleoDefinitionVersion(
			long companyId, String name,
			OrderByComparator<KaleoDefinitionVersion> orderByComparator)
		throws PortalException {

		return kaleoDefinitionVersionPersistence.fetchByC_N_Last(
			companyId, name, orderByComparator);
	}

	@Override
	public KaleoDefinitionVersion getFirstKaleoDefinitionVersion(
			long companyId, String name)
		throws PortalException {

		return kaleoDefinitionVersionPersistence.findByC_N_First(
			companyId, name, new KaleoDefinitionVersionIdComparator(true));
	}

	@Override
	public KaleoDefinitionVersion getKaleoDefinitionVersion(
			long companyId, String name, String version)
		throws PortalException {

		return kaleoDefinitionVersionPersistence.findByC_N_V(
			companyId, name, version);
	}

	@Override
	public List<KaleoDefinitionVersion> getKaleoDefinitionVersions(
		long companyId, int start, int end,
		OrderByComparator<KaleoDefinitionVersion> orderByComparator) {

		return kaleoDefinitionVersionPersistence.findByCompanyId(
			companyId, start, end, orderByComparator);
	}

	@Override
	public List<KaleoDefinitionVersion> getKaleoDefinitionVersions(
			long companyId, String name)
		throws PortalException {

		return kaleoDefinitionVersionPersistence.findByC_N(companyId, name);
	}

	@Override
	public List<KaleoDefinitionVersion> getKaleoDefinitionVersions(
		long companyId, String name, int start, int end,
		OrderByComparator<KaleoDefinitionVersion> orderByComparator) {

		return kaleoDefinitionVersionPersistence.findByC_N(
			companyId, name, start, end, orderByComparator);
	}

	@Override
	public int getKaleoDefinitionVersionsCount(long companyId) {
		return kaleoDefinitionVersionPersistence.countByCompanyId(companyId);
	}

	@Override
	public int getKaleoDefinitionVersionsCount(long companyId, String name) {
		return kaleoDefinitionVersionPersistence.countByC_N(companyId, name);
	}

	@Override
	public KaleoDefinitionVersion[] getKaleoDefinitionVersionsPrevAndNext(
			long companyId, String name, String version)
		throws PortalException {

		KaleoDefinitionVersion kaleoDefinitionVersion =
			kaleoDefinitionVersionPersistence.findByC_N_V(
				companyId, name, version);

		return kaleoDefinitionVersionPersistence.findByC_N_PrevAndNext(
			kaleoDefinitionVersion.getKaleoDefinitionVersionId(), companyId,
			name, new KaleoDefinitionVersionIdComparator(true));
	}

	@Override
	public KaleoDefinitionVersion getLatestKaleoDefinitionVersion(
			long companyId, String name)
		throws PortalException {

		return kaleoDefinitionVersionPersistence.findByC_N_First(
			companyId, name, new KaleoDefinitionVersionIdComparator(false));
	}

	@Override
	public List<KaleoDefinitionVersion> getLatestKaleoDefinitionVersions(
		long companyId, int start, int end,
		OrderByComparator<KaleoDefinitionVersion> orderByComparator) {

		return getLatestKaleoDefinitionVersions(
			companyId, null, WorkflowConstants.STATUS_ANY, start, end,
			orderByComparator);
	}

	@Override
	public List<KaleoDefinitionVersion> getLatestKaleoDefinitionVersions(
		long companyId, String keywords, int status, int start, int end,
		OrderByComparator<KaleoDefinitionVersion> orderByComparator) {

		return doSearch(
			companyId,
			HashMapBuilder.put(
				"name", (Serializable)keywords
			).put(
				"status", status
			).put(
				"title", keywords
			).build(),
			start, end, orderByComparator);
	}

	@Override
	public int getLatestKaleoDefinitionVersionsCount(
		long companyId, String keywords, int status) {

		return doSearchCount(
			companyId,
			HashMapBuilder.put(
				"name", (Serializable)keywords
			).put(
				"status", status
			).put(
				"title", keywords
			).build());
	}

	protected SearchContext buildSearchContext(
		long companyId, Map<String, Serializable> searchAttributes, int start,
		int end, OrderByComparator<KaleoDefinitionVersion> orderByComparator) {

		SearchContext searchContext = new SearchContext();

		searchContext.setAttributes(searchAttributes);
		searchContext.setCompanyId(companyId);
		searchContext.setEnd(end);

		GroupBy groupBy = new GroupBy(_fieldNameOrderByCols.get("name"));

		groupBy.setSize(1);
		groupBy.setSorts(
			new Sort[] {
				new Sort(
					_fieldNameOrderByCols.get("kaleoDefinitionVersionId"), true)
			});

		searchContext.setGroupBy(groupBy);

		searchContext.setStart(start);

		if (orderByComparator != null) {
			searchContext.setSorts(getSortsFromComparator(orderByComparator));
		}

		return searchContext;
	}

	protected List<KaleoDefinitionVersion> doSearch(
		long companyId, Map<String, Serializable> searchAttributes, int start,
		int end, OrderByComparator<KaleoDefinitionVersion> orderByComparator) {

		try {
			Indexer<KaleoDefinitionVersion> indexer =
				IndexerRegistryUtil.getIndexer(
					KaleoDefinitionVersion.class.getName());

			Hits hits = indexer.search(
				buildSearchContext(
					companyId, searchAttributes, start, end,
					orderByComparator));
			
			return Stream.of(
				hits.getGroupedHits()
			).map(
				document -> GetterUtil.getLong(
					document.get(Field.ENTRY_CLASS_PK))
			).map(
				kaleoDefinitionVersionPersistence::fetchByPrimaryKey
			).filter(
				Objects::nonNull
			).collect(
				Collectors.toList()
			);
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		return Collections.emptyList();
	}

	protected int doSearchCount(
		long companyId, Map<String, Serializable> searchAttributes) {

		try {
			Indexer<KaleoDefinitionVersion> indexer =
				IndexerRegistryUtil.getIndexer(
					KaleoDefinitionVersion.class.getName());

			return (int)indexer.searchCount(
				buildSearchContext(
					companyId, searchAttributes, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null));
		}
		catch (PortalException pe) {
			if (_log.isDebugEnabled()) {
				_log.debug(pe, pe);
			}
		}

		return 0;
	}

	protected Sort[] getSortsFromComparator(
		OrderByComparator<KaleoDefinitionVersion> orderByComparator) {

		if (orderByComparator == null) {
			return null;
		}

		return Stream.of(
			orderByComparator.getOrderByFields()
		).map(
			orderByFieldName -> {
				String fieldName = _fieldNameOrderByCols.getOrDefault(
					orderByFieldName, orderByFieldName);

				return new Sort(
					fieldName,
					_fieldNameSortTypes.getOrDefault(
						fieldName, Sort.STRING_TYPE),
					!orderByComparator.isAscending());
			}
		).toArray(
			Sort[]::new
		);
	}

	private static String _getSortableFieldName(String name, String type) {
		return Field.getSortableFieldName(
			StringBundler.concat(name, StringPool.UNDERLINE, type));
	}

	private static final Log _log = LogFactoryUtil.getLog(
		KaleoDefinitionVersionLocalServiceImpl.class);

	private static final Map<String, String> _fieldNameOrderByCols =
		HashMapBuilder.put(
			"createDate", _getSortableFieldName(Field.CREATE_DATE, "Number")
		).put(
			"kaleoDefinitionVersionId",
			_getSortableFieldName("kaleoDefinitionVersionId", "Number")
		).put(
			"name", _getSortableFieldName("name", "String")
		).put(
			"title", _getSortableFieldName("title", "String")
		).put(
			"userId", _getSortableFieldName(Field.USER_ID, "Number")
		).put(
			"version", _getSortableFieldName("version", "Number")
		).build();
	private static final Map<String, Integer> _fieldNameSortTypes =
		HashMapBuilder.put(
			Field.CREATE_DATE, Sort.LONG_TYPE
		).put(
			Field.MODIFIED_DATE, Sort.LONG_TYPE
		).build();

	@Reference
	private KaleoConditionLocalService _kaleoConditionLocalService;

	@Reference
	private KaleoInstanceLocalService _kaleoInstanceLocalService;

	@Reference
	private KaleoNodeLocalService _kaleoNodeLocalService;

	@Reference
	private KaleoTaskLocalService _kaleoTaskLocalService;

	@Reference
	private KaleoTransitionLocalService _kaleoTransitionLocalService;

	@Reference
	private Staging _staging;

}