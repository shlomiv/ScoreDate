#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "fluidsynth.h"
#include "FluidsynthJNI.h"

#ifdef __cplusplus
extern "C" {
#endif

#define ILLEGAL_ARGUMENT_EXCEPTION "java/lang/IllegalArgumentException"
#define IO_EXCEPTION               "java/io/IOException"
#define FILE_NOT_FOUND_EXCEPTION   "java/io/FileNotFoundException"
#define ERROR                      "java/lang/ERROR"

typedef struct _Context {
	fluid_settings_t* settings;
	fluid_synth_t* synth;
	fluid_audio_driver_t* driver;
} Context;

static Context* createContext() {
	Context* context = (Context*) malloc(sizeof(Context));
	context->settings = NULL;
	context->synth = NULL;
	context->driver = NULL;
	return context;
}

static void destroyContext(JNIEnv* env, Context* context) {
	if (context->driver != NULL) {
		delete_fluid_audio_driver(context->driver);
	}
	if (context->synth != NULL) {
		delete_fluid_synth(context->synth);
	}
	if (context->settings != NULL) {
		delete_fluid_settings(context->settings);
	}
	free(context);
}

void throw_exception(JNIEnv* env, char* exception, char* pattern, ...) {

	va_list args;
	va_start(args, pattern);
	char msg[256];
	vsnprintf(msg, 256, pattern, args);
	va_end(args);

	jclass jclass = env->FindClass(exception);
	env->ThrowNew(jclass, msg);
}

typedef struct _ForEachData {
	JNIEnv *env;
	jobject jlist;
	jmethodID jadd;
} ForEachData;

static void initData(JNIEnv* env, ForEachData* data) {
	data->env = env;

	jclass lclass = env->FindClass("java/util/ArrayList");

	jmethodID constructor = env->GetMethodID(lclass, "<init>", "()V");
	data->jlist = env->NewObject(lclass, constructor);

	data->jadd = env->GetMethodID(lclass, "add", "(Ljava/lang/Object;)Z");
}

static void onOption(void* vdata, char* name, char* value) {
	ForEachData* data = (ForEachData*)vdata;

	JNIEnv* env = data->env;

	jstring string = env->NewStringUTF(value);

	env->CallVoidMethod(data->jlist, data->jadd, string);
}

JNIEXPORT
jobject JNICALL Java_Fluidsynth_init(JNIEnv* env, jclass jclass, jstring jname, jint jcores, jint jchannels, jint jpolyphony, jfloat jsampleRate, jstring jaudioDriver, jstring jaudioDevice, jint jdeviceIndex, jint jbuffers, jint jbufferSize, jfloat joverflowAge, jfloat joverflowPercussion, jfloat joverflowReleased, jfloat joverflowSustained, jfloat joverflowVolume) {
			
	Context* context = createContext();

	context->settings = new_fluid_settings();

	fluid_settings_setint(context->settings, "synth.cpu-cores", jcores);

	fluid_settings_setnum(context->settings, "synth.overflow.age", joverflowAge);
	fluid_settings_setnum(context->settings, "synth.overflow.percussion", joverflowPercussion);
	fluid_settings_setnum(context->settings, "synth.overflow.released", joverflowReleased);
	fluid_settings_setnum(context->settings, "synth.overflow.sustained", joverflowSustained);
	fluid_settings_setnum(context->settings, "synth.overflow.volume", joverflowVolume);

	fluid_settings_setint(context->settings, "synth.midi-channels", jchannels);
	fluid_settings_setint(context->settings, "synth.polyphony", jpolyphony);
	fluid_settings_setnum(context->settings, "synth.sample-rate", jsampleRate);
	
	if (jaudioDriver != NULL) {
		const char* audioDriver = env->GetStringUTFChars(jaudioDriver, NULL);

		fluid_settings_setstr(context->settings, "audio.driver", (char*)audioDriver);

		if (jaudioDevice != NULL) {
			const char* audioDevice = env->GetStringUTFChars(jaudioDevice, NULL);

			const char* prefix = "audio.";
			const char* suffix = ".device";
			char* key = (char*)calloc(strlen(prefix) + strlen(audioDriver) + strlen(suffix) + 1, sizeof(char));
			strcat(key, prefix);
			strcat(key, audioDriver);
			strcat(key, suffix);
			fluid_settings_setstr(context->settings, key, (char*)audioDevice);
			free(key);
			
			if (strcmp(audioDriver, "portaudio") == 0 && jdeviceIndex >= 0)
				fluid_settings_setint(context->settings, "audio.portaudio.index", jdeviceIndex);

			env->ReleaseStringUTFChars(jaudioDevice, audioDevice);
		}

		if (strcmp(audioDriver, "jack") == 0) {
			fluid_settings_setint(context->settings, "audio.jack.autoconnect", 1);
			const char* name = env->GetStringUTFChars(jname, NULL);
			fluid_settings_setstr(context->settings, "audio.jack.id", (char*)name);
			env->ReleaseStringUTFChars(jname, name);
		}

		env->ReleaseStringUTFChars(jaudioDriver, audioDriver);
	}

	fluid_settings_setint(context->settings, "audio.periods", jbuffers);
	fluid_settings_setint(context->settings, "audio.period-size", jbufferSize);

	context->synth = new_fluid_synth(context->settings);
	if (context->synth == NULL) {
		destroyContext(env, context);
		throw_exception(env, (char *)IO_EXCEPTION, (char *)"Couldn't create synth");
		return NULL;
	}

	context->driver = new_fluid_audio_driver(context->settings, context->synth);
	if (context->driver == NULL) {
		destroyContext(env, context);
		throw_exception(env, (char *)IO_EXCEPTION, (char *)"Couldn't create audio driver");
		return NULL;
	}

	return env->NewDirectByteBuffer((void*) context, sizeof(Context));
}

JNIEXPORT
void JNICALL Java_Fluidsynth_destroy(JNIEnv* env, jclass jclass, jobject jcontext) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	destroyContext(env, context);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_soundFontLoad(JNIEnv* env, jobject jclass, jobject jcontext, jstring jfilename) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	const char* filename = env->GetStringUTFChars(jfilename, NULL);
	int rc = fluid_synth_sfload(context->synth , filename, 0);
	env->ReleaseStringUTFChars(jfilename, filename);

	if (rc == -1) {
		throw_exception(env, (char *)IO_EXCEPTION, (char *)"Couldn't load soundfont, rc %d", rc);
		return;
	}
}

