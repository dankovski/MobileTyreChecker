package com.example.mobiletyrechecker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;
import org.opencv.dnn.Dnn;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class model_OCR {
    Net model_yolo1;
    Net model_yolo2;

    public model_OCR(Context cont) throws IOException {
        String cfg_path1 = "/storage/emulated/0/Android/data/com.example.mobiletyrechecker/models/yolov3_1.cfg";
        String weights_path1 = "/storage/emulated/0/Android/data/com.example.mobiletyrechecker/models/yolov3_1.weights";
        model_yolo1 = Dnn.readNetFromDarknet(cfg_path1, weights_path1);
        model_yolo1.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
        model_yolo1.setPreferableTarget(Dnn.DNN_TARGET_CPU);
        String cfg_path2 = "/storage/emulated/0/Android/data/com.example.mobiletyrechecker/models/yolov3_2.cfg";
        String weights_path2 = "/storage/emulated/0/Android/data/com.example.mobiletyrechecker/models/yolov3_2.weights";
        model_yolo2 = Dnn.readNetFromDarknet(cfg_path2, weights_path2);
        model_yolo2.setPreferableBackend(Dnn.DNN_BACKEND_OPENCV);
        model_yolo2.setPreferableTarget(Dnn.DNN_TARGET_CPU);
    }

    public List<Object[]> make_prediction(Bitmap bitmap, int number, Net model){
        Mat mat=new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB);
        Mat imageBlob = Dnn.blobFromImage(mat, 0.00392, new Size(416, 416),
                new Scalar(0, 0, 0),/*swapRB*/false, /*crop*/false);
        model.setInput(imageBlob);
        List<Mat> result = new ArrayList<Mat>();
        List<String> layer_names = model.getLayerNames();
        List<String> output_names = new ArrayList<>();
        int[] out_layers = new int[3];
        model.getUnconnectedOutLayers().get(0, 0, out_layers);
        for (int i = 0; i < 3; i++) {
            output_names.add(layer_names.get(out_layers[i] - 1));
        }
        model.forward(result, output_names);
        double confThreshold = 0.5;
        double nmsThreshold = 0.2;
        List<Integer> classIds = new ArrayList<>();
        List<Float> confs = new ArrayList<>();
        List<Rect> box = new ArrayList<>();
        for (int out_number = 0; out_number < result.size(); out_number++) {
            Mat level = result.get(out_number);
            for (int i = 0; i < level.rows(); i++) {
                Mat row = level.row(i);
                Mat scores = row.colRange(5, level.cols());
                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);
                Point classId = mm.maxLoc;
                float confidence = (float) mm.maxVal;
                if (confidence > confThreshold) {
                    int w = (int) (row.get(0,2)[0] * mat.width());
                   int h = (int) (row.get(0, 3)[0] * mat.height());
                    int x = (int) (row.get(0, 0)[0] * mat.width() - w / 2);
                    int y = (int) (row.get(0, 1)[0] * mat.height() - h / 2);
                    box.add(new Rect(x, y, w, h));
                    classIds.add((int) classId.x);
                    confs.add(confidence);
                }
            }
        }
        List<Object[]> predictions = new ArrayList<Object[]>();
        if(confs.size()>0) {
        MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));
        MatOfRect boxes = new MatOfRect(box.toArray(new Rect[0]));
        MatOfInt indices = new MatOfInt();
        Dnn.NMSBoxes(boxes, confidences, (float) confThreshold, (float) nmsThreshold, indices);
        int[] ind = indices.toArray();

        for (int i = 0; i < ind.length; i++) {
            predictions.add(new Object[]{classIds.get(ind[i]), box.get(ind[i]).x+number, box.get(ind[i]).y, box.get(ind[i]).width, box.get(ind[i]).height,confs.get(ind[i])});
        }
    }
