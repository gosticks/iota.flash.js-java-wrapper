import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MultisigAddress {
    private String address;
    private int securitySum;
    private ArrayList<MultisigAddress> children;
    private ArrayList<Bundle> bundles;

    public MultisigAddress(String address, int securitySum) {
        this.address = address;
        this.securitySum = securitySum;
        this.children = new  ArrayList<MultisigAddress>();
        this.bundles = new  ArrayList<Bundle>();

    }

    public ArrayList<MultisigAddress> getChildren() {
        return children;
    }

    public int getSecuritySum() {
        return securitySum;
    }

    public String getAddress() {
        return address;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("address", getAddress());
        map.put("securitySum", getSecuritySum());

        List<Object> childrenList = new ArrayList<Object>();
        for (MultisigAddress ma: children) {
            childrenList.add(ma.toMap());
        }
        map.put("children", childrenList);

        List<Object> bundleList = new ArrayList<Object>();
        for (MultisigAddress ma: children) {
            bundleList.add(ma.toMap());
        }
        map.put("bundle", bundleList);

        return map;
    }
}