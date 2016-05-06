/** 
 *  
 * @author	xuxl
 * @email	leoxuxl@163.com
 * @version  
 *     1.0 2016年1月18日 上午9:33:35 
 */
package com.smartdevicesdk.cscreen;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.smartdevicesdk.utils.ThumbnailUtils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.serialport.api.SerialPort;
import android.serialport.api.SerialPortDataReceived;
import android.util.Log;

/**
 * This class is used for :
 * 
 * @author xuxl
 * @email leoxuxl@163.com
 * @version 1.0 2016年1月18日 上午9:33:35
 */
public class CustomerScreenHelper {
	protected static final String TAG = "CustomerScreenHelper";
	private SerialPort mSerialPort = null;
	String device;
	int baudrate = 115200;
	
	int screenWidth=320;
	int screenHeight=240;

	public CustomerScreenHelper(String device, int baudrate) {
		this.device = device;
		this.baudrate = baudrate;
		mSerialPort = new SerialPort();

		mSerialPort.setOnserialportDataReceived(new SerialPortDataReceived() {
			@Override
			public void onDataReceivedListener(byte[] buffer, int size) {
				if (size > 0) {
					
				}
			}
		});
	}

	public boolean open() {
		if (mSerialPort.open(device, baudrate)) {
			return true;
		} else {
			Log.e(TAG, device + " open error");
		}
		return false;
	}

	public boolean close() {
		if (mSerialPort != null) {
			return mSerialPort.closePort();
		} else {
			return false;
		}
	}

	
	/** 
     * 把Bitmap转Byte 
     * @Author HEH 
     * @EditTime 2010-07-19 上午11:45:56 
     */  
    private static byte[] Bitmap2Bytes(Bitmap bitmap){  
    	int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        byte[] byteArray = byteBuffer.array();
        return byteArray;
    }  
    
    public void openBackLight(byte btFlg)
    {
    	if(mSerialPort==null||!mSerialPort.isOpen)
		{
			Log.e(TAG, "device is null or closed");
			return;
		}
    	byte[] btBlackLight=new byte[]{0x1b, 0x70, btFlg};
    	mSerialPort.WriteNoTheep(btBlackLight);
    }
    
    /*
	    功能:窗口显示RGB565图片数据.
	    返回:操作成功返回0,反之返回1;
	    指令:1b 73 c1 c2 c3 c4 c5 c6 c7 c8 d1 d2 d3 ... dk
	    说明:1b 73                   -- 命令头  
	         c1 c2 c3 c4 c5 c6 c7 c8 -- x轴起始坐标=c0*256+c1.  st7789v_pixel_zone[0]=c1  st7789v_pixel_zone[1]=c2   
	                                    x轴终止坐标=c2*256+c3.  st7789v_pixel_zone[2]=c3  st7789v_pixel_zone[3]=c4    
	                                    y轴起始坐标=c4*256+c5.  st7789v_pixel_zone[4]=c5  st7789v_pixel_zone[5]=c6    
	                                    y轴终止坐标=c6*256+c7.  st7789v_pixel_zone[6]=c7  st7789v_pixel_zone[7]=c8    
	         d1 d2 d3 ... dk         -- dk=((c3*256+c4)-(c1*256+c2))*((c6*256+c7)-(c4*256+c5))           
	                                    共dk个字节的RGB565彩图数据  
    */ 
    public boolean ShowRGB565Image(Bitmap bitmapSrc){
		if(mSerialPort==null||!mSerialPort.isOpen)
		{
			Log.e(TAG, "device is null or closed");
			return false;
		}

		if(bitmapSrc.getWidth()>screenHeight||bitmapSrc.getHeight()>screenWidth)
		{
			bitmapSrc=ThumbnailUtils.extractThumbnail(bitmapSrc, screenWidth, screenHeight);
		}
		
		Matrix matrix=new Matrix(); 
		matrix.setRotate(90);
		
		Paint paint=new Paint();
		Bitmap bitmap=Bitmap.createBitmap(screenHeight, screenWidth, Config.RGB_565);
		Canvas canvas=new Canvas(bitmap);
		canvas.drawBitmap(bitmapSrc, matrix, paint);
		
		int x0=0;
		int y0=0;
		
		int x1=240;
		int y1=320;
		
		byte[] x0Array=new byte[2];
		x0Array[1]=(byte)(x0&0xff); //获得低位字节
		x0Array[0]=(byte)(x0>>>8);//获得高位字节
		
		byte[] x1Array=new byte[2];
		x1Array[1]=(byte)(x1&0xff); //获得低位字节
		x1Array[0]=(byte)(x1>>>8);//获得高位字节
		
		byte[] y0Array=new byte[2];
		y0Array[1]=(byte)(y0&0xff); //获得低位字节
		y0Array[0]=(byte)(y0>>>8);//获得高位字节
		
		byte[] y1Array=new byte[2];
		y1Array[1]=(byte)(y1&0xff); //获得低位字节
		y1Array[0]=(byte)(y1>>>8);//获得高位字节
		
		byte[] imageArray=Bitmap2Bytes(bitmap);
		
		//命令头
		mSerialPort.WriteNoTheep(new byte[]{0x1b,0x73});
		
		mSerialPort.WriteNoTheep(x0Array);
		mSerialPort.WriteNoTheep(x1Array);
		
		mSerialPort.WriteNoTheep(y0Array);
		mSerialPort.WriteNoTheep(y1Array);
		
		mSerialPort.WriteNoTheep(imageArray);
		
		return true;
	}
    

