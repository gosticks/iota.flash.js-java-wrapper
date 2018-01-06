
import com.sun.org.apache.xpath.internal.operations.Mult;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class Main {

    public static void main(String[] argv) throws Exception {
        // Create v8 engine and load lib.
        IotaFlashBridge.boot();

        // Run a test based on the flash example
        // Link: https://github.com/iotaledger/iota.flash.js/blob/master/examples/flash.js

        String oneSeed = "USERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSER";
        String oneSettlement = "USERONE9ADDRESS9USERONE9ADDRESS9USERONE9ADDRESS9USERONE9ADDRESS9USERONE9ADDRESS9U";
        String twoSeed = "USERTWOUSERTWOUSERTWOUSERTWOUSERTWOUSERTWOUSERTWOUSERTWOUSERTWOUSERTWOUSERTWOUSER";
        String twoSettlement = "USERTWO9ADDRESS9USERTWO9ADDRESS9USERTWO9ADDRESS9USERTWO9ADDRESS9USERTWO9ADDRESS9U";

        //////////////////////////////////
        // INITIAL CHANNEL CONDITIONS

        // Security level
        int SECURITY = 2;
        // Number of parties taking signing part in the channel
        int SIGNERS_COUNT = 2;
        // Flash tree depth
        int TREE_DEPTH = 4;
        // Total channel Balance
        int CHANNEL_BALANCE = 2000;
        // Users deposits
        ArrayList<Integer> DEPOSITS = new ArrayList<>();
        DEPOSITS.add(1000);
        DEPOSITS.add(1000);
        // Setup users.
        FlashObject oneFlashObj = new FlashObject(SIGNERS_COUNT, CHANNEL_BALANCE, DEPOSITS);
        UserObject oneFlash = new UserObject(0, oneSeed, TREE_DEPTH, oneFlashObj);

        FlashObject twoFlashObj = new FlashObject(SIGNERS_COUNT, CHANNEL_BALANCE, DEPOSITS);
        UserObject twoFlash = new UserObject(1, twoSeed, TREE_DEPTH, twoFlashObj);

        // USER ONE
        setupUser(oneFlash, TREE_DEPTH);

        // USER TWO
        setupUser(twoFlash, TREE_DEPTH);

        //////////////////////////////////
        // INITAL MULTISIG

        // Make an array of digests
        ArrayList<UserObject> allUsers = new ArrayList<UserObject>();
        allUsers.add(oneFlash);
        allUsers.add(twoFlash);

        // Create partial digests for users.
        createInitialPartialDigests(allUsers, oneFlash);
        createInitialPartialDigests(allUsers, twoFlash);

        ArrayList<MultisigAddress> oneMultisigs = oneFlash.getMultisigDigests();
        ArrayList<MultisigAddress> twoMultisigs = twoFlash.getMultisigDigests();

        // Set renainder address.
        MultisigAddress oneRemainderAddr = oneMultisigs.remove(0); //shiftCopyArray();
        oneFlash.getFlash().setRemainderAddress(oneRemainderAddr);

        MultisigAddress twoRemainderAddr = twoMultisigs.remove(0);
        twoFlash.getFlash().setRemainderAddress(twoRemainderAddr);

        // Build flash trees
        for (int i = 1; i < oneMultisigs.size(); i++) {
            System.out.println("Adding child (" + oneMultisigs.get(0).toString() + ") to root :" + oneMultisigs.get(i - 1).toString() );
            oneMultisigs.get(i - 1).push(oneMultisigs.get(i));
        }

        // Build flash trees
        for (int i = 1; i < twoMultisigs.size(); i++) {
            twoMultisigs.get(i - 1).push(twoMultisigs.get(i));
        }

        oneFlash.getFlash().setRoot(oneMultisigs.remove(0));
        twoFlash.getFlash().setRoot(twoMultisigs.remove(0));

        ArrayList<String> settlementAddresses = new ArrayList<>();
        settlementAddresses.add(oneSettlement);
        settlementAddresses.add(twoSettlement);
        oneFlash.getFlash().setSettlementAddresses(settlementAddresses);
        twoFlash.getFlash().setSettlementAddresses(settlementAddresses);

        System.out.println("Channel Setup!");


        ArrayList<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer(twoSettlement, 200));

        Helpers.createTransaction(oneFlash, transfers, false);
    }

    private static void setupUser(UserObject user, int TREE_DEPTH) {
        // Create digests for the start of the channel
        for (int i = 0; i < TREE_DEPTH + 1; i++) {
            // Create new digest
            Digest digest = IotaFlashBridge.getDigest(
                    user.getSeed(),
                    user.getIndex(),
                    user.getSecurity()
            );
            System.out.println("Adding digest (" + digest.toString() + ") to user " + user.getUserIndex());
            // Increment key index
            user.incrementIndex();
            user.add(digest);
        }
    }

    private static void createInitialPartialDigests(ArrayList<UserObject> allUsers, UserObject currentUser) {

        // Generate the first addresses
        ArrayList<MultisigAddress> oneMultisigs = new ArrayList<MultisigAddress>();


        System.out.println("_________________________________________________________________");
        System.out.println("Creating multisigs on user: " + currentUser.getUserIndex());
        int index = 0;
        // Create address
        for (Digest digest: allUsers.get(index).getPartialDigests()) {
            int i = index;

            MultisigAddress addy = IotaFlashBridge.composeAddress(
                    allUsers.stream().map(u -> u.getPartialDigests().get(i)).collect(Collectors.toCollection(ArrayList::new))
            );

            System.out.println("Multisig: " + addy.toString());

            // Add key index in
            addy.setIndex(digest.getIndex());
            // Add the signing index to the object IMPORTANT
            addy.setSigningIndex(currentUser.getUserIndex() * digest.getSecurity());
            // Get the sum of all digest security to get address security sum
            addy.setSecuritySum(allUsers.stream()
                    .map(u -> u.getPartialDigests().get(i))
                    .mapToInt(Digest::getSecurity)
                    .sum()
            );
            addy.setSecurity(digest.getSecurity());
            oneMultisigs.add(addy);
            index++;
        }
        currentUser.setMultisigDigests(oneMultisigs);
    }

    private static ArrayList<MultisigAddress> shiftCopyArray(ArrayList<MultisigAddress> input) {
        ArrayList<MultisigAddress> output = new ArrayList<>();

        for (int i = 1; i < input.size(); i++) {
            output.add(input.get(i));
        }

        return output;
    }

    private static void test() throws IOException {

        System.out.println("IOTA Flash channel tester");

        String pathToLib = "res/iota.flash.js";

        System.out.println("Loading lib into V8 engine");
        System.out.println("Lib imported");


        System.out.println("Testing getDigest(seed, index, security):");
        Digest digest1 = IotaFlashBridge.getDigest("USERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSER", 0, 2);
        Digest digest2 = IotaFlashBridge.getDigest("USERTWOUSERTWOUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSERONEUSER", 0, 2);
        System.out.println("Digest1: " + digest1.toString());


        System.out.println("Testing composeAddress(digests):");
        ArrayList<Digest> digests = new ArrayList<Digest>();
        digests.add(digest1);
        digests.add(digest2);
        MultisigAddress composedAddr = IotaFlashBridge.composeAddress(digests);
        System.out.println("Got multisig addr for digests: " + composedAddr.getAddress() + ", securitySum: " + composedAddr.getSecuritySum());

        testPrepare();
    }

    private static void testPrepare() {

        System.out.println("Testing prepare()");
        ArrayList<String> settlementAddr = new ArrayList<String>();
        settlementAddr.add("RCZHCRDWMGJPHKROKEGVADVJXPGKEKNJRNLZZFPITUVEWNPGIWNUMKTYKMNB9DCNLWGMJZDNKYQDQKDLC");
        ArrayList<Integer> depositsPrep = new ArrayList<Integer>();
        ArrayList<Transfer> transfers = new ArrayList<Transfer>();

        IotaFlashBridge.prepare(settlementAddr, depositsPrep, 0, transfers);
    }
}