#include <jni.h>
#include <vector>
#include <cstdint>
#include <cmath>

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_greenclicker_NativeProcessor_findGreen(JNIEnv *env, jobject /* this */,
                                                        jbyteArray rgbaArray, jint width, jint height,
                                                        jint step, jint hMin, jint hMax, jint sMin, jint vMin) {
    // rgbaArray: bytes in RGBA order (0..255)
    jbyte *data = env->GetByteArrayElements(rgbaArray, NULL);
    if (!data) return NULL;

    int w = width;
    int h = height;
    int st = step <= 0 ? 1 : step;

    int foundX = -1;
    int foundY = -1;

    for (int y = 0; y < h; y += st) {
        for (int x = 0; x < w; x += st) {
            int idx = (y * w + x) * 4;
            int r = (uint8_t)data[idx] & 0xFF;
            int g = (uint8_t)data[idx+1] & 0xFF;
            int b = (uint8_t)data[idx+2] & 0xFF;
            // RGB to HSV (h 0..179 like OpenCV)
            float rf = r / 255.0f;
            float gf = g / 255.0f;
            float bf = b / 255.0f;
            float maxv = fmaxf(rf, fmaxf(gf, bf));
            float minv = fminf(rf, fminf(gf, bf));
            float delta = maxv - minv;
            float H = 0.0f;
            if (delta != 0.0f) {
                if (maxv == rf) {
                    H = fmodf(((gf - bf) / delta), 6.0f);
                } else if (maxv == gf) {
                    H = ((bf - rf) / delta) + 2.0f;
                } else {
                    H = ((rf - gf) / delta) + 4.0f;
                }
                H *= 60.0f;
                if (H < 0) H += 360.0f;
            }
            int Hi = (int)(H / 2.0f); // 0..179
            int Si = (int)( (maxv==0.0f) ? 0 : (delta / maxv) * 255.0f );
            int Vi = (int)(maxv * 255.0f);

            bool hueOK = (hMin <= hMax) ? (Hi >= hMin && Hi <= hMax) : (Hi >= hMin || Hi <= hMax);
            if (hueOK && Si >= sMin && Vi >= vMin) {
                foundX = x; foundY = y;
                goto done;
            }
        }
    }
done:
    env->ReleaseByteArrayElements(rgbaArray, data, JNI_ABORT);
    jintArray out = env->NewIntArray(2);
    jint tmp[2];
    tmp[0] = foundX;
    tmp[1] = foundY;
    env->SetIntArrayRegion(out, 0, 2, tmp);
    return out;
}
