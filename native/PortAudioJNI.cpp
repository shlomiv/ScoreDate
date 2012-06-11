#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <portaudio.h>
#include "PortAudioJNI.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned char UINT8;
typedef char SINT8;
typedef unsigned short UINT16;
typedef short SINT16;
typedef int SINT32;
typedef unsigned int UINT32;
typedef long long SINT64;
typedef unsigned long long UINT64;
typedef float FLOAT32;
typedef double FLOAT64;

#define DEVICE_SIZE 45
typedef struct {
	FLOAT64 default_high_input_latency;
	FLOAT64 default_high_output_latency;
	FLOAT64 default_low_input_latency;
	FLOAT64 default_low_output_latency;
	FLOAT64 default_sample_rate;
	UINT8 index;
	UINT8 host_api;
	UINT8 max_input_channels;
	UINT8 max_output_channels;
	UINT8 name_length;
} Device;

#define HOST_API_SIZE 6
typedef struct {
	SINT8 default_input_device;
	SINT8 default_output_device;
	UINT8 device_count;
	UINT8 index;
	UINT8 type;
	UINT8 name_length;
} HostAPI;

#define STREAM_CONFIGURATION_SIZE 41
typedef struct {
    FLOAT64 input_latency;
    FLOAT64 output_latency;
    FLOAT64 sample_rate;
    UINT32 input_format;
    UINT32 output_format;
    UINT32 flags;
	UINT8 mode;
	UINT8 input_channels;
    UINT8 input_device;
    UINT8 output_channels;
    UINT8 output_device;
} StreamConfiguration;

typedef struct {
    jmethodID callback;
	jclass clazz;
	JNIEnv * env;
    UINT32 id;
    SINT32 input_frame_size;
	SINT32 output_frame_size;
	UINT8 attached;
} UserData;

JavaVM * virtual_machine;

void org_jpab_throw_exception(JNIEnv *env, PaError * error) {
	jclass clazz = env->FindClass("org/jpab/PortAudioException");
	if (clazz != NULL)
		env->ThrowNew(clazz, Pa_GetErrorText(*error));
}

void org_jpab_hook(void *user_data) {
	UserData * data = (UserData *) user_data;
	data->env->CallStaticVoidMethod(data->clazz, data->env->GetStaticMethodID(data->clazz, "hook", "(I)V"), (jint) data->id);
	free(data);
}

int org_jpab_callback(const void * input, void * output, unsigned long frame_count, const PaStreamCallbackTimeInfo * time_info, PaStreamCallbackFlags status_flags, void * user_data) {
	UserData * data = (UserData *) user_data;
	if (data->attached == 0) {
		data->attached = 1;
		virtual_machine->AttachCurrentThreadAsDaemon((void **) &data->env, NULL);
		data->clazz = data->env->FindClass("org/jpab/PortAudio");
		data->callback = data->env->GetStaticMethodID(data->clazz, "callback", "(ILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I");
	}
	const UINT32 input_size = frame_count * data->input_frame_size, output_size = frame_count * data->output_frame_size;
	
	//fprintf(stderr, "[JNI] received callback !! Size=%d\n", input_size);
	
	return (int) data->env->CallStaticIntMethod(data->clazz, data->callback, (jint) data->id,
		input_size > 0 ? data->env->NewDirectByteBuffer((void *) input, (jlong) input_size) : NULL,
		output_size > 0 ? data->env->NewDirectByteBuffer(output, (jlong) output_size) : NULL
	);
}

PaStreamParameters * parameters(UINT32 channels, UINT32 device, UINT32 format, FLOAT64 latency) {
	PaStreamParameters * parameters = (PaStreamParameters *)malloc(sizeof(PaStreamParameters));
	parameters->channelCount = channels;
	parameters->device = device;
	parameters->sampleFormat = format;
	parameters->suggestedLatency = latency;
	parameters->hostApiSpecificStreamInfo = NULL;
	return parameters;
}

JNIEXPORT jint JNICALL Java_org_jpab_PortAudio_getVersion(JNIEnv *env, jclass paClass) {
	return (jint) Pa_GetVersion();
}

JNIEXPORT jstring JNICALL Java_org_jpab_PortAudio_getVersionText(JNIEnv *env, jclass paClass) {
    const char * version_text = Pa_GetVersionText();
	return env->NewString((jchar *) version_text, (jint) strlen(version_text));
}

JNIEXPORT void JNICALL Java_org_jpab_PortAudio_initialize(JNIEnv *env, jclass paClass) {
    env->GetJavaVM(&virtual_machine);
	PaError error = Pa_Initialize();
	if (error != paNoError)
	    org_jpab_throw_exception(env, &error);
}

