## 消息格式

类型		| 名称		 | 字节序列	| 取值范围	| 备注
--- 	| ----- 	 | ---------| --------- |----
消息头	| magic 	 | 0 		|0x13 	    |魔数
		| msgType	 | 1 		|0x00-0x11  |消息类型
		| len		 | 2-5		|0-2147483647 |消息体长度。	
消息体	| body		 | 变长		|0-    |消息体。格式和消息类型相关，不同的消息类型有不同的消息体格式。消息大小不应超过2G

