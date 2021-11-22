package mwg.wb.client.graph;

import com.orientechnologies.orient.core.command.OCommandDistributedReplicateRequest;
import com.orientechnologies.orient.core.exception.OConcurrentCreateException;
import com.orientechnologies.orient.server.distributed.ODistributedException;
import com.orientechnologies.orient.server.distributed.ODistributedServerManager;
import com.orientechnologies.orient.server.distributed.task.ODistributedOperationException;
import com.orientechnologies.orient.server.distributed.task.ODistributedRecordLockedException;

public class ManagerCluster {
//	protected RuntimeException manageConflicts() {
//	    if (!groupResponsesByResult || request.getTask().getQuorumType() == OCommandDistributedReplicateRequest.QUORUM_TYPE.NONE)
//	        // NO QUORUM
//	        return null;
//	    if (dManager.getNodeStatus() != ODistributedServerManager.NODE_STATUS.ONLINE)
//	        // CURRENT NODE OFFLINE: JUST RETURN
//	        return null;
//	    final int bestResponsesGroupIndex = getBestResponsesGroup();
//	    final List<ODistributedResponse> bestResponsesGroup = responseGroups.get(bestResponsesGroupIndex);
//	    final int maxCoherentResponses = bestResponsesGroup.size();
//	    final int conflicts = getExpectedResponses() - (maxCoherentResponses);
//	    if (isMinimumQuorumReached(true)) {
//	        // QUORUM SATISFIED
//	        if (responseGroups.size() == 1)
//	            // NO CONFLICT
//	            return null;
//	        if (checkNoWinnerCase(bestResponsesGroup))
//	            // TODO: CALL THE RECORD CONFLICT PIPELINE
//	            return null;
//	        if (fixNodesInConflict(bestResponsesGroup, conflicts))
//	            // FIX SUCCEED
//	            return null;
//	    }
//	    // QUORUM HASN'T BEEN REACHED
//	    if (ODistributedServerLog.isDebugEnabled()) {
//	        ODistributedServerLog.debug(this, dManager.getLocalNodeName(), null, DIRECTION.NONE, "Detected %d node(s) in timeout or in conflict and quorum (%d) has not been reached, rolling back changes for request (%s)", conflicts, quorum, request);
//	        ODistributedServerLog.debug(this, dManager.getLocalNodeName(), null, DIRECTION.NONE, composeConflictMessage());
//	    }
//	    if (!undoRequest()) {
//	        // SKIP UNDO
//	        return null;
//	    }
//	    // CHECK IF THERE IS AT LEAST ONE ODistributedRecordLockedException or OConcurrentCreateException
//	    for (Object r : responses.values()) {
//	        if (r instanceof ODistributedRecordLockedException)
//	            throw (ODistributedRecordLockedException) r;
//	        else if (r instanceof OConcurrentCreateException)
//	            throw (OConcurrentCreateException) r;
//	    }
//	    final Object goodResponsePayload = bestResponsesGroup.isEmpty() ? null : bestResponsesGroup.get(0).getPayload();
//	    if (goodResponsePayload instanceof RuntimeException)
//	        // RESPONSE IS ALREADY AN EXCEPTION: THROW THIS
//	        return (RuntimeException) goodResponsePayload;
//	    else if (goodResponsePayload instanceof Throwable)
//	        return OException.wrapException(new ODistributedException(composeConflictMessage()), (Throwable) goodResponsePayload);
//	    else {
//	        if (responseGroups.size() <= 2) {
//	            // CHECK IF THE BAD RESPONSE IS AN EXCEPTION, THEN PROPAGATE IT
//	            for (int i = 0; i < responseGroups.size(); ++i) {
//	                if (i == bestResponsesGroupIndex)
//	                    continue;
//	                final List<ODistributedResponse> badResponses = responseGroups.get(i);
//	                if (badResponses.get(0).getPayload() instanceof RuntimeException)
//	                    return (RuntimeException) badResponses.get(0).getPayload();
//	            }
//	        }
//	        return new ODistributedOperationException(composeConflictMessage());
//	    }
//	}
}
