import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;
import com.github.sarxos.webcam.WebcamPanel;

/***
 * @author Aaron Knobloch
 * @version 1.1
 */
public class WebcamManager {

	private Webcam camera;
	private WebcamPanel cameraPanel;
	private final Dimension DEFAULT_SIZE = new Dimension(320,240);
	private LinkedList<BufferedImage> framesCaptured;
	private long totalFramesCaptured = 0;
	private volatile boolean recording;
	private long recordStartTimeMillis;
	private long captureTime;
	
	/***
	 * The WebcamManager object is responsible for controlling webcam
	 * I/O data, recording from cameras and managing the display panels
	 * of the webcam.
	 */
	public WebcamManager() {
		
		try {
			
			camera = Webcam.getDefault();
			
		} catch(WebcamException we) {
			System.err.println("Could not find webcam.");
			throw we;
		}
		
		cameraPanel = new WebcamPanel(camera, DEFAULT_SIZE, true);
		cameraPanel.setFPSDisplayed(true);
		
		framesCaptured = new LinkedList<>();
		recording = false;
		
	}
	
	/***
	 * The WebcamManager object is responsible for controlling webcam
	 * I/O data, recording from cameras and managing the display panels
	 * of the webcam.
	 * @param inCam
	 * Specifies the camera to be associated with this instance.
	 */
	public WebcamManager(Webcam inCam) {
		
		camera = inCam;
		camera.setViewSize(new Dimension(320, 240));
		
		cameraPanel = new WebcamPanel(camera, DEFAULT_SIZE, true);
		cameraPanel.setFPSDisplayed(true);
		
		framesCaptured = new LinkedList<>();
		recording = false;
		
	}
	
	/***
	 * @return 
	 * Returns the WebcamPanel associated with this instance, in the form
	 * of a Swing component. 
	 */
	public Component getPanel() {
		
		return this.cameraPanel;
	}
	
	/***
	 * Sets the resolution for the capture of the camera device. This does not effect 
	 * the view size displayed by the WebcamPanel associated with this instance. 
	 * <br><br>
	 * Note:
	 * After calling this method, the WebcamPanel must be retrieved and
	 * re-added to the parent container. In addition, the parent container must 
	 * be blocked from all UI interactions in order to ensure the end user does 
	 * not attempt to press other UI elements.
	 * 
	 * @param inDimension
	 * The new dimension to add. The dimension must be a resolution supported by 
	 * the camera associated with this instance.
	 */
	public void setCameraResolution(Dimension inDimension) {
		
		// Set custom view sizes only accepts an array of Dimensions.
		Dimension[] dimensionArray = {inDimension};
		
		camera.close();
		cameraPanel.setVisible(false);
		camera.setCustomViewSizes(dimensionArray);
		camera.setViewSize(inDimension);
		camera.open();
		
		cameraPanel = new WebcamPanel(camera, DEFAULT_SIZE, true);
		
	}
	
	/***
	 * @return
	 * The current recording resolution of the camera associated with this instance.
	 */
	public Dimension getCurrentResolution() {
		return camera.getViewSize();
	}
	
	/***
	 * @return
	 * The resolutions that this camera has been configured to. To add additional 
	 * resolutions, you must invoke the setCameraResolution method.
	 */
	public Dimension[] getAvailableResolutions() {
		
		ArrayList<Dimension> allResolutions = new ArrayList<>();
		allResolutions.addAll(Arrays.asList(camera.getViewSizes()));
		allResolutions.addAll(Arrays.asList(camera.getCustomViewSizes()));
		
		return allResolutions.toArray(new Dimension[allResolutions.size()]);
		 
	}
	
	/***
	 * Launches a new thread that begins to record from the webcam 
	 * associated with this instance.
	 * @throws IOException
	 * If the camera is not currently open. 
	 */
	public void startRecording() throws IOException {
		
		// If the camera is open, recording is good to go
		if(camera.isOpen()) {
			recording = true;
		} else {
			throw new IOException();
		}
		
		recordStartTimeMillis = System.currentTimeMillis();
		Thread recordingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				
				while(recording) {
					
					framesCaptured.add(camera.getImage());
					totalFramesCaptured++;
					captureTime = System.currentTimeMillis() - recordStartTimeMillis;
					
				}
				
			}
		});
		
		recordingThread.start();
		
	}
	
	/***
	 * Stops recording from the camera associated with this instance.
	 */
	public LinkedList<BufferedImage> stopRecording() {
		recording = false;
		return framesCaptured;
	}
	
	/***
	 * @return
	 * Removes and returns the head of the queue of frames stored by this instance.
	 * @throws
	 * NoSuchElementException If the queue is empty
	 */
	public BufferedImage popFrame() {
		
		try {
			
			return framesCaptured.pop();
			
		} catch(NoSuchElementException nsee) {
			throw nsee;
		}
	}
	
	/***
	 * @return
	 * True if this object's storage has at least one image contained.
	 */
	
	public boolean hasImage() {
		
		return ! framesCaptured.isEmpty();
		
	}
	
	/***
	 * @return
	 * The name of the camera that this instance is associated with.
	 */
	public String getCameraName() {
		
		return camera.getName();
		
	}
	
	/***
	 * @return
	 * A list containing all the available webcams available to connect to.
	 */
	public static List<Webcam> getAvailableCameras() {
		
		return Webcam.getWebcams();
		
	}
	
	/***
	 * Changes the camera to which is object is associated. 
	 * <br><br>
	 * Note:
	 * After calling this method, the WebcamPanel must be retrieved and
	 * re-added to the parent container. In addition, the parent container must 
	 * be blocked from all UI interactions in order to ensure the end user does 
	 * not attempt to press other UI elements.
	 * @param inCamera
	 * The new camera to be associated with this instance.
	 */
	public void setCamera(Webcam inCamera) {
		
		camera.close();
		cameraPanel.setVisible(false);
		
		camera = inCamera;
		cameraPanel = new WebcamPanel(camera);
		
	}
		
	/***
	 * Enables the WebcamPanel associated with this instance to display 
	 * FPS, the repaints per second information and the resolution 
	 * that the camera is recording at.
	 */
	public void showDebugInfo() {
		
		cameraPanel.setFPSDisplayed(true);
		cameraPanel.setDisplayDebugInfo(true);
		cameraPanel.setImageSizeDisplayed(true);
		
	}
	
	/***
	 * Take a picture with the currently active webcam and saves it in the local directory
	 * as a .PNG file.
	 * @param fileName
	 * The name of the .PNG file saved by this method.
	 */
	public void quickPic(String fileName) {
		
		try {
			
			ImageIO.write(camera.getImage(), "PNG", new File(fileName + ".png"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/***
	 * @return
	 * Grabs a frame from the camera associated with this instance and returns it.
	 */
	public BufferedImage getImage() {
		
		return camera.getImage();
		
	}
	
	/***
	 * @return
	 * The length of time that the video was recorded, in milliseconds.
	 */
	public long getCaptureTime() {
		return captureTime;
	}
	
	/***
	 * @return
	 * The total number of frames captured by this thread.
	 * Note - This does not indicate the frames still left to be processed.
	 */
	public long getTotalFramesCaptured() {
		return totalFramesCaptured;
	}
	
	/***
	 * @return
	 * True if the camera is record, false otherwise.
	 */
	public boolean isRecording() {
		return recording;
	}
	
}
