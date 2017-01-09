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

package com.liferay.portal.workflow.kaleo.runtime.internal.node;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.KaleoTimer;
import com.liferay.portal.workflow.kaleo.model.KaleoTransition;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.graph.PathElement;
import com.liferay.portal.workflow.kaleo.runtime.node.BaseNodeExecutor;
import com.liferay.portal.workflow.kaleo.runtime.node.NodeExecutor;
import com.liferay.portal.workflow.kaleo.service.KaleoInstanceTokenLocalService;

import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Michael C. Han
 */
@Component(
	immediate = true, property = {"node.type=JOIN_XOR"},
	service = NodeExecutor.class
)
public class JoinXorNodeExecutor extends BaseNodeExecutor {

	@Override
	protected boolean doEnter(
			KaleoNode currentKaleoNode, ExecutionContext executionContext)
		throws PortalException {
		System.out.println("start JoinXorNodeExecutor doEnter");
		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();
		System.out.println("start JoinXorNodeExecutor getKaleoInstanceToken");
		kaleoInstanceToken =
			_kaleoInstanceTokenLocalService.getKaleoInstanceToken(
				kaleoInstanceToken.getKaleoInstanceTokenId());
		System.out.println("end JoinXorNodeExecutor getKaleoInstanceToken");
		if (kaleoInstanceToken.isCompleted()) {
			return false;
		}
		System.out.println("start JoinXorNodeExecutor completeKaleoInstanceToken");
		kaleoInstanceToken =
			_kaleoInstanceTokenLocalService.completeKaleoInstanceToken(
				kaleoInstanceToken.getKaleoInstanceTokenId());
		System.out.println("end JoinXorNodeExecutor completeKaleoInstanceToken");
		System.out.println("start JoinXorNodeExecutor parentKaleoInstanceToken");
		KaleoInstanceToken parentKaleoInstanceToken =
			kaleoInstanceToken.getParentKaleoInstanceToken();
		System.out.println("end JoinXorNodeExecutor parentKaleoInstanceToken");
		if (!parentKaleoInstanceToken.
				hasIncompleteChildrenKaleoInstanceToken()) {

			return false;
		}
		System.out.println("start JoinXorNodeExecutor childrenKaleoInstanceTokens");
		List<KaleoInstanceToken> childrenKaleoInstanceTokens =
			parentKaleoInstanceToken.getChildrenKaleoInstanceTokens();
		System.out.println("end JoinXorNodeExecutor childrenKaleoInstanceTokens");
		for (KaleoInstanceToken childrenKaleoInstanceToken :
				childrenKaleoInstanceTokens) {
			System.out.println("start JoinXorNodeExecutor completeKaleoInstanceToken");
			_kaleoInstanceTokenLocalService.completeKaleoInstanceToken(
				childrenKaleoInstanceToken.getKaleoInstanceTokenId());
			System.out.println("end JoinXorNodeExecutor completeKaleoInstanceToken");
		}
		System.out.println("end JoinXorNodeExecutor doEnter");
		return true;
	}

	@Override
	protected void doExecute(
			KaleoNode currentKaleoNode, ExecutionContext executionContext,
			List<PathElement> remainingPathElements)
		throws PortalException {

		KaleoInstanceToken kaleoInstanceToken =
			executionContext.getKaleoInstanceToken();

		KaleoInstanceToken parentKaleoInstanceToken =
			kaleoInstanceToken.getParentKaleoInstanceToken();

		if (parentKaleoInstanceToken.getCurrentKaleoNodeId() ==
				currentKaleoNode.getKaleoNodeId()) {

			return;
		}

		parentKaleoInstanceToken =
			_kaleoInstanceTokenLocalService.updateKaleoInstanceToken(
				parentKaleoInstanceToken.getKaleoInstanceTokenId(),
				currentKaleoNode.getKaleoNodeId());

		KaleoTransition kaleoTransition =
			currentKaleoNode.getDefaultKaleoTransition();

		ExecutionContext newExecutionContext = new ExecutionContext(
			parentKaleoInstanceToken, executionContext.getWorkflowContext(),
			executionContext.getServiceContext());

		PathElement pathElement = new PathElement(
			currentKaleoNode, kaleoTransition.getTargetKaleoNode(),
			newExecutionContext);

		remainingPathElements.add(pathElement);
	}

	@Override
	protected void doExecuteTimer(
		KaleoNode currentKaleoNode, KaleoTimer kaleoTimer,
		ExecutionContext executionContext) {
	}

	@Override
	protected void doExit(
		KaleoNode currentKaleoNode, ExecutionContext executionContext,
		List<PathElement> remainingPathElements) {
	}

	@Reference
	private KaleoInstanceTokenLocalService _kaleoInstanceTokenLocalService;

}