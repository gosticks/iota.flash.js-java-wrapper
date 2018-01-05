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

public class IotaFlashBridge implements IotaFlashInterface {
    private String iotaLibPath;
    private V8 engine;
    private V8Object transfer;
    private V8Object multisig;

    /**
     *
     * @param path
     * @throws IOException
     */
    IotaFlashBridge(String path) throws IOException {
        this.iotaLibPath = path;

        String file = readFile(path, Charset.defaultCharset());

        this.engine = V8.createV8Runtime();
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
    public MultisigAddress composeAddress(ArrayList<Digest> digests) {
        // Create js object for digest
        // TODO: find more clean way to do thi s.
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
    public Digest getDigest(String seed, int index, int security) {
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
    public void updateLeafToRoot(MultisigAddress root) {
        Map<String, Object> map = root.toMap();
        // Create param list
        List<Object> paramsObj = new ArrayList<Object>();
        paramsObj.add(map);
        V8Array params = V8ObjectUtils.toV8Array(engine, paramsObj);

        multisig.executeFunction("updateLeafToRoot", params);
    }


    /**
     *
     * @param settlementAddresses Array of address of settlement wallet addresses
     * @param deposits array of deposits index of array is user id in flash channel
     * @param index index of the current flash channel user.
     * @param transfers array of all transfers (value, address) pairs
     * @return
     */
    public Object prepare(ArrayList<String> settlementAddresses, ArrayList<Integer> deposits, int index, ArrayList<Transfer> transfers) {
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

        // Call js.
        return transfersReturnJS;
    }

    /**
     *
     * @param balance
     * @param deposits
     * @param outputs
     * @param multisig
     * @param remainderAddress
     * @param history
     * @param transfers
     * @param close
     * @return
     */
    public List<Object> compose(int balance, ArrayList<Integer> deposits, ArrayList<Transfer> outputs, Object multisig, String remainderAddress, ArrayList<Bundle> history, ArrayList<Transfer> transfers, boolean close) {
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

        List<Object> trs = V8ObjectUtils.toList(transfersJS);

        return trs;
    }

    /**
     *
     * @param multisig
     * @param seed
     * @param bundles
     * @return
     */
    public Object sign(Object multisig, String seed, ArrayList<Object> bundles) {
        return null;
    }

    /**
     *
     * @param bundles
     * @param signatures
     * @return
     */
    public Object appliedSignatures(ArrayList<Object> bundles, ArrayList<Object> signatures) {
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
    public Object getDiff(ArrayList<Object> root, ArrayList<Object> remainder, ArrayList<Object> history, ArrayList<Object> bundles) {
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
    public Object applayTransfers(Object root, Object deposit, Object outputs, Object remainderAddress, Object transfers, Object signedBundles) {
        return null;
    }

    /**
     *
     * @param settlementAddresses
     * @param deposits
     * @return
     */
    public Object close(ArrayList<String> settlementAddresses, ArrayList<Integer> deposits) {
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
