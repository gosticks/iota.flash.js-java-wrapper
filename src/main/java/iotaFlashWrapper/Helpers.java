package iotaFlashWrapper;

import iotaFlashWrapper.Model.*;
import jota.IotaAPI;
import jota.error.ArgumentException;


import java.util.ArrayList;
import java.util.List;

public class Helpers {


    /**
     * Get a transaction object. The object contains the address to use and if required the number of new addresses to generate
     * @param root multisig address at the top of the tree
     * @return Transaction object with address and number of addresses to create.
     */
    public static CreateTransactionHelperObject getTransactionHelper(Multisig root) {
        return IotaFlashBridge.updateLeafToRoot(root);
    }


    /**
     *
     * @param transfers
     * @param toUse Transaction helper object
     * @param user
     * @param shouldClose
     * @return
     */
    public static ArrayList<Bundle> createTransaction(ArrayList<Transfer> transfers, CreateTransactionHelperObject toUse, UserObject user, boolean shouldClose) {
        // System.out.println("Creating a transaction of" + transfers.getValue() + " to " + transfers.getAddress());
        System.out.println("[INFO]: using address "  + toUse.getAddress().getAddress() + ", with boundle count" + toUse.getAddress().getBundles().size());

        ArrayList<Transfer> newTransfers;
        FlashObject flash = user.getFlash();

        if (shouldClose) {
            newTransfers = IotaFlashBridge.close(flash.getSettlementAddresses(), flash.getDeposits());
        } else {
            // Prepare a new transaction.
            newTransfers = IotaFlashBridge.prepare(
                    flash.getSettlementAddresses(),
                    flash.getDeposits(),
                    user.getUserIndex(),
                    transfers
                    );
        }

        // Compose the transaction. This may also add some management transactions (moving remainder tokens.)
        ArrayList<Bundle> bundles = IotaFlashBridge.compose(
                flash.getBalance(),
                flash.getDeposits(),
                flash.getOutputs(),
                toUse.getAddress(),
                flash.getRemainderAddress(),
                flash.getTransfers(),
                newTransfers,
                shouldClose
        );

        System.out.println("[SUCCESS] Created signatures for user" + user.getUserIndex());
        // Apply the signature of the transaction creater to the current transactions bundle.
        ArrayList<Signature> signatures = IotaFlashBridge.sign(toUse.getAddress(), user.getSeed(), bundles);

        System.out.println("[SUCCESS] Parial applied Signature for user" +  user.getUserIndex() + " on transfer bundle");
        // Sign bundle with your USER ONE'S signatures
        return IotaFlashBridge.appliedSignatures(bundles, signatures);
    }

    /**
     *
     *  Tree management.
     *
     */

    /**
     *
     * @param user
     * @param toGenerate
     * @return
     */
    public static ArrayList<Digest> getNewBranchDigests(UserObject user, int toGenerate) {
        ArrayList<Digest> digests = new ArrayList<>();
        for (int i = 0; i < toGenerate; i++) {
            Digest digest = IotaFlashBridge.getDigest(user.getSeed(), user.getIndex(), user.getSecurity());
            System.out.println("USING index for digest: " + user.getIndex() );
            user.incrementIndex();
            digests.add(digest);
        }
        return digests;
    }


    /**
     *
     * @param oneDigests
     * @param twoDigests
     * @param user
     * @param address
     * @return
     */
    public static Multisig getNewBranch(ArrayList<Digest> oneDigests, ArrayList<Digest> twoDigests, UserObject user, Multisig address) {
        ArrayList<ArrayList<Digest>> userDigestList = new ArrayList<>();
        userDigestList.add(oneDigests);
        userDigestList.add(twoDigests);
        ArrayList<Multisig> multisigs = getMultisigsForUser(userDigestList, user);

        System.out.println("[INFO]: Adding to address " + address.getAddress());

        // Build flash trees
        for (int i = 1; i < multisigs.size(); i++) {
            multisigs.get(i - 1).push(multisigs.get(i));
        }

        // Clone the address to avoid overwriting params.
        Multisig output = address.clone();

        // Add new multisigs to address.
        output.push(multisigs.get(0));

        return output;
    }


    /**
     *
     *  Digests and Multisig creation
     *
     */

    /**
     * Creates initial digests for a user. This will only create digests for a given TREE_DEPTH -> Digests.size == TREE_DEPTH + 1
     * Other transactions will be generated when required.
     * @param user user for which the digests should be generated
     * @param TREE_DEPTH number of initial digests to generate
     * @return digests for provided user.
     */
    public static ArrayList<Digest> getDigestsForUser(UserObject user, int TREE_DEPTH) {
        ArrayList<Digest> digests = new ArrayList<>();
        // Create digests for the start of the channel
        for (int i = 0; i < TREE_DEPTH + 1; i++) {
            // Create new digest
            Digest digest = IotaFlashBridge.getDigest(
                    user.getSeed(),
                    user.getIndex(),
                    user.getSecurity()
            );
            user.incrementIndex();
            System.out.println("Adding digest (" + digest.toString() + ") to user " + user.getUserIndex());
            // Increment key index

            digests.add(digest);
        }
        return digests;
    }


