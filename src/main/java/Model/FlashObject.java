package Model;

import java.util.ArrayList;

public class FlashObject {
    int signersCount = 2;
    int balance;
    ArrayList<String> settlementAddresses;
    MultisigAddress root;
    MultisigAddress remainderAddress;
    ArrayList<Integer> deposits; // Clone correctly
    ArrayList<Bundle> outputs = new ArrayList<Bundle>();
    ArrayList<Bundle> transfers = new ArrayList<Bundle>();

    public FlashObject(int signersCount, int balance, ArrayList<Integer> deposits) {
        this.signersCount = signersCount;
        this.balance = balance;
        this.deposits = deposits;
    }

    public int getSignersCount() {
        return signersCount;
    }

    public int getBalance() {
        return balance;
    }

    public MultisigAddress getRoot() {
        return root;
    }

    public ArrayList<Integer> getDeposits() {
        return deposits;
    }

    public ArrayList<Bundle> getOutputs() {
        return outputs;
    }

    public ArrayList<Bundle> getTransfers() {
        return transfers;
    }

    public void setRemainderAddress(MultisigAddress remainderAddress) {
        this.remainderAddress = remainderAddress;
    }

    public MultisigAddress getRemainderAddress() {
        return remainderAddress;
    }

    public void setRoot(MultisigAddress root) {
        this.root = root;
    }

    public void setSettlementAddresses(ArrayList<String> settlementAddresses) {
        this.settlementAddresses = settlementAddresses;
    }

    public ArrayList<String> getSettlementAddresses() {
        return settlementAddresses;
    }
}
