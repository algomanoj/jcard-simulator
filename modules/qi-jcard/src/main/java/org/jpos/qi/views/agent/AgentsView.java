package org.jpos.qi.views.agent;

import org.jpos.ee.Agent;
import org.jpos.ee.BLException;
import org.jpos.qi.services.AgentsHelper;
import org.jpos.qi.services.QIHelper;
import org.jpos.qi.views.QIEntityView;
import org.jpos.qi.views.minigl.AccountConverter;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;

public class AgentsView extends QIEntityView<Agent> {

	private Long agentId;

	public AgentsView() {
		super(Agent.class, "agents");
	}

	@Override
	public GridCrud createCrud() {
		GridCrud<Agent> crud = super.createCrud();
		initiateGrid(crud.getGrid());

		TextField walletField = new TextField();
		crud.getCrudFormFactory().setFieldProvider("wallet", () -> walletField);
		crud.getCrudFormFactory().setConverter("wallet", new WalletConverter(true, true, true));

		TextField mccField = new TextField();
		crud.getCrudFormFactory().setFieldProvider("mcc", () -> mccField);
		crud.getCrudFormFactory().setConverter("mcc", new MccConverter(true, true, true));

		crud.getGrid().getSelectionModel().getFirstSelectedItem().ifPresent(item -> {

			if (item != null) {
				Agent ag = (Agent) item;
				this.agentId = ag.getId();
			}
		});

		// ComboBoxProvider comboBox = new ComboBoxProvider("parent", null);
		ComboBox<Agent> comboParent = new ComboBox("parent",
				((AgentsHelper) getHelper()).getParentAgents(this.agentId));

		comboParent.setItemLabelGenerator(
				(ItemLabelGenerator<Agent>) captionGenerator -> captionGenerator != null ? captionGenerator.getName()
						: "");
		crud.getCrudFormFactory().setFieldProvider("parent", () -> comboParent);

		TextField fundingAccount = new TextField();
		crud.getCrudFormFactory().setFieldProvider("fundingAccount", () -> fundingAccount);
		crud.getCrudFormFactory().setConverter("fundingAccount", new AccountConverter(true, true, true));
		doCustomCrudOperation(crud);

		return crud;
	}

	private void doCustomCrudOperation(GridCrud<Agent> crud) {
		crud.setAddOperation(agent -> {
			try {
				return getHelper().saveAgent(agent);
			} catch (BLException e) {
				e.printStackTrace();
			}
			return agent;
		});

		crud.setDeleteOperation(agent -> {
			try {
				getHelper().deleteAgent(agent);
			} catch (BLException e) {
				e.printStackTrace();
			}
		});

		crud.setUpdateOperation(agent -> {
			return getHelper().updateAgent(agent);
		});

	}

	private void initiateGrid(Grid<Agent> grid) {
		grid.removeAllColumns();
		grid.addColumn(Agent::getId).setHeader(getApp().getMessage("agent.id")).setSortable(true).setKey("id");
		grid.addColumn(Agent::getName).setHeader(getApp().getMessage("agent.name")).setSortable(true).setKey("name");
		grid.addColumn(agent -> agent.getWallet() != null ? agent.getWallet().getId() : "")
				.setHeader(getApp().getMessage("agent.wallet")).setSortable(true).setKey("wallet");
		// grid.addColumn(agent -> agent.getParent() != null ? agent.getParent().getId()
		// :
		// "").setHeader(getApp().getMessage("agent.parent")).setSortable(true).setKey("parent");
		grid.addColumn(Agent::getType).setHeader(getApp().getMessage("agent.type")).setSortable(true).setKey("type");
		grid.addColumn(agent -> agent.getFundingAccount() != null ? agent.getFundingAccount().getCode() : "")
				.setHeader(getApp().getMessage("agent.fundingAccount")).setSortable(true).setKey("fundingAccount");
		grid.addColumn(Agent::getActive).setHeader(getApp().getMessage("agent.active")).setSortable(true)
				.setKey("active");
		grid.addColumn(Agent::getMcc).setHeader(getApp().getMessage("agent.mcc")).setSortable(true).setKey("mcc");
	}

	@Override
	public QIHelper createHelper() {
		return new AgentsHelper(getViewConfig());
	}

	public AgentsHelper getHelper() {
		return (AgentsHelper) super.getHelper();
	}
}
