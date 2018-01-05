import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Bundle {
    private ArrayList<Bundle> bundles;

    public Bundle(ArrayList<Bundle> bundles) {
        this.bundles = bundles;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        List<Object> bundleList = new ArrayList<Object>();
        for (Bundle b: bundles) {
            bundleList.add(b.toMap());
        }
        map.put("bundles", bundleList);
        return map;
    }
}


