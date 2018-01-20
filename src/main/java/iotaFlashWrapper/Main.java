package iotaFlashWrapper;

import iotaFlashWrapper.Model.*;
import jota.utils.Checksum;

import java.util.ArrayList;


public class Main {

    public static void main(String[] argv) throws Exception {
        // Create v8 engine and load lib.
        IotaFlashBridge.boot();

        // Run a test based on the flash example
        // Link: https://github.com/iotaledger/iota.flash.js/blob/master/examples/flash.js

        String oneSeed = "GXGIIGUTA9MCRLFMVGLAYBENVGOCWNXRCBQNYGMQKOZRRNSICRPXGONVOUTIMOGLUWSETZSRSYRRSBABR";
        String oneSettlement = "SFAMPVUSPED9NZIVRDNOVSOLNQ9QHZVHDEWBPTYEJJC9QUMOSUQUQWL9BBENVVEFSCQCVBEXHGPACHRVX";
        String twoSeed = "GXGIIGUTA9MCRLFMVGLAYBENVGOCWNXRCBQNYGMQKOZRRNSIIIIXGONVUOTIMOGLUWSETZSRSYRRSBABR";
        String twoSettlement = "9ZPQTA9UPWFW9NHX9SJ9EYPYYGMMZUDZHHYTVHKZHXKBMOCNTDIKRWFVBMFYEUL9IBIALHHNYZWBOEJHA";

        //////////////////////////////////
        // INITIAL CHANNEL CONDITIONS

        // Security level
        int SECURITY = 1;
        // Number of parties taking signing part in the channel
        int SIGNERS_COUNT = 2;
        // Flash tree depth
        int TREE_DEPTH = 4;
        // Total channel Balance
        int CHANNEL_BALANCE = 200;
        // Users deposits
        ArrayList<Double> DEPOSITS = new ArrayList<>();
        DEPOSITS.add(100.0);
        DEPOSITS.add(100.0);
        // Setup users.
        FlashObject oneFlashObj = new FlashObject(SIGNERS_COUNT, CHANNEL_BALANCE, DEPOSITS);
        UserObject oneFlash = new UserObject(0, oneSeed, TREE_DEPTH, SECURITY, oneSettlement, oneFlashObj);

        FlashObject twoFlashObj = new FlashObject(SIGNERS_COUNT, CHANNEL_BALANCE, DEPOSITS);
        UserObject twoFlash = new UserObject(1, twoSeed, TREE_DEPTH, SECURITY, twoSettlement, twoFlashObj);

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
        ArrayList<Multisig> oneMultisigs = Helpers.getMultisigsForUser(allUserDigests, oneFlash);

        // Set renainder address.
        Multisig oneRemainderAddr = oneMultisigs.remove(0); //shiftCopyArray();
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

        ArrayList<Multisig> twoMultisigs = Helpers.getMultisigsForUser(allUserDigests, twoFlash);
        // Set user two remainder addr.
        Multisig twoRemainderAddr = twoMultisigs.remove(0);
        twoFlash.getFlash().setRemainderAddress(twoRemainderAddr);

        // Build flash trees
        for (int i = 1; i < twoMultisigs.size(); i++) {
            twoMultisigs.get(i - 1).push(twoMultisigs.get(i));
        }
        twoFlash.getFlash().setRoot(twoMultisigs.remove(0));



        /***************************************
         Setup settlements.
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
         Root address of channel.
         ***************************************/

        String multisgFulladdr = Checksum.addChecksum(oneFlash.getFlash().getRoot().getAddress());

        System.out.println("[ROOT ADDR]:" + multisgFulladdr);


        /***************************************
         Create transactions.
         ***************************************/


        // Create transfer from user one
        ArrayList<Bundle> suggestedTransfer;

        // Accept transfers from other user
        ArrayList<Bundle> confirmedTransfers;

        // Try to make 10 transfers.
        for (int i = 0; i < 9; i++) {

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
                Multisig multisigOne = Helpers.getNewBranch(newOneDigests, newTwoDigests, oneFlash, helper.getAddress());
                Multisig multisigTwo = Helpers.getNewBranch(newOneDigests, newTwoDigests, twoFlash, helper.getAddress());

                // Find the multisig with the address and append new address to children
                Helpers.updateMultisigChildrenForUser(oneFlash, multisigOne);
                Helpers.updateMultisigChildrenForUser(twoFlash, multisigTwo);

                // Set the updated multisig as origin of the transaction.
                helper.setAddress(multisigOne);
            }

            // Create transfers.
            ArrayList<Transfer> transfers = new ArrayList<>();
            transfers.add(new Transfer(twoSettlement, 10));

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
            Helpers.applyTransfers(signedBundlesOne, oneFlash);
            Helpers.applyTransfers(signedBundlesTwo, twoFlash);

            System.out.println("Transaction Applied! Transactable tokens: " + getFlashDeposits(oneFlash));

            double oneBalance = getBalanceOfUser(oneFlash);
            double twoBalance = getBalanceOfUser(twoFlash);

            System.out.println("Deposits");
            System.out.println("User one:" + oneBalance + ", deposits: " + oneFlash.getFlash().getDeposits() );
            System.out.println("User two:" + twoBalance + ", deposits: " + oneFlash.getFlash().getDeposits());
        }


