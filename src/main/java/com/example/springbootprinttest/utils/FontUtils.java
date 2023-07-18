package com.example.springbootprinttest.utils;

import com.lowagie.text.pdf.BaseFont;

public class FontUtils {

    public static BaseFont getFont(String type){
        if(FontContant.fontMappings.containsKey(type)){
            return FontContant.fontMappings.get(type);
        }
        return FontContant.SIMS_FONT;
    }
}
