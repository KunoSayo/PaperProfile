# PaperProfile —— 人物卡【纸】

# ? ？
没当过kp
也没有什么跑团经验
按照自己云来的想法写的pp
以及自己想到的可能需要的功能都写了一点点点

——水某mcbbs某插件版某比赛的作品

# Command 命令

## 管理命令

* /pp gm list <attribute|buff>  
* * 列出所有属性/状态
* /pp gm join \[user]
* * 变成gm  
    需要权限 paperprofile.gm.command.join
* /pp gm leave \[user]
* * 离开gm  
    需要权限 paperprofile.gm.command.leave  
* /pp gm add <attribute|buff> <key> {<value-type>} \[permission]
  * 添加属性/状态 默认为system
* /pp gm delete <attribute|buff>
  * 删除属性/状态
* /pp gm edit <attribute|buff> <key> <option&value>
  * 修改属性/buff
## 玩家命令
* /r <dice> \[msg]
  * roll点
* /rd \[msg]
  * r暗骰
* /nn \[user] <name>
  * 设置名字
* /pp show \[attribute]
  * 显示信息/属性
* /pp a \<attribute> <op> <dice> \[message]
  * 更改属性

# Attribute 属性
```
"属性key" {
    name = "显示名字 默认为属性Key"
    lore = "属性描述"
    permission = "<system/custom>"
    type = "<string/number>"
    #默认为0 表达数字最小值
    min = 0
    default = "默认值 新建角色卡所需"
    #最大值 数字最大值或字符串最长长度
    #可通过ref开头引用某个属性
    max = 0x7fffffff
    
    display = "属性{0}: %a_属性key_value%"
    item = "展示用物品 默认为paper"
}
```

# Buff 状态
```
"Buff key" {
    name = "显示名字 默认为属性Key"
    lore = "描述"
    permission = "<system/custom>"
    values {
        "keys" {
            name = "显示名字 默认为属性Key"
            lore = "描述"
            type = "<string/number>"
            #之后类似于属性
        }
    }
    gt_op = "[<attribute|buff> <key> <op> <expr>]"
    
    display="[状态]{0}"
    item = "展示用物品 默认为paper"
}
```

# Thought.
臃肿 没文档 的代码
无意义的套娃
