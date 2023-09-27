package com.datachecker.hybridocr.model;

public class MRZModel {

//    "mrz":{"type":"ID","subtype":"<","country":"NLD","number":"SPECI2014","check_digit_document_number":"2","complement":"999999990<<<<<8",
//            "date_of_birth":"1965-03-10","check_digit_date_of_birth":"1","sex":"F","expiration_date":"2024-03-09","check_digit_expiration_date":"6",
//            "nationality":"NLD","optional":"<<<<<<<<<<<","check_digit_composite":"8","lname":"DE","lname_complement":"BRUIJN","fname":"WILLEKE",
//            "fname_complement":"LISELOTTE","mrz_type":"td1",
//            "raw_mrz":["I<NLDSPECI20142999999990<<<<<8","6503101F2403096NLD<<<<<<<<<<<8","DE<BRUIJN<<WILLEKE<LISELOTTE<<"],
//        "personal_number":"","check_digit_personal_number":"8","valid_number":true,"valid_date_of_birth":true,"valid_expiration_date":true,
//                "valid_composite":true,"valid_misc":true,"valid_score":100,"misc":"I","names":"WILLEKE LISELOTTE","surname":"DE BRUIJN","angle":0}}
    private String type;
    private String nationality;
    private String number;

    private String expiration_date;
    private String date_of_birth;
    private String personal_number;
    private String surname;
    private String names;
    private String sex;
    private String raw_mrz[];

    public String getDocumentType() {
        return type;
    }

    public void setDocumentType(String documentType) {
        this.type = documentType;
    }

    public String getNationalityCountryCode() {
        return nationality;
    }

    public void setNationalityCountryCode(String nationalityCountryCode) {
        this.nationality = nationalityCountryCode;
    }

    public String getDocumentNumber() {
        return number;
    }

    public void setDocumentNumber(String documentNumber) {
        this.number = documentNumber;
    }

    public String getExpiryDate() {
        return expiration_date;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiration_date = expiryDate;
    }

    public String getBirthdate() {
        return date_of_birth;
    }

    public void setBirthdate(String birthdate) {
        this.date_of_birth = birthdate;
    }

    public String getPersonalNumber() {
        return personal_number;
    }

    public void setPersonalNumber(String personalNumber) {
        this.personal_number = personalNumber;
    }

    public String getSurnames() {
        return surname;
    }

    public void setSurnames(String surnames) {
        this.surname = surnames;
    }

    public String getGivenNames() {
        return names;
    }

    public void setGivenNames(String givenNames) {
        this.names = givenNames;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String[] getRaw_mrz() {
        return raw_mrz;
    }

    public void setRaw_mrz(String[] raw_mrz) {
        this.raw_mrz = raw_mrz;
    }
}
