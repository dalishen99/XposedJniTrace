

#ifndef QCONTAINER_PRO_APPUTILS_H
#define QCONTAINER_PRO_APPUTILS_H

#include "AllInclude.h"



typedef size_t Addr;


class Jnitrace {
    public:
        /**
         * start jni trace
         *
         * @param env  jniEnv
         * @param os using the list to save the entire collection, the jnitrace only handles the so name inside the list
         * @param isSave  Whether to save the file, save the file is the incoming path, otherwise pass null
         */
        static void startjnitrace(JNIEnv *env, const std::list<string> &filter_list, std::ofstream * os);

        /**
         * stop jni trace
         */
        [[maybe_unused]] static void stopjnitrace();

        private:
                static void init(JNIEnv *env);
};


#endif //QCONTAINER_PRO_APPUTILS_H
