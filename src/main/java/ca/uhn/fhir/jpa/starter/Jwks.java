package ca.uhn.fhir.jpa.starter;

/**
 * Jwks class for parsing jwks json
 */
public class Jwks {

    private RSAKey[] keys;

    public Jwks(RSAKey[] keys) {
        this.keys = keys;
    }

    public RSAKey[] getKeys() {
        return this.keys;
    }
}