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

package com.liferay.portal.workflow.metrics.internal.search.index;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.security.permission.ResourceActionsUtil;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinition;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.metrics.index.InstanceWorkflowMetricsIndexer;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(
	immediate = true,
	service = {
		InstanceWorkflowMetricsIndexer.class,
		InstanceWorkflowMetricsIndexerImpl.class
	}
)
public class InstanceWorkflowMetricsIndexerImpl
	extends BaseWorkflowMetricsIndexer
	implements InstanceWorkflowMetricsIndexer {

	@Override
	public Document add(
		long companyId, long instanceId, Map<Locale, String> assetTitleMap,
		Map<Locale, String> assetTypeMap, String className, long classPK,
		Date completionDate, Date createDate, Date modifiedDate, long duration,
		long processId, String processVersion, long userId, String userName) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, instanceId)
		).setLong(
			"companyId", companyId
		).setString(
			"className", className
		).setLong(
			"classPK", classPK
		).setString(
			"className", className
		).setValue(
			"completed", completionDate != null
		);

		if (completionDate != null) {
			documentBuilder.setDate(
				"completionDate", formatDate(completionDate)
			).setValue(
				Field.getSortableFieldName(
					StringBundler.concat(
						"completionDate", StringPool.UNDERLINE, "Number")),
				completionDate.getTime()
			);
		}

		documentBuilder.setDate(
			"createDate", formatDate(createDate)
		).setValue(
			Field.getSortableFieldName(
				StringBundler.concat(
					"createDate", StringPool.UNDERLINE, "Number")),
			createDate.getTime()
		).setValue(
			"deleted", Boolean.FALSE
		).setLong(
			"instanceId", instanceId
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		);

		if (completionDate != null) {
			documentBuilder.setLong("duration", duration);
		}

		documentBuilder.setLong(
			"processId", processId
		).setString(
			"version", processVersion
		).setLong(
			"userId", userId
		).setString(
			"userName", userName
		);

		setLocalizedField(documentBuilder, "assetTitle", assetTitleMap);
		setLocalizedField(documentBuilder, "assetType", assetTypeMap);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(() -> addDocument(document));

		return document;
	}

	@Override
	public Document add(
		long companyId, long instanceId, Map<Locale, String> assetTitleMap,
		Map<Locale, String> assetTypeMap, String className, long classPK,
		Date createDate, Date modifiedDate, long processId,
		String processVersion, long userId, String userName) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, instanceId)
		).setLong(
			"companyId", companyId
		).setString(
			"className", className
		).setLong(
			"classPK", classPK
		).setString(
			"className", className
		).setDate(
			"createDate", formatDate(createDate)
		).setValue(
			Field.getSortableFieldName(
				StringBundler.concat(
					"createDate", StringPool.UNDERLINE, "Number")),
			createDate.getTime()
		).setValue(
			"completed", Boolean.FALSE
		).setValue(
			"deleted", Boolean.FALSE
		).setLong(
			"instanceId", instanceId
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		).setLong(
			"processId", processId
		).setString(
			"version", processVersion
		).setLong(
			"userId", userId
		).setString(
			"userName", userName
		);

		setLocalizedField(documentBuilder, "assetTitle", assetTitleMap);
		setLocalizedField(documentBuilder, "assetType", assetTypeMap);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(() -> addDocument(document));

		return document;
	}

	@Override
	public Document complete(
		long companyId, long instanceId, Date completionDate, Date modifiedDate,
		long duration) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, instanceId)
		).setLong(
			"companyId", companyId
		).setValue(
			"completed", completionDate != null
		).setDate(
			"completionDate", formatDate(completionDate)
		).setValue(
			Field.getSortableFieldName(
				StringBundler.concat(
					"completionDate", StringPool.UNDERLINE, "Number")),
			completionDate.getTime()
		).setLong(
			"instanceId", instanceId
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		).setLong(
			"duration", duration
		);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(
			() -> {
				updateDocument(document);

				BooleanQuery booleanQuery = queries.booleanQuery();

				booleanQuery.addMustQueryClauses(
					queries.term("companyId", document.getLong("companyId")),
					queries.term("instanceId", document.getLong("instanceId")));

				_slaInstanceResultWorkflowMetricsIndexer.updateDocuments(
					HashMapBuilder.<String, Object>put(
						"completionDate", document.getDate("completionDate")
					).put(
						"instanceCompleted", Boolean.TRUE
					).build(),
					booleanQuery);

				_slaTaskResultWorkflowMetricsIndexer.updateDocuments(
					HashMapBuilder.<String, Object>put(
						"instanceCompleted", Boolean.TRUE
					).build(),
					booleanQuery);

				_taskWorkflowMetricsIndexerImpl.updateDocuments(
					HashMapBuilder.<String, Object>put(
						"instanceCompleted", Boolean.TRUE
					).build(),
					booleanQuery);

				System.out.println(
					StringBundler.concat(
						Thread.currentThread(
						).getName(),
						CharPool.COMMA, CharPool.SPACE,
						"complete tokens with instanceId ",
						document.getLong("instanceId"), " and companyId ",
						document.getLong("companyId")));
			});

		return document;
	}

	@Override
	public void delete(long companyId, long instanceId) {
		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(Field.UID, digest(companyId, instanceId));

		workflowMetricsPortalExecutor.execute(
			() -> deleteDocument(documentBuilder));
	}

	@Override
	public void deleteDocument(Document document) {
		super.deleteDocument(document);

		_slaInstanceResultWorkflowMetricsIndexer.deleteDocuments(
			document.getLong("companyId"), document.getLong("instanceId"));

		_slaTaskResultWorkflowMetricsIndexer.deleteDocuments(
			document.getLong("companyId"), document.getLong("instanceId"));
	}

	@Override
	public String getIndexName() {
		return "workflow-metrics-instances";
	}

	@Override
	public String getIndexType() {
		return "WorkflowMetricsInstanceType";
	}

	@Override
	public void reindex(long companyId) throws PortalException {
		ActionableDynamicQuery actionableDynamicQuery =
			kaleoInstanceLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property companyIdProperty = PropertyFactoryUtil.forName(
					"companyId");

				dynamicQuery.add(companyIdProperty.eq(companyId));
			});
		actionableDynamicQuery.setPerformActionMethod(
			(KaleoInstance kaleoInstance) -> {
				KaleoDefinitionVersion kaleoDefinitionVersion =
					getKaleoDefinitionVersion(
						kaleoInstance.getKaleoDefinitionVersionId());

				KaleoDefinition kaleoDefinition = getKaleoDefinition(
					kaleoInstance.getKaleoDefinitionVersionId());

				if (Objects.isNull(kaleoDefinition) ||
					Objects.isNull(kaleoDefinitionVersion)) {

					return;
				}

				long duration = 0;

				add(
					companyId, kaleoInstance.getKaleoInstanceId(),
					_createAssetTitleLocalizationMap(kaleoInstance),
					_createAssetTypeLocalizationMap(kaleoInstance),
					kaleoInstance.getClassName(), kaleoInstance.getClassPK(),
					kaleoInstance.getCompletionDate(),
					kaleoInstance.getCreateDate(),
					kaleoInstance.getModifiedDate(), duration,
					kaleoDefinition.getKaleoDefinitionId(),
					kaleoDefinitionVersion.getVersion(),
					kaleoInstance.getUserId(), kaleoInstance.getUserName());
			});

		actionableDynamicQuery.performActions();
	}

	@Override
	public Document update(
		long companyId, long instanceId, Map<Locale, String> assetTitleMap,
		Map<Locale, String> assetTypeMap, Date modifiedDate) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, instanceId)
		).setLong(
			"companyId", companyId
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		);

		setLocalizedField(documentBuilder, "assetTitle", assetTitleMap);
		setLocalizedField(documentBuilder, "assetType", assetTypeMap);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(() -> updateDocument(document));

		return document;
	}

	private Map<Locale, String> _createAssetTitleLocalizationMap(
		KaleoInstance kaleoInstance) {

		try {
			AssetRenderer<?> assetRenderer = _getAssetRenderer(
				kaleoInstance.getClassName(), kaleoInstance.getClassPK());

			if (assetRenderer != null) {
				AssetEntry assetEntry = assetEntryLocalService.getEntry(
					assetRenderer.getClassName(), assetRenderer.getClassPK());

				return LocalizationUtil.populateLocalizationMap(
					assetEntry.getTitleMap(), assetEntry.getDefaultLanguageId(),
					assetEntry.getGroupId());
			}
		}
		catch (PortalException portalException) {
			if (_log.isDebugEnabled()) {
				_log.debug(portalException, portalException);
			}
		}

		WorkflowHandler<?> workflowHandler =
			WorkflowHandlerRegistryUtil.getWorkflowHandler(
				kaleoInstance.getClassName());

		Map<Locale, String> localizationMap = new HashMap<>();

		for (Locale availableLocale :
				LanguageUtil.getAvailableLocales(kaleoInstance.getGroupId())) {

			localizationMap.put(
				availableLocale,
				workflowHandler.getTitle(
					kaleoInstance.getClassPK(), availableLocale));
		}

		return localizationMap;
	}

	private Map<Locale, String> _createAssetTypeLocalizationMap(
		KaleoInstance kaleoInstance) {

		Map<Locale, String> localizationMap = new HashMap<>();

		for (Locale availableLocale :
				LanguageUtil.getAvailableLocales(kaleoInstance.getGroupId())) {

			localizationMap.put(
				availableLocale,
				ResourceActionsUtil.getModelResource(
					availableLocale, kaleoInstance.getClassName()));
		}

		return localizationMap;
	}

	private AssetRenderer<?> _getAssetRenderer(String className, long classPK)
		throws PortalException {

		AssetRendererFactory<?> assetRendererFactory = _getAssetRendererFactory(
			className);

		if (assetRendererFactory != null) {
			return assetRendererFactory.getAssetRenderer(classPK);
		}

		return null;
	}

	private AssetRendererFactory<?> _getAssetRendererFactory(String className) {
		return AssetRendererFactoryRegistryUtil.
			getAssetRendererFactoryByClassName(className);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		InstanceWorkflowMetricsIndexerImpl.class);

	@Reference
	private SLAInstanceResultWorkflowMetricsIndexer
		_slaInstanceResultWorkflowMetricsIndexer;

	@Reference
	private SLATaskResultWorkflowMetricsIndexer
		_slaTaskResultWorkflowMetricsIndexer;

	@Reference
	private TaskWorkflowMetricsIndexerImpl _taskWorkflowMetricsIndexerImpl;

}