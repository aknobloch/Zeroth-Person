# Zeroth-Person
Created : March 2016

<i> This project was made obsolete by hardware devices such as Ricoh Theta and Gear 360 </i>

Zeroth Person was a GUI based program I created for my undergraduate research project. It is the software for a patented virtual reality media creation device, and the project that placed first in the Consortium for Computing Sciences in Colleges: Southeastern Regional undergraduate research competition. The purpose of the software and project was to put virtual reality media creation in the hands of the common household. 

The software uses two webcams (either USB connected, internal or both) and microphones to capture stereoscopic video and audio from the cameras. It attempts to synchronize the two webcameras and the audio by first opening streams of data for the input devices. After verifying that all streams are open and feeding data, the user is given the option to begin capture. It then attempts to start the capture in seperate threads, all on the same CPU cycle. Through all testing done, this ensures no noticeable delay for the capture from all input streams. This is important due to the sickness that can be caused by even the smallest fraction of delay between two stereoscopic video streams. In addition, the software attempts to record at 30 FPS. This is done to ensure that the framerates for the cameras are synchronized as well in the event that the user is utilizing two different brands of cameras. 

The video is then exported in a SBS format ready for viewing through a Google Cardboard, ViewMaster VR or other similar phone-enabled VR consumption device.

This project was cut short, and while it is mostly functional there are a few bugs and functional requirements that need to be addressed. These are noted in the Issues section.
