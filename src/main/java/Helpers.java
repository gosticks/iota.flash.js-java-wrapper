import java.util.ArrayList;
import java.util.List;

public class Helpers {
    public static ArrayList<Bundle> createTransaction(UserObject user, ArrayList<Transfer> transfers, boolean shouldClose) {
        CreateTransactionHelperObject toUse = IotaFlashBridge.updateLeafToRoot(user.getFlash().root);

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
                    user.getFlash().settlementAddresses,
                    user.getFlash().deposits,
                    user.getUserIndex(),
                    transfers
                    );
        }

        ArrayList<Bundle> bundles = IotaFlashBridge.compose(
                user.getFlash().balance,
                user.getFlash().deposits,
                user.getFlash().outputs,
                toUse.getAddress(),
                user.getFlash().remainderAddress,
                user.getFlash().transfers,
                newTransfers,
                shouldClose
        );


        signTransaction(user, bundles);

        return null;
    }

    public static Object signTransaction(UserObject user, ArrayList<Bundle> bundles) {
        return IotaFlashBridge.sign(user.getFlash().root, user.getSeed(), bundles);
    }
}
