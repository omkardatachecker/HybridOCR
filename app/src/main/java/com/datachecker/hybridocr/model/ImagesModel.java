package com.datachecker.hybridocr.model;

import androidx.lifecycle.MutableLiveData;

public class ImagesModel extends MutableLiveData<ImagesModel> {
//    var documentType:String
//    var nationalityCountryCode:String
//    var documentNumber:String
//    var expiryDate:Date
//    var birthdate:Date
//    var personalNumber:String
//    var surnames:String
//    var givenNames:String
//    var sex:String
//    var rawMRZ:String
//    var images:[UIImage]
//    var imageRotateAngle:CGFloat


    private MRZModel mrz;
    private String images[];

    private Float imageRotateAngle;

    public String[] getImages() {
        return images;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public Float getImageRotateAngle() {
        return imageRotateAngle;
    }

    public void setImageRotateAngle(Float imageRotateAngle) {
        this.imageRotateAngle = imageRotateAngle;
    }

    public MRZModel getMrz() {
        return mrz;
    }

    public void setMrz(MRZModel mrz) {
        this.mrz = mrz;
    }
}
