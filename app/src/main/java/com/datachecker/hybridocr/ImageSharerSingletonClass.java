package com.datachecker.hybridocr;

import com.datachecker.hybridocrlib.model.ImagesModel;

public class ImageSharerSingletonClass {
    private static ImageSharerSingletonClass sharedInstance = null;

    private ImagesModel imagesModel;

    private ImageSharerSingletonClass() {

    }

    public static synchronized ImageSharerSingletonClass getInstance(){
        if(sharedInstance == null) {
            sharedInstance = new ImageSharerSingletonClass();
        }
//        sharedInstance.imagesModel = imagesModel;
        return sharedInstance;
    }

    public ImagesModel getImagesModel() {
        return imagesModel;
    }

    public void setImagesModel(ImagesModel imagesModel) {
        this.imagesModel = imagesModel;
    }
}