JNIEXPORT jobject JNICALL Java_org_jpab_PortAudio_getDefaultHostAPIAsBuffer(JNIEnv *env, jclass paClass) {
    const UINT32 result = Pa_GetDefaultHostApi();
    if (result < 0) {
		org_jpab_throw_exception(env, (PaError *) &result);
		return NULL;
	}
	return Java_org_jpab_PortAudio_getHostAPIAsBuffer(env, paClass, (jint) result);
}

JNIEXPORT jobject JNICALL Java_org_jpab_PortAudio_getHostAPIAsBuffer(JNIEnv *env, jclass paClass, jint index) {
    const PaHostApiInfo * host_api_info = Pa_GetHostApiInfo((PaHostApiIndex) index);
    const UINT8 size = HOST_API_SIZE + strlen(host_api_info->name);
    char * buffer = (char *)malloc(size), * original = buffer;
    HostAPI * host_api = (HostAPI *) buffer;
    host_api->device_count = host_api_info->deviceCount;
	host_api->default_input_device = host_api_info->defaultInputDevice == -1 ? -1 : Pa_HostApiDeviceIndexToDeviceIndex(index, host_api_info->defaultInputDevice);
	host_api->default_output_device = host_api_info->defaultOutputDevice == -1 ? -1 : Pa_HostApiDeviceIndexToDeviceIndex(index, host_api_info->defaultOutputDevice);
	if (host_api->default_input_device < -1) host_api->default_input_device = -1;
	if (host_api->default_output_device < -1) host_api->default_output_device = -1;
	host_api->type = host_api_info->type;
	host_api->index = index;
	host_api->name_length = strlen(host_api_info->name);
	buffer += HOST_API_SIZE;
	memcpy(buffer, host_api_info->name, host_api->name_length);
    return env->NewDirectByteBuffer(original, (jint) size);
}

JNIEXPORT jobject JNICALL Java_org_jpab_PortAudio_getHostAPIsAsBuffer(JNIEnv *env, jclass paClass) {
	HostAPI * host_api;
	const PaHostApiInfo * host_api_info;
	const UINT8 count = Pa_GetHostApiCount();
	UINT16 index, size = HOST_API_SIZE * count, offset = 0, temp;
	char * buffer = (char *)malloc(size);
	for (index = 0; index < count; index ++) {
        host_api_info = Pa_GetHostApiInfo(index);
		host_api = (HostAPI *) (buffer + offset);
		host_api->default_input_device = host_api_info->defaultInputDevice == -1 ? -1 : Pa_HostApiDeviceIndexToDeviceIndex(index, host_api_info->defaultInputDevice);
		host_api->default_output_device = host_api_info->defaultOutputDevice == -1 ? -1 : Pa_HostApiDeviceIndexToDeviceIndex(index, host_api_info->defaultOutputDevice);
		if (host_api->default_input_device < -1) host_api->default_input_device = -1;
		if (host_api->default_output_device < -1) host_api->default_output_device = -1;
		host_api->device_count = host_api_info->deviceCount;
		host_api->type = host_api_info->type;
		host_api->index = index;
		temp = strlen(host_api_info->name);
		host_api->name_length = temp;
		size += temp;
		buffer = (char *) realloc(buffer, size);
		offset += HOST_API_SIZE;
		memcpy(buffer + offset, host_api_info->name, temp);
		offset += temp;
	}
	return env->NewDirectByteBuffer(buffer, (jint) size);
}

JNIEXPORT jobject JNICALL Java_org_jpab_PortAudio_getHostAPIsDevicesAsBuffer(JNIEnv *env, jclass paClass, jint host_api_index) {
	Device * device;
	const PaDeviceInfo * device_info;
	const UINT8 count = Pa_GetHostApiInfo((PaHostApiIndex) host_api_index)->deviceCount;
	UINT16 index, size = DEVICE_SIZE * count, offset = 0, temp;
	char * buffer = (char *)malloc(size);
	for (index = 0; index < count; index ++) {
        device_info = Pa_GetDeviceInfo(index);
		device = (Device *) (buffer + offset);
		device->default_high_input_latency = device_info->defaultHighInputLatency;
		device->default_high_output_latency = device_info->defaultHighOutputLatency;
		device->default_low_input_latency = device_info->defaultLowInputLatency;
		device->default_low_output_latency = device_info->defaultLowOutputLatency;
		device->default_sample_rate = device_info->defaultSampleRate;
		device->index = index;
		device->host_api = host_api_index;
		device->max_input_channels = device_info->maxInputChannels;
		device->max_output_channels = device_info->maxOutputChannels;
		temp = strlen(device_info->name);
		device->name_length = temp;
		size += temp;
		buffer = (char *) realloc(buffer, size);
		offset += DEVICE_SIZE;
		memcpy(buffer + offset, device_info->name, temp);
		offset += temp;
	}
	return env->NewDirectByteBuffer(buffer, (jint) size);
}

