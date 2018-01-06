import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Helpers {
    public static ArrayList<Bundle> createTransaction(UserObject user, ArrayList<Transfer> transfers, boolean shouldClose) {
        CreateTransactionHelperObject toUse = IotaFlashBridge.updateLeafToRoot(user.getFlash().root);

        if (toUse.getGenerate() != 0) {
            // TODO: tell the server to gen new address.
            System.out.println("No more addresses in channel.");
        }

        ArrayList<Transfer> newTransfers;

        if (shouldClose) {
            // newTransfers = IotaFlashBridge.close(user.getFlash().getSettlementAddresses(), user.getFlash().deposits);
        } else {
            List<Object> test = IotaFlashBridge.prepare(
                    user.getFlash().settlementAddresses,
                    user.getFlash().deposits,
                    user.getUserIndex(),
                    transfers
                    );
        }

        return null;
    }
}
