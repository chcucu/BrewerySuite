# BrewerySuite

给 BreweryX 的酒桶和酿造锅加一层信息显示，另外带一本配方书和一个酒市。服务端装好 BreweryX 即可使用，BreweryX 本身不需要修改。

## 功能

- 酒桶全息：不打开桶也能看到桶体大小、占用量、桶龄和各瓶的陈酿状态
- 逐瓶信息：配方、年份、品质、酒精度、蒸馏次数、木质、状态、剩余陈酿时间
- 酿造锅全息：显示锅内配料和当前状态
- 配方书：列出全部配方，已掌握的配方带标记
- 酒市：把酒卖给系统，支持潜影盒整盒出售、每日额度和出售历史
- 玩家偏好：每个人可以单独开关显示、切换简洁或详细、选择中文或英文、只看附近

## 依赖

| 插件 | 说明 |
| --- | --- |
| BreweryX 3.7.1 | 必装，酒桶、配方和酒品数据都来自它 |
| Vault | 酒市需要，用来对接经济插件 |
| DecentHolograms | 全息显示需要 |
| PlaceholderAPI | 可选 |

## 安装

把 BrewerySuite-1.0.jar 放进 plugins 目录，重启服务器。

首次启动会在 plugins/BrewerySuite 下生成：

- config.yml
- lang/zh_CN.yml
- lang/en_US.yml

## 命令

| 命令 | 说明 |
| --- | --- |
| /brewsuite help | 查看帮助 |
| /brewsuite status | 查看当前状态 |
| /brewsuite toggle | 开关自己看到的全息 |
| /brewsuite mode <brief/detailed> | 切换简洁或详细 |
| /brewsuite lang <zh_CN/en_US> | 切换语言 |
| /brewsuite nearby | 只显示附近的桶和锅 |
| /brewsuite book | 打开配方书 |
| /brewsuite market | 打开酒市 |
| /brewsuite reload | 重载配置和配方 |

别名 /bsuite。

权限：

- brewsuite.use：使用玩家命令，默认所有玩家
- brewsuite.admin：reload，默认 OP

## 配置

config.yml 里常用的几项：

- language：默认语言
- default-mode：默认显示模式
- refresh-ticks：全息刷新间隔，单位 tick
- barrel.show-empty：是否显示空桶
- barrel.max-bottle-lines：每个桶最多显示的瓶数
- aging.minutes-per-year：陈酿一年需要多少分钟，0 表示读取 BreweryX 的设置
- book.enabled：配方书开关
- market.enabled：酒市开关
- market.daily-limit：每人每天出售上限，0 表示不限
- market.prices：按配方名设置单价

## 构建

JDK 17 及以上。编译 src/main/java 时 classpath 需要 Spigot API、BreweryX、DecentHolograms 和 Vault，编译产物与 src/main/resources 一起打包成 jar。

## 许可

MIT，见 LICENSE。
