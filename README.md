# Hellhound

1、地狱犬系统：
用于监控Android系统中用户行为，包括页面的展示/消失；控件的操作(点击、长按、滑动、拖动等)，技术方案采取jvm字节码插桩、hook动态代理技术.

2、版本计划
version1.0：劫持控件操作(点击、长按、滑动、拖动等)行为
version1.1：劫持Activity生命周期，并支持劫持和注入Activity跳转时Intent中传递的数据
version1.2：支持高性能跨进程组件：共享内存，自此以后，Hellhound开始支持了跨进程。
version1.3：劫持Fragment生命周期
version1.4：劫持通知栏行为
version2.0：劫持业务层数据的技术方案
