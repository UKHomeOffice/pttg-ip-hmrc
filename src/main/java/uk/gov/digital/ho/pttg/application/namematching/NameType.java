package uk.gov.digital.ho.pttg.application.namematching;

public enum NameType {
    FIRST, LAST, ALIAS;

    public boolean isA(NameType nameType) {
        return this.equals(nameType);
    }
}
