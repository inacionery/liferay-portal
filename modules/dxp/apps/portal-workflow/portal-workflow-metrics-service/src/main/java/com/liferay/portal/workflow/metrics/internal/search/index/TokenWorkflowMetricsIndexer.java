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

import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.DocumentImpl;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinition;
import com.liferay.portal.workflow.kaleo.model.KaleoDefinitionVersion;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskAssignmentInstanceLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoTaskInstanceTokenLocalService;
import com.liferay.portal.workflow.metrics.internal.sla.processor.WorkflowMetricsSLATaskResult;
import com.liferay.portal.workflow.metrics.model.WorkflowMetricsSLADefinition;
import com.liferay.portal.workflow.metrics.service.WorkflowMetricsSLADefinitionLocalService;
import com.liferay.portal.workflow.metrics.sla.processor.WorkflowMetricsSLAStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Inácio Nery
 */
@Component(immediate = true, service = TokenWorkflowMetricsIndexer.class)
public class TokenWorkflowMetricsIndexer extends BaseWorkflowMetricsIndexer {

	public Document createDocument(
		KaleoTaskInstanceToken kaleoTaskInstanceToken) {

		Document document = new DocumentImpl();

		document.addUID(
			"WorkflowMetricsToken",
			digest(
				kaleoTaskInstanceToken.getCompanyId(),
				kaleoTaskInstanceToken.getKaleoDefinitionVersionId(),
				kaleoTaskInstanceToken.getKaleoInstanceId(),
				kaleoTaskInstanceToken.getKaleoTaskId(),
				kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId()));

		KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance =
			_kaleoTaskAssignmentInstanceLocalService.
				fetchFirstKaleoTaskAssignmentInstance(
					kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId(),
					User.class.getName(), null);

		if (kaleoTaskAssignmentInstance != null) {
			document.addKeyword(
				"assigneeId", kaleoTaskAssignmentInstance.getAssigneeClassPK());
		}

		document.addKeyword("className", kaleoTaskInstanceToken.getClassName());
		document.addKeyword("classPK", kaleoTaskInstanceToken.getClassPK());
		document.addKeyword("companyId", kaleoTaskInstanceToken.getCompanyId());
		document.addKeyword("completed", kaleoTaskInstanceToken.isCompleted());

		Date completionDate = kaleoTaskInstanceToken.getCompletionDate();

		if (kaleoTaskInstanceToken.isCompleted()) {
			document.addDateSortable("completionDate", completionDate);
			document.addKeyword(
				"completionUserId",
				kaleoTaskInstanceToken.getCompletionUserId());
		}

		Date createDate = kaleoTaskInstanceToken.getCreateDate();

		document.addDateSortable("createDate", createDate);

		document.addKeyword("deleted", false);

		if (kaleoTaskInstanceToken.isCompleted()) {
			Duration duration = Duration.between(
				createDate.toInstant(), completionDate.toInstant());

			document.addNumber("duration", duration.toMillis());
		}

		KaleoInstance kaleoInstance =
			_kaleoInstanceLocalService.fetchKaleoInstance(
				kaleoTaskInstanceToken.getKaleoInstanceId());

		if (kaleoInstance != null) {
			document.addKeyword(
				"instanceCompleted", kaleoInstance.isCompleted());
		}

		document.addKeyword(
			"instanceId", kaleoTaskInstanceToken.getKaleoInstanceId());
		document.addDateSortable(
			"modifiedDate", kaleoTaskInstanceToken.getModifiedDate());

		KaleoDefinition kaleoDefinition = getKaleoDefinition(
			kaleoTaskInstanceToken.getKaleoDefinitionVersionId());

		if (kaleoDefinition != null) {
			document.addKeyword(
				"processId", kaleoDefinition.getKaleoDefinitionId());
		}

		document.addKeyword("taskId", kaleoTaskInstanceToken.getKaleoTaskId());
		document.addKeyword(
			"taskName", kaleoTaskInstanceToken.getKaleoTaskName());
		document.addKeyword(
			"tokenId", kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());
		document.addKeyword("userId", kaleoTaskInstanceToken.getUserId());

		KaleoDefinitionVersion kaleoDefinitionVersion =
			getKaleoDefinitionVersion(
				kaleoTaskInstanceToken.getKaleoDefinitionVersionId());

		if (kaleoDefinitionVersion != null) {
			document.addKeyword("version", kaleoDefinitionVersion.getVersion());
		}

		return document;
	}

	public void addDocument(Document document) {
		if (searchEngineAdapter == null) {
			return;
		}
		
		super.addDocument(document);

		List<WorkflowMetricsSLADefinition> workflowMetricsSLADefinitions =
			_workflowMetricsSLADefinitionLocalService.
				getWorkflowMetricsSLADefinitions(
					GetterUtil.getLong(document.get("companyId")), true,
					GetterUtil.getLong(document.get("processId")),
					WorkflowConstants.STATUS_APPROVED, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null);
		
		List<WorkflowMetricsSLATaskResult> workflowMetricsSLATaskResults =
				new ArrayList<WorkflowMetricsSLATaskResult>();

		for (WorkflowMetricsSLADefinition workflowMetricsSLADefinition :
				workflowMetricsSLADefinitions) {

				WorkflowMetricsSLATaskResult workflowMetricsSLATaskResult =
					_createtWorkflowMetricsSLATaskResult(
						document, workflowMetricsSLADefinition);

				if (workflowMetricsSLATaskResult != null) {
					workflowMetricsSLATaskResults.add(
						workflowMetricsSLATaskResult);
				}
		}

		_slaTaskResultWorkflowMetricsIndexer.addDocuments(
			workflowMetricsSLATaskResults);

	}
	
