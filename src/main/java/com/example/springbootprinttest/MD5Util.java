/**
 *  @author   yanghucheng
 *  @created  2017/08/14
 *  Copyright (C),2007-2016, LonBon Technologies Co. Ltd. All Rights Reserved.
 */
package com.example.springbootprinttest;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The type Md 5 util.
 *
 * @author  yanghucheng
 * @Date    2017/08/14
 */
public class MD5Util {
    private static Logger logger = LoggerFactory.getLogger(MD5Util.class);
    /**
     * Byte array to hex string string.
     *
     * @param b the b
     * @return the string
     */
    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }

        return resultSb.toString();
    }

    /**
     * Byte to hex string string.
     *
     * @param b the b
     * @return the string
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * Md 5 encode string.
     *
     * @param origin      the origin
     * @param charsetname the charsetname
     * @return the string
     */
    public static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname)) {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
            } else {
                resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
            }
        } catch (IOException|NoSuchAlgorithmException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return null;
        }
        return resultString.toUpperCase();
    }

    /**
     * Md 5 encode utf 8 string.
     *
     * @param origin the origin
     * @return the string
     */
    public static String MD5EncodeUtf8(String origin) {
        //origin = origin +PropertiesUtil.getProperty("password.salt", "");
        return MD5Encode(origin, "utf-8");
    }

    public static String MD5File(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = null;
            MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
            return null;
        }finally {
            try {
                if (fileInputStream != null){
                    fileInputStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * The constant hexDigits.
     */
    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

}

