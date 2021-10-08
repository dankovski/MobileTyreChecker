package com.example.mobiletyrechecker;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

public class Model {

    protected Interpreter tflite = null;
    protected int imageSizeX = 640;
    protected int imageSizeY = 640;
    protected Context context;

    public Model(Context cont, String model_path) throws IOException {
    context=cont;
    tflite=loadModelFile(model_path);
    }

    public float[] get_output(Bitmap img){ img=getResizedBitmap(img, imageSizeX, imageSizeY);
    float[][] output = new float[1][2];
    tflite.run(convertBitmapToByteBuffer(img), output);
    return output[0];
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

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 640 * 640 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[640 * 640];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(),
                0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < 640; ++i) {
            for (int j = 0; j < 640; ++j) {
                final int val = intValues[pixel++];
                  byteBuffer.putFloat(((val >> 16) & 0xFF));
                  byteBuffer.putFloat(((val >> 8) & 0xFF));
                  byteBuffer.putFloat(((val) & 0xFF));
            }
        }
        return byteBuffer;
    }



}
