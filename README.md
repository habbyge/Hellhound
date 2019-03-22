# Hellhound

这里主要是总结平时工作中主要学习和使用到的技术，部分开源出来，作为技术交流。

##1、地狱犬系统：
用于监控Android系统中用户行为，包括页面的展示/消失；控件的操作(点击、长按、滑动、拖动等)，技术方案主要涉及静态插桩方案和动态hook方案：
###1.1、class字节码插桩技术
利用Gradle Plugin、Transform侵入App Build过程，再使用ASM框架，对javac后(混淆任务执行前)的class节码进行插桩；
需要具备的知识点：App构建过程、Gradle插件化、class字节码文件格式(尤其是常量池这个大总管、还有method_info中的Code属性所代表的方法体)、jvm指令语法、基于操作数栈概念、jvm内存模型、线程栈、方法帧栈、局部变量表等基础知识。
###1.2、hook动态代理技术
需要具备的知识点：App启动过程、深入理解四大组件(尤其是Activity)生命周期执行过程(App进程中的ActivityManagerProxy/IActivityManager；System Server进程中的AMS、PMS(暂时没用到，但是也需要熟悉，这样可以做到放心)、ActivityThread、mH等基础知识点、概念)、动态代理技术、hook技术，性能调优、crash栈分析、机型适配等。


##2、版本计划
version1.0：劫持控件操作(点击、长按、滑动、拖动等)行为
version1.1：劫持Activity生命周期，并支持劫持和注入Activity跳转时Intent中传递的数据
version1.2：支持高性能跨进程组件：共享内存，自此以后，Hellhound开始支持了跨进程。
version1.3：劫持Fragment生命周期
version1.4：劫持通知栏行为
version2.0：劫持业务层数据的技术方案

##3、进度
工作比较忙，目前进度还在version1.0中，目前已经完成了点击的插桩，后续尽快补上其他操作手势的插桩(虽然原理都一样)，和更合理完善的代码！
