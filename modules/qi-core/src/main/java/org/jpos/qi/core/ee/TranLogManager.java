package org.jpos.qi.core.ee;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.jpos.ee.Constants.TRAN_APPROVED;
import static org.jpos.ee.Constants.TRAN_PARTIALLY_APPROVED;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.apache.commons.lang3.StringUtils;
import org.jpos.ee.Card;
import org.jpos.ee.CardHolder;
import org.jpos.ee.CardProduct;
import org.jpos.ee.DB;
import org.jpos.ee.DBManager;
import org.jpos.ee.TranLog;
import org.jpos.ee.Wallet;
import org.jpos.gl.Account;
import java.math.BigDecimal;

public class TranLogManager extends DBManager<TranLog> {

    private Date from = null;
    private Date to = null;
    private Card card;
    private String tranLogId;
    private String realAccount;
    private String[] itcList;
    private String[] midList;
    private boolean approved;
    private boolean pending;
    private boolean rejected;
    private boolean voided;
    private short[] layers;
	private String rrn;
    private CardProduct cardProduct;
    private CardHolder cardHolder;

    public TranLogManager(DB db) {
        super(db,TranLog.class);
    }

    public TranLogManager(DB db, Date from, Date to) {
        this(db, from, to, null);
    }

    public TranLogManager(DB db, Date from, Date to, Card card) {
        super(db,TranLog.class);
        this.from = from;
        this.to = to;
        this.card = card;
    }

    public TranLogManager (DB db, TranLogFilter filter) {
        super(db, TranLog.class);
        this.from = filter.getStart();
        this.to = filter.getEnd();
        this.itcList = filter.getItcList();
        this.midList = filter.getMidList();
        this.layers = filter.getLayers();
        this.realAccount = filter.getRealAccount();
        this.approved = filter.isApproved();
        this.pending = filter.isPending();
        this.rejected = filter.isRejected();
        this.voided = filter.isVoided();
        this.tranLogId = filter.getId();
        this.cardProduct = filter.getCardProduct();
        this.card = filter.getCard();
        this.cardHolder=filter.getCardHolder();
		this.rrn=filter.getRrn();
    }

    @SuppressWarnings("unchecked")
    public List<TranLog> getUnsettledTransactions (Date after, Date before, int limit) {
        @SuppressWarnings("deprecation")
        Criteria crit = db.session().createCriteria(TranLog.class)
          .add(Restrictions.le("date", before))
          .add(Restrictions.ge("date", after))
          .add (
            Restrictions.or(
              Restrictions.eq("irc", TRAN_APPROVED),
              Restrictions.eq("irc", TRAN_PARTIALLY_APPROVED))
          )
          .add (Restrictions.ne("itc", "100.31"))
          .add (
            Restrictions.disjunction()
              .add(Restrictions.like("itc", "100.%"))
              .add(Restrictions.like("itc", "120.%"))
              .add(Restrictions.conjunction().add(Restrictions.like("itc", "420.%"))
                .add(Restrictions.isNotNull("replacementAmount"))
                .add(Restrictions.or(
                  Restrictions.like("originalItc", "100.%"),
                  Restrictions.like("originalItc", "120.%"))
                )
              )
          )
          .add(Restrictions.eq("batchNumber", 0L))
          .add (Restrictions.isNull ("reversalId"))
          .add (Restrictions.isNull ("completionId"))
          .add (Restrictions.isNotNull("glTransaction"))
          .add (Restrictions.isNull("voidId"));

        if (limit > 0)
            crit.setMaxResults(limit);

        return (List<TranLog>) crit.list();
    }
    
    @SuppressWarnings("unchecked")
    public List<TranLog> getByRRN(String rrn) {
        Criteria crit = db.session().createCriteria(TranLog.class)
                .add(Restrictions.eq("rrn",rrn));
        return crit.list();
    }

    public List<TranLog> getTranlogsByWallet (Wallet wallet, int length, Date start, Date end, String[] itcList) {
        return getTranlogsByWallet(wallet, length, start, end, itcList, false);
    }

    public List<TranLog> getTranlogsByWallet (Wallet wallet, int length, Date start, Date end, String[] itcList, boolean ascending) {
        CriteriaBuilder builder = db.session().getCriteriaBuilder();
        CriteriaQuery<TranLog> criteriaQuery = builder.createQuery(TranLog.class);
        Root<TranLog> tranLogRoot = criteriaQuery.from(TranLog.class);
        criteriaQuery.select(tranLogRoot);
        List<Predicate> predicates = new ArrayList<>();
        Predicate walletPredicate = builder.or(
                builder.equal(tranLogRoot.get("wallet"), wallet), builder.equal(tranLogRoot.get("wallet2"), wallet));
        predicates.add(walletPredicate);
        if (start != null)
            predicates.add(builder.greaterThanOrEqualTo(tranLogRoot.get("date"), start));
        if (end != null)
            predicates.add(builder.lessThanOrEqualTo(tranLogRoot.get("date"), end));
        if (itcList != null && itcList.length > 0) {
            Expression<String> itcExpression = tranLogRoot.get("itc");
            Predicate itcListPredicate = itcExpression.in(itcList);
            predicates.add(itcListPredicate);
        }
        Predicate[] predicatesArray = new Predicate[predicates.size()];
        predicates.toArray(predicatesArray);
        criteriaQuery.where(predicatesArray);
        if (ascending)
            criteriaQuery.orderBy(builder.asc(tranLogRoot.get("id")));
        else
            criteriaQuery.orderBy(builder.desc(tranLogRoot.get("id")));
        Query<TranLog> query = db.session().createQuery(criteriaQuery);
        query.setMaxResults(length);
        return query.getResultList();
    }

