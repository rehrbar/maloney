package ch.hsr.maloney.util;

/**
 * Created by oliver on 28.03.17.
 */
public class ProgressInfo {
    private final ProgressInfoType type;
    private final int amount;

    public ProgressInfo(ProgressInfoType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public ProgressInfoType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}
