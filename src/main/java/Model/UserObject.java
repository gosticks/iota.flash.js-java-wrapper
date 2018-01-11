package Model;

import java.lang.reflect.Field;
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

    public UserObject(int userID, String seed, int depth, FlashObject flash) {
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

    @Override
    public String toString() {
        String out = "";
        out += "userIndex: " + userIndex + "\n";
        out += "seed: " + seed + "\n";
        out += "index: " + index + "\n";
        out += "security: " + getSecurity() + "\n";
        out += "depth: " + depth + "\n";
        out += "bundles: " + "\n";
        for (Bundle b: bundles) {
            out += "\t" + b.toString() + "\n";
        }
        out += "partialDigests: " + "\n";
        for (Digest d: partialDigests) {
            out += "\t" + d.toString() + "\n";
        }
        out += "multisigDigests: " + "\n";
        for (MultisigAddress m: multisigDigests) {
            out += "\t" + m.toString() + "\n";
        }
        out += "Flash: " + "\n";
        out += flash.toString();

        return out;
    }

    /**
     *
     * Getters and Setters
     */



    public void setMultisigDigests(ArrayList<MultisigAddress> multisigDigests) {
        this.multisigDigests = multisigDigests;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setBundles(ArrayList<Bundle> bundles) {
        this.bundles = bundles;
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

