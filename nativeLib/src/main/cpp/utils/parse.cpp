//
// Created by Administrator on 2020-09-14.
//

#include "includes/parse.h"
#include "includes/fileUtils.h"
#include "includes/logging.h"


#define BUF_SIZE 1024

using namespace std;

[[maybe_unused]] jstring parse::char2jstring(JNIEnv *env, const char *pat) {

    //定义java String类 strClass
    jclass strClass = (env)->FindClass("java/lang/String");
    //获取String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (env)->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    //建立byte数组
    jbyteArray bytes = (env)->NewByteArray((jsize)strlen(pat));
    //将char* 转换为byte数组
    (env)->SetByteArrayRegion(bytes, 0, (jsize)strlen(pat), (jbyte *) pat);
    // 设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (env)->NewStringUTF("GB2312");
    //将byte数组转换为java String,并输出
    return (jstring) (env)->NewObject(strClass, ctorID, bytes, encoding);
}

string parse::get_process_name() {
    //调用系统原生api
    return getprogname();
}

string parse::get_process_name_pid(pid_t pid) {
    //优先尝试读取cmdline
    auto pidStr = fileUtils::readText(
            string("/proc/").append(to_string(pid)).append("/cmdline"));
    if(!pidStr.empty()){
        return pidStr;
    }
    auto status = string("/proc/").append(to_string(pid)).append("/status").c_str();
    FILE *file = fopen(status, "r");
    if (!file) {
        LOG(ERROR)<<"get_process_name_pid read pid error "<<status;
        return {};
    }
    char line[BUF_SIZE]={0};
    char process_name[BUF_SIZE]={0};
    char *str;
    while (fgets(line, BUF_SIZE, file)) {
        if (strstr(line, "Name:") == line) {
            str = line + strlen("Name:");
            while (*str == ' ' || *str == '\t') {
                ++str;
            }
            strncpy(process_name, str, BUF_SIZE - 1);
            process_name[BUF_SIZE - 1] = '\0';
            break;
        }
    }
    fclose(file);
    return {process_name};
}

string parse::jstring2str(JNIEnv *env, jstring jstr) {
    char *rtn = nullptr;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("UTF-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    auto barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    std::string stemp(rtn);
    free(rtn);
    return stemp;
}

map<string, string> parse::jmap2cmap(JNIEnv *env, jobject jmap) {
    std::map<std::string, std::string> cmap;
    jclass jMapClass = env->FindClass("java/util/HashMap");
    jmethodID jKeySetMethodId = env->GetMethodID(jMapClass, "keySet", "()Ljava/util/Set;");
    jmethodID jGetMethodId = env->GetMethodID(jMapClass, "get",
                                              "(Ljava/lang/Object;)Ljava/lang/Object;");
    jobject jSetKey = env->CallObjectMethod(jmap, jKeySetMethodId);
    jclass jSetClass = env->FindClass("java/util/Set");
    jmethodID jToArrayMethodId = env->GetMethodID(jSetClass, "toArray", "()[Ljava/lang/Object;");
    auto jObjArray = (jobjectArray) env->CallObjectMethod(jSetKey, jToArrayMethodId);
    if (jObjArray == nullptr) {
        return cmap;
    }
    jsize size = env->GetArrayLength(jObjArray);
    int i;
    for (i = 0; i < size; i++) {
        auto jkey = (jstring) env->GetObjectArrayElement(jObjArray, i);
        auto jvalue = (jstring) env->CallObjectMethod(jmap, jGetMethodId, jkey);
        if (jvalue == nullptr) {
            continue;
        }
        char *key = (char *) env->GetStringUTFChars(jkey, nullptr);
        char *value = (char *) env->GetStringUTFChars(jvalue, nullptr);
        cmap[std::string(key)] = std::string(value);
    }
    return cmap;
}





[[maybe_unused]] bool parse::jboolean2bool(jboolean value) {
    return value == JNI_TRUE;
}


