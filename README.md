# 项目描述
SSRF_Fuzz是一款基于BurpSuite MontoyaApi的SSRF漏洞探测插件, 捕获BurpSuite流量，自定义靶机。

## 运行原理
1. 捕获参数中存在URL链接特征的请求包
2. 参数值替换为payload，重新发包记录时间
3. 如果服务器收到请求，对应时间找触发的数据包

服务器可配合邮箱轰炸漏洞，使被访问时向自己账户发送邮件及时提醒，避免错过。

## 插件截图

![Image](https://github.com/user-attachments/assets/46e37c50-56cc-4c90-a818-38a6583e44e8)

![Image](https://github.com/user-attachments/assets/504b8926-6d67-4258-a24f-5aa09dfe7171)

# 注意事项
- 仅支持新版支持Montoya API的Burp Suite, 大概是2023.3之后的版本

## Q&&A
插件使用重复流量过滤器，来防止扫描重复的流量影响效率和结果展示。已被扫描过的请求会放到缓存中，第二次扫描时插件发现该请求在缓存中已存在， 则会放弃扫描。由于缓存是存储在内存中的, 重启BurpSuite时缓存会丢失, 所以插件提供了缓存持久化机制, 可以将缓存对象序列化后存储到指定的文件中, 下次启动时插件自动将缓存对象从文件加载到内存。
