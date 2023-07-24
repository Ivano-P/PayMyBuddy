package com.paymybuddy.paymybuddy.service;

import com.paymybuddy.paymybuddy.model.AccountPayMyBuddy;

public interface AppPmbService {

    AccountPayMyBuddy getAccountPmb();

    boolean checkIfPmbAccountIsPresent();

    void creatPmbAccount();

    String getPmbIban();
}
