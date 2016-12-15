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

package com.liferay.portal.kaleo.runtime.integration.impl.internal.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.blogs.model.BlogsEntry;
import com.liferay.blogs.service.BlogsEntryLocalServiceUtil;
import com.liferay.dynamic.data.lists.model.DDLRecord;
import com.liferay.dynamic.data.lists.model.DDLRecordConstants;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.model.DDLRecordSetConstants;
import com.liferay.dynamic.data.lists.service.DDLRecordLocalServiceUtil;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalServiceUtil;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.dynamic.data.mapping.storage.StorageType;
import com.liferay.dynamic.data.mapping.test.util.DDMFormTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMFormValuesTestUtil;
import com.liferay.dynamic.data.mapping.test.util.DDMStructureTestHelper;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.UserNotificationEventLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.RandomTestUtil;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowInstance;
import com.liferay.portal.kernel.workflow.WorkflowInstanceManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
public class WorkflowTaskManagerImplTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			SynchronousDestinationTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_originalPermissionChecker =
			PermissionThreadLocal.getPermissionChecker();

		_group = GroupTestUtil.addGroup();

		_adminUser = createUser(
			RoleConstants.ADMINISTRATOR, RoleConstants.TYPE_REGULAR);

		_portalContentReviewerUser = createUser(
			RoleConstants.PORTAL_CONTENT_REVIEWER, RoleConstants.TYPE_REGULAR);

		_siteAdminUser = createUser(
			RoleConstants.SITE_ADMINISTRATOR, RoleConstants.TYPE_SITE);
	}

	@After
	public void tearDown() throws PortalException {
		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Sync
	@Test
	public void testApproveDynamicdatalistCP() throws Exception {
		DDMForm ddmForm = DDMFormTestUtil.createDDMForm("TextField1");

		DDMFormValues ddmFormValues = DDMFormValuesTestUtil.createDDMFormValues(
			ddmForm);

		DDMFormFieldValue ddmFormFieldValue =
			DDMFormValuesTestUtil.createLocalizedDDMFormFieldValue(
				"TextField1", StringPool.BLANK);

		ddmFormValues.addDDMFormFieldValue(ddmFormFieldValue);

		DDMStructureTestHelper ddmStructureTestHelper =
			new DDMStructureTestHelper(
				PortalUtil.getClassNameId(DDLRecordSet.class), _group);

		DDMStructure ddmStructure = ddmStructureTestHelper.addStructure(
			ddmForm, StorageType.JSON.toString());

		Map<Locale, String> nameMap = new HashMap<>();

		nameMap.put(LocaleUtil.US, RandomTestUtil.randomString());

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(_group.getGroupId());

		DDLRecordSet recordSet = DDLRecordSetLocalServiceUtil.addRecordSet(
			_adminUser.getUserId(), _group.getGroupId(),
			ddmStructure.getStructureId(), null, nameMap, null,
			DDLRecordSetConstants.MIN_DISPLAY_ROWS_DEFAULT,
			DDLRecordSetConstants.SCOPE_DYNAMIC_DATA_LISTS, serviceContext);

		activeWorkflow(
			DDLRecordSet.class.getName(), recordSet.getRecordSetId());

		DDLRecord record = DDLRecordLocalServiceUtil.addRecord(
			_adminUser.getUserId(), _group.getGroupId(),
			recordSet.getRecordSetId(),
			DDLRecordConstants.DISPLAY_INDEX_DEFAULT, ddmFormValues,
			serviceContext);

		WorkflowTask workflowTask = getWorkflowTask(_adminUser);

		WorkflowTaskManagerUtil.assignWorkflowTaskToUser(
			_adminUser.getCompanyId(), _adminUser.getUserId(),
			workflowTask.getWorkflowTaskId(), _adminUser.getUserId(),
			StringPool.BLANK, null, null);

		Assert.assertEquals(
			WorkflowConstants.STATUS_PENDING, record.getStatus());

		WorkflowTaskManagerUtil.completeWorkflowTask(
			_adminUser.getCompanyId(), _adminUser.getUserId(),
			workflowTask.getWorkflowTaskId(), "approve", StringPool.BLANK,
			null);

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, record.getStatus());

		List<WorkflowInstance> workflowInstances =
			WorkflowInstanceManagerUtil.getWorkflowInstances(
				_adminUser.getCompanyId(),
				workflowTask.getWorkflowDefinitionName(),
				workflowTask.getWorkflowDefinitionVersion(), true,
				QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		Assert.assertEquals(1, workflowInstances.size());

		deactiveWorkflow(
			DDLRecordSet.class.getName(), recordSet.getRecordSetId());
	}

	@Sync
	@Test
	public void testApproveWorkflowTaskAsSiteAdmin() throws Exception {
		activeWorkflow(BlogsEntry.class.getName(), 0);

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			_adminUser.getUserId(), StringUtil.randomString(),
			StringUtil.randomString(), new Date(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_blogsEntries.add(blogsEntry);

		checkUserNotificationEvents(_siteAdminUser.getUserId());

		WorkflowTask workflowTask = getWorkflowTask(_adminUser);

		PermissionChecker siteAdminPermissionChecker =
			PermissionCheckerFactoryUtil.create(_siteAdminUser);

		PermissionThreadLocal.setPermissionChecker(siteAdminPermissionChecker);

		WorkflowTaskManagerUtil.assignWorkflowTaskToUser(
			_siteAdminUser.getCompanyId(), _siteAdminUser.getUserId(),
			workflowTask.getWorkflowTaskId(), _siteAdminUser.getUserId(),
			StringPool.BLANK, null, null);

		WorkflowTaskManagerUtil.completeWorkflowTask(
			_siteAdminUser.getCompanyId(), _siteAdminUser.getUserId(),
			workflowTask.getWorkflowTaskId(), "approve", StringPool.BLANK,
			null);

		blogsEntry = BlogsEntryLocalServiceUtil.getBlogsEntry(
			blogsEntry.getEntryId());

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, blogsEntry.getStatus());

		deactiveWorkflow(BlogsEntry.class.getName(), 0);
	}

	@Sync
	@Test
	public void testAssignApproveWorkflowTaskAsPortalContentReviewer()
		throws Exception {

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			_adminUser.getUserId(), StringUtil.randomString(),
			StringUtil.randomString(), new Date(),
			ServiceContextTestUtil.getServiceContext(_group.getGroupId()));

		_blogsEntries.add(blogsEntry);

		checkUserNotificationEvents(_adminUser.getUserId());

		checkUserNotificationEvents(_portalContentReviewerUser.getUserId());

		WorkflowTask workflowTask = getWorkflowTask(_adminUser);

		PermissionChecker portalContentReviewerPermissionChecker =
			PermissionCheckerFactoryUtil.create(_portalContentReviewerUser);

		PermissionThreadLocal.setPermissionChecker(
			portalContentReviewerPermissionChecker);

		WorkflowTaskManagerUtil.assignWorkflowTaskToUser(
			_adminUser.getCompanyId(), _portalContentReviewerUser.getUserId(),
			workflowTask.getWorkflowTaskId(), _adminUser.getUserId(),
			StringPool.BLANK, null, null);

		checkUserNotificationEvents(_adminUser.getUserId());

		PermissionChecker adminPermissionChecker =
			PermissionCheckerFactoryUtil.create(_adminUser);

		PermissionThreadLocal.setPermissionChecker(adminPermissionChecker);

		WorkflowTaskManagerUtil.assignWorkflowTaskToUser(
			_portalContentReviewerUser.getCompanyId(), _adminUser.getUserId(),
			workflowTask.getWorkflowTaskId(),
			_portalContentReviewerUser.getUserId(), StringPool.BLANK, null,
			null);

		checkUserNotificationEvents(_portalContentReviewerUser.getUserId());

		PermissionThreadLocal.setPermissionChecker(
			portalContentReviewerPermissionChecker);

		WorkflowTaskManagerUtil.completeWorkflowTask(
			_portalContentReviewerUser.getCompanyId(),
			_portalContentReviewerUser.getUserId(),
			workflowTask.getWorkflowTaskId(), "approve", StringPool.BLANK,
			null);

		blogsEntry = BlogsEntryLocalServiceUtil.getBlogsEntry(
			blogsEntry.getEntryId());

		_blogsEntries.add(blogsEntry);

		Assert.assertEquals(
			WorkflowConstants.STATUS_APPROVED, blogsEntry.getStatus());
	}

	protected void activeWorkflow(String className, long classPK)
		throws PortalException {

		WorkflowDefinitionLinkLocalServiceUtil.updateWorkflowDefinitionLink(
			_adminUser.getUserId(), TestPropsValues.getCompanyId(),
			_group.getGroupId(), className, classPK, 0, "Single Approver@1");
	}

	protected void checkUserNotificationEvents(long userId) {
		List<UserNotificationEvent> userNotificationEvents =
			UserNotificationEventLocalServiceUtil.
				getArchivedUserNotificationEvents(
					userId, UserNotificationDeliveryConstants.TYPE_WEBSITE,
					false);

		_userNotificationEvents.addAll(userNotificationEvents);

		Assert.assertEquals(1, userNotificationEvents.size());

		UserNotificationEvent userNotificationEvent =
			userNotificationEvents.get(0);

		userNotificationEvent.setArchived(true);

		UserNotificationEventLocalServiceUtil.updateUserNotificationEvent(
			userNotificationEvent);
	}

	protected User createUser(String roleName, int roleType) throws Exception {
		User user = UserTestUtil.addUser(_group.getGroupId());

		Role role = RoleTestUtil.addRole(roleName, roleType);

		UserLocalServiceUtil.setRoleUsers(
			role.getRoleId(), new long[] {user.getUserId()});

		long[] userIds = {user.getUserId()};

		UserGroupRoleLocalServiceUtil.addUserGroupRoles(
			userIds, _group.getGroupId(), role.getRoleId());

		return user;
	}

	protected void deactiveWorkflow(String className, long classPK)
		throws PortalException {

		WorkflowDefinitionLinkLocalServiceUtil.updateWorkflowDefinitionLink(
			_adminUser.getUserId(), TestPropsValues.getCompanyId(),
			_group.getGroupId(), className, classPK, 0, null);
	}

	protected WorkflowTask getWorkflowTask(User user) throws WorkflowException {
		List<WorkflowTask> workflowTasks =
			WorkflowTaskManagerUtil.getWorkflowTasksBySubmittingUser(
				user.getCompanyId(), user.getUserId(), false, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null);

		Assert.assertEquals(1, workflowTasks.size());

		return workflowTasks.get(0);
	}

	@DeleteAfterTestRun
	private User _adminUser;

	@DeleteAfterTestRun
	private final List<BlogsEntry> _blogsEntries = new ArrayList<>();

	@DeleteAfterTestRun
	private Group _group;

	private PermissionChecker _originalPermissionChecker;

	@DeleteAfterTestRun
	private User _portalContentReviewerUser;

	@DeleteAfterTestRun
	private User _siteAdminUser;

	@DeleteAfterTestRun
	private final List<UserNotificationEvent> _userNotificationEvents =
		new ArrayList<>();

}