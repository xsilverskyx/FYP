/**
 *
 */
package agentbase;

import simbase.AgentManager;
import modelbase.AgentLogicModel;
import modelbase.Entity;
import modelbase.SellerLogicModel;

/**
 * @author akai
 * 
 */
public class SimpleAgentManager extends AgentManager {
	public int getHonestSellerNum() {
		int honestSellerNum = 0;
		for (Entity e : getSellers().getAll())
			if (((SellerLogicModel) ((Agent) e).getLogicModel()).responseQuery()) {
				honestSellerNum++;
			}
		return honestSellerNum;
	}
}