JNIEXPORT jobject JNICALL Java_org_jpab_PortAudio_getDeviceAsBuffer(JNIEnv *env, jclass paClass, jint device_index) {
    const PaDeviceInfo * device_info = Pa_GetDeviceInfo((PaDeviceIndex) device_index);
    const UINT8 size = DEVICE_SIZE + strlen(device_info->name);
    char * buffer = (char *)malloc(size), * original = buffer;
    Device * device = (Device *) buffer;
	device->default_high_input_latency = device_info->defaultHighInputLatency;
	device->default_high_output_latency = device_info->defaultHighOutputLatency;
	device->default_low_input_latency = device_info->defaultLowInputLatency;
	device->default_low_output_latency = device_info->defaultLowOutputLatency;
	device->default_sample_rate = device_info->defaultSampleRate;
	device->index = device_index;
	device->host_api = device_info->hostApi;
	device->max_input_channels = device_info->maxInputChannels;
	device->max_output_channels = device_info->maxOutputChannels;
	const UINT8 temp = strlen(device_info->name);
	device->name_length = temp;
	buffer += DEVICE_SIZE;
	memcpy(buffer, device_info->name, temp);
    return env->NewDirectByteBuffer(original, (jint) size);
}

JNIEXPORT jobject JNICALL Java_org_jpab_PortAudio_getDevicesAsBuffer(JNIEnv *env, jclass paClass) {
	Device * device;
	const PaDeviceInfo * device_info;
	const UINT8 count = Pa_GetDeviceCount();
	UINT16 index, size = DEVICE_SIZE * count, offset = 0, temp;
	char * buffer = (char *)malloc(size);
	for (index = 0; index < count; index ++) {
        device_info = Pa_GetDeviceInfo(index);
		device = (Device *) (buffer + offset);
		device->default_high_input_latency = device_info->defaultHighInputLatency;
		device->default_high_output_latency = device_info->defaultHighOutputLatency;
		device->default_low_input_latency = device_info->defaultLowInputLatency;
		device->default_low_output_latency = device_info->defaultLowOutputLatency;
		device->default_sample_rate = device_info->defaultSampleRate;
		device->index = index;
		device->host_api = device_info->hostApi;
		device->max_input_channels = device_info->maxInputChannels;
		device->max_output_channels = device_info->maxOutputChannels;
		temp = strlen(device_info->name);
		device->name_length = temp;
		size += temp;
		buffer = (char *) realloc(buffer, size);
		offset += DEVICE_SIZE;
		memcpy(buffer + offset, device_info->name, temp);
		offset += temp;
	}
	return env->NewDirectByteBuffer(buffer, (jint) size);
}



JNIEXPORT void JNICALL Java_org_jpab_PortAudio_terminate(JNIEnv *env, jclass paClass) {
    PaError error = Pa_Terminate();
    if (error != paNoError) org_jpab_throw_exception(env, &error);
}

JNIEXPORT void JNICALL Java_org_jpab_PortAudio_abortStream(JNIEnv *env, jclass paClass, jint stream_id) {
    PaError error = Pa_AbortStream((PaStream *) stream_id);
    if (error != paNoError) org_jpab_throw_exception(env, &error);
}

JNIEXPORT void JNICALL Java_org_jpab_PortAudio_closeStream(JNIEnv *env, jclass paClass, jint stream_id) {
    PaError error = Pa_CloseStream((PaStream *) stream_id);
    if (error != paNoError) org_jpab_throw_exception(env, &error);
}

JNIEXPORT jdouble JNICALL Java_org_jpab_PortAudio_getStreamCpuLoad(JNIEnv *env, jclass paClass, jint stream_id) {
	return Pa_GetStreamCpuLoad((PaStream *) stream_id);
}

JNIEXPORT jdouble JNICALL Java_org_jpab_PortAudio_getStreamTime(JNIEnv *env, jclass paClass, jint stream_id) {
    return Pa_GetStreamTime((PaStream *) stream_id);
}

