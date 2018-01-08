import java.util.HashMap;
import java.util.Map;

class Transaction {
    private int timestamp;
    private String address;
    private int value;
    private String obsoleteTag;
    private String tag;

    // Signature stuff
    private String bundle = "";
    private String signatureMessageFragment = "";
    private String trunkTransaction = "";
    private String branchTransaction = "";

    private String attachmentTimestamp = "";
    private String attachmentTimestampUpperBound = "";
    private String attachmentTimestampLowerBound = "";

    private String nonce = "";

    // Unsigned constructor
    public Transaction(String address, int value, String obsoleteTag, String tag, int timestamp) {
        this.address = address;
        this.value = value;
        this.obsoleteTag = obsoleteTag;
        this.tag = tag;
        this.timestamp = timestamp;
    }

    // Signed constructor
    public Transaction(String address,
                       String bundle,
                       int value,
                       String obsoleteTag,
                       String tag,
                       int timestamp,
                       String signatureMessageFragment,
                       String trunkTransaction,
                       String branchTransaction,

                       String attachmentTimestamp,
                       String attachmentTimestampUpperBound,
                       String attachmentTimestampLowerBound,

                       String nonce
    ) {
        this.address = address;
        this.value = value;
        this.obsoleteTag = obsoleteTag;
        this.tag = tag;
        this.timestamp = timestamp;
        this.signatureMessageFragment = signatureMessageFragment;
        this.trunkTransaction = trunkTransaction;
        this.branchTransaction = branchTransaction;

        this.attachmentTimestamp = attachmentTimestamp;
        this.attachmentTimestampUpperBound = attachmentTimestampUpperBound;
        this.attachmentTimestampLowerBound = attachmentTimestampLowerBound;

        this.nonce = nonce;
    }

    public int getValue() {
        return value;
    }
    public String getAddress() {
        return address;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("address", address);
        map.put("value", value);
        map.put("obsoleteTag", obsoleteTag);
        map.put("tag", tag);
        map.put("timestamp", timestamp);
        map.put("signatureMessageFragment", signatureMessageFragment);
        map.put("trunkTransaction", trunkTransaction);
        map.put("branchTransaction", branchTransaction);
        map.put("attachmentTimestamp", attachmentTimestamp);
        map.put("attachmentTimestampLowerBound", attachmentTimestampLowerBound);
        map.put("attachmentTimestampUpperBound", attachmentTimestampUpperBound);
        return map;
    }

    public String toString() {
        Map<String, Object> mapObj = toMap();
        String value = "{";
        for (Map.Entry<String, Object> entry: mapObj.entrySet()) {
            value += "'" + entry.getKey() + "':'" + entry.getValue().toString() + "', ";
        }
        value += "}";
        return  value;
    }
}