import Model.*;
import com.eclipsesource.v8.*;
import com.eclipsesource.v8.utils.V8ObjectUtils;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IotaFlashBridge {
    private static String iotaLibPath = "res/iota.flash.js";
    private static V8 engine;
    private static V8Object transfer;
    private static V8Object multisig;

    public static void boot() throws IOException {
        String file = readFile(iotaLibPath, Charset.defaultCharset());

        engine = V8.createV8Runtime();
        // Eval lib into current v8 context.
        engine.executeVoidScript(file);
        multisig = (V8Object) engine.executeScript("iotaFlash.multisig");
        transfer = (V8Object) engine.executeScript("iotaFlash.transfer");
    }

    /**
     *
     * @param digests
     * @return
     */
    public static MultisigAddress composeAddress(ArrayList<Digest> digests) {
        // Create js object for digest
        List<Object> list = new ArrayList<Object>();
        for (Digest digest: digests) {
            list.add(digest.toMap());
        }
        V8Array digestsJS = V8ObjectUtils.toV8Array(engine, list);
        // Call js.
        List<Object> paramsList = new ArrayList<Object>();
        paramsList.add(digestsJS);
        V8Array params = V8ObjectUtils.toV8Array(engine, paramsList);

        V8Object retV8 = multisig.executeObjectFunction("composeAddress", params);

        // Parse return values from JS into Java world.
        Map<String, ? super Object> resultMap = V8ObjectUtils.toMap(retV8);
        // Parse result into Java Obj.
        String addr = (String) resultMap.get("address");
        int secSum = (Integer) resultMap.get("securitySum");
        MultisigAddress ret = new MultisigAddress(addr, secSum);

        return ret;
    }

    /**
     *
     * @param seed
     * @param index
     * @param security
     * @return
     */
    public static Digest getDigest(String seed, int index, int security) {
        if (seed.length() < 81) {
            System.out.println("Seed is too short");
            return null;
        }
        V8Array params = new V8Array(engine);
        params.push(seed);
        params.push(index);
        params.push(security);
        V8Object ret = multisig.executeObjectFunction("getDigest", params);
        String dig = ret.getString("digest");
        int sec = ret.getInteger("security");
        int i = ret.getInteger("index");

        return new Digest(dig, i, sec);
    }

    /**
     *
     * @param root
     */
    public static CreateTransactionHelperObject updateLeafToRoot(MultisigAddress root) {
        Map<String, Object> map = root.toMap();
        // Create param list
        List<Object> paramsObj = new ArrayList<Object>();
        paramsObj.add(map);
        V8Array params = V8ObjectUtils.toV8Array(engine, paramsObj);

        V8Object ret = multisig.executeObjectFunction("updateLeafToRoot", params);
        int generate = ret.getInteger("generate");
        Map<String, ? super Object> multiSigMap = V8ObjectUtils.toMap((V8Object) ret.getObject("multisig"));
        // Parse result into Java Obj.
        String addr = (String) multiSigMap.get("address");
        int secSum = (Integer) multiSigMap.get("securitySum");
        MultisigAddress multisig = new MultisigAddress(addr, secSum);
        return new CreateTransactionHelperObject(generate, multisig);
    }


    /**
     *
     * @param settlementAddresses Array of address of settlement wallet addresses
     * @param deposits array of deposits index of array is user id in flash channel
     * @param index index of the current flash channel user.
     * @param transfers array of all transfers (value, address) pairs
     * @return
     */
    public static ArrayList<Transaction> prepare(ArrayList<String> settlementAddresses, ArrayList<Integer> deposits, int index, ArrayList<Transfer> transfers) {

        // Now put all params into JS ready array.
        List<Object> params = new ArrayList<>();
        params.add(V8ObjectUtils.toV8Array(engine, settlementAddresses));
        params.add(V8ObjectUtils.toV8Array(engine, deposits));
        params.add(index);
        params.add(V8Converter.transferListToV8Array(engine, transfers));

        // Call js function.
        V8Array ret = transfer.executeArrayFunction("prepare", V8ObjectUtils.toV8Array(engine, params));
        List<Object> transfersReturnJS = V8ObjectUtils.toList(ret);

        ArrayList<Transaction> returnTransfers = new ArrayList<>();

        for (Object b: transfersReturnJS) {
            Map<String, ? super Object> values = (Map<String, ? super Object>) b;
            String obsoleteTag = (String) values.get("obsoleteTag");
            String address = (String) values.get("address");
            Integer value = (Integer) values.get("value");

            returnTransfers.add(new Transaction(address, value, "", "", 0));
        }

        // Call js.
        return returnTransfers;
    }

    /**
     *
     * @param balance
     * @param deposits
     * @param outputs
     * @param root
     * @param remainderAddress
     * @param history
     * @param transactions
     * @param close
     * @return
     */
    public static ArrayList<Bundle> compose(int balance,
                                            List<Integer> deposits,
                                            ArrayList<Bundle> outputs,
                                            MultisigAddress root,
                                            MultisigAddress remainderAddress,
                                            ArrayList<Bundle> history,
                                            ArrayList<Transaction> transactions,
                                            boolean close) {



        // Create params.
        // Now put all params into JS ready array.
        List<Object> params = new ArrayList<Object>();
        params.add(balance);
        params.add(V8ObjectUtils.toV8Array(engine, deposits));
        params.add(V8Converter.bundleListToV8Array(engine, outputs));
        params.add(V8Converter.multisigToV8Object(engine, root));
        params.add(V8Converter.multisigToV8Object(engine, remainderAddress));
        params.add(V8Converter.bundleListToV8Array(engine, history));
        params.add(V8Converter.transactionListToV8Array(engine, transactions));

        // Call js function.
        V8Array ret = transfer.executeArrayFunction("compose", V8ObjectUtils.toV8Array(engine, params));
        List<Object> transfersReturnJS = V8ObjectUtils.toList(ret);

        return V8Converter.bundleListFromV8Array(ret);
    }

    /**
     *
     * @param root
     * @param seed
     * @param bundles
     * @return
     */
    public static ArrayList<Signature> sign(MultisigAddress root, String seed, ArrayList<Bundle> bundles) {

        // Create params.
        // Now put all params into JS ready array.
        List<Object> params = new ArrayList<>();
        params.add(V8Converter.multisigToV8Object(engine, root));
        params.add(seed);
        params.add(V8Converter.bundleListToV8Array(engine, bundles));

        V8Array returnArray = transfer.executeArrayFunction("sign", V8ObjectUtils.toV8Array(engine, params));

        return V8Converter.v8ArrayToSignatureList(returnArray);
    }

    /**
     *
     * @param bundles
     * @param signatures
     * @return
     */
    public static ArrayList<Bundle> appliedSignatures(ArrayList<Bundle> bundles, ArrayList<Signature> signatures) {
        List<Object> params = new ArrayList<>();
        params.add(V8Converter.bundleListToV8Array(engine, bundles));
        params.add(V8Converter.signatureListToV8Array(engine, signatures));

        V8Array returnArray = transfer.executeArrayFunction("appliedSignatures", V8ObjectUtils.toV8Array(engine, params));
        // Parse returns
        return V8Converter.bundleListFromV8Array(returnArray);
    }

    /**
     *
     * @param root
     * @param remainder
     * @param history
     * @param bundles
     * @return
     */
    public static Object getDiff(ArrayList<Object> root, ArrayList<Object> remainder, ArrayList<Object> history, ArrayList<Object> bundles) {
        return null;
    }

    /**
     *
     * @param root
     * @param deposits
     * @param outputs
     * @param remainderAddress
     * @param transfers
     * @param signedBundles
     * @return
     */
    public static void applayTransfers(MultisigAddress root,
                                         ArrayList<Integer> deposits,
                                         ArrayList<Bundle> outputs,
                                         MultisigAddress remainderAddress,
                                         ArrayList<Bundle> transfers,
                                         ArrayList<Bundle> signedBundles) {
        // Construct Java params
        List<Object> params = new ArrayList<>();
        params.add(V8Converter.multisigToV8Object(engine, root));
        params.add(V8ObjectUtils.toV8Array(engine, deposits));
        params.add(V8Converter.bundleListToV8Array(engine, outputs));
        params.add(V8Converter.multisigToV8Object(engine, remainderAddress));
        params.add(V8Converter.bundleListToV8Array(engine, transfers));
        params.add(V8Converter.bundleListToV8Array(engine, signedBundles));

        transfer.executeFunction("applyTransfers", V8ObjectUtils.toV8Array(engine, params));
    }

    /**
     *
     * @param settlementAddresses
     * @param deposits
     * @return
     */
    public static Object close(ArrayList<String> settlementAddresses, ArrayList<Integer> deposits) {
        V8Array saJS = V8ObjectUtils.toV8Array(engine, settlementAddresses);
        // Deposits
        V8Array depositsJS = V8ObjectUtils.toV8Array(engine, deposits);

        // Add to prams
        ArrayList<Object> paramsObj = new ArrayList<Object>();

        paramsObj.add(saJS);
        paramsObj.add(depositsJS);
        V8Object res = transfer.executeObjectFunction("close", V8ObjectUtils.toV8Array(engine, paramsObj));
        return res;
    }

    /// Utils

    /**
     *
     * @param path
     * @param encoding
     * @return
     * @throws IOException
     */
    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException error) {
            System.out.println("Failed to load file: " + error.getLocalizedMessage());
            return "";
        }
        return new String(encoded, encoding);
    }
}
