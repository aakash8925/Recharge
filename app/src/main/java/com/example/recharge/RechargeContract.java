package com.example.recharge;

import android.provider.BaseColumns;

public final class RechargeContract {

    private RechargeContract() {
    }

    public static class RechargeEntry implements BaseColumns {
        public static final String TABLE_NAME = "recharges";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_NUMBER = "number";
        public static final String COLUMN_OPERATOR = "operator";
        public static final String COLUMN_PACK = "pack";
        public static final String COLUMN_PROFITPERCENT = "profitPercent";
        public static final String COLUMN_PROFIT = "profit";
        public static final String COLUMN_MODE = "mode";
    }
}
