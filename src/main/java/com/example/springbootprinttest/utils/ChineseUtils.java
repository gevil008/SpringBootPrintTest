package com.example.springbootprinttest.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ChineseUtils {
    private static final Logger logger = LoggerFactory.getLogger(ChineseUtils.class);
    //中文宽度
    private static final int CHINESE_COUNT = 2;
    //英文宽度
    private static final int NOT_CHINESE_COUNT = 1;
    //英文空白
    private static final String BLANK = " ";
    //中文空白
    private static final String CHINESE_BLANK = "　";

    /**
     * 判断中英文
     * @param c
     * @return
     */
    public static boolean isChineseByBlock(char c) {
        Character.UnicodeScript sc = Character.UnicodeScript.of(c);
        if (sc == Character.UnicodeScript.HAN) {
            return true;
        }

        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
                || ub == Character.UnicodeBlock.NUMBER_FORMS
                || ub == Character.UnicodeBlock.VERTICAL_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 获取长度
     * @param text
     * @return
     */
    public static int getLength(String text) {
        AtomicInteger count = new AtomicInteger();
        if(Objects.equals(text,null) || Objects.equals("",text)){
            return count.get();
        }

        String[] chars = text.split("");
        Stream.of(chars).forEach(each->{
            if(isChineseByBlock(each.toCharArray()[0])){
                count.addAndGet(CHINESE_COUNT);
                return;
            }
            count.addAndGet(NOT_CHINESE_COUNT);
        });
        return count.get();
    }

    /**
     * 靠左填充空白
     * @param text 源文本
     * @param count  填充后宽度
     * @param position  标识
     * @return
     */
    public static String fillStr(Object text,int count,FillPosition position, String rowSize, String rowSizeNoInclude) {
        if(count == 0){
            count = 10;
        }
        if(position == FillPosition.CENTER){
            return fillStrCenter(text,count,rowSize,rowSizeNoInclude);
        }
        if(position == FillPosition.LEFT){
            return fillStrLeft(text,count,rowSize,rowSizeNoInclude);
        }
        if(position == FillPosition.RIGHT){
            return fillStrRight(text,count,rowSize,rowSizeNoInclude);
        }
        return null;
    }

    private static String fillStrLeft(Object text, int count, String rowSize, String rowSizeNoInclude) {
        StringBuilder sb = new StringBuilder();
        if(Objects.equals(text,null)){
            while(count >= 2){
                sb.append(CHINESE_BLANK);
                count = count - 2;
            }
            for (int i = 0; i < count; i++) {
                sb.append(BLANK);
            }
            return sb.toString();
        }

        String orignStr = text.toString();

        int length = getLength(text.toString());
        if(length >= count) {
            //扩充数据长度
            return orignStr;
        }
        //差值
        int div = count - length;
        sb.append(text);

        String cont = "";
        if(div >= 2){
            if(div%2 != 0){
                sb.append(BLANK);
                div--;
            }
            while(div >= 2){
                sb.append(CHINESE_BLANK);
                div = div-2;
            }
        }else{
            for (int i = 0; i < div; i++) {
                sb.append(BLANK);
                div--;
            }
        }

        logger.debug("填充后字符数:"+ChineseUtils.getLength(sb.toString()));
        return sb.toString();
    }

    private static String fillStrRight(Object text, int count, String rowSize, String rowSizeNoInclude) {
        StringBuilder sb = new StringBuilder();
        if(Objects.equals(text,null)){
            while(count >= 2){
                sb.append(CHINESE_BLANK);
                count = count - 2;
            }
            for (int i = 0; i < count; i++) {
                sb.append(BLANK);
            }
            return sb.toString();
        }

        String orignStr = text.toString();

        int length = getLength(text.toString());
        if(length >= count) {
            //扩充数据长度
            return orignStr;
        }
        //差值
        int div = count - length;


        String cont = "";
        if(div >= 2){
            if(div%2 != 0){
                sb.append(BLANK);
                div--;
            }
            while(div >= 2){
                sb.append(CHINESE_BLANK);
                div = div-2;
            }
        }else{
            for (int i = 0; i < div; i++) {
                sb.append(BLANK);
                div--;
            }
        }
        sb.append(text);
        logger.debug("填充后字符数:"+ChineseUtils.getLength(sb.toString()));
        return sb.toString();
    }

    /**
     * 靠左填充空白
     * @param text 源文本
     * @param count  填充后宽度
     * @param rowSize
     * @param rowSizeNoInclude
     * @return
     */
    public static String fillStr(Object text, int count, String rowSize, String rowSizeNoInclude) {
        if(count == 0){
            count = 20;
        }
        return fillStr(text,count,FillPosition.CENTER, rowSize, rowSizeNoInclude);
    }

    /**
     * 居中填充
     * @param text
     * @param count
     * @return
     */
    private static String fillStrCenter(Object text,int count, String rowSize, String rowSizeNoInclude) {
        StringBuilder sb = new StringBuilder();
        if(Objects.equals(text,null)){
            while(count >= 2){
                sb.append(CHINESE_BLANK);
                count = count - 2;
            }
            for (int i = 0; i < count; i++) {
                sb.append(BLANK);
            }
            return sb.toString();
        }

        String orignStr = text.toString();
        int length = getLength(text.toString());
        if(length >= count) {
            return orignStr;
        }
        //差值
        int div = count - length;
        //奇数偶数
        int remainder = div % 2;
        int divisor = div/2;

        StringBuilder left = new StringBuilder();
        if(divisor>2){
            left.insert(0,CHINESE_BLANK);
            int count1 = divisor - 2;
            while(count1 >= 2){
                left.append(CHINESE_BLANK);
                count1 = count1 - 2;
            }
            for (int i = 0; i < count1; i++) {
                left.append(BLANK);
            }
        }else{
            for (int i = 0; i < divisor; i++) {
                left.append(BLANK);
            }
        }
        sb.append(left);
        sb.append(orignStr);

        int rightFill =  remainder == 0 ? divisor : divisor+1;
        StringBuilder right = new StringBuilder("");
        if(rightFill>2){
            right.insert(0,CHINESE_BLANK);
            int count1 = rightFill - 2;
            while(count1 >= 2){
                right.append(CHINESE_BLANK);
                count1 = count1 - 2;
            }
            for (int i = 0; i < count1; i++) {
                right.append(BLANK);
            }
        }else{
            for (int i = 0; i < rightFill; i++) {
                right.append(BLANK);
            }
        }

        String reverse = StringUtils.reverse(right.toString());
        sb.append(reverse);
//        for (int i = 0; i < rightFill; i++) {
//            sb.append(BLANK);
//        }

        return sb.toString();
    }

    /**
     * 枚举  目前只实现center
     */
    enum FillPosition{
        LEFT,CENTER,RIGHT
    }
}