    @Override
    protected Predicate[] buildFilters(Root<TranLog> root) {
        ArrayList<Predicate> predicates = new ArrayList<>();
        Predicate notDeletedPredicate = db.session().getCriteriaBuilder().isFalse(root.get("deleted"));
        predicates.add(notDeletedPredicate);
        Predicate cardPredicate;
        Predicate datePredicate;
        if (card != null) {
            cardPredicate = db.session().getCriteriaBuilder().equal(root.get("card"), card);
            predicates.add(cardPredicate);
        }
        if (cardHolder != null) {
            predicates.add(db.session().getCriteriaBuilder().equal(root.get("cardHolder"), cardHolder));
        }
        if (rrn != null) {
            Predicate idPredicate = db.session().getCriteriaBuilder().equal(root.get("rrn"), rrn);
            predicates.add(idPredicate);
        }
        if (tranLogId != null) {
            Predicate idPredicate = db.session().getCriteriaBuilder().equal(root.get("id"), tranLogId);
            predicates.add(idPredicate);
        }
        if (from != null && to != null) {
            datePredicate = db.session().getCriteriaBuilder().between(root.get("date"), from, to);
            predicates.add(datePredicate);
        }
        if (itcList != null && itcList.length > 0) {
            List<Predicate> itcsOr = new ArrayList<>();
            for (String itc : itcList) {
                Expression<String> itcExpression = root.get("itc");
                Predicate itcListPredicate = db.session().getCriteriaBuilder().like(itcExpression, itc.trim());
                itcsOr.add(itcListPredicate);
            }
            predicates.add(db.session().getCriteriaBuilder().or(itcsOr.toArray(new Predicate[0])));
        }
        if (midList != null && midList.length > 0) {
            List<Predicate> midsOr = new ArrayList<>();
            for (String mid : midList) {
                Expression<String> midExpression = root.get("mid");
                Predicate midListPredicate = db.session().getCriteriaBuilder().like(midExpression, mid.trim());
                midsOr.add(midListPredicate);
            }
            predicates.add(db.session().getCriteriaBuilder().or(midsOr.toArray(new Predicate[0])));
        }
        
        if (pending) {
            Predicate completionPredicate = db.session().getCriteriaBuilder().isNull(root.get("completionId"));
            predicates.add(completionPredicate);
        }
        
        ArrayList<Predicate> approvedOrRejectedPredList = new ArrayList<>();
        if (approved) {
        	Predicate approvedPredicate = db.session().getCriteriaBuilder().equal(root.get("irc"), "0000");
             approvedOrRejectedPredList.add(approvedPredicate);
        }
       
        if (rejected) {
        	Predicate rejectedPredicate  = db.session().getCriteriaBuilder().notEqual(root.get("irc"), "0000");
        	approvedOrRejectedPredList.add(rejectedPredicate);
        }
        
        if(approved && rejected) {
        	predicates.add(db.session().getCriteriaBuilder().or(approvedOrRejectedPredList.toArray(new Predicate[] {})));
        }else {
        	predicates.addAll(approvedOrRejectedPredList);
        }
        
       
        
        if (voided) {
            Predicate voidedPredicate = db.session().getCriteriaBuilder().isNotNull(root.get("voidId"));
            predicates.add(voidedPredicate);
        }
        if (layers != null && layers.length > 0) {
            for (short layer : layers) {
                Expression<String> currencyExpression = root.get("currencyCode");
                Predicate currencyPredicate = db.session().getCriteriaBuilder().equal(
                  currencyExpression, String.valueOf(layer)
                );
                predicates.add(currencyPredicate);
            }
        }
        if (cardProduct != null) {
            Join<TranLog, Card> tranLogCardJoin = root.join("card");
            Join<Card, CardProduct> cpJoin = tranLogCardJoin.join("cardProduct");
            predicates.add(db.session().getCriteriaBuilder().equal(cpJoin.get("name"), cardProduct.getName()));
        }
        Predicate[] array = new Predicate[predicates.size()];
        return predicates.toArray(array);
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
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

    public short[] getLayers() {
        return layers;
    }

    public void setLayers(short[] layers) {
        this.layers = layers;
    }

    public CardProduct getCardProduct() {
        return cardProduct;
    }

    public void setCardProduct(CardProduct cardProduct) {
        this.cardProduct = cardProduct;
    }
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
}