return predictions;


    }

    public Bitmap cartesian_to_polar(Bitmap image, int x_center, int y_center, int r) {
        int width = (int) (2 * Math.PI * r);
        int height = (int) r;
        Bitmap image_polar = Bitmap.createBitmap(width, height/2, Bitmap.Config.ARGB_8888);
        for (int alfa = 0; alfa < width; alfa++) {
            float sinArg = (float) Math.sin(Math.PI * (alfa / (width / 360.0)) / 180.0);
            float cosArg = (float) Math.cos(Math.PI * (alfa / (width / 360.0)) / 180.0);
            for (int radius = 0; radius < height/2; radius++) {
                image_polar.setPixel(alfa, height/2 - radius - 1, image.getPixel((int) ((radius+height/2) * sinArg + x_center), (int) ((radius+height/2) * cosArg + y_center)));
            }
        }
        Mat img_mat = new Mat();
        Utils.bitmapToMat(image_polar, img_mat);
        Core.flip(img_mat, img_mat, 1);
        Utils.matToBitmap(img_mat, image_polar);
        return image_polar;
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight){
        int width = bm.getWidth() ;
        int height = bm.getHeight();
        float scaleWidth = (float)newWidth / width;
        float scaleHeight = (float)newHeight / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false ) ;
    }

   public List<Object[]> OCR(Bitmap image){

        image=getResizedBitmap(image, 3104, 3104);
        image = cartesian_to_polar(image, image.getWidth()/2, image.getHeight()/2, image.getWidth()/2);
        image=getResizedBitmap(image, 9750, 750);
        List<Object[]> predictions1 = predict1(image);
        List<Object[]> predictions2 = new ArrayList<>();
        List<Integer> symbol = new ArrayList<>();
        List<Object[]> result = new ArrayList<>();
        Bitmap img_parameter;
        for(int i=0; i<predictions1.size(); i++){

            if((int)predictions1.get(i)[1]+(int)predictions1.get(i)[3]<=image.getWidth()) {
                img_parameter = Bitmap.createBitmap(image, (int) predictions1.get(i)[1], (int) predictions1.get(i)[2], (int) predictions1.get(i)[3], (int) predictions1.get(i)[4]);
            }
            else{
                img_parameter = Bitmap.createBitmap(image, (int) predictions1.get(i)[1], (int) predictions1.get(i)[2], image.getWidth()-(int) predictions1.get(i)[1], (int) predictions1.get(i)[4]);
            }

            symbol.clear();
            symbol.add((int)predictions1.get(i)[0]);

            if((int)predictions1.get(i)[0]!=0 && (int)predictions1.get(i)[0]!=1 && (int)predictions1.get(i)[0]!=2
                    && (int)predictions1.get(i)[0]!=3) {
                predictions2 = predict2(img_parameter);
                for (int iter1 = 0; iter1 < predictions2.size() - 1; iter1++) {
                    for (int iter2 = iter1 + 1; iter2 < predictions2.size(); iter2++) {
                        if ((int) predictions2.get(iter2)[1] < (int) predictions2.get(iter1)[1]) {
                            Collections.swap(predictions2, iter2, iter1);}}}


                for (int k = 0; k < predictions2.size(); k++) {
                    symbol.add((int)predictions2.get(k)[0]);
                }
            }

        result.add(symbol.toArray());
        }

        return result;
    }

    public List<Object[]> predict1(Bitmap img_in){

        Bitmap imgBitmap;
        List<List<Object[]>> predictions = new ArrayList<>();
        for (int i=0; i<16; i++){
           imgBitmap = Bitmap.createBitmap(img_in, i*600, 0, 750, 750);
            predictions.add(make_prediction(imgBitmap, i*600, model_yolo1));
        }
        int label=0;
        float prob=0;
        boolean contains=false;
        List<Object[]> final_predictions = new ArrayList<>();
        long start= System.currentTimeMillis();
        for (int i=0; i<predictions.size(); i++){
            if(i==0) {
                for(int l=0; l<predictions.get(i).size(); l++){
                    final_predictions.add(new Object[]{
                            predictions.get(i).get(l)[0], predictions.get(i).get(l)[1],
                            predictions.get(i).get(l)[2], predictions.get(i).get(l)[3],
                            predictions.get(i).get(l)[4], predictions.get(i).get(l)[5]}
                    );
                }
            }
            else {
                for (int k=0;k<predictions.get(i).size();k++){
                    contains = false;
                    label = (int) predictions.get(i).get(k)[0];
                    prob = (float) predictions.get(i).get(k)[5];
                    for(int j=0;j<final_predictions.size(); j++){
                    if((int) final_predictions.get(j)[0] == label){
                        if((float)final_predictions.get(j)[5] < prob) {
                            final_predictions.add(new Object[]{
                                    predictions.get(i).get(k)[0], predictions.get(i).get(k)[1],
                                    predictions.get(i).get(k)[2], predictions.get(i).get(k)[3],
                                    predictions.get(i).get(k)[4], predictions.get(i).get(k)[5]});
                            final_predictions.remove(j);
                        }
                        contains=true;
                    }
                    }
                    if(!contains){

                        final_predictions.add(new Object[]{
                                predictions.get(i).get(k)[0], predictions.get(i).get(k)[1],
                                predictions.get(i).get(k)[2], predictions.get(i).get(k)[3],
                                predictions.get(i).get(k)[4], predictions.get(i).get(k)[5]}
                        );
                    }

                }
            }
         }
        for (int i=0; i<final_predictions.size();i++){
        }
        return final_predictions;
    }

    public List<Object[]> predict2(Bitmap img){
        List<Object[]> predictions;
        predictions = make_prediction(img, 0, model_yolo2);
        return predictions;
    }


}