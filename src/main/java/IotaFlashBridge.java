import com.eclipsesource.v8.*;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.sun.org.apache.xpath.internal.operations.Mult;

import javax.jws.Oneway;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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
    public static List<Object> prepare(ArrayList<String> settlementAddresses, ArrayList<Integer> deposits, int index, ArrayList<Transfer> transfers) {
        V8Array settlementAddressesJS = V8ObjectUtils.toV8Array(engine, settlementAddresses);
        V8Array depositJS = V8ObjectUtils.toV8Array(engine, deposits);
        List<Object> transferObj = new ArrayList<Object>();
        for (Transfer t: transfers) {
            transferObj.add(t.toMap());
        }
        V8Array transferJS = V8ObjectUtils.toV8Array(engine, transferObj);

        // Now put all params into JS ready array.
        List<Object> params = new ArrayList<Object>();
        params.add(settlementAddressesJS);
        params.add(depositJS);
        params.add(index);
        params.add(transferJS);

        // Call js function.
        V8Array ret = transfer.executeArrayFunction("prepare", V8ObjectUtils.toV8Array(engine, params));
        List<Object> transfersReturnJS = V8ObjectUtils.toList(ret);

        for (Object b: transfersReturnJS) {
            Map<String, Object> test = V8ObjectUtils.toMap((V8Object) b);

        }

        // Call js.
        return transfersReturnJS;
    }

    /**
     *
     * @param balance
     * @param deposits
     * @param outputs
     * @param root
     * @param remainderAddress
     * @param history
     * @param transfers
     * @param close
     * @return
     */
    public static List<Object> compose(int balance, ArrayList<Integer> deposits, ArrayList<Transfer> outputs, MultisigAddress root, String remainderAddress, ArrayList<Bundle> history, ArrayList<Transfer> transfers, boolean close) {
        V8Array depositsJS = V8ObjectUtils.toV8Array(engine, deposits);
        // Outputs
        List<Object> outputsObj = new ArrayList<Object>();
        for (Transfer t: outputs) {
            outputsObj.add(t.toMap());
        }
        V8Array outputsJS = V8ObjectUtils.toV8Array(engine, outputsObj);



        List<Object> transfersObj = new ArrayList<Object>();
        for (Transfer t: transfers) {
            transfersObj.add(t.toMap());
        }
        V8Array transfersJS = V8ObjectUtils.toV8Array(engine, transfersObj);

        // Create params.
        // Now put all params into JS ready array.
        List<Object> params = new ArrayList<Object>();
        params.add(balance);
        params.add(depositsJS);
        params.add(outputsJS);


        // Call js function.
        V8Array ret = transfer.executeArrayFunction("compose", V8ObjectUtils.toV8Array(engine, params));
        List<Object> transfersReturnJS = V8ObjectUtils.toList(ret);

        // Parse return as array of bundles


        return transfersReturnJS;
    }

    /**
     *
     * @param root
     * @param seed
     * @param bundles
     * @return
     */
    public static Object sign(MultisigAddress root, String seed, ArrayList<Bundle> bundles) {
        Map<String, Object> multisig = root.toMap();
        V8Object rootJS = V8ObjectUtils.toV8Object(engine, multisig);

        List<Object> bundleTmp = new ArrayList<Object>();
        for (Bundle b: bundles) {
            bundleTmp.add(b.toMap());
        }
        V8Array bundlesJS = V8ObjectUtils.toV8Array(engine, bundleTmp);

        // Create params.
        // Now put all params into JS ready array.
        List<Object> params = new ArrayList<Object>();
        params.add(rootJS);
        params.add(seed);
        params.add(bundles);

        V8Object signatures = transfer.executeObjectFunction("sign", V8ObjectUtils.toV8Array(engine, params));

        // TODO: add singature object.
        return signatures;
    }

    /**
     *
     * @param bundles
     * @param signatures
     * @return
     */
    public static Object appliedSignatures(ArrayList<Object> bundles, ArrayList<Object> signatures) {
        return null;
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
     * @param deposit
     * @param outputs
     * @param remainderAddress
     * @param transfers
     * @param signedBundles
     * @return
     */
    public static Object applayTransfers(Object root, Object deposit, Object outputs, Object remainderAddress, Object transfers, Object signedBundles) {
        return null;
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