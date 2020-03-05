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

package com.liferay.portal.workflow.metrics.rest.resource.v1_0.test.helper;

import com.liferay.petra.function.UnsafeSupplier;
import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.DateFormatFactoryUtil;
import com.liferay.portal.search.document.Document;
import com.liferay.portal.search.document.DocumentBuilder;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.engine.adapter.document.UpdateDocumentRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchRequest;
import com.liferay.portal.search.engine.adapter.search.CountSearchResponse;
import com.liferay.portal.search.engine.adapter.search.SearchSearchRequest;
import com.liferay.portal.search.engine.adapter.search.SearchSearchResponse;
import com.liferay.portal.search.hits.SearchHit;
import com.liferay.portal.search.hits.SearchHits;
import com.liferay.portal.search.query.BooleanQuery;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.search.test.util.IdempotentRetryAssert;
import com.liferay.portal.workflow.metrics.index.InstanceWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.index.NodeWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.index.ProcessWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.index.TaskWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Creator;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Instance;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Node;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Process;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.ProcessMetric;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Task;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.TaskMetric;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * @author Inácio Nery
 */
public class WorkflowMetricsRESTTestHelper {

	public WorkflowMetricsRESTTestHelper(
		DocumentBuilderFactory documentBuilderFactory, InstanceWorkflowMetricsIndexer instanceWorkflowMetricsIndexer,
		NodeWorkflowMetricsIndexer nodeWorkflowMetricsIndexer,
		ProcessWorkflowMetricsIndexer processWorkflowMetricsIndexer,
		Queries queries,
		SearchEngineAdapter searchEngineAdapter,
		TaskWorkflowMetricsIndexer taskWorkflowMetricsIndexer
) {

		_documentBuilderFactory = documentBuilderFactory;
		_instanceWorkflowMetricsIndexer = instanceWorkflowMetricsIndexer;
		_nodeWorkflowMetricsIndexer = nodeWorkflowMetricsIndexer;
		_processWorkflowMetricsIndexer = processWorkflowMetricsIndexer;
		_queries = queries;
		_searchEngineAdapter = searchEngineAdapter;
		_taskWorkflowMetricsIndexer = taskWorkflowMetricsIndexer;
	}

	public Instance addInstance(
			long companyId, boolean completed, long processId)
		throws Exception {

		Instance instance = new Instance();

		instance.setCreator(new Creator() {
			{
				id = RandomTestUtil.nextLong();
				name = RandomTestUtil.randomString();
			}
		});

		instance.setCompleted(completed);

		if (completed) {
			instance.setDateCompletion(RandomTestUtil.nextDate());
		}

		instance.setId(RandomTestUtil.randomLong());
		instance.setProcessId(processId);

		return addInstance(companyId, instance);
	}

	public Instance addInstance(
			long companyId, Date dateCompletion, long processId)
		throws Exception {

		Instance instance = new Instance();

		instance.setCreator(new Creator() {
			{
				id = RandomTestUtil.randomLong();
				name = RandomTestUtil.randomString();
			}
		});
		instance.setDateCompletion(dateCompletion);
		instance.setId(RandomTestUtil.randomLong());
		instance.setProcessId(processId);

		return addInstance(companyId, instance);
	}

	public Instance addInstance(long companyId, Instance instance)
		throws Exception {

		Creator creator = instance.getCreator();

		_instanceWorkflowMetricsIndexer.add(
			companyId, instance.getId(),
			_createLocalizationMap(instance.getAssetTitle()),
			_createLocalizationMap(instance.getAssetType()),
			StringPool.BLANK, 0, instance.getDateCompletion(),
			Optional.ofNullable(
				instance.getDateCreated()
			).orElseGet(
				Date::new
			),
			Optional.ofNullable(
				instance.getDateModified()
			).orElseGet(
				Date::new
			),
			Optional.ofNullable(
				instance.getDuration()
			).orElse(
				1000L
			),
			instance.getProcessId(),
			instance.getProcessVersion(), creator.getId(), creator.getName()
		);

		_retryAssertCount(
			"workflow-metrics-instances", "companyId", companyId, "deleted",
			false, "instanceId", instance.getId(), "processId",
			instance.getProcessId());

		return instance;
	}

