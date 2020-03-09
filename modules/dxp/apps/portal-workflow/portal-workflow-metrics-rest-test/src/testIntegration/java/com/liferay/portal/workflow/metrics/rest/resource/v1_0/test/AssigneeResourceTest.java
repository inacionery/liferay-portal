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

package com.liferay.portal.workflow.metrics.rest.resource.v1_0.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.portal.search.document.DocumentBuilderFactory;
import com.liferay.portal.search.engine.adapter.SearchEngineAdapter;
import com.liferay.portal.search.query.Queries;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.workflow.metrics.index.InstanceWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.index.NodeWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.index.ProcessWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.index.TaskWorkflowMetricsIndexer;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Assignee;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Node;
import com.liferay.portal.workflow.metrics.rest.client.dto.v1_0.Process;
import com.liferay.portal.workflow.metrics.rest.resource.v1_0.test.helper.WorkflowMetricsRESTTestHelper;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * @author Rafael Praxedes
 */
@RunWith(Arquillian.class)
public class AssigneeResourceTest extends BaseAssigneeResourceTestCase {
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		BaseNodeResourceTestCase.setUpClass();

		_workflowMetricsRESTTestHelper = new WorkflowMetricsRESTTestHelper(
			_documentBuilderFactory, _instanceWorkflowMetricsIndexer,
			_nodeWorkflowMetricsIndexer, _processWorkflowMetricsIndexer,
			_queries, _searchEngineAdapter, _taskWorkflowMetricsIndexer);
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		_process = _workflowMetricsRESTTestHelper.addProcess(
			testGroup.getCompanyId());
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();

		if (_process != null) {
			_workflowMetricsRESTTestHelper.deleteProcess(
				testGroup.getCompanyId(), _process);
		}
	}
	
	@Override
	protected Long testGetProcessAssigneesPage_getProcessId() throws Exception {
		return _process.getId();
	}
	
	@Override
	protected Assignee testGetProcessAssigneesPage_addAssignee(
		Long processId, Assignee assignee) throws Exception {

		return super.testGetProcessAssigneesPage_addAssignee(processId, assignee);
	}
	
	@Inject
	private static DocumentBuilderFactory _documentBuilderFactory;

	@Inject
	private static InstanceWorkflowMetricsIndexer
		_instanceWorkflowMetricsIndexer;

	@Inject
	private static NodeWorkflowMetricsIndexer _nodeWorkflowMetricsIndexer;

	@Inject
	private static ProcessWorkflowMetricsIndexer _processWorkflowMetricsIndexer;

	@Inject
	private static Queries _queries;

	@Inject(blocking = false, filter = "search.engine.impl=Elasticsearch")
	private static SearchEngineAdapter _searchEngineAdapter;

	@Inject
	private static TaskWorkflowMetricsIndexer _taskWorkflowMetricsIndexer;

	private static WorkflowMetricsRESTTestHelper _workflowMetricsRESTTestHelper;

	private Process _process;
}