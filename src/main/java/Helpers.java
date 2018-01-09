import Model.*;

import java.util.ArrayList;

public class Helpers {
    public static ArrayList<Bundle> createTransaction(UserObject user, ArrayList<Transfer> transfers, boolean shouldClose) {
        CreateTransactionHelperObject toUse = IotaFlashBridge.updateLeafToRoot(user.getFlash().getRoot());

        if (toUse.getGenerate() != 0) {
            // TODO: tell the server to gen new address.
            System.out.println("No more addresses in channel.");
        }

        ArrayList<Transaction> newTransfers;

        if (shouldClose) {
            newTransfers = new ArrayList<>();
            // newTransfers = IotaFlashBridge.close(user.getFlash().getSettlementAddresses(), user.getFlash().deposits);
        } else {
            newTransfers = IotaFlashBridge.prepare(
                    user.getFlash().getSettlementAddresses(),
                    user.getFlash().getDeposits(),
                    user.getUserIndex(),
                    transfers
                    );
        }

        ArrayList<Bundle> bundles = IotaFlashBridge.compose(
                user.getFlash().getBalance(),
                user.getFlash().getDeposits(),
                user.getFlash().getOutputs(),
                toUse.getAddress(),
                user.getFlash().getRemainderAddress(),
                user.getFlash().getTransfers(),
                newTransfers,
                shouldClose
        );
        return bundles;
    }

    public static ArrayList<Signature> signTransaction(UserObject user, ArrayList<Bundle> bundles) {
        return IotaFlashBridge.sign(user.getFlash().getRoot(), user.getSeed(), bundles);
    }

    public static void applyTransfers(UserObject user, ArrayList<Bundle> bundles) {
        IotaFlashBridge.applayTransfers(
                user.getFlash().getRoot(),
                user.getFlash().getDeposits(),
                user.getFlash().getOutputs(),
                user.getFlash().getRemainderAddress(),
                user.getFlash().getTransfers(),
                bundles
        );
    }
}