	public Node addNode(long companyId, long processId, String version)
		throws Exception {

		Node node = new Node();

		node.setId(RandomTestUtil.randomLong());
		node.setName(RandomTestUtil.randomString());
		node.setProcessVersion(version);

		return addNode(companyId, node, processId, version);
	}

	public Node addNode(
			long companyId, Node node, long processId, String version)
		throws Exception {

		_nodeWorkflowMetricsIndexer.add(
			companyId, 
			Optional.ofNullable(
					node.getDateCreated()
				).orElseGet(
					Date::new
				),
			false,
			Optional.ofNullable(
					node.getDateModified()
				).orElseGet(
					Date::new
				),
			node.getName(), node.getId(), processId, version, false, "STATE");

		_retryAssertCount(
			"workflow-metrics-nodes", "companyId", companyId, "deleted", false,
			"name", node.getName(), "processId", processId, "version", version);

		return node;
	}

	public Process addProcess(long companyId) throws Exception {
		Process process = new Process() {
			{
				id = RandomTestUtil.randomLong();
				setTitle(RandomTestUtil.randomString());
			}
		};

		return addProcess(companyId, process, "1.0");
	}

	public Process addProcess(long companyId, Process process)
		throws Exception {

		return addProcess(companyId, process, "1.0");
	}

	public Process addProcess(long companyId, Process process, String version)
		throws Exception {

		_processWorkflowMetricsIndexer.add(
			companyId, true,
			Optional.ofNullable(
				process.getDateCreated()
			).orElseGet(
				Date::new
			),
			process.getDescription(),
			Optional.ofNullable(
				process.getDateModified()
			).orElseGet(
				Date::new
			),
			process.getId(), process.getName(), process.getTitle(),
			_createLocalizationMap(process.getTitle()),
			Optional.ofNullable(
				process.getVersion()
			).orElse(
				version
			)
		);

		_retryAssertCount(
			"workflow-metrics-processes", "companyId", companyId, "deleted",
			false, "processId", process.getId());

		return process;
	}

	public ProcessMetric addProcessMetric(long companyId) throws Exception {
		return addProcessMetric(companyId, "1.0");
	}

	public ProcessMetric addProcessMetric(
			long companyId, ProcessMetric processMetric)
		throws Exception {

		return addProcessMetric(companyId, processMetric, "1.0");
	}

	public ProcessMetric addProcessMetric(
			long companyId, ProcessMetric processMetric, String version)
		throws Exception {

		Process process = addProcess(
			companyId, processMetric.getProcess(), version);

		Long onTimeInstanceCount = processMetric.getOnTimeInstanceCount();
		Long overdueInstanceCount = processMetric.getOverdueInstanceCount();

		for (int i = 0; i < processMetric.getInstanceCount(); i++) {
			Instance instance = addInstance(companyId, false, process.getId());

			if (onTimeInstanceCount > 0) {
				addSLAInstanceResult(companyId, instance, true);

				onTimeInstanceCount--;
			}
			else if (overdueInstanceCount > 0) {
				addSLAInstanceResult(companyId, instance, false);

				overdueInstanceCount--;
			}
		}

		return processMetric;
	}

	public ProcessMetric addProcessMetric(long companyId, String version)
		throws Exception {

		ProcessMetric processMetric = new ProcessMetric() {
			{
				instanceCount = 0L;
				onTimeInstanceCount = 0L;
				overdueInstanceCount = 0L;

				process = new Process() {
					{
						id = RandomTestUtil.randomLong();
						setTitle(RandomTestUtil.randomString());
					}
				};

				untrackedInstanceCount = 0L;
			}
		};

		return addProcessMetric(companyId, processMetric, version);
	}

