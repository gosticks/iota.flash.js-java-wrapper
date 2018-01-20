package iotaFlashWrapper.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bundle extends jota.model.Bundle {
    private List<Transaction> wrappedTransactions = new ArrayList<>();

    public Bundle(List<Transaction> transactions) {
        super((ArrayList<jota.model.Transaction>) (ArrayList<? extends jota.model.Transaction>) transactions, transactions.size());
    }

    public Bundle() {
        super();
    }

    public List<Transaction> getWrappedTransactions() {
        return wrappedTransactions;
    }

    public void setWrappedTransactions(List<Transaction> wrappedTransactions) {
        this.wrappedTransactions = wrappedTransactions;
    }

    @Override
    public String toString() {
        String out = "";
        for (Transaction t: getWrappedTransactions()) {
            out += t.toString();
            out += "\n";
        }
        return out;
    }

    public String[] toTrytesArray() {
        String[] bundleTrytes = new String[getTransactions().size()];
        List<jota.model.Transaction> transactions = getTransactions();
        for (int i = 0; i < bundleTrytes.length; i++) {
            bundleTrytes[i] =  transactions.get(i).toTrytes();
        }

        return bundleTrytes;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("bundles", toArrayList());
        return map;
    }

    public List<Object> toArrayList() {
        List<Object> bundleList = new ArrayList<Object>();
        for (Transaction b: getWrappedTransactions()) {
            bundleList.add(b.toMap());
        }
        return bundleList;
    }

    public Bundle clone() {
        ArrayList<Transaction> clonedTransactions = new ArrayList<>();
        for (Transaction t: getWrappedTransactions()) {
            clonedTransactions.add(t.clone());
        }
        return new Bundle(clonedTransactions);
    }
}


