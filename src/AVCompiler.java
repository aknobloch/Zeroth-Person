import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

/***
 * Responsible for compiling both the audio and video into side-by-side 
 * formatting and exporting the finished product.
 * 
 * @author Aaron Knobloch
 * @version 1.0
 *
 */

public class AVCompiler {

	private static WebcamManager leftManager; 
	private static WebcamManager rightManager;
	public volatile static boolean encoding = false;
	private static volatile long frameTime = 0;
	
	
	private AVCompiler() {
		// Prevents instantiation (AVCompiler is a static class)
	}
	
	/***
	 * Sets the camera manager that should be associated with the left video feed.
	 * @param inManager
	 * The left manager to be associated with the compiler.
	 */
	public static void setLeftCamera(WebcamManager inManager) {
		leftManager = inManager;
	}
	
	/***
	 * Sets the camera manager that should be associated with the right video feed.
	 * @param inManager
	 * The right manager to be associated with the compiler.
	 */
	public static void setRightCamera(WebcamManager inManager) {
		rightManager = inManager;
	}
	
	/***
	 * Begin compilation for this video. Launches a new thread that pulls frames
	 * from the left and right managers. Compilation terminates once the video
	 * managers are no longer recording and there are no more frames to process.
	 * 
	 * @throws IOException
	 * If the left and right managers have not been set.
	 */
	public static void beginCompilation() throws IOException {
		
		if(leftManager == null || rightManager == null) {
			
			System.err.println("Could not find managers. Did you set the left and right cameras?");
			throw new IOException();
			
		}
		
		AVRunnable compileTask = new AVRunnable();
		Thread compilationThread = new Thread(compileTask);
		compilationThread.start();
		
	}
	
	private static BufferedImage joinBufferedImage(BufferedImage img1,BufferedImage img2) {

        //do some calculate first
        int offset  = 5;
        int wid = img1.getWidth()+img2.getWidth()+offset;
        int height = Math.max(img1.getHeight(),img2.getHeight())+offset;
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, img1.getWidth()+offset, 0);
        g2.dispose();
        return newImage;
    }
	
	/***
	 * Task that handles grabbing frames from the managers and
	 * encoding them into a video file.
	 * 
	 * @author Aaron Knobloch
	 * @version 1.0
	 */
	static class AVRunnable implements Runnable {

		@Override
		public void run() {
			
			// the actual writer that takes frames and writes to video
			IMediaWriter encoder = ToolFactory.makeWriter("AVCompiler.mov");

			// adds a new stream to the output file
			Dimension resolutionFinder = leftManager.getCurrentResolution();
			encoder.addVideoStream(0, 0, resolutionFinder.width * 2 , resolutionFinder.height);
			
			// if either manager is not recording, wait for a second before starting
			if ( ! leftManager.isRecording() || ! rightManager.isRecording()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// while cameras are recording or still have images to process
			while( ( leftManager.hasImage() && rightManager.hasImage() ) || 
				   ( leftManager.isRecording() && rightManager.isRecording())) {
				
				// if the cameras are still recording, but there are no images, skip.
				if(leftManager.hasImage() && rightManager.hasImage() ) {
					
					// calculate increment time for frame. lets the compiler know where to place video
					long incrementTime = leftManager.getCaptureTime() / leftManager.getTotalFramesCaptured();
					
					// get the left and right camera frames
					BufferedImage leftImage = leftManager.popFrame();
					BufferedImage rightImage = rightManager.popFrame();
	
					// compile frames into side-by-side image
					BufferedImage sideBySideImage = joinBufferedImage(leftImage, rightImage);
	
					encoder.encodeVideo(0, sideBySideImage , frameTime, TimeUnit.MILLISECONDS);
					frameTime += incrementTime;
				}
				
			}
			
			encoder.close();
			System.out.println("Encoder closed.");
			
		}
	}
	
}
