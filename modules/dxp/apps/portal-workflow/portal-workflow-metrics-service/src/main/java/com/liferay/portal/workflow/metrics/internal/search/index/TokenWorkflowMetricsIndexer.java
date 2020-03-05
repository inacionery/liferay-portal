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

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.HashMapBuilder;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinition;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.metrics.index.TaskWorkflowMetricsIndexer;

import java.time.Duration;

import java.util.Date;
import java.util.HashMap;
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
		TaskWorkflowMetricsIndexer.class, TokenWorkflowMetricsIndexer.class
	}
)
public class TokenWorkflowMetricsIndexer
	extends BaseWorkflowMetricsIndexer implements TaskWorkflowMetricsIndexer {

	@Override
	public Document add(
		long companyId, long nodeId, long assigneeId, String className,
		long classPK, boolean completed, long completionUserId,
		Date completionDate, Date createDate, Date modifiedDate,
		long instanceId, boolean instanceCompleted, String name,
		long processId, String processVersion, long taskId, long userId) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, taskId)
		).setString(
			"className", className
		).setLong(
			"classPK", classPK
		).setLong(
			"companyId", companyId
		).setValue(
			"completed", completed
		);

		if (completed) {
			documentBuilder.setDate(
				"completionDate", formatDate(completionDate)
			).setLong(
				"completionUserId", completionUserId
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
			"deleted", false
		);

		if (completed) {
			documentBuilder.setLong(
				"duration", _getDuration(completionDate, createDate));
		}

		documentBuilder.setValue(
			"instanceCompleted", instanceCompleted
		).setLong(
			"instanceId", instanceId
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		).setLong(
			"processId", processId
		).setLong(
			"nodeId", nodeId
		).setString(
			"name", name
		).setLong(
			"taskId", taskId
		).setLong(
			"userId", userId
		).setString(
			"version", processVersion
		);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(() -> addDocument(document));

		return document;
	}

	@Override
	public Document add(
		long companyId, long nodeId, String className, long classPK,
		Date createDate, Date modifiedDate, long instanceId, String name,
		long processId, String processVersion, long taskId, long userId) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, taskId)
		).setString(
			"className", className
		).setLong(
			"classPK", classPK
		).setLong(
			"companyId", companyId
		).setValue(
			"completed", false
		).setDate(
			"createDate", formatDate(createDate)
		).setValue(
			Field.getSortableFieldName(
				StringBundler.concat(
					"createDate", StringPool.UNDERLINE, "Number")),
			createDate.getTime()
		).setValue(
			"deleted", false
		).setValue(
			"instanceCompleted", false
		).setLong(
			"instanceId", instanceId
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		).setString(
			"name", name
		).setLong(
			"processId", processId
		).setLong(
			"nodeId", nodeId
		).setLong(
			"taskId", taskId
		).setLong(
			"userId", userId
		).setString(
			"version", processVersion
		);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(() -> addDocument(document));

		return document;
	}

	@Override
	public Document complete(
		long companyId, long completionUserId, Date completionDate,
		Date modifiedDate, long duration, long taskId, long userId) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, taskId)
		).setLong(
			"companyId", companyId
		).setValue(
			"completed", true
		).setDate(
			"completionDate", formatDate(completionDate)
		).setLong(
			"completionUserId", completionUserId
		).setValue(
			"deleted", false
		).setLong(
			"duration", duration
		).setLong(
			"duration", duration
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		).setLong(
			"taskId", taskId
		).setLong(
			"userId", userId
		);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(
			() -> {
				updateDocument(document);

				Map<String, Object> fieldsMap = new HashMap<>();

				if (!Objects.isNull(document.getDate("completionDate"))) {
					fieldsMap.put(
						"completionDate", document.getDate("completionDate"));
				}

				if (!Objects.isNull(document.getLong("completionUserId"))) {
					fieldsMap.put(
						"completionUserId",
						document.getLong("completionUserId"));
				}

				if (!Objects.isNull(document.getBoolean("instanceCompleted"))) {
					fieldsMap.put(
						"instanceCompleted",
						document.getBoolean("instanceCompleted"));
				}

				if (!fieldsMap.isEmpty()) {
					BooleanQuery booleanQuery = queries.booleanQuery();

					booleanQuery.addMustQueryClauses(
						queries.term("companyId",document.getLong("companyId")),
						queries.term("taskId", document.getLong("taskId")));

					_slaTaskResultWorkflowMetricsIndexer.updateDocuments(
						fieldsMap, booleanQuery);
				}
			});


		return document;
	}

	@Override
	public void delete(long companyId, long taskId) {
		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(Field.UID, digest(companyId, taskId));

		workflowMetricsPortalExecutor.execute(
			() -> deleteDocument(documentBuilder));
	}

	@Override
	public String getIndexName() {
		return "workflow-metrics-tokens";
	}

	@Override
	public String getIndexType() {
		return "WorkflowMetricsTokenType";
	}

	@Override
	public void reindex(long companyId) throws PortalException {
		ActionableDynamicQuery actionableDynamicQuery =
			kaleoTaskInstanceTokenLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property companyIdProperty = PropertyFactoryUtil.forName(
					"companyId");

				dynamicQuery.add(companyIdProperty.eq(companyId));
			});
		actionableDynamicQuery.setPerformActionMethod(
			(KaleoTaskInstanceToken kaleoTaskInstanceToken) ->
				workflowMetricsPortalExecutor.execute(
					() -> {
						KaleoDefinitionVersion kaleoDefinitionVersion =
							getKaleoDefinitionVersion(
								kaleoTaskInstanceToken.
									getKaleoDefinitionVersionId());

						KaleoDefinition kaleoDefinition = getKaleoDefinition(
							kaleoTaskInstanceToken.
								getKaleoDefinitionVersionId());

						if (Objects.isNull(kaleoDefinition) ||
							Objects.isNull(kaleoDefinitionVersion)) {

							return;
						}

						KaleoTaskAssignmentInstance
							kaleoTaskAssignmentInstance =
								kaleoTaskAssignmentInstanceLocalService.
									fetchFirstKaleoTaskAssignmentInstance(
										kaleoTaskInstanceToken.
											getKaleoTaskInstanceTokenId(),
										User.class.getName(), null);

						Long assigneeId = null;

						if (kaleoTaskAssignmentInstance != null) {
							assigneeId =
								kaleoTaskAssignmentInstance.
									getAssigneeClassPK();
						}

						if (kaleoTaskInstanceToken.isCompleted()) {
							KaleoInstance kaleoInstance =
								kaleoInstanceLocalService.fetchKaleoInstance(
									kaleoTaskInstanceToken.
										getKaleoInstanceId());

							add(
								kaleoTaskInstanceToken.getCompanyId(),
								kaleoTaskInstanceToken.getKaleoTaskId(),
								assigneeId,
								kaleoTaskInstanceToken.getClassName(),
								kaleoTaskInstanceToken.getClassPK(),
								kaleoTaskInstanceToken.isCompleted(),
								kaleoTaskInstanceToken.getCompletionUserId(),
								kaleoTaskInstanceToken.getCompletionDate(),
								kaleoTaskInstanceToken.getCreateDate(),
								kaleoTaskInstanceToken.getModifiedDate(),
								kaleoTaskInstanceToken.getKaleoInstanceId(),
								kaleoInstance.isCompleted(),
								kaleoTaskInstanceToken.getKaleoTaskName(),
								kaleoDefinition.getKaleoDefinitionId(),
								kaleoDefinitionVersion.getVersion(),
								kaleoTaskInstanceToken.
									getKaleoTaskInstanceTokenId(),
								kaleoTaskInstanceToken.getUserId());
						}
						else {
							add(
								kaleoTaskInstanceToken.getCompanyId(),
								kaleoTaskInstanceToken.getKaleoTaskId(),
								kaleoTaskInstanceToken.getClassName(),
								kaleoTaskInstanceToken.getClassPK(),
								kaleoTaskInstanceToken.getCreateDate(),
								kaleoTaskInstanceToken.getModifiedDate(),
								kaleoTaskInstanceToken.getKaleoInstanceId(),
								kaleoTaskInstanceToken.getKaleoTaskName(),
								kaleoDefinition.getKaleoDefinitionId(),
								kaleoDefinitionVersion.getVersion(),
								kaleoTaskInstanceToken.
									getKaleoTaskInstanceTokenId(),
								kaleoTaskInstanceToken.getUserId());
						}
					}));

		actionableDynamicQuery.performActions();
	}

	@Override
	public Document update(
		long companyId, Long assigneeId, Date modifiedDate, long taskId,
		long userId) {

		DocumentBuilder documentBuilder = documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID, digest(companyId, taskId)
		).setLong(
			"assigneeId", assigneeId
		).setLong(
			"companyId", companyId
		).setDate(
			"modifiedDate", formatDate(modifiedDate)
		).setLong(
			"taskId", taskId
		).setLong(
			"userId", userId
		);

		Document document = documentBuilder.build();

		workflowMetricsPortalExecutor.execute(
			() -> {
				updateDocument(document);

				if (!Objects.isNull(document.getLong("assigneeId"))) {
					BooleanQuery booleanQuery = queries.booleanQuery();

					booleanQuery.addMustQueryClauses(
						queries.term("companyId", document.getLong("companyId")),
						queries.term("taskId", document.getLong("taskId")));

					_slaTaskResultWorkflowMetricsIndexer.updateDocuments(
						HashMapBuilder.<String, Object>put(
							"assigneeId",
							document.getLong("assigneeId")).build(),
						booleanQuery);
				}

			});

		return document;
	}

	private long _getDuration(Date completionDate, Date createDate) {
		Duration duration = Duration.between(
			createDate.toInstant(), completionDate.toInstant());

		return duration.toMillis();
	}

	@Reference
	private SLATaskResultWorkflowMetricsIndexer
		_slaTaskResultWorkflowMetricsIndexer;

}