// JNI bridge to stable-diffusion.cpp. One context is created per model load
// (expensive) and reused across generations; generate_image blocks and is
// driven from a background thread on the Kotlin side. Raw RGB from sd_image_t
// is packed into an ARGB_8888 int[] so Kotlin can build a Bitmap without the
// jnigraphics dependency.
#include <jni.h>
#include <android/log.h>
#include <cstdint>
#include <cstdlib>
#include "stable-diffusion.h"

#define LOG_TAG "sdjni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {
void forward_sd_log(enum sd_log_level_t level, const char* text, void* /*data*/) {
    int prio = level == SD_LOG_ERROR ? ANDROID_LOG_ERROR
             : level == SD_LOG_WARN  ? ANDROID_LOG_WARN
                                     : ANDROID_LOG_INFO;
    __android_log_print(prio, "sd.cpp", "%s", text ? text : "");
}
}  // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_me_kerooker_rpgnpcgenerator_repository_image_SdCppNative_nativeInit(
        JNIEnv* env, jobject /*thiz*/, jstring jmodel, jstring jvae, jstring jtaesd, jint threads) {
    sd_set_log_callback(forward_sd_log, nullptr);

    const char* model = env->GetStringUTFChars(jmodel, nullptr);
    const char* vae   = jvae   ? env->GetStringUTFChars(jvae, nullptr)   : nullptr;
    const char* taesd = jtaesd ? env->GetStringUTFChars(jtaesd, nullptr) : nullptr;

    sd_ctx_params_t params;
    sd_ctx_params_init(&params);
    params.model_path = model;
    if (vae)   params.vae_path   = vae;
    if (taesd) params.taesd_path = taesd;
    params.n_threads = threads;

    sd_ctx_t* ctx = new_sd_ctx(&params);

    env->ReleaseStringUTFChars(jmodel, model);
    if (vae)   env->ReleaseStringUTFChars(jvae, vae);
    if (taesd) env->ReleaseStringUTFChars(jtaesd, taesd);

    if (ctx == nullptr) {
        LOGE("new_sd_ctx failed");
        return 0;
    }
    return reinterpret_cast<jlong>(ctx);
}

extern "C" JNIEXPORT jintArray JNICALL
Java_me_kerooker_rpgnpcgenerator_repository_image_SdCppNative_nativeGenerate(
        JNIEnv* env, jobject /*thiz*/, jlong ctxPtr, jstring jprompt, jstring jneg,
        jint width, jint height, jint steps, jfloat cfg, jlong seed) {
    auto* ctx = reinterpret_cast<sd_ctx_t*>(ctxPtr);
    if (ctx == nullptr) return nullptr;

    const char* prompt = env->GetStringUTFChars(jprompt, nullptr);
    const char* neg    = jneg ? env->GetStringUTFChars(jneg, nullptr) : "";

    sd_img_gen_params_t p;
    sd_img_gen_params_init(&p);
    p.prompt                        = prompt;
    p.negative_prompt               = neg;
    p.width                         = width;
    p.height                        = height;
    p.sample_params.sample_steps    = steps;
    p.sample_params.guidance.txt_cfg = cfg;
    p.seed                          = seed;
    p.batch_count                   = 1;

    sd_image_t* images = nullptr;
    int num_images = 0;
    bool ok = generate_image(ctx, &p, &images, &num_images);

    env->ReleaseStringUTFChars(jprompt, prompt);
    if (jneg) env->ReleaseStringUTFChars(jneg, neg);

    if (!ok || num_images < 1 || images == nullptr || images[0].data == nullptr) {
        LOGE("generate_image failed (ok=%d, n=%d)", ok, num_images);
        if (images != nullptr) {
            for (int i = 0; i < num_images; i++) free(images[i].data);
            free(images);
        }
        return nullptr;
    }

    sd_image_t img = images[0];
    const int pixels = static_cast<int>(img.width) * static_cast<int>(img.height);
    const int ch = static_cast<int>(img.channel);

    jintArray result = env->NewIntArray(pixels);
    if (result != nullptr) {
        auto* argb = new jint[pixels];
        for (int i = 0; i < pixels; i++) {
            uint8_t r = img.data[i * ch + 0];
            uint8_t g = ch > 1 ? img.data[i * ch + 1] : r;
            uint8_t b = ch > 2 ? img.data[i * ch + 2] : r;
            argb[i] = (0xFF << 24) | (r << 16) | (g << 8) | b;
        }
        env->SetIntArrayRegion(result, 0, pixels, argb);
        delete[] argb;
    }

    for (int i = 0; i < num_images; i++) free(images[i].data);
    free(images);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_me_kerooker_rpgnpcgenerator_repository_image_SdCppNative_nativeFree(
        JNIEnv* /*env*/, jobject /*thiz*/, jlong ctxPtr) {
    auto* ctx = reinterpret_cast<sd_ctx_t*>(ctxPtr);
    if (ctx != nullptr) free_sd_ctx(ctx);
}