	protected WorkflowMetricsSLATaskResult
		_createtWorkflowMetricsSLATaskResult(
			Document tokenDocument,
			WorkflowMetricsSLADefinition workflowMetricsSLADefinition) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();
	
		searchSearchRequest.setIndexNames(
			"workflow-metrics-sla-instance-results");
	
		BooleanQuery booleanQuery = queries.booleanQuery();
	
		booleanQuery.addMustQueryClauses(
			queries.term(
				"companyId", workflowMetricsSLADefinition.getCompanyId()),
			queries.term("deleted", false),
			queries.term(
				"processId", workflowMetricsSLADefinition.getProcessId()), 
			queries.term(
				"slaDefinitionId",
				workflowMetricsSLADefinition.
					getWorkflowMetricsSLADefinitionId()));

		searchSearchRequest.setQuery(booleanQuery);

		return Stream.of(
			searchEngineAdapter.execute(searchSearchRequest)
		).map(
			SearchSearchResponse::getSearchHits
		).map(
			SearchHits::getSearchHits
		).flatMap(
			List::parallelStream
		).map(
			SearchHit::getDocument
		).findFirst(
		).map(
			document -> new WorkflowMetricsSLATaskResult() {
				{
					setCompanyId(
						workflowMetricsSLADefinition.getCompanyId());
					setInstanceId(document.getLong("instanceId"));
					setLastCheckLocalDateTime(
						LocalDateTime.parse(
							document.getString("lastCheckDate"),
							_dateTimeFormatter));
					setOnTime(
						GetterUtil.getBoolean(document.getValue("onTime")));
					setProcessId(
						workflowMetricsSLADefinition.getProcessId());
					setSLADefinitionId(
						workflowMetricsSLADefinition.
							getWorkflowMetricsSLADefinitionId());
					setTaskId(GetterUtil.getLong(tokenDocument.get("taskId")));
					setTaskName(tokenDocument.get("taskName"));
					setTokenId(GetterUtil.getLong(tokenDocument.get("tokenId")));
					setWorkflowMetricsSLAStatus(
						WorkflowMetricsSLAStatus.valueOf(
							document.getString("status")));
				}
			}
		).orElseGet(
			() -> null
		);
	}

	private final DateTimeFormatter _dateTimeFormatter =
		DateTimeFormatter.ofPattern(
			PropsUtil.get(PropsKeys.INDEX_DATE_FORMAT_PATTERN));
	
	@Override
	public void updateDocument(Document document) {
		super.updateDocument(document);

		if (GetterUtil.getBoolean(document.get("completed"))) {
			BooleanQuery booleanQuery = queries.booleanQuery();

			booleanQuery.addMustQueryClauses(
				queries.term(
					"companyId", GetterUtil.getLong(document.get("companyId"))),
				queries.term(
					"instanceId",
					GetterUtil.getLong(document.get("instanceId"))));

			_slaTaskResultWorkflowMetricsIndexer.updateDocuments(
				documentImpl -> new DocumentImpl() {
					{
						addKeyword(
							Field.UID, documentImpl.getString(Field.UID));

						try {
							addDateSortable(
								"completionDate",
								document.getDate("completionDate"));
						}
						catch (Exception e) {
						}

						addKeyword(
							"completionUserId",
							GetterUtil.getLong(
								document.get("completionUserId")));
						addKeyword(
							"instanceCompleted", true);
						addKeyword(
							"status",
							WorkflowMetricsSLAStatus.COMPLETED.name());
					}
				},
				booleanQuery);
		}
	}

	@Override
	protected String getIndexName() {
		return "workflow-metrics-tokens";
	}

	@Override
	protected String getIndexType() {
		return "WorkflowMetricsTokenType";
	}

	@Override
	protected void reindex(long companyId) throws PortalException {
		ActionableDynamicQuery actionableDynamicQuery =
			_kaleoTaskInstanceTokenLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property companyIdProperty = PropertyFactoryUtil.forName(
					"companyId");

				dynamicQuery.add(companyIdProperty.eq(companyId));
			});
		actionableDynamicQuery.setPerformActionMethod(
			(KaleoTaskInstanceToken kaleoTaskInstanceToken) ->
				workflowMetricsPortalExecutor.execute(
					() -> super.addDocument(
						createDocument(kaleoTaskInstanceToken))));

		actionableDynamicQuery.performActions();
	}

	@Reference
	private WorkflowMetricsSLADefinitionLocalService
		_workflowMetricsSLADefinitionLocalService;

	@Reference
	private KaleoInstanceLocalService _kaleoInstanceLocalService;

	@Reference
	private KaleoTaskAssignmentInstanceLocalService
		_kaleoTaskAssignmentInstanceLocalService;

	@Reference
	private KaleoTaskInstanceTokenLocalService
		_kaleoTaskInstanceTokenLocalService;

	@Reference
	private SLAInstanceResultWorkflowMetricsIndexer
		_slaInstanceResultWorkflowMetricsIndexer;

	@Reference
	private SLATaskResultWorkflowMetricsIndexer
		_slaTaskResultWorkflowMetricsIndexer;

}