JNIEXPORT void JNICALL Java_org_jpab_PortAudio_isFormatSupported(JNIEnv *env, jclass paClass, jobject byte_buffer) {
    StreamConfiguration * stream_configuration = (StreamConfiguration *) env->GetDirectBufferAddress(byte_buffer);
	PaStreamParameters * input_parameters, * output_parameters;
	switch (stream_configuration->mode) {
		case 1:
            input_parameters = parameters(stream_configuration->input_channels, stream_configuration->input_device, stream_configuration->input_format, stream_configuration->input_latency);
            output_parameters = NULL;
			break;
		case 2:
            input_parameters = NULL;
            output_parameters = parameters(stream_configuration->output_channels, stream_configuration->output_device, stream_configuration->output_format, stream_configuration->output_latency);
			break;
		case 3:
            input_parameters = parameters(stream_configuration->input_channels, stream_configuration->input_device, stream_configuration->input_format, stream_configuration->input_latency);
            output_parameters = parameters(stream_configuration->output_channels, stream_configuration->output_device, stream_configuration->output_format, stream_configuration->output_latency);
			break;
	}
	PaError error = Pa_IsFormatSupported(input_parameters, output_parameters, stream_configuration->sample_rate);
	if (error != 0) org_jpab_throw_exception(env, &error);
}

JNIEXPORT jboolean JNICALL Java_org_jpab_PortAudio_isStreamActive(JNIEnv *env, jclass paClass, jint stream_id) {
    PaError error = (UINT32) Pa_IsStreamActive((PaStream *) stream_id);
    if (error < 0) org_jpab_throw_exception(env, (PaError *) &error);
	return error == 1 ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_org_jpab_PortAudio_isStreamStopped(JNIEnv *env, jclass paClass, jint stream_id) {
    PaError error = Pa_IsStreamStopped((PaStream *) stream_id);
    if (error < 0) org_jpab_throw_exception(env, (PaError *) &error);
    return error == 1 ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_org_jpab_PortAudio_openStream(JNIEnv *env, jclass paClass, jobject byte_buffer) {
	StreamConfiguration * stream_configuration = (StreamConfiguration *) env->GetDirectBufferAddress(byte_buffer);
	PaStreamParameters * input_parameters, * output_parameters;
	switch (stream_configuration->mode) {
		case 1:
            input_parameters = parameters(stream_configuration->input_channels, stream_configuration->input_device, stream_configuration->input_format, stream_configuration->input_latency);
            output_parameters = NULL;
			break;
		case 2:
            input_parameters = NULL;
            output_parameters = parameters(stream_configuration->output_channels, stream_configuration->output_device, stream_configuration->output_format, stream_configuration->output_latency);
			break;
		case 3:
            input_parameters = parameters(stream_configuration->input_channels, stream_configuration->input_device, stream_configuration->input_format, stream_configuration->input_latency);
            output_parameters = parameters(stream_configuration->output_channels, stream_configuration->output_device, stream_configuration->output_format, stream_configuration->output_latency);
			break;
	}
	PaStream * stream;
	UserData * data = (UserData *)malloc(sizeof(UserData));
	data->input_frame_size = (input_parameters != NULL) ? Pa_GetSampleSize(input_parameters->sampleFormat) * input_parameters->channelCount : 0;
	data->output_frame_size = (output_parameters != NULL) ? Pa_GetSampleSize(output_parameters->sampleFormat) * output_parameters->channelCount : 0;
	data->attached = 0;
	fprintf(stderr, "[JNI] opening stream with frame size: %d. Sample rate: %f\n", data->input_frame_size, stream_configuration->sample_rate);
	PaError error = Pa_OpenStream(& stream, input_parameters, output_parameters, stream_configuration->sample_rate, paFramesPerBufferUnspecified, stream_configuration->flags, org_jpab_callback, data);
    if (error != paNoError) {
    	org_jpab_throw_exception(env, & error);
		return -1;
	}
    error = Pa_SetStreamFinishedCallback(stream, org_jpab_hook);
    if (error != paNoError) {
        org_jpab_throw_exception(env, & error);
		return -1;
	}
	data->id = (int) stream;
	return data->id;
}

JNIEXPORT void JNICALL Java_org_jpab_PortAudio_startStream(JNIEnv *env, jclass paClass, jint stream_id) {
    PaError error = Pa_StartStream((PaStream *) stream_id);
    if (error != paNoError) org_jpab_throw_exception(env, (PaError *) &error);
}

JNIEXPORT void JNICALL Java_org_jpab_PortAudio_stopStream(JNIEnv *env, jclass paClass, jint stream_id) {
    PaError error = Pa_StopStream((PaStream *) stream_id);
    if (error != paNoError) org_jpab_throw_exception(env, (PaError *) &error);
}

JNIEXPORT void JNICALL Java_org_jpab_PortAudio_free(JNIEnv *env, jclass paClass, jobject buffer) {
    void * pointer = env->GetDirectBufferAddress(buffer);
	free(pointer);
}

#ifdef __cplusplus
}
#endif
