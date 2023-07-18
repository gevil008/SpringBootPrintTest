package com.example.springbootprinttest.utils;

import com.lowagie.text.pdf.BaseFont;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;

public class FontContant {

    /**宋体常规*/
    public static final ClassPathResource SIMS = new ClassPathResource("font/simsun.ttc,0");
    /**黑体常规*/
    public static final ClassPathResource SIMHEI = new ClassPathResource("font/simhei.ttf");
    /**常规宋体常规*/
    public static final ClassPathResource STSONG = new ClassPathResource("font/STSONG.TTF");
    /**Calibri*/
    public static final ClassPathResource CALIBRI = new ClassPathResource("font/calibri.ttf");
    /**Times New Roman*/
    public static final ClassPathResource ROMAN = new ClassPathResource("font/times.ttf");
    /**WINDING2*/
    public static final ClassPathResource WINDING2 = new ClassPathResource("font/WINGDNG2.TTF");

    public static BaseFont SIMS_FONT;
    public static BaseFont SIMHEI_FONT;
    public static BaseFont STSONG_FONT;
    public static BaseFont CALIBRI_FONT;
    public static BaseFont ROMAN_FONT;
    public static BaseFont WINDING2_FONT;

    public static HashMap<String,BaseFont> fontMappings;

    static{
        try {
            SIMS_FONT =  BaseFont.createFont(FontContant.SIMS.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            SIMHEI_FONT =  BaseFont.createFont(FontContant.SIMHEI.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            STSONG_FONT =  BaseFont.createFont(FontContant.STSONG.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            CALIBRI_FONT =  BaseFont.createFont(FontContant.CALIBRI.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            ROMAN_FONT =  BaseFont.createFont(FontContant.ROMAN.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            WINDING2_FONT =  BaseFont.createFont(FontContant.WINDING2.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
        }

        fontMappings = new HashMap<>();
        fontMappings.put("黑体",SIMHEI_FONT);
        fontMappings.put("宋体",SIMS_FONT);
        fontMappings.put("Calibri",CALIBRI_FONT);
        fontMappings.put("Times New Roman",ROMAN_FONT);
        fontMappings.put("Wingdings 2",WINDING2_FONT);
    }
}
