package org.jpos.qi.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jpos.ee.Agent;
import org.jpos.ee.AgentManager;
import org.jpos.ee.BLException;
import org.jpos.ee.DB;
import org.jpos.qi.QI;
import org.jpos.qi.ViewConfig;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public class AgentsHelper extends QIHelper {

	public AgentsHelper(ViewConfig viewConfig) {
		super(Agent.class, viewConfig);
	}

	public Agent saveAgent(Agent newAgent) throws BLException {

		try {
			return (Agent) DB.execWithTransaction(db -> {

				if (newAgent.getWallet() != null) {
					newAgent.getWallet().setAgent(newAgent);
					// db.session().merge(newAgent.getWallet());
				}

				db.session().save(newAgent);
				return newAgent;
			});
		} catch (Exception e) {
			throw new BLException(e.getMessage());
		}
	}

	public Set<Agent> getParentAgents(Long agentId) {
		 Map<String,Boolean> orders = new HashMap<>();
		try {
			return (Set<Agent>) getPossibleParents(0,100,orders,agentId).collect(Collectors.toSet());
		} catch (Exception e) {
			getApp().getLog().error(e);
			return null;
		}
	}

	@Override
	public String getItemId(Object item) {
		return String.valueOf(((Agent) item).getId());
	}


	public Stream getPossibleParents(int offset, int limit, Map<String, Boolean> orders, Long agentId)
			throws Exception {
		List<Agent> agents = (List<Agent>) DB.exec(db -> {
			AgentManager mgr = new AgentManager(db);
			return mgr.getPossibleParents(offset, limit, orders, agentId);
		});
		return agents.stream();
	}

	private int getPossibleParentsCount(Long agentId) throws Exception {
		return (int) DB.exec(db -> {
			AgentManager mgr = new AgentManager(db);
			return mgr.getPossibleParentsCount(agentId);
		});
	}

	private Stream getAllChildren(int offset, int limit, Map<String, Boolean> orders, Agent parent) throws Exception {
		List<Agent> agents = (List<Agent>) DB.exec(db -> {
			AgentManager mgr = new AgentManager(db);
			return mgr.getAllChildren(offset, limit, orders, parent);
		});
		return agents.stream();
	}

	private boolean isParent(Agent parent) throws Exception {
		return (boolean) DB.exec(db -> {
			db.session().refresh(parent);
			return !parent.getChildren().isEmpty();
		});

	}

	private int getChildrenCount(Agent parent) throws Exception {
		return (int) DB.exec(db -> {
			db.session().refresh(parent);
			return parent.getChildren().size();
		});

	}

	public DataProvider getParentDataProvider(Long agentId) {
		Map<String, Boolean> orders = new HashMap<>();
		DataProvider dataProvider = DataProvider.fromCallbacks((CallbackDataProvider.FetchCallback) query -> {
			int offset = query.getOffset();
			int limit = query.getLimit();
			Iterator it = query.getSortOrders().iterator();
			while (it.hasNext()) {
				QuerySortOrder order = (QuerySortOrder) it.next();
				orders.put(order.getSorted(), order.getDirection() == SortDirection.DESCENDING);
			}
			try {
				return getPossibleParents(offset, limit, orders, agentId);
			} catch (Exception e) {
				getApp().getLog().error(e);
				return null;
			}
		}, (CallbackDataProvider.CountCallback<Agent, Void>) query -> {
			try {
				return getPossibleParentsCount(agentId);
			} catch (Exception e) {
				getApp().getLog().error(e);
				return 0;
			}
		});
		return dataProvider;
	}

	public boolean deleteAgent(Agent agent) throws BLException {
		try {
			return DB.execWithTransaction(db -> {
				if (agent.getWallet() != null) {
					db.session().delete(agent.getWallet());
				}
				db.session().delete(agent);
				addRevisionRemoved(db, getEntityName(), getItemId(agent));
				return true;
			});
		} catch (Exception e) {
			throw new BLException(e.getMessage());
		}
	}

	public Agent updateAgent(Agent agent) {
		try {
			return (Agent) DB.execWithTransaction((db) -> {
				Agent oldAgent = (Agent) ((Agent) getOriginalEntity()).clone();
				if (agent.isParentAgent() && agent.isSubAgent()) {
					getApp().displayNotification("Error updating entity an agent with children cannot have a parent");
					return oldAgent;
				}
				db.session().merge(agent);
				if (agent.getWallet() != null) {
					agent.getWallet().setAgent(agent);
					db.session().merge(agent.getWallet());
				} else if (oldAgent.getWallet() != null) {
					oldAgent.getWallet().setAgent(null);
					db.session().merge(oldAgent.getWallet());
				}
				return addRevisionUpdated(db, getEntityName(), String.valueOf(agent.getId()), oldAgent, agent,
						new String[] { "name", "wallet", "parent", "type", "fundingAccount", "active", "mcc" });
			});
		} catch (Exception e) {
			QI.getQI().getLog().error(e);
			return agent;
		}
	}

}
