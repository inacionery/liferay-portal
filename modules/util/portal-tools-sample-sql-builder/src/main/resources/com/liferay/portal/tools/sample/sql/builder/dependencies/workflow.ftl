<#list dataFactory.newKaleoDefinitionModels() as kaleoDefinitionModel>
	${dataFactory.toInsertSQL(kaleoDefinitionModel)}

	<#assign kaleoDefinitionVersionModel = dataFactory.newKaleoDefinitionVersionModel(kaleoDefinitionModel) />

	${dataFactory.toInsertSQL(kaleoDefinitionVersionModel)}

	${csvFileWriter.write("workflow", kaleoDefinitionModel.kaleoDefinitionId + "," + kaleoDefinitionModel.name + "\n")}

	<#assign approvedKaleoNodeModel = dataFactory.newApprovedKaleoNodeModel(kaleoDefinitionVersionModel) />

	${dataFactory.toInsertSQL(approvedKaleoNodeModel)}

	${dataFactory.toInsertSQL(dataFactory.newApproveKaleoActionModel(approvedKaleoNodeModel))}

	<#assign createdKaleoNodeModel = dataFactory.newCreatedKaleoNodeModel(kaleoDefinitionVersionModel) />

	${dataFactory.toInsertSQL(createdKaleoNodeModel)}

	<#assign reviewKaleoNodeModel = dataFactory.newReviewKaleoNodeModel(kaleoDefinitionVersionModel) />

	${dataFactory.toInsertSQL(reviewKaleoNodeModel)}

	<#assign reviewKaleoTaskModel = dataFactory.newKaleoTaskModel(reviewKaleoNodeModel) />

	${dataFactory.toInsertSQL(reviewKaleoTaskModel)}

	<#assign roleKaleoTaskAssignmentModel = dataFactory.newRoleKaleoTaskAssignmentModel(reviewKaleoTaskModel) />

	${dataFactory.toInsertSQL(roleKaleoTaskAssignmentModel)}

	<#assign updateKaleoNodeModel = dataFactory.newUpdateKaleoNodeModel(kaleoDefinitionVersionModel) />

	${dataFactory.toInsertSQL(updateKaleoNodeModel)}

	${dataFactory.toInsertSQL(dataFactory.newRejectKaleoActionModel(updateKaleoNodeModel))}

	<#assign updateKaleoTaskModel = dataFactory.newKaleoTaskModel(updateKaleoNodeModel) />

	${dataFactory.toInsertSQL(updateKaleoTaskModel)}

	${dataFactory.toInsertSQL(dataFactory.newUserKaleoTaskAssignmentModel(updateKaleoTaskModel))}

	${dataFactory.toInsertSQL(dataFactory.newKaleoTransitionModel("approve", reviewKaleoNodeModel, approvedKaleoNodeModel, true))}

	${dataFactory.toInsertSQL(dataFactory.newKaleoTransitionModel("reject", reviewKaleoNodeModel, updateKaleoNodeModel, false))}

	${dataFactory.toInsertSQL(dataFactory.newKaleoTransitionModel("resubmit", updateKaleoNodeModel, reviewKaleoNodeModel, true))}

	${dataFactory.toInsertSQL(dataFactory.newKaleoTransitionModel("review", createdKaleoNodeModel, reviewKaleoNodeModel, true))}

	<#-- Configure Definition -->

	${dataFactory.toInsertSQL(dataFactory.newWorkflowDefinitionLinkModel(kaleoDefinitionModel))}

	<#-- Add MBMessage -->

	<#assign mbThreadModel = dataFactory.newMBThreadModel(dataFactory.getCounterNext(), kaleoDefinitionModel.groupId, dataFactory.getCounterNext()) />

	${dataFactory.toInsertSQL(mbThreadModel)}

	<#assign mbMessageModels = dataFactory.newWorkflowMBMessageModels(mbThreadModel) />

	<#list mbMessageModels as mbMessageModel>
		<@insertMBMessage _mbMessageModel=mbMessageModel />

		<#assign kaleoInstanceModel = dataFactory.newKaleoInstanceModel(kaleoDefinitionModel, kaleoDefinitionVersionModel, mbMessageModel.messageId, false) />

		${dataFactory.toInsertSQL(kaleoInstanceModel)}

		${dataFactory.toInsertSQL(dataFactory.newWorkflowInstanceLinkModel(kaleoInstanceModel))}

		<#assign kaleoInstanceTokenModel = dataFactory.newKaleoInstanceTokenModel(kaleoInstanceModel, reviewKaleoNodeModel) />

		${dataFactory.toInsertSQL(kaleoInstanceTokenModel)}

		<#assign kaleoTaskInstanceTokenModel = dataFactory.newKaleoTaskInstanceTokenModel(kaleoInstanceTokenModel, reviewKaleoTaskModel) />

		${dataFactory.toInsertSQL(kaleoTaskInstanceTokenModel)}

		${dataFactory.toInsertSQL(dataFactory.newKaleoTaskAssignmentInstanceModel(roleKaleoTaskAssignmentModel, kaleoTaskInstanceTokenModel))}
	</#list>
</#list>