	public void addSLAInstanceResult(
			long companyId, Instance instance, boolean onTime)
		throws Exception {

		long slaDefinitionId = RandomTestUtil.randomLong();

		_invokeAddDocument(
			_getIndexer(_CLASS_NAME_SLA_INSTANCE_RESULT_INDEXER),
			_creatWorkflowMetricsSLAInstanceResultDocument(
				companyId, Objects.nonNull(instance.getDateCompletion()),
				instance.getId(), onTime, instance.getProcessId(),
				slaDefinitionId));

		_retryAssertCount(
			"workflow-metrics-sla-instance-results", "companyId", companyId,
			"deleted", false, "instanceCompleted",
			Objects.nonNull(instance.getDateCompletion()), "instanceId",
			instance.getId(), "onTime", onTime, "processId",
			instance.getProcessId(), "slaDefinitionId", slaDefinitionId);
	}

	public void addSLATaskResult(
			long assigneeId, boolean breached, long companyId,
			Instance instance, boolean onTime, String status, long nodeId,
			long taskId, String taskName)
		throws Exception {

		long slaDefinitionId = RandomTestUtil.randomLong();

		_invokeAddDocument(
			_getIndexer(_CLASS_NAME_SLA_TASK_RESULT_INDEXER),
			_creatWorkflowMetricsSLATaskResultDocument(
				assigneeId, breached, companyId,
				Objects.nonNull(instance.getDateCompletion()), instance.getId(),
				onTime, instance.getProcessId(), slaDefinitionId, status,
				nodeId, taskName, taskId));

		_retryAssertCount(
			"workflow-metrics-sla-task-results", "breached", breached,
			"assigneeId", assigneeId, "companyId", companyId, "deleted", false,
			"instanceCompleted", Objects.nonNull(instance.getDateCompletion()),
			"instanceId", instance.getId(), "onTime", onTime, "processId",
			instance.getProcessId(), "slaDefinitionId", slaDefinitionId,
			"taskId", taskId, "taskName", taskName);
	}

	public TaskMetric addTaskMetric(
			long assigneeId, long companyId, long processId)
		throws Exception {

		String randomString = RandomTestUtil.randomString();

		TaskMetric taskMetric = new TaskMetric() {
			{
				durationAvg = 0L;
				instanceCount = 1L;
				onTimeInstanceCount = 0L;
				overdueInstanceCount = 0L;
				task = new Task() {
					{
						id = RandomTestUtil.randomLong();
						label = randomString;
						name = randomString;
						nodeId = RandomTestUtil.randomLong();
					}
				};
			}
		};

		return addTaskMetric(
			assigneeId, companyId, processId, "RUNNING", taskMetric, "1.0");
	}

	public TaskMetric addTaskMetric(
			long assigneeId, long companyId, long processId, String status,
			TaskMetric task, String version)
		throws Exception {

		return addTaskMetric(
			assigneeId, companyId,
			() -> addInstance(companyId, false, processId), processId, status,
			task, version);
	}

	public TaskMetric addTaskMetric(
			long assigneeId, long companyId,
			UnsafeSupplier<Instance, Exception> instanceSuplier, long processId,
			String status)
		throws Exception {

		String randomString = RandomTestUtil.randomString();

		TaskMetric task = new TaskMetric() {
			{
				durationAvg = Objects.equals(status, "COMPLETED") ? 1000L : 0L;
				instanceCount = 1L;
				onTimeInstanceCount = 0L;
				overdueInstanceCount = 0L;
				task = new Task() {
					{
						id = RandomTestUtil.randomLong();
						label = randomString;
						name = randomString;
						nodeId = RandomTestUtil.randomLong();
					}
				};
			}
		};

		return addTaskMetric(
			assigneeId, companyId, instanceSuplier, processId, status, task,
			"1.0");
	}

