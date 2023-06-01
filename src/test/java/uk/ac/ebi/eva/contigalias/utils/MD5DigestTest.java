package uk.ac.ebi.eva.contigalias.utils;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MD5DigestTest {

    @Test
    void hash() throws NoSuchAlgorithmException {
        MD5Digest md5Digest = new MD5Digest();
        String toBeHashed = "AAA";
        String MD5Digest = "8880cd8c1fb402585779766f681b868b";
        assertEquals(MD5Digest,md5Digest.hash(toBeHashed));
    }
}