import java.util.ArrayList;

public class UserObject {
    private int userIndex = 1;
    private String seed;
    private int index = 0;
    private int security = 2;
    private int depth = 4;
    private ArrayList<Bundle> bundles = new ArrayList<Bundle>();
    private ArrayList<Digest> partialDigests = new ArrayList<Digest>();
    private ArrayList<MultisigAddress> multisigDigests = new ArrayList<MultisigAddress>();
    private FlashObject flash;

    UserObject(int userID, String seed, int depth, FlashObject flash) {
        this.userIndex = userID;
        this.seed = seed;
        this.depth = depth;
        this.flash = flash;
    }

    public void incrementIndex() {
        index++;
    }

    public void add(Digest digest) {
        partialDigests.add(digest);
    }


    /**
     *
     * Getters and Setters
     */

    public void setMultisigDigests(ArrayList<MultisigAddress> multisigDigests) {
        this.multisigDigests = multisigDigests;
    }

    public ArrayList<MultisigAddress> getMultisigDigests() {
        return multisigDigests;
    }

    public int getSecurity() {
        return security;
    }

    public String getSeed() {
        return seed;
    }

    public int getIndex() {
        return index;
    }

    public int getUserIndex() {
        return userIndex;
    }

    public ArrayList<Bundle> getBundles() {
        return bundles;
    }

    public ArrayList<Digest> getPartialDigests() {
        return partialDigests;
    }

    public FlashObject getFlash() {
        return flash;
    }

}

class FlashObject {
    int signersCount = 2;
    int balance;
    ArrayList<String> settlementAddresses;
    MultisigAddress root;
    MultisigAddress remainderAddress;
    ArrayList<Integer> deposits; // Clone correctly
    ArrayList<Bundle> outputs = new ArrayList<Bundle>();
    ArrayList<Transfer> transfers = new ArrayList<Transfer>();

    FlashObject(int signersCount, int balance, ArrayList<Integer> deposits) {
        this.signersCount = signersCount;
        this.balance = balance;
        this.deposits = deposits;
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
