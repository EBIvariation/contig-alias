package uk.ac.ebi.eva.contigalias.utils;

import org.webpki.jcs.JsonCanonicalizer;

import java.io.IOException;
import java.util.Optional;

public class SerializationService {

    /**
     * Return a serialized version of the input jsonString using the
     * RFC-8785, using the implementation provided by  cyberphone/json-canonicalization
     * (see on GitHub).
     * The jsonString should respect some strict format rules, for example:
     * should be delimited with '{ }', etc*/
    public Optional<String> serialize(String jsonString) throws IOException {
        JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(jsonString);
        String result = jsonCanonicalizer.getEncodedString();
        return Optional.of(result);
    }
}
