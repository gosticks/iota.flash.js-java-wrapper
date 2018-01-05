import java.util.HashMap;
import java.util.Map;

class Transaction {
    private String timestamp;
    private String address;
    private int value;
    private int obsoleteTag;
    private int tag;

    // Signature stuff
    private String signatureMessageFragment = "";
    private String trunkTransaction = "";
    private String branchTransaction = "";

    private String attachmentTimestamp = "";
    private String attachmentTimestampUpperBound = "";
    private String attachmentTimestampLowerBound = "";

    private String nonce = "";

    // Unsigned constructor
    public Transaction(String address, int value, int obsoleteTag, int tag, String timestamp) {
        this.address = address;
        this.value = value;
        this.obsoleteTag = obsoleteTag;
        this.tag = tag;
        this.timestamp = timestamp;
    }

    // Signed constructor
    public Transaction(String address,
                       int value,
                       int obsoleteTag,
                       int tag,
                       String timestamp,
                       String signatureMessageFragment,
                       String trunkTransaction,
                       String branchTransaction,

                       String attachmentTimestamp,
                       String attachmentTimestampUpperBound,
                       String attachmentTimestampLowerBound
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
        return map;
    }
}