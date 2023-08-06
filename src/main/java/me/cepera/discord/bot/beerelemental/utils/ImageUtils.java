package me.cepera.discord.bot.beerelemental.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageUtils {

    public static BufferedImage readImage(byte[] imageContent) {
        try {
            return ImageIO.read(new ByteArrayInputStream(imageContent));
        }catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public static byte[] writeImagePng(BufferedImage image) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            return bos.toByteArray();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getMaxDimension(BufferedImage image) {
        if(image.getHeight() > image.getWidth()) {
            return image.getHeight();
        }else {
            return image.getWidth();
        }
    }

    public static BufferedImage setMaxDimension(BufferedImage image, int maxDimension) {
        int targetWidth;
        int targetHeight;
        if(image.getHeight() == image.getWidth()) {
            targetHeight = maxDimension;
            targetWidth = maxDimension;
        }else if(image.getHeight() > image.getWidth()) {
            targetHeight = maxDimension;
            targetWidth = (int) (1.0D * image.getWidth() * targetHeight / image.getHeight());
        }else {
            targetWidth = maxDimension;
            targetHeight = (int) (1.0D * image.getHeight() * targetWidth / image.getWidth());
        }

        Image scaledImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        return resizedImage;
    }

    public static BufferedImage getSubImage(BufferedImage image, int x, int y, int width, int height) {
        Image imagePart = image.getSubimage(x, y, width, height);

        BufferedImage subImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        subImage.getGraphics().drawImage(imagePart, 0, 0, null);

        return subImage;
    }

}
