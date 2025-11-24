# 数据库说明

## user.db

`user.db` 是 SQLite 数据库文件，用于存储注册用户信息。

### 自动创建

**第一次运行服务器时，数据库会自动创建**，无需手动操作。

服务器启动时会自动检测 `user.db` 文件：
- **如果不存在**：自动创建并初始化3个测试用户
- **如果存在**：直接加载现有用户数据

**初始化的测试用户**：

| ID | 密码 | 昵称 | 性别 |
|----|------|------|------|
| 1  | admin | Admin | 男 |
| 2  | 123   | yong  | 男 |
| 3  | 123   | anni  | 女 |

### 数据库结构

服务器启动时会自动创建以下表结构：

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    nickname TEXT,
    gender TEXT
);
```

### 初始数据

数据库初始为空，用户需要通过客户端注册界面创建账户。

### 重置数据库

如果需要清空所有用户数据：

1. **关闭服务器**
2. **删除 `user.db` 文件**
3. **重新启动服务器**（数据库会重新创建）

### 备份数据库

如果需要备份用户数据：

```bash
# 复制数据库文件
copy user.db user.db.backup

# 或指定日期
copy user.db user.db.backup.2024-11-24
```

### 数据库位置

- **路径**: 项目根目录 `ChatRoom/user.db`
- **相对于服务器**: 与 `MainServer.java` 编译后的类文件同级

### 查看数据库内容

**使用 SQLite 客户端工具**：

- [DB Browser for SQLite](https://sqlitebrowser.org/) - 图形界面
- SQLite 命令行工具

```bash
# 命令行查看
sqlite3 user.db
sqlite> SELECT * FROM users;
sqlite> .quit
```

### 注意事项

⚠️ **不要在服务器运行时删除或修改数据库文件**

✅ **建议定期备份 `user.db` 文件**

✅ **`user.db` 已包含在 Git 仓库中**（初始为空，首次运行时创建）

