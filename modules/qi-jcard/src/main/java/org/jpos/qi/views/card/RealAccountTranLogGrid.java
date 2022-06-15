package org.jpos.qi.views.card;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.Query;

import org.jpos.ee.Card;
import org.jpos.ee.TranLog;
import org.jpos.ee.TranLogFilter;
import org.jpos.gl.Layer;
import org.jpos.qi.services.TranLogHelper;
import org.jpos.qi.util.DateRange;
import java.util.List;

import static org.jpos.qi.QIUtils.getCaptionFromId;

public class RealAccountTranLogGrid extends Grid<TranLog> {
    private DateRange dateRange = new DateRange(DateRange.THIS_MONTH);
    private Card card;
    private String[] itcList;
    private TranLogHelper helper;
    private boolean withoutCompletion;
    private int itemCount;
    private  ConfigurableFilterDataProvider dataProvider;

    public RealAccountTranLogGrid(Card card, DateRange dateRange) {
        this(card, dateRange, null, false);
    }

    public RealAccountTranLogGrid(Card card,
                                  DateRange dateRange,
                                  String[] itcList, boolean withoutCompletion) {
        super();
        this.card = card;
        this.itcList = itcList;
        this.helper = new TranLogHelper();
        if (dateRange != null)
            this.dateRange = dateRange;
        this.withoutCompletion = withoutCompletion;
        dataProvider = (ConfigurableFilterDataProvider) helper.getDataProvider();
        TranLogFilter filter = new TranLogFilter();
        filter.setCard(card);
        filter.setItcList(itcList);
        filter.setStart(this.dateRange.getStart());
        filter.setEnd(this.dateRange.getEnd());
        filter.setPending(withoutCompletion);
        dataProvider.setFilter(filter);
        itemCount = dataProvider.size(new Query());

//        addItemClickListener(this::navigateToSpecificView);
//        sort(getColumnByKey("date"), SortDirection.ASCENDING);
//        getColumn("ca_name").setSortProperty("cardAcceptor");
//        getColumn("ca_country").setSortProperty("cardAcceptor");
//        setColumnOrder("date", "approvalNumber", "lastFour", "itc" , "ca_name", "ca_country", "tid");
    }

    public Component renderGrid() {
        setWidth("100%");
        setSelectionMode(Grid.SelectionMode.SINGLE);
        setColumnReorderingAllowed(true);
        setGridGetters();
        setItems(dataProvider);
        return this;
    }

    public int getItemCount() {
        return itemCount;
    }

    private Component getDefaultLabel(String msg) {
        Label cardLabel= new Label();
        cardLabel.addClassName("searchCardHolderLabel");
        cardLabel.setText(msg);
        return cardLabel;
    }

    public void setGridGetters () {
        addColumn(TranLog::getDate)
          .setHeader(getCaptionFromId("column.date"))
          .setId("date");
//          .setRenderer(createTimestampRenderer()).setCaption(getCaptionFromId("column.date"));
        addColumn(TranLog::getApprovalNumber).setHeader(getCaptionFromId("approvalNumber")).setId("approvalNumber");
        addColumn(tranLog ->
          tranLog.getCard() != null ? tranLog.getCard().getLastFour() : "")
          .setHeader(getCaptionFromId("column.lastFour"))
          .setId("lastFour");
//        addColumn(tranLog -> getItcDescription(tranLog))
//          .setId("itc").setCaption(getCaptionFromId("itc"));
        addColumn(TranLog::getMid).setHeader(getCaptionFromId("mid")).setId("mid");
        addColumn(tranLog -> tranLog.getCardAcceptor() != null ? tranLog.getCardAcceptor().getName() : "")
          .setHeader(getCaptionFromId("column.ca_name"))
          .setId("ca_name");
//        addColumn(tranLog -> tranLog.getCardAcceptor() != null ? tranLog.getCardAcceptor().getCountry() : "")
//          .setId("ca_country")
//          .setCaption(getCaptionFromId("column.ca_country"));
//        addColumn(TranLog::getRrn).setId("rrn").setCaption(getCaptionFromId("rrn"));
//        List<Layer> layers = getHelper().getAllLayers();
//        addColumn(tranLog -> {
//            Layer layer = getLayer(layers, tranLog.getCurrencyCode());
//            return layer != null && layer.getName() != null ? layer.getName() : tranLog.getCurrencyCode();
//        }).setId("currencyCode").setCaption(getCaptionFromId("column.currencyCode"));
//        Column amountCol = addColumn(TranLog::getAmount).setId("amount")
//          .setRenderer(createAmountRenderer())
//          .setCaption(getCaptionFromId("amount"));
//        amountCol.setStyleGenerator(obj -> {
//            Object value = amountCol.getValueProvider().apply(obj);
//            if (value instanceof BigDecimal && !amountCol.getId().equals("id")) {
//                return "align-right";
//            }
//            return null;
//        });
//        addColumn(TranLog::getRc).setId("rc").setCaption(getCaptionFromId("column.rc"));
//        addColumn(TranLog::getTid).setId("tid").setCaption(getCaptionFromId("column.tid"));
    }

    public void refreshGrid (DateRange dateRange) {
        if (this.dateRange != null)
            this.dateRange = dateRange;
        ConfigurableFilterDataProvider wrapper = (ConfigurableFilterDataProvider) getDataProvider();
        TranLogFilter filter = new TranLogFilter();
        filter.setCard(card);
        filter.setItcList(itcList);
        filter.setStart(dateRange.getStart());
        filter.setEnd(dateRange.getEnd());
        filter.setPending(withoutCompletion);
        wrapper.setFilter(filter);
        wrapper.refreshAll();
    }

    public void refreshGrid (TranLogFilter filter) {
        filter.setCard(card);
        ConfigurableFilterDataProvider wrapper = (ConfigurableFilterDataProvider) getDataProvider();
        wrapper.setFilter(filter);
        wrapper.refreshAll();
    }

//    protected void navigateToSpecificView(Grid.ItemClick event) {
//        TranLog tranLog = (TranLog) event.getItem();
//        if (tranLog != null) {
//            String url = "/tranlog/" + tranLog.getId();
//            QI.getQI().getNavigator().navigateTo(url);
//        }
//    }

    Layer getLayer(List<Layer> layers, String layerString) {
        if (layers != null && layerString != null) {
            short layer;
            try {
                layer = Short.parseShort(layerString);
            } catch (NumberFormatException e) {
                return null;
            }
            for (Layer l : layers) {
                if (layer == l.getId())
                    return l;
            }
        }
        return null;
    }

    public TranLogHelper getHelper() {
        return helper;
    }

    public void setHelper(TranLogHelper helper) {
        this.helper = helper;
    }

//    private String getItcDescription (TranLog tranLog) {
//        String itc = "";
//        if (tranLog != null) {
//            itc = tranLog.getItc() + ": " + getItcFromConfigX(tranLog);
//            if (EntryMode.ECOMMERCE.equals(tranLog.getEntryMode()))
//                itc = itc + " " + "online";
//            if (tranLog.getReversalId() != null)
//                itc =" (" + QI.getQI().getMessage("reversed") + ")" + itc;
//            if (tranLog.getVoidId() != null)
//                itc = " (" + QI.getQI().getMessage("voided") + ")" + itc;
//        }
//        return itc;
//    }
//
//    private String getItcFromConfigX (TranLog tranLog) {
//        String desc = configX.getCodeDescription( new TranDescription(tranLog.getItc(), tranLog.getSs(), tranLog.getCurrencyCode()));
//        return desc;
//    }
}
