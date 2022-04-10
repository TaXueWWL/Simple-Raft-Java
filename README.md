# Simple-Raft-Java

> A simple Java implementation of the Raft paper
> 
> https://raft.github.io/



~~~
core     raft核心
   |-config   raft配置
boot     raft启动
kv       raft base kv
client   客户端
~~~

## stage 1.0

> 阶段1，实现leader选举

~~~
  定时器组件-->一致性算法组件 ---> 成员表组件
                    |
                    |--------> 日志组件
~~~
