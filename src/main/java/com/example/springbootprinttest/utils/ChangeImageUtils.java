package com.example.springbootprinttest.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * 图片变换的工具类。
 * @author zhangchao
 */
public final class ChangeImageUtils {
    

    /**
     * 把图片顺时针旋转90度
     * @param file 原来的图片文件
     * @param outputFile 新生成的图片文件
     * @param formatName 图片文件扩展名
     */
    public final static void clockwise90(final File file, final File outputFile, final String formatName) {
        try {
            // 原图片
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();

            // 新生成的图片
            BufferedImage imageNew = new BufferedImage(height, width, image.getType());
            // 把原图顺时针旋转后，像素存入新图片。
            // 算法实现方式：
            //    把原图片的最后一行，转变为新图片的第1列.
            //    把原图片的倒数第2行，转变为新图片的第2列.
            //    ......
            // 以此类推，进行变换。代码中行数列数从0开始计算。
            int maxY = height - 1;
            for (int y = maxY; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    imageNew.setRGB(maxY - y, x, rgb);
                }
            }
            // 把图片输出到硬盘上。
            ImageIO.write(imageNew, formatName, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把图片逆时针旋转90度
     * @param file 原来的图片文件
     * @param outputFile 新生成的图片文件
     * @param formatName 图片文件扩展名
     */
    public final static void anticlockwise90(final File file, final File outputFile, final String formatName) {
        try {
            // 原图片
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();

            // 新生成的图片
            BufferedImage imageNew = new BufferedImage(height, width, image.getType());

            // 把原图逆时针旋转后，像素存入新图片。
            // 算法实现方式：
            //    把原图片第1行，转变为新图片第1列。并且旧行中第1个像素是新列中最后1个像素.
            //    把原图片第2行，转变为新图片第2列。并且旧行中第1个像素是新列中最后1个像素.
            //    ......
            // 以此类推，进行变换。代码中行数列数从0开始计算。
            int maxX = width - 1;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    imageNew.setRGB(y, maxX - x, rgb);
                }
            }

            // 把图片输出到硬盘上。
            ImageIO.write(imageNew, formatName, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把图片顺时针旋转180度
     * @param file 原来的图片文件
     * @param outputFile 新生成的图片文件
     * @param formatName 图片文件扩展名
     */
    public final static void clockwise180(final File file, final File outputFile, final String formatName) {
        try {
            // 原图片
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            // 新生成的图片
            BufferedImage imageNew = new BufferedImage(width, height, image.getType());

            // 把原图片顺时针旋转180度后，像素存入新图片。
            // 算法实现方式：
            //    原图片第1行转变为新图片倒数第1行。并且旧行中像素的顺序，在新行倒转了过来。
            //    原图片第2行转变为新图片倒数第2行。并且旧行中像素的顺序，在新行倒转了过来。
            //    ......
            // 以此类推，进行变换。代码中行数列数从0开始计算。
            int maxX = width - 1;
            int maxY = height - 1;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    imageNew.setRGB(maxX - x, maxY - y, rgb);
                }
            }

            // 把图片输出到硬盘上。
            ImageIO.write(imageNew, formatName, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把图片水平翻转
     * @param file 原来的图片文件
     * @param outputFile 新生成的图片文件
     * @param formatName 图片文件扩展名
     */
    public final static void flipHorizontally(final File file, final File outputFile, final String formatName) {
        try {
            // 原图片
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            // 新生成的图片
            BufferedImage imageNew = new BufferedImage(width, height, image.getType());

            // 把原图片水平翻转后，像素存入新图片。
            // 实现方法就是把列的排列顺序反过来。
            int maxX = width - 1;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int rgb = image.getRGB(x, y);
                    imageNew.setRGB(maxX - x, y, rgb);
                }
            }

            // 把图片输出到硬盘上。
            ImageIO.write(imageNew, formatName, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把图片垂直翻转
     * @param file 原来的图片文件
     * @param outputFile 新生成的图片文件
     * @param formatName 图片文件扩展名
     */
    public final static void flipVertically(final File file, final File outputFile, final String formatName) {
        try {
            // 原图片
            BufferedImage image = ImageIO.read(file);
            int width = image.getWidth();
            int height = image.getHeight();
            // 新生成的图片
            BufferedImage imageNew = new BufferedImage(width, height, image.getType());

            // 把原图片垂直翻转后，像素存入新的图片。
            // 把图片看作矩阵，行与行之间的顺序做了翻转。
            int maxY = height - 1;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb = image.getRGB(x, y);
                    imageNew.setRGB(x, maxY - y, rgb);
                }
            }

            // 把图片输出到硬盘上。
            ImageIO.write(imageNew, formatName, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
    	File originFile = new File("E:\\IdeaWorke-space\\springbootprinttest\\src\\main\\resources\\templates\\1_1.jpg");
    	File clockwise90File = new File("E:\\IdeaWorke-space\\springbootprinttest\\src\\main\\resources\\templates\\1_11.jpg");
    	ChangeImageUtils.clockwise90(originFile, clockwise90File, "jpg");
    	
    	File anticlockwise90File = new File("E:\\IdeaWorke-space\\springbootprinttest\\src\\main\\resources\\templates\\1_12.jpg");
    	ChangeImageUtils.anticlockwise90(originFile, anticlockwise90File, "jpg");
    	
    	File clockwise180File = new File("E:\\IdeaWorke-space\\springbootprinttest\\src\\main\\resources\\templates\\1_13.jpg");
    	ChangeImageUtils.clockwise180(originFile, clockwise180File, "jpg");
    	
    	File flipHorizontallyFile = new File("E:\\IdeaWorke-space\\springbootprinttest\\src\\main\\resources\\templates\\1_14.jpg");
    	ChangeImageUtils.flipHorizontally(originFile, flipHorizontallyFile, "jpg");
    	
    	File flipVerticallyFile = new File("E:\\IdeaWorke-space\\springbootprinttest\\src\\main\\resources\\templates\\1_15.jpg");
    	ChangeImageUtils.flipVertically(originFile, flipVerticallyFile, "jpg");
    	
    	System.out.print("main");
    }
}


