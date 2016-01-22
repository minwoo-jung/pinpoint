package com.navercorp.pinpoint.web.view;

import static org.junit.Assert.*;

import org.junit.Test;

public class TransactionInfoViewModelTest {

    @Test
    public void test() {
        TransactionInfoViewModel viewModel = new TransactionInfoViewModel(null, null, null, null, null, true, null, "logSystem.pinpoint", null);
        viewModel.getLogPageUrl();
    }

}