JNIEXPORT
jobject JNICALL Java_Fluidsynth_getProgramsList(JNIEnv* env, jclass jclass, jobject jcontext) {

	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);
	fluid_preset_t preset;
	/* get handle of the soundfont loaded on the top of the stack */
    fluid_sfont_t* sfont = fluid_synth_get_sfont(context->synth, 0);
	
	ForEachData data;
	initData(env, &data);
	
	sfont->iteration_start(sfont);
	while (sfont->iteration_next(sfont, &preset))
    {
		jstring string = env->NewStringUTF(preset.get_name(&preset));
        env->CallBooleanMethod(data.jlist, data.jadd, string);
		/* env->CallVoidMethod(data.jlist, data.jadd, string);

        oP.m_uiBank    = preset.get_banknum(&preset);
        oP.m_uiProgram = preset.get_num(&preset);
        oP.m_asName   = AnsiString( preset.get_name(&preset) );*/
    }

	return data.jlist;
}

JNIEXPORT
void JNICALL Java_Fluidsynth_setGain(JNIEnv* env, jclass jclass, jobject jcontext, jfloat jgain) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_set_gain(context->synth, jgain);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_setInterpolate(JNIEnv* env, jclass jclass, jobject jcontext, jint jinterpolate) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_set_interp_method(context->synth, -1, jinterpolate);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_noteOn(JNIEnv* env, jclass jclass, jobject jcontext, jint jchannel, jint jpitch, jint jvelocity) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_noteon(context->synth, jchannel, jpitch, jvelocity);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_noteOff(JNIEnv* env, jclass jclass, jobject jcontext, jint jchannel, jint jpitch) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_noteoff(context->synth, jchannel, jpitch);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_controlChange(JNIEnv* env, jclass jclass, jobject jcontext, jint jchannel, jint jcontroller, jint jvalue) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_cc(context->synth, jchannel, jcontroller, jvalue);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_pitchBend(JNIEnv* env, jclass jclass, jobject jcontext, jint jchannel, jint jvalue) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_pitch_bend(context->synth, jchannel, jvalue); 
}