        // Create transaction helper and check if we need to add nodes
        CreateTransactionHelperObject closeHelper = Helpers.getTransactionHelper(oneFlash.getFlash().getRoot());

        // Check if we need to create new addresses. This must be done before a transaction is prepared.
        // The createTransaction will then create funding fundles for the new address.
        if (closeHelper.getGenerate() != 0) {
            System.out.println("[WARN]: generating " + closeHelper.getGenerate() + "new branches!");

            // Add user one digests.
            ArrayList<Digest> newOneDigests = Helpers.getNewBranchDigests(oneFlash, closeHelper.getGenerate());

            // Add user two digests
            ArrayList<Digest> newTwoDigests = Helpers.getNewBranchDigests(twoFlash, closeHelper.getGenerate());

            // Now we can create new multisig addresses
            Multisig multisigOne = Helpers.getNewBranch(newOneDigests, newTwoDigests, oneFlash, closeHelper.getAddress());
            Multisig multisigTwo = Helpers.getNewBranch(newOneDigests, newTwoDigests, twoFlash, closeHelper.getAddress());

            // Find the multisig with the address and append new address to children
            Helpers.updateMultisigChildrenForUser(oneFlash, multisigOne);
            Helpers.updateMultisigChildrenForUser(twoFlash, multisigTwo);

            // Set the updated multisig as origin of the transaction.
            closeHelper.setAddress(multisigOne);


        }

        System.out.println("[INFO] Closing channel...");

        // Create transfers.
        ArrayList<Transfer> closeTransfers = new ArrayList<>();
        closeTransfers.add(new Transfer(oneSettlement, 5));
        closeTransfers.add(new Transfer(twoSettlement, 5));
        suggestedTransfer = Helpers.createTransaction(closeTransfers, closeHelper, oneFlash, true);

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
        Helpers.applyTransfers(signedBundlesOne, oneFlash);
        Helpers.applyTransfers(signedBundlesTwo, twoFlash);

        System.out.println("[INFO] Channel closed!");

        Helpers.POWClosedBundle(signedBundlesTwo);

        // System.out.println("[INFO] Final address:" + Attach signedBundlesOne);
    }


    public static double getFlashDeposits(UserObject user) {
        double sum = 0;
        for (double deposit : user.getFlash().getDeposits()) {
            sum += deposit;
        }
        return sum;
    }

    public static double getBalanceOfUser(UserObject user) {
        double balance = 0.0;
        ArrayList<Bundle> transfers = user.getBundles();
        if (transfers.size() == 0) {
            return 0.0;
        }

        for (Bundle bundle : transfers) {
            for (Transaction tx : bundle.getWrappedTransactions()) {
                if (tx.getAddress().equals(user.getAddress())) {
                    balance += tx.getValue() / 2;
                }
            }
        }

        return balance;
    }
}
