package iotaFlashWrapper.Model;

import com.eclipsesource.v8.utils.V8ObjectUtils;

import java.util.*;

public class FlashObject {
    int signersCount = 2;
    int balance;
    List<String> settlementAddresses;
    List<Double> deposits;
    Map<String, Integer> outputs = new HashMap<>();
    List<Bundle> transfers = new ArrayList<Bundle>();
    Multisig root;
    Multisig remainderAddress;


    public FlashObject(int signersCount, int balance, double[] deposits) {
        this.signersCount = signersCount;
        this.balance = balance;
        this.deposits = new ArrayList<>();
        for (double deposit : deposits){
            this.deposits.add(deposit);
        }
    }

    public FlashObject(int signersCount, int balance, List<String> settlementAddresses, List<Double> deposits, Map<String, Integer> outputs, List<Bundle> transfers, Multisig root, Multisig remainderAddress) {
        this.signersCount = signersCount;
        this.balance = balance;
        this.settlementAddresses = settlementAddresses;
        this.deposits = deposits;
        this.outputs = outputs;
        this.transfers = transfers;
        this.root = root;
        this.remainderAddress = remainderAddress;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("signersCount", signersCount);
        objectMap.put("balance", getBalance());
        objectMap.put("root", root.toMap());
        objectMap.put("remainderAddress", remainderAddress.toMap());
        objectMap.put("settlementAddresses", getSettlementAddresses());

        // Wrap outputs inside an array.
        objectMap.put("outputs", getOutputs());

        objectMap.put("deposits", getDeposits());

        ArrayList<Object> transfersMap = new ArrayList<>();
        for (Bundle b: getTransfers()) {
            transfersMap.add(b.toArrayList());
        }
        objectMap.put("transfers", transfersMap);
        return objectMap;

    }

    public int getSignersCount() {
        return signersCount;
    }

    public int getBalance() {
        return balance;
    }

    public Multisig getRoot() {
        return root;
    }

    public List<Double> getDeposits() {
        return deposits;
    }

    public Map<String, Integer> getOutputs() {
        return outputs;
    }

    public List<Bundle> getTransfers() {
        return transfers;
    }

    public void setRemainderAddress(Multisig remainderAddress) {
        this.remainderAddress = remainderAddress;
    }

    public Multisig getRemainderAddress() {
        return remainderAddress;
    }

    public void setRoot(Multisig root) {
        this.root = root;
    }

    public void setSettlementAddresses(ArrayList<String> settlementAddresses) {
        this.settlementAddresses = settlementAddresses;
    }

    public List<String> getSettlementAddresses() {
        return settlementAddresses;
    }
}
