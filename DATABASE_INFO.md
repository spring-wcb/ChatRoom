# Database Information

## user.db

`user.db` is a SQLite database file used to store registered user information.

### Automatic Creation

**The database is automatically created when you first run the server** - no manual setup required.

When the server starts, it automatically checks for the `user.db` file:
- **If it doesn't exist**: Automatically creates it and initializes 3 test users
- **If it exists**: Loads existing user data

**Default Test Users**:

| ID | Account | Password | Nickname | Gender |
|----|---------|----------|----------|--------|
| 1  | 101     | 123      | Admin    | M      |
| 2  | 102     | 123      | yong     | M      |
| 3  | 103     | 123      | anni     | F      |

### Database Structure

The user database stores the following information for each user:

**User Entity Fields**:
- `id` (long) - Unique numeric identifier (auto-generated)
- `account` (String) - Unique account username (3-20 alphanumeric characters)
- `password` (String) - User password
- `nickname` (String) - Display name
- `sex` (char) - Gender ('M' or 'F')
- `head` (int) - Avatar image ID (0-10)

### Data Persistence

The database file is stored using Java serialization:
- **Location**: Project root directory (`user.db`)
- **Format**: Java serialized object (List<User>)
- **Auto-saved**: User data is automatically saved when:
  - New users register
  - Server initializes default users

### Important Notes

1. **Local File**: `user.db` is a local file and should not be committed to version control
2. **Auto-regenerate**: If deleted, the server will automatically create a new database with test users on next startup
3. **Account Uniqueness**: Each account username must be unique - registration will fail if account already exists
4. **Password Security**: In production, passwords should be hashed (currently stored in plain text for demonstration)

### Login Methods

Users can login using their **account username** (not numeric ID):
- **Account**: The unique username chosen during registration
- **Password**: The password set during registration

### Reset Database

To reset the database to default test users:
1. Stop the server
2. Delete `user.db` file
3. Restart the server - it will automatically create a fresh database with 3 test users

### File Location Configuration

The database path can be configured in `src/serverconfig.properties`:

```properties
dbpath=user.db
```

### Manual Database Initialization

If you need to manually initialize the database, you can run:

```bash
# Navigate to the compiled classes directory
cd out/production/ChatRoom

# Run the UserService main method
java -cp "." server.model.service.UserService
```

This will create `user.db` with the default test users.

## Chat History

Chat messages are stored separately from user data in JSON format:

**Location**: `chat_history/[userId]/`
- `group_chat.json` - Group chat messages
- `[targetUserId].json` - Private chat messages with specific user

Chat history files are:
- Automatically created when first message is sent
- Loaded automatically when user logs in
- Stored in `.gitignore` (not committed to version control)

### Example Chat History Structure

```json
{
  "userId": 1,
  "targetId": -1,
  "messages": [
    {
      "fromUser": {
        "id": 1,
        "account": "101",
        "nickname": "Admin"
      },
      "message": "Hello everyone!",
      "sendTime": "2024-01-01T10:00:00"
    }
  ]
}
```
