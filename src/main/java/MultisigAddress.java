import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CreateTransactionHelperObject {
    private int generate = 0;
    private MultisigAddress address;

    CreateTransactionHelperObject(int gen, MultisigAddress addr) {
        this.generate = gen;
        this.address = addr;
    }

    public int getGenerate() {
        return generate;
    }

    public MultisigAddress getAddress() {
        return address;
    }
}

class MultisigAddress {
    private String address;
    private int securitySum;
    private int index;
    private int signingIndex;
    private int security;
    private ArrayList<MultisigAddress> children;
    private ArrayList<Bundle> bundles;

    public MultisigAddress(String address, int securitySum) {
        this.address = address;
        this.securitySum = securitySum;
        this.children = new  ArrayList<MultisigAddress>();
        this.bundles = new  ArrayList<Bundle>();

    }

    public void push(MultisigAddress addr) {
        children.add(addr);
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
    public int getIndex() {
        return index;
    }

    public int getSigningIndex() {
        return signingIndex;
    }

    public int getSecurity() {
        return security;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setSecurity(int security) {
        this.security = security;
    }

    public void setSecuritySum(int securitySum) {
        this.securitySum = securitySum;
    }

    public void setSigningIndex(int signingIndex) {
        this.signingIndex = signingIndex;
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
        for (Bundle b: bundles) {
            bundleList.add(b.getBundles());
        }
        map.put("bundle", bundleList);

        return map;
    }

    public V8Object toV8Object(V8 engine) {
        return V8ObjectUtils.toV8Object(engine, this.toMap());
    }

    @Override
    public String toString() {
        return "{'address':'" + address + "', securitySum:" + securitySum + ", signingIndex: " + signingIndex + "}";
    }
}