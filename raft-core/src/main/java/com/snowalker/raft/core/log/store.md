# 实现日志存储

RandomAccessFile 随机写文件  NIO

## 日志序列化  

基于protobuffer实现日志序列化

## 定义一个日志的接口

实现日志基本操作方法

## sequence

生成日志的sequence序列，以便于后续进行快照生成使用（Raft  snapshot）


## 日志entryFile的结构

    int(4)    int(4)     int(4)   int(4)    byte
    kind      index      term     length    command bytes
    kind      index      term     length    command bytes
    kind      index      term     length    command bytes

## 日志索引EntryIndexFile

    int(4)          int(4)
    minEntryIndex   maxEntryIndex
         offset                              kind    term
         offset                              kind    term


日志索引index可以计算得出：第一条为minEntryIndex，第N条为  minEntryIndex + n -1 最后一条为maxEntryIndex


