package me.cepera.discord.bot.beerelemental.utils;

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

}
