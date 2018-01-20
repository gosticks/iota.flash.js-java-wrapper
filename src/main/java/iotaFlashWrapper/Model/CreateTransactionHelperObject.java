package iotaFlashWrapper.Model;

public class CreateTransactionHelperObject {
    private int generate = 0;
    private MultisigAddress address;

    public CreateTransactionHelperObject(int gen, MultisigAddress addr) {
        this.generate = gen;
        this.address = addr;
    }

    public int getGenerate() {
        return generate;
    }

    public MultisigAddress getAddress() {
        return address;
    }

    public void setAddress(MultisigAddress address) {
        this.address = address;
    }
}