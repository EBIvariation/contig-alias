package uk.ac.ebi.eva.contigalias.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

public class MD5Digest {

    /**
     * Return the digest of the text using the MD5 algorithm*/
    public String hash(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(text.getBytes());
        byte[] digest = md.digest();
        String textHash = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return textHash.toLowerCase();
    }
}