	/*
	功能:全屏显示RGB2色图片数据.
	返回:操作成功返回0,反之返回1;
	指令:1b 72 25 80 c1 c2 c3 c4 d1 d2 d3 ... d9600
	说明:1b 72 25 80        -- 命令头  
	     c1 c2 c3 c4        -- ST7789V_RGB2Color[0]=c1*256+c2 ST7789V_RGB2Color[0]为RGB565的背景色
	                           ST7789V_RGB2Color[1]=c3*256+c4 ST7789V_RGB2Color[1]为RGB565的前景色  
	     d1 d2 d3 ... d9600 -- 共9600个字节的RGB2色图片数据
	*/
	public boolean ShowDotImage(int BackColor,int ForeColor,Bitmap bitmapSrc){
		if(mSerialPort==null||!mSerialPort.isOpen)
		{
			Log.e(TAG, "device is null or closed");
			return false;
		}

		if(bitmapSrc.getWidth()>screenHeight||bitmapSrc.getHeight()>screenWidth)
		{
			bitmapSrc=ThumbnailUtils.extractThumbnail(bitmapSrc, screenWidth, screenHeight);
		}
		
		Matrix matrix=new Matrix(); 
		matrix.setRotate(90);
		Bitmap bitmap=Bitmap.createBitmap(bitmapSrc,0,0,screenWidth, screenHeight,matrix,true);
		
		byte[] backColorArray=new byte[2];
		backColorArray[1]=(byte)(BackColor&0xff); //获得低位字节
		backColorArray[0]=(byte)(BackColor>>>8);//获得高位字节
		
		byte[] foreColorArray=new byte[2];
		foreColorArray[1]=(byte)(ForeColor&0xff); //获得低位字节
		foreColorArray[0]=(byte)(ForeColor>>>8);//获得高位字节
		
		Bitmap bm=bitmap.copy(Config.RGB_565, false);
		
		byte[] imageArray=BitmapLib.getBitmapData(bitmap);
		
		//命令头
		mSerialPort.WriteNoTheep(new byte[]{0x1b,0x72,0x25,(byte) 0x80});
		//背景色
		mSerialPort.WriteNoTheep(backColorArray);
		//前景色
		mSerialPort.WriteNoTheep(foreColorArray);
		
		mSerialPort.WriteNoTheep(imageArray);
		
		return true;
	}
}
