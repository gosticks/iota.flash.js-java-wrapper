package iotaFlashWrapper;

import com.sun.org.apache.xpath.internal.operations.Mult;
import iotaFlashWrapper.Model.*;

import java.io.IOException;
import java.util.ArrayList;
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
        int SECURITY = 1;
        // Number of parties taking signing part in the channel
        int SIGNERS_COUNT = 2;
        // Flash tree depth
        int TREE_DEPTH = 4;
        // Total channel Balance
        int CHANNEL_BALANCE = 2000;
        // Users deposits
        ArrayList<Double> DEPOSITS = new ArrayList<>();
        DEPOSITS.add(1000.0);
        DEPOSITS.add(1000.0);
        // Setup users.
        FlashObject oneFlashObj = new FlashObject(SIGNERS_COUNT, CHANNEL_BALANCE, DEPOSITS);
        UserObject oneFlash = new UserObject(0, oneSeed, TREE_DEPTH, SECURITY, oneFlashObj);

        FlashObject twoFlashObj = new FlashObject(SIGNERS_COUNT, CHANNEL_BALANCE, DEPOSITS);
        UserObject twoFlash = new UserObject(1, twoSeed, TREE_DEPTH, SECURITY, twoFlashObj);

        // USER ONE
        ArrayList<Digest> oneDigests = Helpers.getDigestsForUser(oneFlash, TREE_DEPTH);

        // USER TWO
        ArrayList<Digest> twoDigests = Helpers.getDigestsForUser(twoFlash, TREE_DEPTH);

        //////////////////////////////////
        // INITAL MULTISIG

        // Make an array of digests
        ArrayList<ArrayList<Digest>> allUserDigests = new ArrayList<>();
        allUserDigests.add(oneDigests);
        allUserDigests.add(twoDigests);



        /***************************************
            User one setup.
         ***************************************/

        // Create multisigs.
        ArrayList<MultisigAddress> oneMultisigs = Helpers.getMultisigsForUser(allUserDigests, oneFlash);

        // Set renainder address.
        MultisigAddress oneRemainderAddr = oneMultisigs.remove(0); //shiftCopyArray();
        oneFlash.getFlash().setRemainderAddress(oneRemainderAddr);

        // Build flash trees
        for (int i = 1; i < oneMultisigs.size(); i++) {
            System.out.println(oneMultisigs.get(i - 1).toString() + " -> "  + oneMultisigs.get(i).toString());
            oneMultisigs.get(i - 1).push(oneMultisigs.get(i));
        }
        oneFlash.getFlash().setRoot(oneMultisigs.remove(0));



        /***************************************
         User one setup.
         ***************************************/

        ArrayList<MultisigAddress> twoMultisigs = Helpers.getMultisigsForUser(allUserDigests, twoFlash);
        // Set user two remainder addr.
        MultisigAddress twoRemainderAddr = twoMultisigs.remove(0);
        twoFlash.getFlash().setRemainderAddress(twoRemainderAddr);

        // Build flash trees
        for (int i = 1; i < twoMultisigs.size(); i++) {
            twoMultisigs.get(i - 1).push(twoMultisigs.get(i));
        }
        twoFlash.getFlash().setRoot(twoMultisigs.remove(0));



        /***************************************
         Setup tettlements.
         ***************************************/

        ArrayList<String> settlementAddresses = new ArrayList<>();
        settlementAddresses.add(oneSettlement);
        settlementAddresses.add(twoSettlement);



        /***************************************
         Setup tettlements.
         ***************************************/

        oneFlash.getFlash().setSettlementAddresses(settlementAddresses);
        twoFlash.getFlash().setSettlementAddresses(settlementAddresses);

        // Set digest/key index
        oneFlash.setIndex(oneDigests.size());
        twoFlash.setIndex(twoDigests.size());

        System.out.println("Channel Setup completed!");

        /***************************************
         Create transactions.
         ***************************************/


        // Create transfer from user one
        ArrayList<Bundle> suggestedTransfer;

        // Accept transfers from other user
        ArrayList<Bundle> confirmedTransfers;

        // Try to make 10 transfers.
        for (int i = 0; i < 10; i++) {

            // Create transaction helper and check if we need to add nodes
            CreateTransactionHelperObject helper = Helpers.getTransactionHelper(oneFlash.getFlash().getRoot());

            // Check if we need to create new addresses. This must be done before a transaction is prepared.
            // The createTransaction will then create funding fundles for the new address.
            if (helper.getGenerate() != 0) {
                System.out.println("[WARN]: generating " + helper.getGenerate() + "new branches!");

                // Add user one digests.
                ArrayList<Digest> newOneDigests = Helpers.getNewBranchDigests(oneFlash, helper.getGenerate());

                // Add user two digests
                ArrayList<Digest> newTwoDigests = Helpers.getNewBranchDigests(twoFlash, helper.getGenerate());

                // Now we can create new multisig addresses
                MultisigAddress multisigAddressOne = Helpers.getNewBranch(newOneDigests, newTwoDigests, oneFlash, helper.getAddress());
                MultisigAddress multisigAddressTwo = Helpers.getNewBranch(newOneDigests, newTwoDigests, twoFlash, helper.getAddress());

                // Find the multisig with the address and append new address to children
                Helpers.updateMultisigChildrenForUser(oneFlash, multisigAddressOne);
                Helpers.updateMultisigChildrenForUser(twoFlash, multisigAddressTwo);

                // Set the updated multisig as origin of the transaction.
                helper.setAddress(multisigAddressOne);
            }

            // Create transfers.
            ArrayList<Transfer> transfers = new ArrayList<>();
            transfers.add(new Transfer(twoSettlement, 20));

            // Create a transaction from a transfer.
            suggestedTransfer = Helpers.createTransaction(transfers, helper, oneFlash, false);

            System.out.println("[INFO] Created transfer suggestion.");

            // TODO: check here if transaction is valid.

            // If transactions should be signed create signatures.
            ArrayList<Signature> userTwoSignatures = IotaFlashBridge.sign(twoFlash.getFlash().getRoot(), twoFlash.getSeed(), suggestedTransfer);
            System.out.println("[INFO] Created user two signatures.");
            // TODO: the signatures should be sind to the first user.

            // Apply transfers by all users.
            System.out.println("[INFO] Signing transfers.");
            ArrayList<Bundle> signedBundlesOne = IotaFlashBridge.appliedSignatures(suggestedTransfer, userTwoSignatures);
            ArrayList<Bundle> signedBundlesTwo = IotaFlashBridge.appliedSignatures(suggestedTransfer, userTwoSignatures);
            applyTransfers(signedBundlesOne, oneFlash);
            applyTransfers(signedBundlesTwo, twoFlash);

            System.out.println("Transaction Applied! Transactable tokens: " + getFlashDeposits(oneFlash));
        }


        System.out.println("Closing channel... not yet working...");
    }

    public static void applyTransfers(ArrayList<Bundle> signedBundles, UserObject user) {
        // Apply transfers to User ONE
        FlashObject newFlash = IotaFlashBridge.applyTransfersToUser(user, signedBundles);

        // Set new flash object to user
        user.setFlash(newFlash);

        // Save latest channel bundles
        user.setBundles(signedBundles);
    }

    public static double getFlashDeposits(UserObject user) {
        double sum = 0;
        for (double deposit : user.getFlash().getDeposits()) {
            sum += deposit;
        }
        return sum;
    }


    /**
     * acceptTransfer applies signatures of a
     * @param bundles half signed transfers
     * @param signatures signatures of the second user
     */
    public static void acceptTransfer(ArrayList<Bundle> bundles, ArrayList<Signature> signatures, UserObject user) {
        ArrayList<Bundle> signedBundles = IotaFlashBridge.appliedSignatures(bundles, signatures);
        applyTransfers(signedBundles, user);
    }
}
