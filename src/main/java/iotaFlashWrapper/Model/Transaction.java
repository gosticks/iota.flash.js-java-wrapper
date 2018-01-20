package iotaFlashWrapper.Model;

import java.util.HashMap;
import java.util.Map;

public class Transaction extends jota.model.Transaction {

    // Unsigned constructor
    public Transaction(String address, long value, String obsoleteTag, String tag, long timestamp) {
        super(address, value, tag, timestamp);
    }

    public Transaction(String signatureFragments, Long currentIndex, Long lastIndex, String nonce,
                       String hash, String obsoleteTag, Long timestamp, String trunkTransaction,
                       String branchTransaction, String address, Long value, String bundle, String tag,
                       Long attachmentTimestamp, Long attachmentTimestampLowerBound, Long attachmentTimestampUpperBound) {
        super(
            signatureFragments,
            currentIndex,
            lastIndex,
            nonce,
            hash,
            obsoleteTag,
            timestamp,
            trunkTransaction,
            branchTransaction,
            address,
            value,
            bundle,
            tag,
            attachmentTimestamp,
            attachmentTimestampLowerBound,
            attachmentTimestampUpperBound
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        if (getHash() != null && !getHash().equals("")) {
            map.put("hash", getHash());
        }
        map.put("signatureMessageFragment", getSignatureFragments());
        map.put("address", getAddress());
        map.put("value", getValue());
        map.put("obsoleteTag", getObsoleteTag());
        map.put("currentIndex", getCurrentIndex());
        map.put("timestamp", getTimestamp());
        map.put("lastIndex", getLastIndex());
        map.put("bundle", getBundle());
        map.put("trunkTransaction", getTrunkTransaction());
        map.put("branchTransaction", getBranchTransaction());
        map.put("nonce", getNonce());
        map.put("attachmentTimestamp", String.valueOf(getAttachmentTimestamp()));
        map.put("tag", getTag());
        map.put("attachmentTimestampLowerBound", String.valueOf(getAttachmentTimestampLowerBound()));
        map.put("attachmentTimestampUpperBound", String.valueOf(getAttachmentTimestampUpperBound()));
        return map;
    }

    public Transaction clone() {
        return new Transaction(
            this.getSignatureFragments(),
            this.getCurrentIndex(),
            this.getLastIndex(),
            this.getNonce(),
            this.getHash(),
            this.getObsoleteTag(),
            this.getTimestamp(),
            this.getTrunkTransaction(),
            this.getBranchTransaction(),
            this.getAddress(),
            this.getValue(),
            this.getBundle(),
            this.getTag(),
            this.getAttachmentTimestamp(),
            this.getAttachmentTimestampLowerBound(),
            this.getAttachmentTimestampUpperBound()
        );
    }

    public String toString() {
        Map<String, Object> mapObj = toMap();
        String value = "{";
        for (Map.Entry<String, Object> entry: mapObj.entrySet()) {
            value += "'" + entry.getKey() + "':'" + entry.getValue().toString() + "', \n";
        }
        value += "}";
        return  value;
    }
}