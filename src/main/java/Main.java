
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import netscape.javascript.JSObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Main {


    public static void main(String[] argv) throws Exception {
        System.out.println("IOTA Flash channel tester");

        String pathToLib = "res/iota.flash.js";

        System.out.println("Loading lib into V8 engine");
        IotaFlashInterface lib = new IotaFlashBridge(pathToLib);
        System.out.println("Lib imported");


        System.out.println("Testing getDigest(seed, index, security):");
        Digest digest1 = lib.getDigest("USERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSER", 0, 2);
        Digest digest2 = lib.getDigest("USERTWOUSERTWOUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSER", 0, 2);
        System.out.println("Digest1: " + digest1.toString());


        System.out.println("Testing composeAddress(digests):");
        ArrayList<Digest> digests = new ArrayList<Digest>();
        digests.add(digest1);
        digests.add(digest2);
        MultisigAddress composedAddr = lib.composeAddress(digests);
        System.out.println("Got multisig addr for digests: " + composedAddr.getAddress() + ", securitySum: " + composedAddr.getSecuritySum());

        testPrepare(lib);
    }

    private static void testPrepare(IotaFlashInterface lib) {

        System.out.println("Testing prepare()");
        ArrayList<String> settlementAddr = new ArrayList<String>();
        settlementAddr.add("RCZHCRDWMGJPHKROKEGVADVJXPGKEKNJRNLZZFPITUVEWNPGIWNUMKTYKMNB9DCNLWGMJZDNKYQDQKDLC");
        ArrayList<Integer> depositsPrep = new ArrayList<Integer>();
        ArrayList<Transfer> transfers = new ArrayList<Transfer>();

        lib.prepare(settlementAddr, depositsPrep, 0, transfers);
    }
}
