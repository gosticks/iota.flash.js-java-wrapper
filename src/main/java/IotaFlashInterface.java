import java.util.ArrayList;
import java.util.List;

public interface IotaFlashInterface {
    // Multisig
    public MultisigAddress composeAddress(ArrayList<Digest> digests);

    public void updateLeafToRoot(MultisigAddress root);

    // Transfer
    public Object prepare(ArrayList<String> settlementAddresses, ArrayList<Integer> deposits, int index, ArrayList<Transfer> transfers);
    public List<Object> compose(int balance, ArrayList<Integer> deposits, ArrayList<Transfer> outputs, Object multisig, String remainderAddress, ArrayList<Bundle> history, ArrayList<Transfer> transfers, boolean close);

    public Digest getDigest(String seed, int index, int security);

    public Object sign(Object multisig, String seed, ArrayList<Object> bundles);
    public Object appliedSignatures(ArrayList<Object> bundles, ArrayList<Object> signatures);
    public Object getDiff(ArrayList<Object> root,
                                   ArrayList<Object> remainder,
                                   ArrayList<Object> history,
                                   ArrayList<Object> bundles);
    public Object applayTransfers(Object root,
                                           Object deposit,
                                           Object outputs,
                                           Object remainderAddress,
                                           Object transfers,
                                           Object signedBundles);
    public Object close(ArrayList<String> settlementAddresses, ArrayList<Integer> deposits);
}



