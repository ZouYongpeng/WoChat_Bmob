# WoChat_Bmob
# 注册界面（RegisterActivity）
  1、自定义控件CleanEditText作为文本输入框，点击×可清空输入框
	2、利用正则表达式判断用户名和密码的格式
	3、当格式正确、两次密码一致并且用户名尚未存在时可成功注册
# 登录界面（LoginActivity）
  1、查询SharePreferences是否存在上次登录成功的用户，并获取相关数据
	1、设置监听器，根据用户输入的用户名自动获取储存的密码
	2、登录时判断是否成功连接服务器
	3、登陆成功时利用SharePreferences存储内容：
		1）当前登录名
		2）是否记住密码
		3）如果点击记住密码则储存当前用户的密码
# 主界面（MainActivity）
  1、屏幕左侧右滑可呼出NavigationView
	2、右上角的菜单栏包括添加好友和创建群聊（功能没写）
	3、content部分（红框区域）为消息和联系人两个Fragment，可实现左右滑动切换。两个Fragment里面都有一个RecycleView，通过RecycleView显示服务器返回消息列      表和好友列表。
	4、底部标题栏由TabHost选项卡组成
	5、当点击下面菜单时,上面的能实现左右切换。当滑动上面的fragment时，菜单也会变化
# NavigationView
  含有五个菜单选项，设置和帮助还没写
	1、个人资料：查看、修改自己的个人资料
	2、退出：当前用户退出，应用返回至登录界面
# 个人资料（MyDataActivity）
  1、读取云端数据库的个人数据并显示
	2、利用正则表达式判断电话和邮箱的格式是否正确，不正确则修改失败
	3、点击头像可从底部弹出一个自定义的Dialog，选择拍照或从相册中选一张照片作为自己的头像（现在还没写提交头像到云端服务器的功能……）
	4、拍照功能用到了内容提供器FileProvider
# 添加好友界面（AddFriendActivity）
  1、上方为搜索框，含自定义CleanEditText和一个搜索按钮
	2、下方为一个RecyclerView，因为要显示其他人发过来的好友请求和搜索的用户列表，所以用了两个不同的Adapter和ViewHolder。
	3、添加好友时可以直接点击添加按钮，也可以点击用户头像进入用户资料详情页再决定是否添加。
	4、添加好友时会弹出一个对话框AlertDialog，填写好友请求并发送。
	5、用SharePreferences存储添加好友的状态（未添加、发起请求等待回复以及已添加）。
# 用户详情
  1、从云端获取用户的资料并显示
	2、利用SharePreferences储存的状态判断显示内容
		1）未发送好友请求时显示添加好友
		2）对方向我发送好友请求时显示同意添加
		3）已经添加为好友时，可点击按钮直接进入聊天界面发起对话
# 聊天界面（ChatActivity）
  1、参考《第一行代码》的聊天布局，考虑到未来可能要添加发送语音、文件、位置等功能，所以用了不同的ViewHolder。
	2、消息发送时会显示进度条
	3、消息发送失败会显示红色感叹号，点击可重发。
  
# 2018.06.10
  1、（失效）开机自启功能添加失败
  2、新增打开前台服务
  
# 2018.06.11
  1、新增“长按两次返回键退出”
  
  



  



