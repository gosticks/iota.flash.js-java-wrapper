package Model;

import java.util.HashMap;
import java.util.Map;

public class Transfer {
    private String address;
    private int value;

    public Transfer(String address, int value) {
        this.address = address;
        this.value = value;
    }

    public String getAddress() {
        return address;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "{'address':'" + getAddress() + "','value':" + getValue() +" }";
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("address", getAddress());
        map.put("value", getValue());
        return map;
    }
}