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

import uk.co.caprica.picam.Camera;
import uk.co.caprica.picam.CameraConfiguration;
import uk.co.caprica.picam.CameraException;
import uk.co.caprica.picam.CaptureFailedException;
import uk.co.caprica.picam.FilePictureCaptureHandler;
import uk.co.caprica.picam.NativeLibraryException;
import uk.co.caprica.picam.PicamNativeLibrary;
import uk.co.caprica.picam.PictureCaptureHandler;
import uk.co.caprica.picam.app.Environment;
import uk.co.caprica.picam.enums.Encoding;

@Controller
public class CameraController {

	private static final int DEFAULT_PICTURE_HEIGHT = 1080;
	private static final int DEFAULT_PICTURE_WIDTH = 1920;
	private static final Encoding DEFAULT_PICTURE_ENCODING = Encoding.JPEG;
	private static final String DEFAULT_PICTURE_NAME = "photo.jpg";
	private static final long DEFAULT_PICTURE_TIMEOUT_IN_MILLIS = 1000;
	private static final int DEFAULT_PICTURE_QUALITY = 100;

	private static final int DEFAULT_VIDEO_HEIGHT = 480;
	private static final int DEFAULT_VIDEO_WIDTH = 640;
	private static final String DEFAULT_VIDEO_NAME = "video.mp4";
	private static final int DEFAULT_VIDEOLENGTH_IN_MILLIS = 20000;

	private final Logger logger = LoggerFactory.getLogger(CameraController.class);

	private final CameraConfiguration configuration;

	private static final Semaphore cameraUse = new Semaphore(1);

	public CameraController() {
		Environment.dumpEnvironment();

		try {
			logger.info("Installed native library to {}", PicamNativeLibrary.installTempLibrary());
		} catch (NativeLibraryException e) {
			logger.error("Not native running, failed to install library for cam", e);
		}

		configuration = CameraConfiguration.cameraConfiguration();
		defaultConfiguration(DEFAULT_PICTURE_WIDTH, DEFAULT_PICTURE_HEIGHT);
	}

	public CameraConfiguration configuration() {
		return configuration;
	}

	public CameraConfiguration defaultConfiguration(Integer width, Integer height) {
		// @formatter:off
		return configuration()
		        .width(width)
		        .height(height)
		        .encoding(DEFAULT_PICTURE_ENCODING)
		        .quality(DEFAULT_PICTURE_QUALITY)
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
		// @formatter:on
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
		return recordVideo(DEFAULT_VIDEO_NAME, DEFAULT_VIDEO_WIDTH, DEFAULT_VIDEO_HEIGHT,
				DEFAULT_VIDEOLENGTH_IN_MILLIS);
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
		return recordVideo(name, width, height, DEFAULT_VIDEOLENGTH_IN_MILLIS);
	}

	public <T> T takePicture(Integer width, Integer height, Integer quality, long timeout, Encoding encoding,
			PictureCaptureHandler<T> pictureCaptureHandler)
			throws CaptureFailedException, InterruptedException {
		T picture = null;

		// @formatter:off
		configuration()
			.width(width)
			.height(height)
		 // .automaticWhiteBalanceMode(AutomaticWhiteBalanceMode.AUTO)
			.encoding(encoding)
			.quality(quality);
		// @formatter:on

		boolean acquiredCamera = cameraUse.tryAcquire(timeout, TimeUnit.SECONDS);
		if (acquiredCamera) {
			try (Camera camera = new Camera(configuration())) {
				if (camera.open()) {
					picture = camera.takePicture(pictureCaptureHandler);
					camera.close();
				}
			} catch (CameraException e) {
				logger.error("Could not get camera for takePicture(...)", e);
			}
			cameraUse.release();
			logger.info("Shot photo '{}' with following parameters {}", picture, width, height, quality, encoding);
		} else {
			logger.error("Could not acquire camera for taking picture within {} seconds, returning null");
		}
		return picture;
	}

	public <T> T takePicture(Integer width, Integer height, Integer quality, long timeout,
			PictureCaptureHandler<T> pictureCaptureHandler)
			throws CaptureFailedException, InterruptedException {
		if (!DEFAULT_PICTURE_ENCODING.equals(configuration.encoding())) {
			logger.warn("changing camera to default encoding, it was {}", configuration.encoding());
		}
		return takePicture(width, height, quality, timeout, DEFAULT_PICTURE_ENCODING, pictureCaptureHandler);

	}

	public File takePicture(String name, Integer width, Integer height, Integer quality, long timeout,
			Encoding encoding) throws CaptureFailedException, InterruptedException {
		File file = new File(name);
		PictureCaptureHandler<File> pictureCaptureHandler = new FilePictureCaptureHandler(file);
		return takePicture(width, height, quality, timeout, encoding, pictureCaptureHandler);
	}

	public File takePicture(String name, Integer width, Integer height, Integer quality, long timeout)
			throws CaptureFailedException, InterruptedException {
		if (!DEFAULT_PICTURE_ENCODING.equals(configuration.encoding())) {
			logger.warn("changing camera to default encoding, it was {}", configuration.encoding());
		}
		return takePicture(name, width, height, quality, timeout, DEFAULT_PICTURE_ENCODING);
	}

	public File takePicture(String name, Integer width, Integer height, Integer quality, Encoding encoding)
			throws CaptureFailedException, InterruptedException {
		return takePicture(name, width, height, quality, DEFAULT_PICTURE_TIMEOUT_IN_MILLIS, encoding);
	}

	public File takePicture(String name, Integer width, Integer height, Integer quality)
			throws CaptureFailedException, InterruptedException {
		if (!DEFAULT_PICTURE_ENCODING.equals(configuration.encoding())) {
			logger.warn("changing camera to default encoding, it was {}", configuration.encoding());
		}
		return takePicture(name, width, height, quality, DEFAULT_PICTURE_ENCODING);
	}

	public File takePicture(String name, Integer width, Integer height, Encoding encoding)
			throws CaptureFailedException, InterruptedException {
		return takePicture(name, width, height, DEFAULT_PICTURE_QUALITY, DEFAULT_PICTURE_TIMEOUT_IN_MILLIS, encoding);
	}

	public File takePicture(String name, Integer width, Integer height)
			throws CaptureFailedException, InterruptedException {
		return takePicture(name, width, height, DEFAULT_PICTURE_ENCODING);
	}

	public File takePicture(String name, Encoding encoding)
			throws CaptureFailedException, InterruptedException {
		return takePicture(name, DEFAULT_PICTURE_WIDTH, DEFAULT_PICTURE_HEIGHT, DEFAULT_PICTURE_QUALITY,
				DEFAULT_PICTURE_TIMEOUT_IN_MILLIS, encoding);
	}

	public File takePicture(String name) throws CaptureFailedException, InterruptedException {
		return takePicture(name, DEFAULT_PICTURE_ENCODING);
	}

	public File takePicture(Encoding encoding) throws CaptureFailedException, InterruptedException {
		return takePicture(DEFAULT_PICTURE_NAME, encoding);
	}

	public File takePicture() throws CaptureFailedException, InterruptedException {
		return takePicture(DEFAULT_PICTURE_ENCODING);
	}


	public boolean startPictureTaker() {

		return false;
	}

	public boolean stopPictureTaker() {

		return false;
	}

}
