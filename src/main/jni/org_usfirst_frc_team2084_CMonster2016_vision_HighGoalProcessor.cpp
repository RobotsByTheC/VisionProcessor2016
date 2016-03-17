#include "org_usfirst_frc_team2084_CMonster2016_vision_HighGoalProcessor.h"
#include <iostream>
#include <opencv2/cudafilters.hpp>
#include <opencv2/cudaimgproc.hpp>

using namespace cv;

Mat hsvImage;
Mat blurImage;

cuda::GpuMat gpuImage;
cuda::GpuMat gpuHsvImage;
cuda::GpuMat gpuBlurImage;

Ptr<cuda::Filter> blurFilter;

bool gpu = cuda::getCudaEnabledDeviceCount() > 0;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	if (gpu) {
		blurFilter = cuda::createGaussianFilter(CV_8UC3, CV_8UC3, Size(5, 5),
				0);
	}

	return JNI_VERSION_1_2;
}

/*
 * Class:     org_usfirst_frc_team2084_CMonster2016_vision_HighGoalProcessor_GPU
 * Method:    process
 * Signature: (Lorg/opencv/core/Mat;)V
 */
JNIEXPORT void JNICALL Java_org_usfirst_frc_team2084_CMonster2016_vision_HighGoalProcessor_processNative(JNIEnv * env, jclass clazz,
		jlong inputImageAddr, jlong outputImageAddr,
		jint blurSize) {
	//std::cout << "Test"<< std::endl;

	Mat& image = *(Mat*) inputImageAddr;
	Mat& thresholdImage = *(Mat*) outputImageAddr;

	if(gpu) {
		gpuImage.upload(image);
		cuda::cvtColor(gpuImage, gpuHsvImage, CV_BGR2HSV);
		blurFilter->apply(gpuHsvImage, gpuBlurImage);
		gpuBlurImage.download(thresholdImage);
	} else {
		cvtColor(image, hsvImage, CV_BGR2HSV);
		medianBlur(hsvImage, blurImage, 2 * blurSize + 1);
	}

	inRange(blurImage, Scalar(0, 0, 0), Scalar(50, 100, 150), thresholdImage);

//	Mat gpuMat;
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
	std::cout << "Unload" << std::endl;
	if (gpu) {
		gpuImage.release();
		gpuHsvImage.release();
		gpuBlurImage.release();
	}
}
