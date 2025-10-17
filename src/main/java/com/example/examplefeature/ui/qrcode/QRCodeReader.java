package com.example.examplefeature.ui.qrcode;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Classe responsável por ler códigos QR de imagens utilizando a biblioteca ZXing.
 */
public class QRCodeReader {

    /**
     * Lê um código QR a partir de um ficheiro de imagem.
     *
     * @param file Ficheiro de imagem (.png, .jpg, etc.)
     * @return Texto decodificado do QR ou mensagem de erro.
     */
    public static String readQRCode(File file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();

        } catch (NotFoundException e) {
            return "Nenhum QR Code encontrado na imagem.";
        } catch (IOException e) {
            return "Erro ao ler o ficheiro: " + e.getMessage();
        } catch (Exception e) {
            return "Erro inesperado: " + e.getMessage();
        }
    }
}
