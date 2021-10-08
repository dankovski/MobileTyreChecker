package com.example.mobiletyrechecker;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.TensorFlowLite;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class model_dmg {

    Interpreter.Options tfliteOptions = new Interpreter.Options();
    private Interpreter tflite = null;
    private int imageSizeX = 640;
    private int imageSizeY = 640;
    private Context context;
    public int damage_number=0;

    public model_dmg(Context cont, String model_path) throws IOException {
        context=cont;
        tflite= loadModelFile(model_path);
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 640 * 640 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[640 * 640];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < 640; ++i) {
            for (int j = 0; j < 640; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF)-128)/128.0f);
                byteBuffer.putFloat((((val >> 8) & 0xFF)-128)/128.0f);
                byteBuffer.putFloat((((val) & 0xFF)-128)/128.0f);}
        }
        return byteBuffer;
    }

    public Bitmap get_output(Bitmap img){

        img=getResizedBitmap(img, imageSizeX, imageSizeY);
        float[][][] output1 = new float[1][10][4];
        float[][] output2 = new float[1][10];
        float[][] output3 = new float[1][10];
        float[] output4 = new float[1];
        Object[] inputs = {convertBitmapToByteBuffer(img)};
        Map<Integer, Object> outputs = new HashMap<>();
        outputs.put(0, output1);
        outputs.put(1, output2);
        outputs.put(2, output3);
        outputs.put(3, output4);
        tflite.runForMultipleInputsOutputs(inputs, outputs);

        damage_number=0;
        Mat img_mat=new Mat();
        Utils.bitmapToMat(img, img_mat);
        Rect rec = new Rect();
        for(int i=0; i<10; i++){
            if(output3[0][i]>0.5){
            damage_number++;
                rec.set(new double[]{640*output1[0][i][1],640*output1[0][i][0],
                        640*(output1[0][i][3]-output1[0][i][1]) , 640*(output1[0][i][2]-output1[0][i][0])
                });
                Imgproc.rectangle(img_mat, rec,new Scalar(255,0,0,255), 2);
                if(output2[0][i]==0.0) {
                    Imgproc.putText(img_mat, "uszkodzenie opony", new Point(640 * (output1[0][i][1]+(output1[0][i][3]-output1[0][i][1])/2)-95, 640 * output1[0][i][0] - 5), Imgproc.FONT_HERSHEY_SIMPLEX,
                            0.7, new Scalar(255, 0, 0,255), 2);
                }
                else{
                    Imgproc.putText(img_mat, "uszkodzenie felgi", new Point(640 * (output1[0][i][1]+(output1[0][i][3]-output1[0][i][1])/2)-90, 640 * output1[0][i][0] - 5), Imgproc.FONT_HERSHEY_SIMPLEX,
                            0.7, new Scalar(255, 0, 0,255), 2);

                }
            }
        }
        Utils.matToBitmap(img_mat, img);

        return img;
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

    private Interpreter loadModelFile(String model_path) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(model_path);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        Long startOffset = fileDescriptor.getStartOffset();
        Long declaredLength = fileDescriptor.getDeclaredLength();
        Interpreter.Options tfliteOptions = new Interpreter.Options();
        MappedByteBuffer mappedByteBuffer = fileChannel.map( FileChannel.MapMode.READ_ONLY,
                startOffset, declaredLength);
        Interpreter loadedModel = new Interpreter(mappedByteBuffer, tfliteOptions);
        return loadedModel;
    }


}


