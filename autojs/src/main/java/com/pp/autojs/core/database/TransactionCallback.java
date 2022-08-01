package com.pp.autojs.core.database;

public interface TransactionCallback {
    void handleEvent(Transaction transaction);
}
