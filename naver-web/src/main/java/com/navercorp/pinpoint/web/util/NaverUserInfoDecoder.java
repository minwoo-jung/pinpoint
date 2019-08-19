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
package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * @author minwoo.jung
 */
public class NaverUserInfoDecoder implements UserInfoDecoder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private final byte[] keys;
    private final byte[] initialVectors;

    public NaverUserInfoDecoder(String encodeKey) {
        keys = parseEncodeKey(encodeKey);
        initialVectors = newInitialVector(keys);
    }

    private byte[] newInitialVector(byte[] keys) {
        String keysString = BytesUtils.toString(keys);
        String reverseKey = StringUtils.reverse(keysString);
        return BytesUtils.toBytes(reverseKey);
    }

    private byte[] parseEncodeKey(String encodeKey) {
        String[] keyList = StringUtils.split(encodeKey, ',');
        byte[] keyBytes = new byte[keyList.length];

        for(int i = 0 ; i < keyList.length; i++) {
            keyBytes[i] = (byte) (Integer.parseInt(StringUtils.deleteWhitespace(keyList[i]),16) & 0xff);
        }
        return keyBytes;
    }


    @Override
    public List<String> decodePhoneNumberList(final List<String> encodedPhoneNumberList) {
        if (CollectionUtils.isEmpty(encodedPhoneNumberList)) {
            return encodedPhoneNumberList;
        }

        List<String> phoneNumberList = new ArrayList<>(encodedPhoneNumberList.size());

        for (String encodedPhoneNumber : encodedPhoneNumberList) {
            try {
                phoneNumberList.add(decrypt(encodedPhoneNumber));
            } catch (Exception e) {
                logger.error("could not decode phoneNumber (encode value : {})", encodedPhoneNumber, e);
            }
        }

        return phoneNumberList;
    }

    private String decrypt(String cipherText) throws Exception {
        if (StringUtils.isEmpty(cipherText)) {
            return cipherText;
        }
        byte[] cipherTextBytes = Hex.decodeHex(cipherText.toCharArray());
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec secretKeySpecy = new SecretKeySpec(keys, ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVectors);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpecy, ivParameterSpec);
        byte[] plainBytes = cipher.doFinal(cipherTextBytes);

        return BytesUtils.toString(plainBytes);
    }
}
