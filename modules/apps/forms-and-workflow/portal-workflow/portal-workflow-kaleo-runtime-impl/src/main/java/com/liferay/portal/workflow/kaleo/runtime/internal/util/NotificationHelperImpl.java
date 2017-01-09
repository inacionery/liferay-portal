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

package com.liferay.portal.workflow.kaleo.runtime.internal.util;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.workflow.kaleo.definition.ExecutionType;
import com.liferay.portal.workflow.kaleo.model.KaleoNotification;
import com.liferay.portal.workflow.kaleo.model.KaleoNotificationRecipient;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.internal.notification.NotificationMessageGeneratorFactory;
import com.liferay.portal.workflow.kaleo.runtime.internal.notification.NotificationSenderFactory;
import com.liferay.portal.workflow.kaleo.runtime.notification.NotificationHelper;
import com.liferay.portal.workflow.kaleo.runtime.notification.NotificationMessageGenerator;
import com.liferay.portal.workflow.kaleo.runtime.notification.NotificationSender;
import com.liferay.portal.workflow.kaleo.service.KaleoNotificationLocalService;
import com.liferay.portal.workflow.kaleo.service.KaleoNotificationRecipientLocalService;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(immediate = true, service = NotificationHelper.class)
public class NotificationHelperImpl implements NotificationHelper {

	@Override
	public void sendKaleoNotifications(
			String kaleoClassName, long kaleoClassPK,
			ExecutionType executionType, ExecutionContext executionContext)
		throws PortalException {

		List<KaleoNotification> kaleoNotifications =
			_kaleoNotificationLocalService.getKaleoNotifications(
				kaleoClassName, kaleoClassPK, executionType.getValue());

		for (KaleoNotification kaleoNotification : kaleoNotifications) {
			sendKaleoNotification(kaleoNotification, executionContext);
		}
	}

	protected void sendKaleoNotification(
			KaleoNotification kaleoNotification,
			ExecutionContext executionContext)
		throws PortalException {
		System.out.println("start in NotificationHelperImpl sendKaleoNotifications");
		
		System.out.println("start in NotificationHelperImpl notificationMessageGenerator");
		NotificationMessageGenerator notificationMessageGenerator =
			_notificationMessageGeneratorFactory.
				getNotificationMessageGenerator(
					kaleoNotification.getTemplateLanguage());
		System.out.println("end in NotificationHelperImpl notificationMessageGenerator");
		
		System.out.println("start in NotificationHelperImpl 1 generateMessage");
		String notificationMessage =
			notificationMessageGenerator.generateMessage(
				kaleoNotification.getKaleoClassName(),
				kaleoNotification.getKaleoClassPK(),
				kaleoNotification.getName(),
				kaleoNotification.getTemplateLanguage(),
				kaleoNotification.getTemplate(), executionContext);
		System.out.println("end in NotificationHelperImpl 1 generateMessage");
		
		String notificationSubject = StringPool.BLANK;

		if (Validator.isNotNull(kaleoNotification.getDescription())) {
			System.out.println("start in NotificationHelperImpl 2 generateMessage");
			notificationSubject = notificationMessageGenerator.generateMessage(
				kaleoNotification.getKaleoClassName(),
				kaleoNotification.getKaleoClassPK(),
				kaleoNotification.getName(),
				kaleoNotification.getTemplateLanguage(),
				kaleoNotification.getDescription(), executionContext);
			System.out.println("end in NotificationHelperImpl 2 generateMessage");
		}

		String[] notificationTypes = StringUtil.split(
			kaleoNotification.getNotificationTypes());

		System.out.println("start in NotificationHelperImpl kaleoNotificationRecipient");
		List<KaleoNotificationRecipient> kaleoNotificationRecipient =
			_kaleoNotificationRecipientLocalService.
				getKaleoNotificationRecipients(
					kaleoNotification.getKaleoNotificationId());
		System.out.println("end in NotificationHelperImpl kaleoNotificationRecipient");
		
		for (String notificationType : notificationTypes) {
			NotificationSender notificationSender =
				_notificationSenderFactory.getNotificationSender(
					notificationType);

			System.out.println("start in NotificationHelperImpl sendNotification");
			notificationSender.sendNotification(
				kaleoNotificationRecipient, notificationSubject,
				notificationMessage, executionContext);
			System.out.println("end in NotificationHelperImpl sendNotification");
		}
		System.out.println("end in NotificationHelperImpl sendKaleoNotifications");
	}

	@Reference
	private KaleoNotificationLocalService _kaleoNotificationLocalService;

	@Reference
	private KaleoNotificationRecipientLocalService
		_kaleoNotificationRecipientLocalService;

	@Reference
	private NotificationMessageGeneratorFactory
		_notificationMessageGeneratorFactory;

	@Reference
	private NotificationSenderFactory _notificationSenderFactory;

}