	public TaskMetric addTaskMetric(
			long assigneeId, long companyId,
			UnsafeSupplier<Instance, Exception> instanceSuplier, long processId,
			String status, TaskMetric taskMetric, String version)
		throws Exception {

		Task task = taskMetric.getTask();

		Node node = addNode(
			companyId,
			new Node() {
				{
					id = task.getNodeId();
					name = task.getName();
					type = "TASK";
					processId = task.getProcessId();
					processVersion = task.getProcessVersion();
				}
			},
			processId, version);

		Long onTimeInstanceCount = taskMetric.getOnTimeInstanceCount();
		Long overdueInstanceCount = taskMetric.getOverdueInstanceCount();

		for (int i = 0; i < taskMetric.getInstanceCount(); i++) {
			Instance instance = instanceSuplier.get();

			if (onTimeInstanceCount > 0) {
				addSLATaskResult(
					assigneeId, false, companyId, instance, true, status,
					node.getId(), task.getId(), task.getName());

				onTimeInstanceCount--;
			}
			else if (overdueInstanceCount > 0) {
				addSLATaskResult(
					assigneeId, true, companyId, instance, false, status,
					node.getId(), task.getId(), task.getName());

				overdueInstanceCount--;
			}

			addToken(
				assigneeId, companyId, taskMetric.getDurationAvg(),
				instance.getCompleted(), instance.getId(), processId,
				node.getId(), task.getId(), task.getName());
		}

		_retryAssertCount(
			"workflow-metrics-nodes", "companyId", companyId, "deleted", false,
			"name", node.getName(), "processId", processId);

		return taskMetric;
	}

	public void addToken(long assigneeId, long companyId, Instance instance)
		throws Exception {

		addToken(
			assigneeId, companyId,  0L, instance.getCompleted(),
			instance.getId(), instance.getProcessId(),
			RandomTestUtil.randomLong(), RandomTestUtil.randomLong(),
			RandomTestUtil.randomString());
	}

	public void addToken(
			long assigneeId, long companyId, long durationAvg,
			boolean instanceCompleted, long instanceId, long processId,
			long nodeId, long taskId, String name)
		throws Exception {

		_taskWorkflowMetricsIndexer.add(
			companyId, nodeId, StringPool.BLANK, 0, new Date(), new Date(),
			instanceId, name, processId, "1.0", taskId, 0);

		if (assigneeId != 0) {
			_taskWorkflowMetricsIndexer.update(
				companyId, assigneeId, new Date(), taskId, 0);
		}

		if (instanceCompleted) {
			_instanceWorkflowMetricsIndexer.complete(
				companyId, instanceId, new Date(), new Date(), durationAvg);

			_retryAssertCount(
				"workflow-metrics-instances", "companyId", companyId, "deleted",
				false, "instanceId", instanceId, "instanceCompleted", true,
				"processId", processId);
		}

		_retryAssertCount(
			"workflow-metrics-tokens", "assigneeId", assigneeId, "companyId",
			companyId, "deleted", false, "instanceCompleted", instanceCompleted,
			"instanceId", instanceId, "processId", processId, "nodeId", nodeId,
			"name", name, "taskId", taskId);
	}

	public void deleteInstance(long companyId, Instance instance)
		throws Exception {

		_instanceWorkflowMetricsIndexer.delete(
			companyId, instance.getId());

		_retryAssertCount(
			"workflow-metrics-instances", "companyId", companyId, "deleted",
			true, "instanceId", instance.getId(), "processId",
			instance.getProcessId());
	}

	public void deleteNode(long companyId, Node node, long processId)
		throws Exception {

		_nodeWorkflowMetricsIndexer.delete(companyId, node.getId());

		_retryAssertCount(
			"workflow-metrics-nodes", "companyId", companyId, "deleted", true,
			"name", node.getName(), "processId", processId);
	}

	public void deleteProcess(long companyId, long processId) throws Exception {
		_processWorkflowMetricsIndexer.delete(companyId, processId);

		_retryAssertCount(
			"workflow-metrics-processes", "companyId", companyId, "deleted",
			true, "processId", processId);
	}

