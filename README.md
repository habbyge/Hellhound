# Hellhound

这里主要是总结平时工作中主要学习和使用到的技术，部分开源出来，作为技术交流。

## 1、地狱犬系统：
用于监控Android系统中的各种行为，包括页面行为、控件行为、以及方法(method)、指令级别的执行监控等，技术方案主要涉及静态插桩方案和动态hook方案，基于这个技术方案，可以扩展支持很多业务需求，例如：行为监控、APM、甚至后续可以扩展实现完全0反射的插件化方案。
### 1.1、class字节码插桩技术
利用Gradle Plugin、Transform侵入App Build过程，再使用ASM框架，对javac后(混淆任务执行前)的class节码进行插桩；
需要具备的知识点：App构建过程、Gradle插件化、class字节码文件格式(尤其是常量池这个大总管、还有method_info中的Code属性所代表的方法体)、jvm指令语法、基于操作数栈概念(*尤其需要注意，对象方法的this指针用于在局部变量表的slot-0位置，类方法无需这个this指针，且局部变量表存储顺序一般是this指针、接着是参数列表按从左到右顺序存储、之后是方法中声明的局部变量，也是按照声明顺序存储的，所以我们在使用jvm汇编指令插桩时非常容易获取参数，只要会数数就行了，同时为了性能，也可以不设置自动获取局部变量表和栈帧大小，直接数数来手工设置亦可，我们在使用jvm汇编指令调用方法时，也是按照这个顺序从局部变量表中取数据push到栈中来匹配调用方法的形参的，然后才是invoke方法*)、jvm内存模型(*尤其是线程栈、方法帧栈、局部变量表，运行时栈是以线程为单位分配的，栈帧是以方法为单位分配的，可以看出来n个栈帧组成了栈，n亦是方法数，局部变量表、操作数栈存在于栈帧中*)等基础知识。

## 2、版本计划
- version1.0：劫持控件操作(点击、长按、滑动、拖动等)行为
- version1.1：劫持Activity生命周期、startActivity/finish/moveTaskToBack等方法，并支持劫持和注入Activity跳转时Intent中传递的数据
- version1.2：劫持Fragment生命周期
- version1.3：劫持通知栏行为
- version1.4：支持高性能跨进程组件：共享内存，自此以后，Hellhound开始支持了跨进程。
- version1.5：劫持业务层数据的技术方案
- version2.0：抽象成框架，提供对外友好一致的接口：
--对外接口需要提供被插桩方法的所属类全名、方法名、描述符。然后内部根据参数实现插桩。
--需要传递对外注册的观察者，内部实现使用类全名、方法名、描述符作为key存储，根据插桩劫持的到的key参数，来寻址callback方法，完成callback

## 3、进度
目前进度在version1.2中。
- 最新进度：Activity生命周期监控、startActivity/finish/moveTaskToBack方法监控，并抓取调用者、目标对象，以及Intent等传递的参数数据，回调出来，从而可以形成页面链路；View监控点击的部分已完成，View点击、长按已完成、监控android.jar中的android/app/Activity、android/app/Fragment的生命周期，已完成.

## 4、部分技术细节
注入页面行为函数的策略：
### 4.1、继承android.app.Activity的页面，下同fragment，无法字节码注入，可以从另外一个角度解决：扫描当前Project的工程目录文件(注意不是jar文件)，然后有两个具体方案(这也是比其他方案优秀的其中一个原因)：
- 方案1、扫描当前业务Activity，父类是直接继承android.app.Activity，遍历是否存在需要注入的方法(例如：onNewIntent、onPause...)，如果已经存在，则直接在目标方法中注入插桩；反之，在ClassVistor.visitEnd()中，也就是整个class文件的末尾插入缺失的目标方法，同时注入插桩，选取在class文件结尾注入的原因是，不改变原代码代码行号。
- 方案2：自己实现一个BaseActivity，继承android.app.Activity，override目标方法，然后扫描Project的目录，替换系统Activity为自己生成的BaseActivity即可。
### 4.2、继承v4包中的activity和fragment，无需1中方案，直接扫描jar中class文件：FragmentActivity和Fragment，在对应的目标方法中注入插桩即可。

## 5、gradle plugin debug方法
- 【run】->【Edit configures】，针对自己的需要调试的plugin，新建remote调试选项，起个名字+选择对应需要调试的plugin
- 在Terminal中输入命令：./gradlew assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true, 然后会等待attach对应的plugin，选择【run】->【Edit configures】中对应需要debug的remote选项中的plugin，ok即可。
- 再次进入【Run】->【Debug "选择要debug的插件名"】，即可breakpoint debug。
- 注意: debug之前，最好./gradlew clean一次工程，因为增量编译的话，可能会跳过一些编译过程，导致brakpoint不能执行到.
