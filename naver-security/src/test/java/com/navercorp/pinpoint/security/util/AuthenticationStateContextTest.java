/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.security.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class AuthenticationStateContextTest {

    @Test
    public void stateContextTest1() {
        AuthenticationStateContext stateContext = new AuthenticationStateContext();

        assertState(stateContext, AuthenticationStateContext.INIT);

        boolean changed = stateContext.changeStateSuccess();
        Assert.assertFalse(changed);
        assertState(stateContext, AuthenticationStateContext.INIT);

        changed = stateContext.changeStateFail();
        Assert.assertFalse(changed);
        assertState(stateContext, AuthenticationStateContext.INIT);

        changed = stateContext.changeStateProgress();
        Assert.assertTrue(changed);
        assertState(stateContext, AuthenticationStateContext.IN_PROGRESS);

        changed = stateContext.changeStateProgress();
        Assert.assertFalse(changed);
        assertState(stateContext, AuthenticationStateContext.IN_PROGRESS);

        changed = stateContext.changeStateSuccess();
        Assert.assertTrue(changed);
        assertState(stateContext, AuthenticationStateContext.SUCCESS);

        changed = stateContext.changeStateFail();
        Assert.assertFalse(changed);
        assertState(stateContext, AuthenticationStateContext.SUCCESS);
    }

    @Test
    public void stateContextTest2() {
        AuthenticationStateContext stateContext = new AuthenticationStateContext();

        boolean changed = stateContext.changeStateProgress();
        Assert.assertTrue(changed);
        assertState(stateContext, AuthenticationStateContext.IN_PROGRESS);

        changed = stateContext.changeStateFail();
        Assert.assertTrue(changed);
        assertState(stateContext, AuthenticationStateContext.FAIL);

        changed = stateContext.changeStateProgress();
        Assert.assertFalse(changed);
        assertState(stateContext, AuthenticationStateContext.FAIL);

        changed = stateContext.changeStateSuccess();
        Assert.assertFalse(changed);
        assertState(stateContext, AuthenticationStateContext.FAIL);
    }

    private void assertState(AuthenticationStateContext stateContext, int expectedState) {
        int state = stateContext.getState();
        Assert.assertEquals(expectedState, state);
    }

}