	public void deleteProcess(long companyId, Process process)
		throws Exception {

		deleteProcess(companyId, process.getId());
	}

	public void deleteSLATaskResults(long companyId, long processId)
		throws Exception {

		_deleteDocuments(
			"workflow-metrics-sla-task-results",
			"WorkflowMetricsSLATaskResultType", "companyId", companyId,
			"processId", processId);
	}

	public void deleteTask(long companyId, long processId, Task task)
		throws Exception {

		_nodeWorkflowMetricsIndexer.delete(companyId, task.getNodeId());

		_retryAssertCount(
			"workflow-metrics-nodes", "companyId", companyId, "deleted", true,
			"name", task.getName(), "processId", processId);
	}

	public void deleteTokens(long companyId, long processId) throws Exception {
		_deleteDocuments(
			"workflow-metrics-tokens", "WorkflowMetricsTokenType", "companyId",
			companyId, "processId", processId);
	}

	public Document[] getDocuments(long companyId) throws Exception {
		if (_searchEngineAdapter == null) {
			return new Document[0];
		}

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames("workflow-metrics-processes");

		BooleanQuery booleanQuery = _queries.booleanQuery();

		searchSearchRequest.setQuery(
			booleanQuery.addMustQueryClauses(
				_queries.term("companyId", companyId),
				_queries.term("deleted", Boolean.FALSE)));

		searchSearchRequest.setSize(10000);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		return Stream.of(
			searchSearchResponse.getSearchHits()
		).map(
			SearchHits::getSearchHits
		).flatMap(
			List::stream
		).map(
			SearchHit::getDocument
		).toArray(
			Document[]::new
		);
	}

	public void restoreProcess(Document document) throws Exception {
		_processWorkflowMetricsIndexer.add(
			document.getLong("companyId"), document.getBoolean("active"),
			_parseDate(document.getDate("createDate")),
			document.getString("description"),
			_parseDate(document.getDate("modifiedDate")),
			document.getLong("processId"), document.getString("name"),
			document.getString("title"),
			_createLocalizationMap(document.getString("title")),
			document.getString("version"));
		
		_retryAssertCount(
			"workflow-metrics-processes", "companyId",
			document.getLong("companyId"), "deleted", false, "processId",
			document.getLong("processId"));
	}

	private  Date _parseDate(String formattedDate) {
		DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyyMMddHHmmss");

		try {
			return dateFormat.parse(formattedDate);
		}
		catch (Exception exception) {
			return new Date();
		}
	}


	public void updateProcess(long companyId, long processId, String version)
		throws Exception {

		_processWorkflowMetricsIndexer.update(
			companyId, Optional.empty(), Optional.empty(), new Date(),
			processId, Optional.empty(), Optional.empty(),
			Optional.of(version));

		_retryAssertCount(
			"workflow-metrics-processes", "companyId", companyId, "deleted",
			false, "processId", processId, "version", version);
	}

	private Map<Locale, String> _createLocalizationMap(String value) {
		Map<Locale, String> localizationMap = new HashMap<>();

		for (Locale availableLocale : LanguageUtil.getAvailableLocales()) {
			localizationMap.put(availableLocale, value);
		}

		return localizationMap;
	}

	private Document _creatWorkflowMetricsSLAInstanceResultDocument(
		long companyId, boolean instanceCompleted, long instanceId,
		boolean onTime, long processId, long slaDefinitionId) {

		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID,
			_digest(companyId, instanceId, processId, slaDefinitionId)
		).setValue(
			"companyId", companyId
		).setValue(
			"deleted", false
		).setValue(
			"elapsedTime", onTime ? 1000 : -1000
		).setValue(
			"instanceCompleted", instanceCompleted
		).setValue(
			"instanceId", instanceId
		).setValue(
			"onTime", onTime
		).setValue(
			"processId", processId
		).setValue(
			"slaDefinitionId", slaDefinitionId
		).setValue(
			"status", "RUNNING"
		);

