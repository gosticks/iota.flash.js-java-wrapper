import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Bundle {
    private ArrayList<Transaction> bundles;

    public Bundle(ArrayList<Transaction> bundles) {
        this.bundles = bundles;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        List<Object> bundleList = new ArrayList<Object>();
        for (Transaction b: bundles) {
            bundleList.add(b.toMap());
        }
        map.put("bundles", bundleList);
        return map;
    }

    public ArrayList<Transaction> getBundles() {
        return bundles;
    }
}


