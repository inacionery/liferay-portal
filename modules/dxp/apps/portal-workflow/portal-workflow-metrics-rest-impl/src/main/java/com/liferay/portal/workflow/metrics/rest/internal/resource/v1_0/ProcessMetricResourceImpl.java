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

package com.liferay.portal.workflow.metrics.rest.internal.resource.v1_0;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.odata.entity.EntityModel;
import com.liferay.portal.search.aggregation.AggregationResult;
import com.liferay.portal.search.aggregation.Aggregations;
import com.liferay.portal.search.aggregation.bucket.Bucket;
import com.liferay.portal.search.aggregation.bucket.FilterAggregation;
import com.liferay.portal.search.aggregation.bucket.FilterAggregationResult;
import com.liferay.portal.search.aggregation.bucket.TermsAggregation;
import com.liferay.portal.search.aggregation.bucket.TermsAggregationResult;
import com.liferay.portal.search.aggregation.pipeline.BucketSelectorPipelineAggregation;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.engine.adapter.search.CountSearchRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchResponse;
import com.liferay.portal.search.engine.adapter.search.SearchRequestExecutor;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.query.StringQuery;
import com.liferay.portal.search.query.TermsQuery;
import com.liferay.portal.search.script.Scripts;
import com.liferay.portal.search.sort.FieldSort;
import com.liferay.portal.search.sort.SortOrder;
import com.liferay.portal.search.sort.Sorts;
import com.liferay.portal.vulcan.pagination.Page;
import com.liferay.portal.vulcan.pagination.Pagination;
import com.liferay.portal.vulcan.resource.EntityModelResource;
import com.liferay.portal.workflow.metrics.rest.dto.v1_0.Process;
import com.liferay.portal.workflow.metrics.rest.dto.v1_0.ProcessMetric;
import com.liferay.portal.workflow.metrics.rest.internal.dto.v1_0.util.ProcessUtil;
import com.liferay.portal.workflow.metrics.rest.internal.odata.entity.v1_0.ProcessMetricEntityModel;
import com.liferay.portal.workflow.metrics.rest.internal.resource.helper.ResourceHelper;
import com.liferay.portal.workflow.metrics.rest.resource.v1_0.ProcessMetricResource;
import com.liferay.portal.workflow.metrics.search.index.name.WorkflowMetricsIndexNameBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author Rafael Praxedes
 */
