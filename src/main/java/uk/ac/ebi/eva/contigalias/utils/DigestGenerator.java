package uk.ac.ebi.eva.contigalias.utils;

import java.security.NoSuchAlgorithmException;

public abstract class DigestGenerator {
    public abstract String hash(String text) throws NoSuchAlgorithmException;
}
