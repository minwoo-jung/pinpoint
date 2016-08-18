/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase.stat.compatibility;

import com.navercorp.pinpoint.common.server.bo.stat.JvmGcDetailedBo;
import com.navercorp.pinpoint.web.dao.stat.JvmGcDetailedDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext-test.xml")
public class JvmGcDetailedDaoCompatibilityTest extends HbaseAgentStatDaoCompatibilityTestBase<JvmGcDetailedBo> {

    @Autowired
    @Qualifier("jvmGcDetailedDaoV1")
    private JvmGcDetailedDao v1Dao;

    @Autowired
    @Qualifier("jvmGcDetailedDaoV2")
    private JvmGcDetailedDao v2Dao;

    @Before
    public void setUp() {
        super.v1Dao = this.v1Dao;
        super.v2Dao = this.v2Dao;
    }

    @Override
    protected void verifyBos(List<JvmGcDetailedBo> v1Bos, List<JvmGcDetailedBo> v2Bos) {
        // v1 does not support JvmGcDetailed stat collection
        Assert.assertTrue(v1Bos.isEmpty());
        Assert.assertTrue(!v2Bos.isEmpty());
    }
}