@Component(
	properties = "OSGI-INF/liferay/rest/v1_0/process-metric.properties",
	scope = ServiceScope.PROTOTYPE, service = ProcessMetricResource.class
)
public class ProcessMetricResourceImpl
	extends BaseProcessMetricResourceImpl implements EntityModelResource {

	@Override
	public EntityModel getEntityModel(MultivaluedMap multivaluedMap)
		throws Exception {

		return _entityModel;
	}

	@Override
	public ProcessMetric getProcessMetric(
			Long processId, Boolean completed, Date dateEnd, Date dateStart)
		throws Exception {

		Map<Long, ProcessMetric> processMetrics = _getProcessMetrics(
			null, null, processId, null);

		if (processMetrics.isEmpty()) {
			return new ProcessMetric();
		}

		ProcessMetric processMetric = processMetrics.get(processId);

		Bucket instanceBucket = _getInstanceBucket(
			GetterUtil.getBoolean(completed), dateEnd, dateStart, processId);

		if (instanceBucket != null) {
			_setInstanceCount(instanceBucket, processMetric);
			_setOnTimeInstanceCount(instanceBucket, processMetric);
			_setOverdueInstanceCount(instanceBucket, processMetric);
			
			_setUntrackedInstanceCount(processMetric);
		}

		return processMetric;
	}

	@Override
	public Page<ProcessMetric> getProcessMetricsPage(
			String title, Pagination pagination, Sort[] sorts)
		throws Exception {

		long processCount = _getProcessCount(title);

		if (processCount > 0) {
			FieldSort fieldSort = _toFieldSort(sorts);

			if (_isOrderByTitle(fieldSort.getField())) {
				Map<Long, ProcessMetric> processMetrics = _getProcessMetrics(
					fieldSort, pagination, null, title);

				Collection<Bucket> instanceBuckets = _getInstanceBuckets(
					processMetrics.keySet());

				return Page.of(
					_getProcessMetrics(instanceBuckets, processMetrics),
					pagination, processCount);
			}

			Map<Long, Bucket> instanceBuckets = _getInstanceBuckets(
				fieldSort, pagination, title);

			Map<Long, ProcessMetric> processMetrics = _getProcessMetrics(
				instanceBuckets.keySet());

			return Page.of(
				_getProcessMetrics(instanceBuckets.values(), processMetrics),
				pagination, processCount);
		}

		return Page.of(Collections.emptyList());
	}

	private BooleanQuery _createBooleanQuery(boolean completed) {
		BooleanQuery booleanQuery = _queries.booleanQuery();

		return booleanQuery.addShouldQueryClauses(
			_queries.term("completed", completed),
			_queries.term("instanceId", 0));
	}

	private BooleanQuery _createBooleanQuery(
		boolean completed, Date dateEnd, Date dateStart, Set<Long> processIds) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		booleanQuery.setMinimumShouldMatch(1);

		BooleanQuery instancesBooleanQuery = _queries.booleanQuery();

		instancesBooleanQuery.addFilterQueryClauses(
			_queries.term(
				"_index",
				_instanceWorkflowMetricsIndexNameBuilder.getIndexName(
					contextCompany.getCompanyId())));
		instancesBooleanQuery.addMustNotQueryClauses(
			_queries.term("instanceId", 0));
		instancesBooleanQuery.addMustQueryClauses(
			_createInstanceBooleanQuery(
				completed, dateEnd, dateStart, processIds));

		BooleanQuery slaInstanceResultsBooleanQuery = _queries.booleanQuery();

		slaInstanceResultsBooleanQuery.addFilterQueryClauses(
			_queries.term(
				"_index",
				_slaInstanceResultWorkflowMetricsIndexNameBuilder.getIndexName(
					contextCompany.getCompanyId())));
		slaInstanceResultsBooleanQuery.addMustNotQueryClauses(
			_queries.term("slaDefinitionId", 0));
		slaInstanceResultsBooleanQuery.addMustQueryClauses(
			_createSLAInstanceResultsBooleanQuery(
				completed, dateEnd, dateStart, processIds));

		return booleanQuery.addShouldQueryClauses(
			instancesBooleanQuery, slaInstanceResultsBooleanQuery);
	}

	private BucketSelectorPipelineAggregation
		_createBucketSelectorPipelineAggregation() {

		BucketSelectorPipelineAggregation bucketSelectorPipelineAggregation =
			_aggregations.bucketSelector(
				"bucketSelector", _scripts.script("params.instanceCount > 0"));

		bucketSelectorPipelineAggregation.addBucketPath(
			"instanceCount", "instanceCount.value");

		return bucketSelectorPipelineAggregation;
	}

	private BooleanQuery _createCompletionDateBooleanQuery(
		Date dateEnd, Date dateStart) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		return booleanQuery.addShouldQueryClauses(
			_queries.rangeTerm(
				"completionDate", true, true,
				_resourceHelper.getDate(dateStart),
				_resourceHelper.getDate(dateEnd)),
			_queries.term("slaDefinitionId", 0));
	}

	private BooleanQuery _createInstanceBooleanQuery(
		boolean completed, Date dateEnd, Date dateStart, Set<Long> processIds) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		if (completed && (dateEnd != null) && (dateStart != null)) {
			booleanQuery.addMustQueryClauses(
				_queries.rangeTerm(
					"completionDate", true, true,
					_resourceHelper.getDate(dateStart),
					_resourceHelper.getDate(dateEnd)));
		}

		return booleanQuery.addMustQueryClauses(
			_queries.term("deleted", Boolean.FALSE),
			_createBooleanQuery(completed),
			_createProcessIdTermsQuery(processIds));
	}

	private BooleanQuery _createProcessBooleanQuery(
		Long processId, String title) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		if (Validator.isNotNull(processId)) {
			booleanQuery.addMustQueryClauses(
				_queries.term("processId", processId));
		}

		return booleanQuery.addMustQueryClauses(
			_queries.term("deleted", Boolean.FALSE),
			_createTitleBooleanQuery(title));
	}

	private TermsQuery _createProcessIdTermsQuery(Set<Long> processIds) {
		TermsQuery termsQuery = _queries.terms("processId");

		termsQuery.addValues(
			Stream.of(
				processIds
			).flatMap(
				Set::stream
			).map(
				String::valueOf
			).toArray(
				Object[]::new
			));

		return termsQuery;
	}

	private ProcessMetric _createProcessMetric(Document document) {
		return new ProcessMetric() {
			{
				instanceCount = 0L;
				onTimeInstanceCount = 0L;
				overdueInstanceCount = 0L;
				process = ProcessUtil.toProcess(
					document, contextAcceptLanguage.getPreferredLocale());
				untrackedInstanceCount = 0L;
			}
		};
	}

	private BooleanQuery _createSLAInstanceResultsBooleanQuery(
		boolean completed, Date dateEnd, Date dateStart, Set<Long> processIds) {

		BooleanQuery booleanQuery = _queries.booleanQuery();

		if (completed) {
			BooleanQuery shouldBooleanQuery = _queries.booleanQuery();

			shouldBooleanQuery.addShouldQueryClauses(
				_queries.term("slaDefinitionId", 0),
				_queries.term("instanceCompleted", Boolean.TRUE));

			booleanQuery.addMustQueryClauses(shouldBooleanQuery);

			if ((dateEnd != null) && (dateStart != null)) {
				booleanQuery.addMustQueryClauses(
					_createCompletionDateBooleanQuery(dateEnd, dateStart));
			}
		}
		else {
			booleanQuery.addMustQueryClauses(
				_queries.term("instanceCompleted", Boolean.FALSE));
		}

		return booleanQuery.addMustQueryClauses(
			_queries.term("companyId", contextCompany.getCompanyId()),
			_queries.term("deleted", Boolean.FALSE),
			_createProcessIdTermsQuery(processIds));
	}

	private BooleanQuery _createTitleBooleanQuery(String title) {
		BooleanQuery booleanQuery = _queries.booleanQuery();

		if (Validator.isNotNull(title)) {
			StringQuery stringQuery = _queries.string(title + StringPool.STAR);

			stringQuery.setDefaultField(_getTitleFieldName());

			booleanQuery.addShouldQueryClauses(
				stringQuery, _queries.match(_getTitleFieldName(), title));
		}

		return booleanQuery;
	}

	private Bucket _getInstanceBucket(
		boolean completed, Date dateEnd, Date dateStart, long processId) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			"processId", "processId");

		FilterAggregation onTimeFilterAggregation = _aggregations.filter(
			"onTime", _queries.term("slaStatus", "OnTime"));

		FilterAggregation overdueFilterAggregation = _aggregations.filter(
			"overdue", _queries.term("slaStatus", "Overdue"));

		termsAggregation.addChildrenAggregations(
			onTimeFilterAggregation, overdueFilterAggregation);

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_instanceWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));

		BooleanQuery booleanQuery = _queries.booleanQuery();

		searchSearchRequest.setQuery(
			booleanQuery.addFilterQueryClauses(
				_createInstanceBooleanQuery(
					completed, dateEnd, dateStart,
					Collections.singleton(processId))));

		SearchSearchResponse searchSearchResponse =
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResultsMap.get("processId");

		return termsAggregationResult.getBucket(String.valueOf(processId));
	}

	private Map<Long, Bucket> _getInstanceBuckets(
		FieldSort fieldSort, Pagination pagination, String title) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			"processId", "processId");

		termsAggregation.addChildrenAggregations(
			_aggregations.filter(
				"onTime", _queries.term("slaStatus", "OnTime")),
			_aggregations.filter(
				"overdue", _queries.term("slaStatus", "Overdue")));

		termsAggregation.addPipelineAggregation(
			_resourceHelper.createBucketSortPipelineAggregation(
				fieldSort, pagination));

		termsAggregation.setSize(pagination.getPageSize());

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_instanceWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));

		BooleanQuery booleanQuery = _queries.booleanQuery();

		searchSearchRequest.setQuery(
			booleanQuery.addFilterQueryClauses(
				_queries.term("completed", Boolean.FALSE),
				_queries.term("deleted", Boolean.FALSE),
				_createTitleBooleanQuery(title)));

		searchSearchRequest.setSize(0);

		return Stream.of(
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest)
		).map(
			SearchSearchResponse::getAggregationResultsMap
		).map(
			aggregationResultsMap ->
				(TermsAggregationResult)aggregationResultsMap.get("processId")
		).map(
			TermsAggregationResult::getBuckets
		).flatMap(
			Collection::stream
		).collect(
			LinkedHashMap::new,
			(map, bucket) -> map.put(Long.valueOf(bucket.getKey()), bucket),
			Map::putAll
		);
	}

	private Collection<Bucket> _getInstanceBuckets(Set<Long> processIds) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			"processId", "processId");

		termsAggregation.addChildrenAggregations(
			_aggregations.filter(
				"onTime", _queries.term("slaStatus", "OnTime")),
			_aggregations.filter(
				"overdue", _queries.term("slaStatus", "Overdue")));

		termsAggregation.setSize(processIds.size());

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_instanceWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));

		BooleanQuery booleanQuery = _queries.booleanQuery();

		searchSearchRequest.setQuery(
			booleanQuery.addFilterQueryClauses(
				_queries.term("completed", Boolean.FALSE),
				_queries.term("deleted", Boolean.FALSE),
				_createProcessIdTermsQuery(processIds)));

		searchSearchRequest.setSize(0);

		SearchSearchResponse searchSearchResponse =
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResultsMap.get("processId");

		return termsAggregationResult.getBuckets();
	}

	private TermsAggregationResult _getInstanceTermsAggregationResult(
		boolean completed, FieldSort fieldSort, Pagination pagination,
		Set<Long> processIds) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			"processId", "processId");

		BooleanQuery booleanQuery = _queries.booleanQuery();

		FilterAggregation filterAggregation = _aggregations.filter(
			"instanceCountFilter",
			booleanQuery.addMustNotQueryClauses(
				_queries.term("instanceId", "0")));

		filterAggregation.addChildAggregation(
			_aggregations.valueCount("instanceCount", "instanceId"));

		termsAggregation.addChildrenAggregations(filterAggregation);

		if ((fieldSort != null) &&
			_isOrderByInstanceCount(fieldSort.getField())) {

			termsAggregation.addPipelineAggregation(
				_resourceHelper.createBucketSortPipelineAggregation(
					fieldSort, pagination));
		}

		termsAggregation.setSize(processIds.size());

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_instanceWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));
		searchSearchRequest.setQuery(
			_createInstanceBooleanQuery(completed, null, null, processIds));

		SearchSearchResponse searchSearchResponse =
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		return (TermsAggregationResult)aggregationResultsMap.get("processId");
	}

	private Bucket _getProcessBucket(
		boolean completed, Date dateEnd, Date dateStart, long processId) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			"processId", "processId");

		FilterAggregation onTimeFilterAggregation = _aggregations.filter(
			"onTime", _resourceHelper.createMustNotBooleanQuery());

		onTimeFilterAggregation.addChildAggregation(
			_resourceHelper.createOnTimeScriptedMetricAggregation());

		FilterAggregation overdueFilterAggregation = _aggregations.filter(
			"overdue", _resourceHelper.createMustNotBooleanQuery());

		overdueFilterAggregation.addChildAggregation(
			_resourceHelper.createOverdueScriptedMetricAggregation());

		termsAggregation.addChildrenAggregations(
			onTimeFilterAggregation, overdueFilterAggregation,
			_resourceHelper.creatInstanceCountScriptedMetricAggregation(
				Collections.emptyList(), null, dateEnd, dateStart,
				Collections.emptyList()));

		termsAggregation.addPipelineAggregations(
			_createBucketSelectorPipelineAggregation());

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_instanceWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()),
			_slaInstanceResultWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));

		searchSearchRequest.setQuery(
			_createBooleanQuery(
				completed, dateEnd, dateStart,
				Collections.singleton(processId)));

		SearchSearchResponse searchSearchResponse =
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		TermsAggregationResult termsAggregationResult =
			(TermsAggregationResult)aggregationResultsMap.get("processId");

		return termsAggregationResult.getBucket(String.valueOf(processId));
	}

	private long _getProcessCount(String title) {
		CountSearchRequest countSearchRequest = new CountSearchRequest();

		countSearchRequest.setIndexNames(
			_processWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));

		BooleanQuery booleanQuery = _queries.booleanQuery();

		countSearchRequest.setQuery(
			booleanQuery.addFilterQueryClauses(
				_createProcessBooleanQuery(null, title)));

		CountSearchResponse countSearchResponse =
			_searchRequestExecutor.executeSearchRequest(countSearchRequest);

		return countSearchResponse.getCount();
	}

	private List<ProcessMetric> _getProcessMetrics(
		Collection<Bucket> buckets, Map<Long, ProcessMetric> processMetrics) {

		return Stream.of(
			buckets
		).flatMap(
			Collection::stream
		).map(
			bucket -> {
				ProcessMetric processMetric = processMetrics.get(
					Long.valueOf(bucket.getKey()));

				_setInstanceCount(bucket, processMetric);
				_setOnTimeInstanceCount(bucket, processMetric);
				_setOverdueInstanceCount(bucket, processMetric);

				_setUntrackedInstanceCount(processMetric);

				return processMetric;
			}
		).collect(
			Collectors.toList()
		);
	}

	private Map<Long, ProcessMetric> _getProcessMetrics(
		FieldSort fieldSort, Pagination pagination, Long processId,
		String title) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		if (fieldSort != null) {
			searchSearchRequest.addSorts(fieldSort);
		}

		searchSearchRequest.setIndexNames(
			_processWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));

		BooleanQuery booleanQuery = _queries.booleanQuery();

		searchSearchRequest.setQuery(
			booleanQuery.addFilterQueryClauses(
				_createProcessBooleanQuery(processId, title)));

		if (pagination != null) {
			searchSearchRequest.setSize(pagination.getPageSize());
			searchSearchRequest.setStart(pagination.getStartPosition());
		}
		else {
			searchSearchRequest.setSize(1);
		}

		return Stream.of(
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest)
		).map(
			SearchSearchResponse::getSearchHits
		).map(
			SearchHits::getSearchHits
		).flatMap(
			List::stream
		).map(
			SearchHit::getDocument
		).map(
			this::_createProcessMetric
		).collect(
			LinkedHashMap::new,
			(map, processMetric) -> {
				Process process = processMetric.getProcess();

				map.put(process.getId(), processMetric);
			},
			Map::putAll
		);
	}

	private Collection<ProcessMetric> _getProcessMetrics(
			FieldSort fieldSort, Pagination pagination, SearchHits searchHits)
		throws Exception {

		List<ProcessMetric> processMetrics = new LinkedList<>();

		Map<Long, ProcessMetric> processMetricsMap = Stream.of(
			searchHits.getSearchHits()
		).flatMap(
			List::stream
		).map(
			SearchHit::getDocument
		).map(
			this::_createProcessMetric
		).collect(
			LinkedHashMap::new,
			(map, processMetric) -> {
				Process process = processMetric.getProcess();

				map.put(process.getId(), processMetric);
			},
			Map::putAll
		);

		TermsAggregationResult instanceTermsAggregationResult =
			_getInstanceTermsAggregationResult(
				false, fieldSort, pagination, processMetricsMap.keySet());
		TermsAggregationResult slaTermsAggregationResult =
			_getSLATermsAggregationResult(
				false, fieldSort, pagination, processMetricsMap.keySet());

		if (_isOrderByInstanceCount(fieldSort.getField())) {
			for (Bucket bucket : instanceTermsAggregationResult.getBuckets()) {
				ProcessMetric processMetric = processMetricsMap.remove(
					Long.valueOf(bucket.getKey()));

				_populateProcessWithSLAMetrics(
					slaTermsAggregationResult.getBucket(bucket.getKey()),
					processMetric);
				_setInstanceCount(bucket, processMetric);
				_setUntrackedInstanceCount(processMetric);

				processMetrics.add(processMetric);
			}
		}
		else if (_isOrderByTitle(fieldSort.getField())) {
			for (ProcessMetric processMetric : processMetricsMap.values()) {
				Process process = processMetric.getProcess();

				_populateProcessWithSLAMetrics(
					slaTermsAggregationResult.getBucket(
						String.valueOf(process.getId())),
					processMetric);
				_setInstanceCount(
					instanceTermsAggregationResult.getBucket(
						String.valueOf(process.getId())),
					processMetric);

				_setUntrackedInstanceCount(processMetric);

				processMetrics.add(processMetric);
			}
		}
		else {
			for (Bucket bucket : slaTermsAggregationResult.getBuckets()) {
				ProcessMetric processMetric = processMetricsMap.remove(
					Long.valueOf(bucket.getKey()));

				_populateProcessWithSLAMetrics(bucket, processMetric);
				_setInstanceCount(
					instanceTermsAggregationResult.getBucket(bucket.getKey()),
					processMetric);
				_setUntrackedInstanceCount(processMetric);

				processMetrics.add(processMetric);
			}
		}

		return processMetrics;
	}

	private Map<Long, ProcessMetric> _getProcessMetrics(Set<Long> processIds) {
		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(
			_processWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));

		BooleanQuery booleanQuery = _queries.booleanQuery();

		searchSearchRequest.setQuery(
			booleanQuery.addFilterQueryClauses(
				_queries.term("deleted", Boolean.FALSE),
				_createProcessIdTermsQuery(processIds)));

		searchSearchRequest.setSize(processIds.size());

		return Stream.of(
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest)
		).map(
			SearchSearchResponse::getSearchHits
		).map(
			SearchHits::getSearchHits
		).flatMap(
			List::stream
		).map(
			SearchHit::getDocument
		).map(
			this::_createProcessMetric
		).collect(
			LinkedHashMap::new,
			(map, processMetric) -> {
				Process process = processMetric.getProcess();

				map.put(process.getId(), processMetric);
			},
			Map::putAll
		);
	}

	private SearchSearchResponse _getProcessMetricsSearchSearchResponse(
		FieldSort fieldSort, Pagination pagination, Long processId,
		String title) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(
			_processWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));
		searchSearchRequest.setQuery(
			_createProcessBooleanQuery(processId, title));

		if ((fieldSort != null) && _isOrderByTitle(fieldSort.getField())) {
			searchSearchRequest.setSize(pagination.getPageSize());
			searchSearchRequest.setSorts(Collections.singletonList(fieldSort));
			searchSearchRequest.setStart(pagination.getStartPosition());
		}
		else {
			searchSearchRequest.setSize(10000);
			searchSearchRequest.setStart(0);
		}

		return _searchRequestExecutor.executeSearchRequest(searchSearchRequest);
	}

	private TermsAggregationResult _getSLATermsAggregationResult(
		boolean completed, FieldSort fieldSort, Pagination pagination,
		Set<Long> processIds) {

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		TermsAggregation termsAggregation = _aggregations.terms(
			"processId", "processId");

		FilterAggregation onTimeFilterAggregation = _aggregations.filter(
			"onTime", _resourceHelper.createMustNotBooleanQuery());

		onTimeFilterAggregation.addChildAggregation(
			_resourceHelper.createOnTimeScriptedMetricAggregation());

		FilterAggregation overdueFilterAggregation = _aggregations.filter(
			"overdue", _resourceHelper.createMustNotBooleanQuery());

		overdueFilterAggregation.addChildAggregation(
			_resourceHelper.createOverdueScriptedMetricAggregation());

		termsAggregation.addChildrenAggregations(
			onTimeFilterAggregation, overdueFilterAggregation);

		if ((fieldSort != null) &&
			!_isOrderByInstanceCount(fieldSort.getField()) &&
			!_isOrderByTitle(fieldSort.getField())) {

			termsAggregation.addPipelineAggregation(
				_resourceHelper.createBucketSortPipelineAggregation(
					fieldSort, pagination));
		}

		termsAggregation.setSize(processIds.size());

		searchSearchRequest.addAggregation(termsAggregation);

		searchSearchRequest.setIndexNames(
			_slaInstanceResultWorkflowMetricsIndexNameBuilder.getIndexName(
				contextCompany.getCompanyId()));
		searchSearchRequest.setQuery(
			_createSLAInstanceResultsBooleanQuery(
				completed, null, null, processIds));

		SearchSearchResponse searchSearchResponse =
			_searchRequestExecutor.executeSearchRequest(searchSearchRequest);

		Map<String, AggregationResult> aggregationResultsMap =
			searchSearchResponse.getAggregationResultsMap();

		return (TermsAggregationResult)aggregationResultsMap.get("processId");
	}

	private String _getTitleFieldName() {
		return Field.getLocalizedName(
			contextAcceptLanguage.getPreferredLocale(), "title");
	}

	private boolean _isOrderByInstanceCount(String fieldName) {
		return StringUtil.startsWith(fieldName, "instanceCount");
	}

	private boolean _isOrderByTitle(String fieldName) {
		return StringUtil.startsWith(fieldName, "title");
	}

	private void _populateProcessWithSLAMetrics(
		Bucket bucket, ProcessMetric processMetric) {

		_setOnTimeInstanceCount(bucket, processMetric);
		_setOverdueInstanceCount(bucket, processMetric);
	}

	private void _setInstanceCount(Bucket bucket, ProcessMetric processMetric) {
		processMetric.setInstanceCount(bucket.getDocCount());
	}

	private void _setOnTimeInstanceCount(
		Bucket bucket, ProcessMetric processMetric) {

		FilterAggregationResult filterAggregationResult =
			(FilterAggregationResult)bucket.getChildAggregationResult("onTime");

		processMetric.setOnTimeInstanceCount(
			filterAggregationResult.getDocCount());
	}

	private void _setOverdueInstanceCount(
		Bucket bucket, ProcessMetric processMetric) {

		FilterAggregationResult filterAggregationResult =
			(FilterAggregationResult)bucket.getChildAggregationResult(
				"overdue");

		processMetric.setOverdueInstanceCount(
			filterAggregationResult.getDocCount());
	}

	private void _setUntrackedInstanceCount(ProcessMetric processMetric) {
		long onTimeInstanceCount = GetterUtil.getLong(
			processMetric.getOnTimeInstanceCount());
		long overdueInstanceCount = GetterUtil.getLong(
			processMetric.getOverdueInstanceCount());

		processMetric.setUntrackedInstanceCount(
			processMetric.getInstanceCount() - onTimeInstanceCount -
				overdueInstanceCount);
	}

	private FieldSort _toFieldSort(Sort[] sorts) {
		String titleFieldName = Field.getSortableFieldName(
			_getTitleFieldName());

		Sort sort = new Sort(titleFieldName, false);

		if (sorts != null) {
			sort = sorts[0];
		}

		String fieldName = sort.getFieldName();

		if (StringUtil.startsWith(fieldName, "instanceCount")) {
			fieldName = "_count";
		}
		else if (StringUtil.startsWith(fieldName, "title")) {
			fieldName = titleFieldName;
		}
		else {
			fieldName = 
				StringUtil.extractFirst(fieldName, "InstanceCount") + ">_count";
		}

		FieldSort fieldSort = _sorts.field(fieldName);

		fieldSort.setSortOrder(
			sort.isReverse() ? SortOrder.DESC : SortOrder.ASC);

		return fieldSort;
	}

	private static final EntityModel _entityModel =
		new ProcessMetricEntityModel();

	@Reference
	private Aggregations _aggregations;

	@Reference(target = "(workflow.metrics.index.entity.name=instance)")
	private WorkflowMetricsIndexNameBuilder
		_instanceWorkflowMetricsIndexNameBuilder;

	@Reference(target = "(workflow.metrics.index.entity.name=process)")
	private WorkflowMetricsIndexNameBuilder
		_processWorkflowMetricsIndexNameBuilder;

	@Reference
	private Queries _queries;

	@Reference
	private ResourceHelper _resourceHelper;

	@Reference
	private Scripts _scripts;

	@Reference
	private SearchRequestExecutor _searchRequestExecutor;

	@Reference(
		target = "(workflow.metrics.index.entity.name=sla-instance-result)"
	)
	private WorkflowMetricsIndexNameBuilder
		_slaInstanceResultWorkflowMetricsIndexNameBuilder;

	@Reference
	private Sorts _sorts;

}