		return documentBuilder.build();
	}

	private Document _creatWorkflowMetricsSLATaskResultDocument(
		long assigneeId, boolean breached, long companyId,
		boolean instanceCompleted, long instanceId, boolean onTime,
		long processId, long slaDefinitionId, String status, long nodeId,
		String taskName, long taskId) {

		DocumentBuilder documentBuilder = _documentBuilderFactory.builder();

		documentBuilder.setString(
			Field.UID,
			_digest(companyId, instanceId, processId, slaDefinitionId, taskId));
		documentBuilder.setValue(
			"assigneeId", assigneeId
		).setValue(
			"breached", breached
		).setValue(
			"companyId", companyId
		);
		
		if (Objects.equals(status, "COMPLETED")) {
			documentBuilder.setDate(
				"completionDate",  _formatDate(new Date())
			).setValue(
				"completionUserId", assigneeId);
		}

		documentBuilder.setValue(
			"deleted", false
		).setValue(
			"elapsedTime", onTime ? 1000 : -1000
		).setValue(
			"instanceCompleted", instanceCompleted
		).setValue(
			"instanceId", instanceId
		).setValue(
			"onTime", onTime
		).setValue(
			"processId", processId
		).setValue(
			"slaDefinitionId", slaDefinitionId
		).setValue(
			"status", status
		).setValue(
			"nodeId", nodeId
		).setValue(
			"taskName", taskName
		).setValue(
			"taskId", taskId
		);

		return documentBuilder.build();
	}
	
	protected String _formatDate(Date date) {
		DateFormat dateFormat = DateFormatFactoryUtil.getSimpleDateFormat(
			"yyyyMMddHHmmss");

		try {
			return dateFormat.format(date);
		}
		catch (Exception exception) {
			return null;
		}
	}


	private void _deleteDocuments(
			String indexName, String indexType, Object... parameters)
		throws Exception {

		if (_searchEngineAdapter == null) {
			return;
		}

		SearchSearchRequest searchSearchRequest = new SearchSearchRequest();

		searchSearchRequest.setIndexNames(indexName);

		BooleanQuery booleanQuery = _queries.booleanQuery();

		for (int j = 0; j < parameters.length; j = j + 2) {
			booleanQuery.addMustQueryClauses(
				_queries.term(
					String.valueOf(parameters[j]), parameters[j + 1]));
		}

		searchSearchRequest.setQuery(booleanQuery);

		searchSearchRequest.setSize(10000);
		searchSearchRequest.setTypes(indexType);

		SearchSearchResponse searchSearchResponse =
			_searchEngineAdapter.execute(searchSearchRequest);

		Stream.of(
			searchSearchResponse
		).map(
			SearchSearchResponse::getSearchHits
		).map(
			SearchHits::getSearchHits
		).flatMap(
			List::stream
		).map(
			SearchHit::getDocument
		).map(
			_documentBuilderFactory::builder
		).map(
			documentBuilder -> {
				documentBuilder.setValue("deleted", true);

				return documentBuilder.build();
			}
		).forEach(
			document -> {
				UpdateDocumentRequest updateDocumentRequest =
					new UpdateDocumentRequest(
						indexName, document.getString("uid"), document);

				updateDocumentRequest.setRefresh(true);
				updateDocumentRequest.setType(indexType);

				_searchEngineAdapter.execute(updateDocumentRequest);
			}
		);

		_retryAssertCount(
			searchSearchResponse.getCount(), indexName,
			ArrayUtil.append(new Object[] {"deleted", true}, parameters));
	}

	private String _digest(Serializable... parts) {
		StringBuilder sb = new StringBuilder();

		for (Serializable part : parts) {
			sb.append(part);
		}

		return DigestUtils.sha256Hex(sb.toString());
	}

	private Object _getIndexer(String className) throws Exception {
		if (_indexers.containsKey(className)) {
			return _indexers.get(className);
		}

		Bundle bundle = FrameworkUtil.getBundle(
			WorkflowMetricsRESTTestHelper.class);

		BundleContext bundleContext = bundle.getBundleContext();

		int count = 0;

		ServiceReference<?> serviceReference = null;

		do {
			ServiceReference<?>[] serviceReferences =
				bundleContext.getServiceReferences(
					className, "(objectClass=" + className + ")");

			if (ArrayUtil.isEmpty(serviceReferences)) {
				count++;

				if (count >= 5) {
					throw new IllegalStateException(
						"Unable to get reference to " + className);
				}

				Thread.sleep(500);
			}

			serviceReference = serviceReferences[0];
		}
		while (serviceReference == null);

		Object indexer = bundleContext.getService(serviceReference);

		_indexers.put(className, indexer);

		return indexer;
	}

	private void _invokeAddDocument(Object indexer, Document document)
		throws Exception {

		_invokeMethod(indexer, "addDocument", document);
	}

	private void _invokeMethod(
			Object indexer, String methodName, Document document)
		throws Exception {

		Class<?> indexerClass = indexer.getClass();

		Method method = null;

		while ((indexerClass != Object.class) && (method == null)) {
			try {
				method = ReflectionUtil.getDeclaredMethod(
					indexerClass, methodName, Document.class);
			}
			catch (NoSuchMethodException noSuchMethodException) {
			}

			indexerClass = indexerClass.getSuperclass();
		}

		method.invoke(indexer, document);
	}

	private void _retryAssertCount(
			long expectedCount, String indexName, Object... parameters)
		throws Exception {

		if (_searchEngineAdapter == null) {
			return;
		}

		if (parameters == null) {
			return;
		}

		if ((parameters.length % 2) != 0) {
			throw new IllegalArgumentException(
				"Parameters length is not an even number");
		}

		IdempotentRetryAssert.retryAssert(
			30, TimeUnit.SECONDS,
			() -> {
				CountSearchRequest countSearchRequest =
					new CountSearchRequest();

				countSearchRequest.setIndexNames(indexName);

				BooleanQuery booleanQuery = _queries.booleanQuery();

				for (int i = 0; i < parameters.length; i = i + 2) {
					booleanQuery.addMustQueryClauses(
						_queries.term(
							String.valueOf(parameters[i]), parameters[i + 1]));
				}

				countSearchRequest.setQuery(booleanQuery);

				CountSearchResponse countSearchResponse =
					_searchEngineAdapter.execute(countSearchRequest);

				Assert.assertEquals(
					countSearchResponse.getSearchRequestString(),
					expectedCount, countSearchResponse.getCount());

				return null;
			});
	}

	private void _retryAssertCount(String indexName, Object... parameters)
		throws Exception {

		_retryAssertCount(1, indexName, parameters);
	}

	private static final String _CLASS_NAME_SLA_INSTANCE_RESULT_INDEXER =
		"com.liferay.portal.workflow.metrics.internal.search.index." +
			"SLAInstanceResultWorkflowMetricsIndexer";

	private static final String _CLASS_NAME_SLA_TASK_RESULT_INDEXER =
		"com.liferay.portal.workflow.metrics.internal.search.index." +
			"SLATaskResultWorkflowMetricsIndexer";

	private static Map<String, Object> _indexers = new HashMap<>();

	private final DocumentBuilderFactory _documentBuilderFactory;
	private final Queries _queries;
	private final SearchEngineAdapter _searchEngineAdapter;
	private final ProcessWorkflowMetricsIndexer _processWorkflowMetricsIndexer;
	private final InstanceWorkflowMetricsIndexer _instanceWorkflowMetricsIndexer;
	private final NodeWorkflowMetricsIndexer _nodeWorkflowMetricsIndexer;
	private final TaskWorkflowMetricsIndexer _taskWorkflowMetricsIndexer;

}