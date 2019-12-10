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

	private final Logger logger = LoggerFactory.getLogger(CameraController.class);

	private final CameraConfiguration configuration;

	private final Semaphore cameraUse = new Semaphore(1);

	public CameraController() {
		Environment.dumpEnvironment();

		try {
			System.out.printf("Installed native library to %s%n%n", PicamNativeLibrary.installTempLibrary());
		} catch (NativeLibraryException e) {
			// TODO Auto-generated catch block
			logger.error("Not native running, failed to install library for cam", e);
			e.printStackTrace();
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

	public File recordVideo() throws FailedToRunRaspistillException, IOException, InterruptedException {
		boolean acquired = cameraUse.tryAcquire(10, TimeUnit.SECONDS);
		if (acquired) {
			RPiCamera rPiCamera = new RPiCamera("~");
			File recordVideo = rPiCamera.recordVideo("video.mp4", 640, 430, 20000, true);
			cameraUse.release();
			return recordVideo;
		} else {
			return null;
		}
	}

	public File takePicture() {

		return null;
	}

	public boolean startPictureTaker() {

		return false;
	}

	public boolean stopPictureTaker() {

		return false;
	}

}
