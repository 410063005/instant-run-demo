# instant-run-demo
一个用于演示instant run的demo

demo的效果如下：

https://github.com/410063005/instant-run-demo/blob/master/screenshots/instant-run-client-demo.mp4

demo的使用方法：

+ 下载demo源码[Github](https://github.com/410063005/instant-run-demo)
+ Android Studio中运行demo的app module，确认黄色闪电图标亮起
+ 从上一步生成的`build-info.xml`文件找到token (一长串数字)
+ 下载并运行`appclient.jar`，运行方法`java -jar appclient.jar`

注：

+ `build-info.xml`文件的具体路径是`/build/intermediates/build-info/debug/build-info.xml`
+ `appclient.jar`编译自demo源码中的appclient module，编译方法`./gradlew shadowJar`