    /**
     *
     * @param allDigests
     * @param currentUser
     * @return
     */
    public static ArrayList<Multisig> getMultisigsForUser(ArrayList<ArrayList<Digest>> allDigests, UserObject currentUser) {

        // Generate the first addresses
        ArrayList<Multisig> multisigs = new ArrayList<Multisig>();

        // Loop for all digests.
        for (int index = 0; index < allDigests.get(0).size(); index++) {
            ArrayList<Digest> alignedDigests = new ArrayList<>();

            int securitySum = 0;

            // Loop for all users.
            for (int userIndex = 0; userIndex < allDigests.size(); userIndex++) {
                Digest digest = allDigests.get(userIndex).get(index);
                // Get array of digests for all users.
                alignedDigests.add(digest);
                securitySum += digest.getSecurity();
            }

            // Create multisgAddr from digests.
            Multisig multisig = IotaFlashBridge.composeAddress(alignedDigests);

            // Get digests data for current user.
            Digest digest = allDigests.get(currentUser.getUserIndex()).get(index);

            multisig.setIndex(digest.getIndex());
            multisig.setSigningIndex(currentUser.getUserIndex() * digest.getSecurity());
            multisig.setSecuritySum(securitySum);
            multisig.setSecurity(digest.getSecurity());

            System.out.println("Creating address " + multisig.getAddress() + " index" + multisig.getIndex() + " signingIndex: " + multisig.getSigningIndex());

            multisigs.add(multisig);
        }

        return multisigs;
    }


    /**
     *
     * @param user
     * @param multisig
     * @return
     */
    public static Multisig updateMultisigChildrenForUser(UserObject user, Multisig multisig) {
        FlashObject flash = user.getFlash();
        Multisig originAddress = flash.getRoot().find(multisig.getAddress());
        if (originAddress != null) {

            System.out.println("[INFO]: found address in user" + user.getUserIndex() + " data");
            originAddress.setChildren(multisig.getChildren());
            originAddress.setBundles(multisig.getBundles());
            originAddress.setSecurity(multisig.getSecurity());
            return originAddress;
        }
        return null;
    }

    public static void applyTransfers(ArrayList<Bundle> signedBundles, UserObject user) {
        // Apply transfers to User ONE
        FlashObject newFlash = IotaFlashBridge.applyTransfersToUser(user, signedBundles);

        // Set new flash object to user
        user.setFlash(newFlash);

        // Save latest channel bundles
        user.getBundles().addAll(signedBundles);
    }


    public static void sendTrytes(String[] trytes) {
        IotaAPI.Builder iota = new IotaAPI.Builder();
        try {
            iota.build().sendTrytes(trytes, 5, 10);
        } catch (ArgumentException exception) {
            System.out.println("[ERROR]: could not send trytes");
        }

    }


    public static void POWClosedBundle(List<Bundle> bundles) {
        for (Bundle b : bundles) {
            String[] trytes = b.toTrytesArray();
            sendTrytes(trytes);
        }
    }

    //static async POWClosedBundle(bundles) {
//            try {
//                console.log('attachAndPOWClosedBundle', bundles)
//                bundles = Attach.getBundles(bundles)
//                var trytesPerBundle = []
//                for (var bundle of bundles) {
//                    var trytes = Attach.bundleToTrytes(bundle)
//                    trytesPerBundle.push(trytes)
//                }
//                console.log('closing room with trytes', trytesPerBundle)
//                var results = []
//                for (var trytes of trytesPerBundle) {
//                    console.log(trytes)
//                    if (isWindow()) {
//                        curl.init()
//                        curl.overrideAttachToTangle(iota)
//                    }
//                    var result = await Attach.sendTrytes(trytes)
//                    results.push(result)
//                }
//                return results
//            } catch (e) {
//                return e
//            }
//        }
//    export class Attach {
//        static bundleToTrytes(bundle) {
//            var bundleTrytes = []
//            bundle.forEach(function(bundleTx) {
//                bundleTrytes.push(iota.utils.transactionTrytes(bundleTx))
//            })
//            return bundleTrytes.reverse()
//        }
//
//        static async sendTrytes(trytes) {
//            return new Promise(function(resolve, reject) {
//      iota.api.sendTrytes(
//                trytes,
//                Presets.PROD ? 6 : 5,
//                Presets.PROD ? 15 : 10,
//                        (e, r) => {
//                    console.log('sendTrytes', e, r)
//                    if (e !== null) {
//                        reject(e)
//                    } else {
//                        resolve(r)
//                    }
//                }
//      )
//            })
//        }
//
//        static getBundles(bundles) {
//            var ret = []
//            for (var bundle of bundles) {
//                if (bundle !== null || bundle.value !== 0) {
//                    ret.push(bundle)
//                }
//            }
//            return ret
//        }
//
//        static async POWClosedBundle(bundles) {
//            try {
//                console.log('attachAndPOWClosedBundle', bundles)
//                bundles = Attach.getBundles(bundles)
//                var trytesPerBundle = []
//                for (var bundle of bundles) {
//                    var trytes = Attach.bundleToTrytes(bundle)
//                    trytesPerBundle.push(trytes)
//                }
//                console.log('closing room with trytes', trytesPerBundle)
//                var results = []
//                for (var trytes of trytesPerBundle) {
//                    console.log(trytes)
//                    if (isWindow()) {
//                        curl.init()
//                        curl.overrideAttachToTangle(iota)
//                    }
//                    var result = await Attach.sendTrytes(trytes)
//                    results.push(result)
//                }
//                return results
//            } catch (e) {
//                return e
//            }
//        }
//    }

}
