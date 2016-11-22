import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.sarxos.webcam.Webcam;
/***
 * @author Aaron Knobloch
 * @version 1.0
 */
public class UI_Thread {

	private static WebcamManager leftManager;
	private static WebcamManager rightManager;
	private static List<Webcam> availableCameras;
	// references for Webcam panels in GridLayout
	private static final String LEFT_CAMERA = "LEFT CAMERA";
	private static final String RIGHT_CAMERA = "RIGHT CAMERA";
	
	/**
	 * Name: main
	 * Desc: The main UI thread of the application.
	 * 
	 * @param args: N/A
	 */
	
	public static void main(String[] args) {
		
		// initialization
		leftManager = new WebcamManager();
		rightManager = new WebcamManager();
		availableCameras = Webcam.getWebcams();
		
		// Set up the main window
		JFrame mainWindow = new JFrame("Zeroth Person");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setLocationRelativeTo(null);
		
		// create logo
		JLabel logoLabel = getLogoLabel();
		
		// set up center display
		JLabel centerDisplay = new JLabel();
		centerDisplay.setLayout(new GridLayout(0,2));
		
		// TODO: Error handling for no webcams found.
		// TODO: add documentation for camDisplay panels in the LayoutGuide
		// add camera display panels
		JPanel leftCamDisplay = new JPanel();
		Component leftCameraPanel = leftManager.getPanel();
		leftCamDisplay.add(leftCameraPanel);
		
		JLabel rightCamDisplay = new JLabel();
		Component rightCameraPanel = rightManager.getPanel();
		rightCamDisplay.add(rightCameraPanel);
		
		// set up left controls
		JLabel leftControlDisplay = new JLabel();
		leftControlDisplay.setLayout(new BoxLayout(leftControlDisplay, BoxLayout.Y_AXIS));
		
		// set up right controls
		JLabel rightControlDisplay = new JLabel();
		rightControlDisplay.setLayout(new BoxLayout(rightControlDisplay, BoxLayout.Y_AXIS));
		
		// create drop down menu for cameras
		String[] cameraNames = new String[availableCameras.size()];
		// TODO: refresh button 
		for(int i = 0; i < cameraNames.length; i++) {
			cameraNames[i] = availableCameras.get(i).getName();
		}
		
		JComboBox<String> leftCameraList = new JComboBox<>(cameraNames);
		JComboBox<String> rightCameraList = new JComboBox<>(cameraNames);
		
		// add functionality to camera lists
		leftCameraList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				@SuppressWarnings("unchecked")
				JComboBox<String> focusBox = (JComboBox<String>) e.getSource();
				
				String selectedCamName = (String) focusBox.getSelectedItem();
				// find camera based on name
				Webcam selectedCamera = Webcam.getDefault();
				for(Webcam focusCam : availableCameras) {
					if(focusCam.getName().equals(selectedCamName)) {
						selectedCamera = focusCam;
					}
				}
				
				// TODO : Loading screen
				// TODO: Add check for if trying to "switch" to the camera that is already selected.
				leftManager.setCamera(selectedCamera);
				leftCamDisplay.add(leftManager.getPanel(), 0);
			}
		});
		
		// add camera lists to respective displays
		leftControlDisplay.add(leftCameraList);
		rightControlDisplay.add(rightCameraList);
		
		// add components to center display
		centerDisplay.add(leftCamDisplay);
		centerDisplay.add(rightCamDisplay);
		centerDisplay.add(leftControlDisplay);
		centerDisplay.add(rightControlDisplay);
		
		// add components to main frame
		mainWindow.add(logoLabel, BorderLayout.NORTH);
		mainWindow.add(centerDisplay, BorderLayout.CENTER);
		
		// pack to wrap contents, show the window
		mainWindow.pack();
		mainWindow.setVisible(true);
	}
	
	
	/**
	 * Name: getLogoLabel()
	 * Desc: Gets the logo from the resources folder, creates a JPanel containing
	 * 		 the logo and returns said panel.
	 * 
	 * @return JLabel: The label containing the Zeroth Person logo.
	 */
	
	private static JLabel getLogoLabel() {
		
		BufferedImage logo;
		try {
			logo = ImageIO.read(new File("res/Logo.png"));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not load logo.");
			logo = null;
		}
		ImageIcon logoIcon = new ImageIcon(logo);
		JLabel logoLabel = new JLabel(logoIcon);
		return logoLabel;
		
	}
	
}
