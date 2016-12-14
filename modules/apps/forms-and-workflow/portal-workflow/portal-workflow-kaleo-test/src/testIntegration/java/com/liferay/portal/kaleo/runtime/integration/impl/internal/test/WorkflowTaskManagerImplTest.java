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
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.UserNotificationDeliveryConstants;
import com.liferay.portal.kernel.model.UserNotificationEvent;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.UserGroupRoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.UserNotificationEventLocalServiceUtil;
import com.liferay.portal.kernel.service.WorkflowDefinitionLinkLocalServiceUtil;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.rule.Sync;
import com.liferay.portal.kernel.test.rule.SynchronousDestinationTestRule;
import com.liferay.portal.kernel.test.util.RoleTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.test.util.TestPropsValues;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.kernel.workflow.WorkflowTask;
import com.liferay.portal.kernel.workflow.WorkflowTaskManagerUtil;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

		_adminUser = createUser(
			RoleConstants.ADMINISTRATOR, RoleConstants.TYPE_REGULAR);

		_portalContentReviewerUser = createUser(
			RoleConstants.PORTAL_CONTENT_REVIEWER, RoleConstants.TYPE_REGULAR);

		_siteAdminUser = createUser(
			RoleConstants.SITE_ADMINISTRATOR, RoleConstants.TYPE_SITE);

		WorkflowDefinitionLinkLocalServiceUtil.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(),
			TestPropsValues.getGroupId(), BlogsEntry.class.getName(), 0, 0,
			"Single Approver@1");
	}

	@After
	public void tearDown() throws PortalException {
		WorkflowDefinitionLinkLocalServiceUtil.updateWorkflowDefinitionLink(
			TestPropsValues.getUserId(), TestPropsValues.getCompanyId(),
			TestPropsValues.getGroupId(), BlogsEntry.class.getName(), 0, 0,
			null);

		PermissionThreadLocal.setPermissionChecker(_originalPermissionChecker);
	}

	@Sync
	@Test
	public void testApproveWorkflowTaskAsSiteAdmin() throws Exception {
		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			_adminUser.getUserId(), StringUtil.randomString(),
			StringUtil.randomString(), new Date(),
			ServiceContextTestUtil.getServiceContext());

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
	}

	@Sync
	@Test
	public void testAssignApproveWorkflowTaskAsPortalContentReviewer()
		throws Exception {

		BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
			_adminUser.getUserId(), StringUtil.randomString(),
			StringUtil.randomString(), new Date(),
			ServiceContextTestUtil.getServiceContext());

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
		User user = UserTestUtil.addUser();

		Role role = RoleTestUtil.addRole(roleName, roleType);

		UserLocalServiceUtil.setRoleUsers(
			role.getRoleId(), new long[] {user.getUserId()});

		long[] userIds = {user.getUserId()};

		UserGroupRoleLocalServiceUtil.addUserGroupRoles(
			userIds, TestPropsValues.getGroupId(), role.getRoleId());

		return user;
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

	private PermissionChecker _originalPermissionChecker;

	@DeleteAfterTestRun
	private User _portalContentReviewerUser;

	@DeleteAfterTestRun
	private User _siteAdminUser;

	@DeleteAfterTestRun
	private final List<UserNotificationEvent> _userNotificationEvents =
		new ArrayList<>();

}