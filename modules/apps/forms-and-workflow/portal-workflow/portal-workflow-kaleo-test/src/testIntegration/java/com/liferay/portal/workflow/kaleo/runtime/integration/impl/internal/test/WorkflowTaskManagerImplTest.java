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

package com.liferay.portal.workflow.kaleo.runtime.integration.impl.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.dynamic.data.lists.model.DDLRecord;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.model.DDLRecordVersion;
import com.liferay.dynamic.data.lists.service.DDLRecordLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.scripting.ScriptingExecutor;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.SynchronousMailTestRule;
import com.liferay.registry.Registry;
import com.liferay.registry.RegistryUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Inácio Nery
 */
@RunWith(Arquillian.class)
@Sync
public class WorkflowTaskManagerImplTest
	extends BaseWorkflowTaskManagerTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(), GraphWalkerTestRule.INSTANCE,
			SynchronousDestinationTestRule.INSTANCE,
			SynchronousMailTestRule.INSTANCE);

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();

		setUpGroovy();

		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();
	}

	public void setUpGroovy() throws Exception {
		Registry registry = RegistryUtil.getRegistry();

		ScriptingExecutor[] scriptingExecutors;

		scriptingExecutors = registry.getServices(
			"com.liferay.portal.kernel.scripting.ScriptingExecutor",
			"(scripting.language=groovy)");

		Map<String, Object> inputObjects = Collections.emptyMap();
		Set<String> outputNames = Collections.emptySet();

		scriptingExecutors[0].eval(
			null, inputObjects, outputNames, "println \"Hello World\"");
	}

	@After
	public void tearDown() throws PortalException {
		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Test
	public void testApproveWorkflowBlogsEntryAsSiteAdmin() throws Exception {
		System.out.println("Start testApproveWorkflowBlogsEntryAsSiteAdmin");
		activeSingleApproverWorkflow(BlogsEntry.class.getName(), 0);

		BlogsEntry blogsEntry = addBlogsEntry();

		checkUserNotificationEventsByUsers(
			adminUser, portalContentReviewerUser, siteAdminUser);

		assignWorkflowTaskToUser(siteAdminUser, siteAdminUser);

		approveWorkflowTask(siteAdminUser);

		System.out.println("Start getBlogsEntry");

		blogsEntry = BlogsEntryLocalServiceUtil.getBlogsEntry(
			blogsEntry.getEntryId());

		System.out.println("End getBlogsEntry");

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, blogsEntry.getStatus());

		deactiveWorkflow(BlogsEntry.class.getName(), 0);

		System.out.println("End testApproveWorkflowBlogsEntryAsSiteAdmin");
	}

	@Test
	public void testApproveWorkflowDDLRecordAsAdmin() throws Exception {
		System.out.println("Start testApproveWorkflowDDLRecordAsAdmin");
		DDLRecordSet recordSet = addRecordSet();

		activeSingleApproverWorkflow(
			DDLRecordSet.class.getName(), recordSet.getRecordSetId());

		DDLRecord record = addRecord(recordSet);

		checkUserNotificationEventsByUsers(
			adminUser, portalContentReviewerUser, siteAdminUser);

		assignWorkflowTaskToUser(adminUser, adminUser);

		record = DDLRecordLocalServiceUtil.getRecord(record.getRecordId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_PENDING, record.getStatus());

		approveWorkflowTask(adminUser);

		record = DDLRecordLocalServiceUtil.getRecord(record.getRecordId());

		DDLRecordVersion recordVersion = record.getRecordVersion();

		checkWorkflowInstance(
			DDLRecord.class.getName(), recordVersion.getRecordVersionId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, record.getStatus());

		deactiveWorkflow(
			DDLRecordSet.class.getName(), recordSet.getRecordSetId());

		System.out.println("End testApproveWorkflowDDLRecordAsAdmin");
	}

	@Test
	public void testAssignApproveWorkflowBlogsEntryAsPortalContentReviewer()
		throws Exception {

		activeSingleApproverWorkflow(BlogsEntry.class.getName(), 0);

		BlogsEntry blogsEntry = addBlogsEntry();

		checkUserNotificationEventsByUsers(
			adminUser, portalContentReviewerUser, siteAdminUser);

		assignWorkflowTaskToUser(portalContentReviewerUser, adminUser);

		checkUserNotificationEventsByUsers(adminUser);

		assignWorkflowTaskToUser(adminUser, portalContentReviewerUser);

		checkUserNotificationEventsByUsers(portalContentReviewerUser);

		approveWorkflowTask(portalContentReviewerUser);

		blogsEntry = BlogsEntryLocalServiceUtil.getBlogsEntry(
			blogsEntry.getEntryId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, blogsEntry.getStatus());

		deactiveWorkflow(BlogsEntry.class.getName(), 0);
	}

	private PermissionChecker _originalPermissionChecker;

}