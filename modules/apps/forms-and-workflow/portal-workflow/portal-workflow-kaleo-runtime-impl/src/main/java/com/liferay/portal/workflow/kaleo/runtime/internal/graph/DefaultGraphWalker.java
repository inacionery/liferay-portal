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

package com.liferay.portal.workflow.kaleo.runtime.internal.graph;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.transaction.Isolation;
import com.liferay.portal.kernel.transaction.Propagation;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.spring.extender.service.ServiceReference;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.runtime.graph.GraphWalker;
import com.liferay.portal.workflow.kaleo.runtime.graph.PathElement;
import com.liferay.portal.workflow.kaleo.runtime.internal.BaseKaleoBean;
import com.liferay.portal.workflow.kaleo.runtime.internal.node.NodeExecutorFactory;
import com.liferay.portal.workflow.kaleo.runtime.node.NodeExecutor;
import com.liferay.portal.workflow.kaleo.runtime.util.ExecutionContextHelper;

import java.util.List;

/**
 * @author Michael C. Han
 */
@Transactional(
	isolation = Isolation.PORTAL, propagation = Propagation.REQUIRES_NEW,
	rollbackFor = {Exception.class}
)
public class DefaultGraphWalker extends BaseKaleoBean implements GraphWalker {

	@Override
	public void follow(
			KaleoNode sourceKaleoNode, KaleoNode targetKaleoNode,
			List<PathElement> remainingPathElements,
			ExecutionContext executionContext)
		throws PortalException {

		System.out.println("start in DefaultGraphWalker follow");

		if (sourceKaleoNode != null) {
			System.out.println("start in DefaultGraphWalker _nodeExecutorFactory.getNodeExecutor");
			NodeExecutor nodeExecutor = _nodeExecutorFactory.getNodeExecutor(
				sourceKaleoNode.getType());

			nodeExecutor.exit(
				sourceKaleoNode, executionContext, remainingPathElements);
			System.out.println("end in DefaultGraphWalker _nodeExecutorFactory.getNodeExecutor");
		}

		if (targetKaleoNode != null) {
			System.out.println("start in DefaultGraphWalker kaleoLogLocalService.addNodeEntryKaleoLog");
			kaleoLogLocalService.addNodeEntryKaleoLog(
				executionContext.getKaleoInstanceToken(), sourceKaleoNode,
				targetKaleoNode, executionContext.getServiceContext());
			System.out.println("end in DefaultGraphWalker kaleoLogLocalService.addNodeEntryKaleoLog");

			System.out.println("start in DefaultGraphWalker _nodeExecutorFactory.getNodeExecutor");
			System.out.println("targetKaleoNode.getType() = " + targetKaleoNode.getType());
			NodeExecutor nodeExecutor = _nodeExecutorFactory.getNodeExecutor(
				targetKaleoNode.getType());
			System.out.println("end in DefaultGraphWalker _nodeExecutorFactory.getNodeExecutor");

			System.out.println("start in DefaultGraphWalker nodeExecutor.enter");
			boolean performExecute = nodeExecutor.enter(
				targetKaleoNode, executionContext);
			System.out.println("end in DefaultGraphWalker nodeExecutor.enter");

			if (performExecute) {
				System.out.println("star in DefaultGraphWalker nodeExecutor.execute");
				nodeExecutor.execute(
					targetKaleoNode, executionContext, remainingPathElements);
				System.out.println("end in DefaultGraphWalker nodeExecutor.execute");
			}
		}

		_executionContextHelper.checkKaleoInstanceComplete(executionContext);
		
		System.out.println("end in DefaultGraphWalker follow");
	}

	@ServiceReference(type = ExecutionContextHelper.class)
	private ExecutionContextHelper _executionContextHelper;

	@ServiceReference(type = NodeExecutorFactory.class)
	private NodeExecutorFactory _nodeExecutorFactory;

}