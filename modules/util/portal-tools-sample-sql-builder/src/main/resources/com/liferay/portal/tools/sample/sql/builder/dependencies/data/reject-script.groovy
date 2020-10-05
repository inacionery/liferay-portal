import com.liferay.portal.kernel.workflow.WorkflowStatusManagerUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

WorkflowStatusManagerUtil.updateStatus(
	WorkflowConstants.getLabelStatus("denied"), workflowContext);

WorkflowStatusManagerUtil.updateStatus(
	WorkflowConstants.getLabelStatus("pending"), workflowContext);