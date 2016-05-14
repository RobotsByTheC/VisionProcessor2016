#include "org_usfirst_frc_team2084_CMonster2016_vision_HighGoalProcessor.h"
#include <iostream>
#include <opencv2/cudafilters.hpp>
#include <opencv2/cudaimgproc.hpp>

using namespace cv;

UMat hsvImage;
UMat blurImage;

cuda::GpuMat gpuImage;
cuda::GpuMat gpuHsvImage;
cuda::GpuMat gpuBlurImage;
cuda::GpuMat gpuGrayImage;

Ptr<cuda::Filter> blurFilter;

int oldBlurSize;

bool gpu = cuda::getCudaEnabledDeviceCount() > 0;

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
	if (gpu) {
		blurFilter = cuda::createGaussianFilter(CV_8UC3, CV_8UC3, Size(5, 5),
				0);
	}

	return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL Java_org_usfirst_frc_team2084_CMonster2016_vision_HighGoalProcessor_processNative(JNIEnv * env, jclass clazz,
		jlong inputImageAddr, jlong outputImageAddr, jlong grayImageAddr,
		jint blurSize, jdouble hMin, jdouble sMin, jdouble vMin, jdouble hMax, jdouble sMax, jdouble vMax) {

	UMat image = (*(Mat*) inputImageAddr).getUMat(ACCESS_READ);
	Mat& thresholdImage = *(Mat*) outputImageAddr;
	Mat& grayImage = *(Mat*) grayImageAddr;

	int kernelSize = 2 * blurSize + 1;

	if (gpu) {
		gpuImage.upload(image);
		cuda::cvtColor(gpuImage, gpuHsvImage, CV_BGR2HSV);
		cuda::cvtColor(gpuImage, gpuGrayImage, CV_BGR2GRAY);
		if(oldBlurSize != blurSize) {
			blurFilter = cuda::createGaussianFilter(CV_8UC3, CV_8UC3, Size(kernelSize, kernelSize), 0);
		}
		blurFilter->apply(gpuHsvImage, gpuBlurImage);
		gpuBlurImage.download(blurImage);
		gpuGrayImage.download(grayImage);
	} else {
		cvtColor(image, hsvImage, CV_BGR2HSV);
		cvtColor(image, grayImage, CV_BGR2GRAY);
		GaussianBlur(hsvImage, blurImage, Size(kernelSize, kernelSize), 0);
	}

	oldBlurSize = blurSize;

	inRange(blurImage, Scalar(hMin, sMin, vMin), Scalar(hMax, sMax, vMax), thresholdImage);
}

void JNI_OnUnload(JavaVM *vm, void *reserved) {
	if (gpu) {
		gpuImage.release();
		gpuHsvImage.release();
		gpuBlurImage.release();
		gpuGrayImage.release();
	}
}
