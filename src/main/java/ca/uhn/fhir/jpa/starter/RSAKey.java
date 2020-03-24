package ca.uhn.fhir.jpa.starter;

/**
 * RSA Key class for parsing jwks json
 */
public class RSAKey {

    private String e;
    private String n;
    private String alg;
    private String kid;
    private String kty;
    private String use;

    public RSAKey(String e, String n, String alg, String kid, String kty, String use) {
        this.e = e;
        this.n = n;
        this.alg = alg;
        this.kid = kid;
        this.kty = kty;
        this.use = use;
    }

    public String getExponent() {
        return this.e;
    }

    public String getModulus() {
        return this.n;
    }

    public String getAlgorithm() {
        return this.alg;
    }

    public String getKeyId() {
        return this.kid;
    }

    public String getKeyClass() {
        return this.kty;
    }

    public String getUse() {
        return this.use;
    }
}