JNIEXPORT
void JNICALL Java_Fluidsynth_programChange(JNIEnv* env, jclass jclass, jobject jcontext, jint jchannel, jint jprogram) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_program_change(context->synth, jchannel, jprogram); 
}

JNIEXPORT
void JNICALL Java_Fluidsynth_setReverbOn(JNIEnv* env, jclass jclass, jobject jcontext, jboolean jon) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_set_reverb_on(context->synth, jon);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_setReverb(JNIEnv* env, jclass jclass, jobject jcontext, jdouble jroomsize, jdouble jdamping, jdouble jwidth, jdouble jlevel) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_set_reverb(context->synth, jroomsize, jdamping, jwidth, jlevel);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_setChorusOn(JNIEnv* env, jclass jclass, jobject jcontext, jboolean jon) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_set_chorus_on(context->synth, jon);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_setChorus(JNIEnv* env, jclass jclass, jobject jcontext, jint jnr, jdouble jlevel, jdouble jspeed, jdouble jdepth_ms, jint jtype) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	fluid_synth_set_chorus(context->synth, jnr, jlevel, jspeed, jdepth_ms, jtype);
}

JNIEXPORT
void JNICALL Java_Fluidsynth_setTuning(JNIEnv* env, jclass jclass, jobject jcontext, jint jtuningBank, jint jtuningProgram, jstring jname, jdoubleArray jderivations) {
	Context* context = (Context*) env->GetDirectBufferAddress(jcontext);

	jdouble derivations[12];
	env->GetDoubleArrayRegion(jderivations, 0, 12, derivations);

	const char* name = env->GetStringUTFChars(jname, NULL);

	fluid_synth_create_octave_tuning(context->synth, jtuningBank, jtuningProgram, (char*)name, derivations);

	env->ReleaseStringUTFChars(jname, name);
}

JNIEXPORT
jobject JNICALL Java_Fluidsynth_getAudioDrivers(JNIEnv* env, jclass jclass) {

	ForEachData data;
	initData(env, &data);

	fluid_settings_t* settings = new_fluid_settings();
	fluid_settings_foreach_option(settings, "audio.driver", &data, onOption);
	delete_fluid_settings(settings);

	return data.jlist;
}

JNIEXPORT
jobject JNICALL Java_Fluidsynth_getAudioDevices(JNIEnv* env, jclass jclass, jstring jaudioDriver) {

	ForEachData data;
	initData(env, &data);

	const char* prefix = "audio.";
	const char* audioDriver = env->GetStringUTFChars(jaudioDriver, NULL);
	const char* suffix = ".device";

	char *key = (char*)calloc(strlen(prefix) + strlen(audioDriver) + strlen(suffix) + 1, sizeof(char));
	strcat(key, prefix);
	strcat(key, audioDriver);
	strcat(key, suffix);
 
	fluid_settings_t* settings = new_fluid_settings();
	fluid_settings_foreach_option(settings, key, &data, onOption);
	delete_fluid_settings(settings);

	free(key);

	env->ReleaseStringUTFChars(jaudioDriver, audioDriver);

	return data.jlist;
}

#ifdef __cplusplus
}
#endif
