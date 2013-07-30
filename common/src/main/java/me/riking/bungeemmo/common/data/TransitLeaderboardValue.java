package me.riking.bungeemmo.common.data;

public class TransitLeaderboardValue implements java.io.Serializable {
    /**
     * Version 1.4.06
     * <p>
     * TODO change on protocol updates to keep data consistency
     */
    private static final long serialVersionUID = 4013455844031280864L;

    public final String name;
    public final int val;

    public TransitLeaderboardValue(String name, int val) {
        this.name = name;
        this.val = val;
    }
}
