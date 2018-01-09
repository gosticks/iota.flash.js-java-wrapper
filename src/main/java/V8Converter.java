import Model.*;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class V8Converter {
    public static ArrayList<Signature> v8ArrayToSignatureList(V8Array array) {
        // Try parsing signature array from response.
        ArrayList<Signature> signatures = new ArrayList<>();
        for (Object o: V8ObjectUtils.toList(array)) {
            Map<String, Object> returnValues = (Map<String, Object>) o;
            String addr = (String) returnValues.get("address");
            ArrayList<String> signatureFragments = (ArrayList<String>) returnValues.get("signatureFragments");
            Signature sig = new Signature();
            sig.setAddress(addr);
            sig.setSignatureFragments(signatureFragments);
            signatures.add(sig);
        }

        return signatures;
    }

    public static V8Object multisigToV8Object(V8 engine, MultisigAddress multisig) {
        Map<String, Object> sigMapg = multisig.toMap();
        return V8ObjectUtils.toV8Object(engine, sigMapg);
    }

    public static V8Array bundleListToV8Array(V8 engine, ArrayList<Bundle> bundles) {

        List<Object> bundleTmp = new ArrayList<Object>();
        for (Bundle b: bundles) {
            List<Object> transactions = new ArrayList<Object>();
            for (Transaction t: b.getBundles()) {
                transactions.add(t.toMap());
            }
            bundleTmp.add(transactions);
        }
        return V8ObjectUtils.toV8Array(engine, bundleTmp);
    }

    public static ArrayList<Bundle> bundleListFromV8Array(V8Array input) {
        List<Object> inputList = V8ObjectUtils.toList(input);
        // Parse return as array of bundles
        ArrayList<Bundle> returnBundles = new ArrayList<>();
        for (Object bundleItem: inputList) {
            ArrayList<Object> bundleContent = (ArrayList<Object>) bundleItem;

            ArrayList<Transaction> returnedTransactions = new ArrayList<>();
            for (Object rawTransaction: bundleContent) {
                returnedTransactions.add(transactionFromObject(rawTransaction));
            }

            Bundle bundle = new Bundle(returnedTransactions);
            returnBundles.add(bundle);
        }
        return  returnBundles;
    }

    public static V8Array signatureListToV8Array(V8 engine, ArrayList<Signature> signatures) {
        List<Object> returnArr = new ArrayList<>();
        for (Signature sig: signatures) {
            Map<String, Object> signatureMap = new HashMap<String, Object>();
            signatureMap.put("address", sig.getAddress());
            signatureMap.put("signatureFragments", sig.getSignatureFragments());
            returnArr.add(signatureMap);
        }
        return V8ObjectUtils.toV8Array(engine, returnArr);
    }

    public static V8Array transferListToV8Array(V8 engine, ArrayList<Transfer> transfers) {
        List<Object> transferObj = new ArrayList<Object>();
        for (Transfer t: transfers) {
            transferObj.add(t.toMap());
        }
        return V8ObjectUtils.toV8Array(engine, transferObj);
    }

    public static V8Array transactionListToV8Array(V8 engine, ArrayList<Transaction> transactions) {
        List<Object> transfersObj = new ArrayList<Object>();
        for (Transaction t: transactions) {
            transfersObj.add(t.toMap());
        }
        return V8ObjectUtils.toV8Array(engine, transfersObj);
    }


    public static Transaction transactionFromObject(Object input) {
        Map<String, Object> bundleData = (Map<String, Object>) input;
        String signatureMessageFragment = (String) bundleData.get("signatureMessageFragment");
        String bundle = (String) bundleData.get("bundle");
        String address = (String) bundleData.get("address");
        String attachmentTimestampLowerBound = (String) bundleData.get("attachmentTimestampLowerBound");
        String attachmentTimestampUpperBound = (String) bundleData.get("attachmentTimestampUpperBound");
        String trunkTransaction = (String) bundleData.get("trunkTransaction");
        String attachmentTimestamp = (String) bundleData.get("attachmentTimestamp");
        Integer timestamp = (Integer) bundleData.get("timestamp");
        String tag = (String) bundleData.get("tag");
        String branchTransaction = (String) bundleData.get("branchTransaction");
        String nonce = (String) bundleData.get("nonce");
        String obsoleteTag = (String) bundleData.get("obsoleteTag");

        Integer currentIndex = (Integer) bundleData.get("currentIndex");
        Integer value = (Integer) bundleData.get("value");
        Integer lastIndex = (Integer) bundleData.get("lastIndex");

        Transaction parsedTransaction = new Transaction(
                address,
                bundle,
                value.intValue(),
                obsoleteTag,
                tag,
                timestamp,
                signatureMessageFragment,
                trunkTransaction,
                branchTransaction,

                attachmentTimestamp,
                attachmentTimestampUpperBound,
                attachmentTimestampLowerBound,
                nonce
        );
        return parsedTransaction;
    }
}
