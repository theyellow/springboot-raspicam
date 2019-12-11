package io.github.theyellow.springbootraspicam;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

import uk.co.caprica.picam.CameraConfiguration;
import uk.co.caprica.picam.NativeLibraryException;
import uk.co.caprica.picam.PicamNativeLibrary;
import uk.co.caprica.picam.app.Environment;
import uk.co.caprica.picam.enums.Encoding;

@Controller
public class CameraController {

	private static final int DEFAULT_VIDEO_HEIGHT = 480;

	private static final int DEFAULT_VIDEO_WIDTH = 640;

	private static final String DEFAULT_VIDEO_NAME = "video.mp4";

	private static final int DEFAULT_LENGTH_IN_MILLIS = 20000;

	private final Logger logger = LoggerFactory.getLogger(CameraController.class);

	private final CameraConfiguration configuration;

	private static final Semaphore cameraUse = new Semaphore(1);

	public CameraController() {
		Environment.dumpEnvironment();

		try {
			System.out.printf("Installed native library to %s%n%n", PicamNativeLibrary.installTempLibrary());
		} catch (NativeLibraryException e) {
			logger.error("Not native running, failed to install library for cam", e);
		}

		configuration = CameraConfiguration.cameraConfiguration();
		defaultConfiguration(640, 480);
	}

	public CameraConfiguration configuration() {
		return configuration;
	}

	public CameraConfiguration defaultConfiguration(Integer width, Integer height) {
// 		@formatter:off
		return configuration()
		        .width(width)
		        .height(height)
		        .encoding(Encoding.JPEG)
		        .quality(85)
		        .captureTimeout(10000)
//		        .brightness(50)
//		        .contrast(-30)
//		        .saturation(80)
//		        .sharpness(100)
//		        .stabiliseVideo()
//		        .shutterSpeed(10)
//		        .iso(4)
//		        .exposureMode(ExposureMode.FIREWORKS)
//		        .exposureMeteringMode(ExposureMeteringMode.BACKLIT)
//		        .exposureCompensation(5)
//		        .dynamicRangeCompressionStrength(DynamicRangeCompressionStrength.MAX)
//		        .automaticWhiteBalance(AutomaticWhiteBalanceMode.FLUORESCENT)
//		        .imageEffect(ImageEffect.SKETCH)
//		        .flipHorizontally()
//		        .flipVertically()
//		        .rotation(rotation)
//		        .crop(0.25f, 0.25f, 0.5f, 0.5f)
	        ;
// 		@formatter:on
	}

	public File recordVideo(String name, Integer width, Integer height, Integer lengthInMillis)
			throws FailedToRunRaspistillException, IOException, InterruptedException {

		boolean acquired = cameraUse.tryAcquire(10, TimeUnit.SECONDS);

		if (acquired) {
			RPiCamera rPiCamera = new RPiCamera("~");
			File recordVideo = rPiCamera.recordVideo(name, width, height, lengthInMillis, true);
			cameraUse.release();
			logger.info("recorded video {} with width {} and height {} and a length of {} successfully",
					recordVideo.getAbsolutePath(), width, height, lengthInMillis);

			return recordVideo;
		} else {
			logger.warn("Could not get camera within the last 10 seconds exclusively, so quit and return null");
			return null;
		}
	}

	/**
	 * Convenience-method for test-video with default name "video.mp4" placed under
	 * ~/ <br/>
	 * <br/>
	 * HEIGHT,WIDTH and LENGTH (in millis) are also standard values (640, 480,
	 * 20000).
	 *
	 * @return file-handle with test-video
	 * 
	 * @throws FailedToRunRaspistillException
	 *             raspivid-exception
	 * @throws IOException
	 *             problems with IO
	 * @throws InterruptedException
	 *             problems with synchronising camera resource internally
	 */
	public File recordVideo() throws FailedToRunRaspistillException, IOException, InterruptedException {
		return recordVideo(DEFAULT_VIDEO_NAME, DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT, DEFAULT_LENGTH_IN_MILLIS);
	}

	/**
	 * 
	 * Record video with a given name and length in millis. Resolution will be
	 * default (640, 480) normally
	 * 
	 * @param name
	 *            name of video
	 * @param length
	 *            length of video (in millis)
	 * 
	 * @return file-handle with test-video
	 * 
	 * @throws FailedToRunRaspistillException
	 *             raspivid-exception
	 * @throws IOException
	 *             problems with IO
	 * @throws InterruptedException
	 *             problems with synchronising camera resource internally
	 */
	public File recordVideo(String name, Integer length)
			throws FailedToRunRaspistillException, IOException, InterruptedException {
		return recordVideo(name, DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT, length);
	}

	public File recordVideo(Integer length) throws FailedToRunRaspistillException, IOException, InterruptedException {
		return recordVideo(DEFAULT_VIDEO_NAME, DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT, length);
	}

	public File recordVideo(String name, Integer width, Integer height)
			throws FailedToRunRaspistillException, IOException, InterruptedException {
		return recordVideo(name, width, height, DEFAULT_LENGTH_IN_MILLIS);
	}

	public File takePicture(String name, Integer width, Integer height, Integer quality) {

		return null;
	}

	public boolean startPictureTaker() {

		return false;
	}

	public boolean stopPictureTaker() {

		return false;
	}

}
