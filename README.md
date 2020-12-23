# MultiThreadHandle
多线程处理工具，对于失败的任务可重试，欢迎大家指点

核心处理位于utils包下MultiThreadUtil


## 使用方法
其他构造方法自行发掘
```java
new MultiThreadUtil<String>(  //T：同list里的泛型
                      10,     //核心线程数
                      list,   //任务list
                      1000L,  //异常重试间隔毫秒值
                      3,      //最大重试次数
                      new Function<String, Boolean>() {   //对于list中每个元素的处理，返回false则会被重试
                        @Override
                        public Boolean apply(String obj) {
                          int ran = new Random().nextInt(3);
                          log.info(obj+":"+ran);
                          if(ran == 0) {
                            return true;
                          }
                          return false;
                        }
                      }
).begin();    //启用
```
