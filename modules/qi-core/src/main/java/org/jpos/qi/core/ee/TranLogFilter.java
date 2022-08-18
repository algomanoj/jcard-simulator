package org.jpos.qi.core.ee;

import java.util.Date;

import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardProduct;

public class TranLogFilter {
    private String realAccount;
    private String[] itcList;
    private String[] midList;
    private String id;
    private Date start;
    private Date end;
    private boolean approved; // IRC is 0
    private boolean pending;  // Completion is NULL
    private boolean rejected; // IRC is not 0
    private boolean voided;   // Void is not NULL
    private short[] layers;
    private CardProduct cardProduct;
    private Card card;
    private CardHolder cardHolder;
    private String rrn;

    public TranLogFilter() {
        super();
    }
    
    public String[] getItcList() {
        return itcList;
    }

    public void setItcList(String[] itcList) {
        this.itcList = itcList;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

    public String getRealAccount() {
        return realAccount;
    }

    public void setRealAccount(String realAccount) {
        this.realAccount = realAccount;
    }

    public short[] getLayers() {
        return layers;
    }

    public void setLayers(short[] layers) {
        this.layers = layers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CardProduct getCardProduct() {
        return cardProduct;
    }

    public void setCardProduct(CardProduct cardProduct) {
        this.cardProduct = cardProduct;
    }

    public String[] getMidList() {
        return midList;
    }

    public void setMidList(String[] midList) {
        this.midList = midList;
    }
    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

	public CardHolder getCardHolder() {
		return cardHolder;
	}

	public void setCardHolder(CardHolder cardHolder) {
		this.cardHolder = cardHolder;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
    
}
