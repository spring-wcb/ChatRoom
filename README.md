# SpringChat - Modern Chat Application

A feature-rich, modern desktop chat application built with Java Swing, supporting real-time messaging, file sharing, and message persistence.

![Java](https://img.shields.io/badge/Java-8+-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## 📋 Features

### Core Functionality
- ✅ **User Registration & Login** - Secure user authentication
- ✅ **Group Chat** - Public chat room for all online users
- ✅ **Private Messaging** - One-on-one private conversations
- ✅ **Real-time Updates** - Instant message delivery and user status updates
- ✅ **Message Persistence** - Chat history saved locally in JSON format

### Interactive Features
- ✅ **Emoji Support** - 60+ common emojis with picker panel
- ✅ **File Sharing** - Send files up to 10MB with progress feedback
- ✅ **In-Chat Search** - Search messages within current conversation
- ✅ **Message Reply/Quote** - Reply to specific messages with context
- ✅ **Message Recall** - Recall messages within 2 minutes
- ✅ **Window Shake** - Send shake notifications to other users
- ✅ **Read Receipts** - Double-tick display for read messages
- ✅ **Unread Message Badges** - Red count badges on user list

### Modern UI/UX
- 🎨 **WhatsApp-style Interface** - Clean and intuitive design
- 🎨 **Message Bubbles** - Different colors for sent/received messages
- 🎨 **Avatar Display** - Round profile pictures with online status indicators
- 🎨 **Smooth Animations** - Window shake and UI transitions
- 🎨 **Responsive Layout** - Adaptive interface components

## 🛠️ Tech Stack

- **Language**: Java 8+
- **UI Framework**: Swing
- **Database**: SQLite (user data storage)
- **Serialization**: Gson (JSON for message history)
- **Build Tool**: Maven
- **Architecture**: Client-Server with multi-threading

## 📦 Project Structure

```
ChatRoom/
├── src/
│   ├── client/              # Client-side code
│   │   ├── ui/              # User interface
│   │   │   ├── SpringChatFrame.java      # Main chat window
│   │   │   ├── ModernLoginFrame.java     # Modern login UI
│   │   │   ├── ModernRegisterFrame.java  # Modern register UI
│   │   │   ├── LoginFrame.java           # Classic login UI
│   │   │   ├── RegisterFrame.java        # Classic register UI
│   │   │   └── ChatFrame.java            # Legacy chat UI
│   │   ├── util/            # Client utilities
│   │   │   ├── ClientUtil.java           # Helper methods
│   │   │   ├── JFrameShaker.java         # Window shake effect
│   │   │   └── MessageHistoryService.java # Local storage
│   │   ├── model/entity/    # Client data models
│   │   ├── ClientMain.java  # Client entry point
│   │   ├── ClientThread.java # Client message receiver
│   │   └── DataBuffer.java  # Client data cache
│   ├── server/              # Server-side code
│   │   ├── ui/              # Server control panel
│   │   │   └── ServerInfoFrame.java      # Server monitoring UI
│   │   ├── controller/      # Request handlers
│   │   │   └── RequestProcessor.java     # Message routing
│   │   ├── model/           # Server data models
│   │   │   ├── entity/      # Table models
│   │   │   └── service/     # Business logic
│   │   ├── MainServer.java  # Server entry point
│   │   ├── OnlineClientIOCache.java # Connection pool
│   │   └── DataBuffer.java  # Server data cache
│   ├── common/              # Shared code
│   │   ├── model/entity/    # Shared data models
│   │   │   ├── User.java
│   │   │   ├── Message.java
│   │   │   ├── Request.java
│   │   │   ├── Response.java
│   │   │   └── FileInfo.java
│   │   └── util/            # Shared utilities
│   │       ├── IOUtil.java
│   │       └── SocketUtil.java
│   └── serverconfig.properties # Server configuration
├── images/                  # UI resources
│   ├── group_avatar.png     # Group chat icon
│   ├── *.png                # User avatars & icons
│   └── whatsapp_bg.png      # Chat background
├── lib/                     # External libraries
│   ├── gson-2.10.1.jar
│   └── sqlite-jdbc-3.42.0.0.jar
├── pom.xml                  # Maven configuration
├── fix-bom.ps1              # BOM cleanup utility
└── README.md                # This file
```

## 🚀 Getting Started

### Prerequisites

- **Java JDK 8 or higher**
- **Maven** (recommended) or manual JAR management
- **IntelliJ IDEA** / Eclipse / NetBeans (recommended)

### Installation

1. **Clone the repository**

```bash
git clone https://github.com/yourusername/SpringChat.git
cd SpringChat/ChatRoom
```

2. **Configure IDE**

**IntelliJ IDEA**:
- Open the project: `File → Open → Select ChatRoom folder`
- Wait for Maven to import dependencies
- Set JDK: `File → Project Structure → Project SDK`

**Eclipse**:
- Import as Maven project: `File → Import → Existing Maven Projects`

3. **Build the project**

```bash
mvn clean compile
```

Or in IDE:
```
Build → Rebuild Project
```

### Running the Application

#### 1. Start the Server

**Run in IDE**:
```
Right-click server/MainServer.java → Run 'MainServer.main()'
```

**Run from command line**:
```bash
java -cp "out/production/ChatRoom;lib/*" server.MainServer
```

**Expected output**:
```
Server started on port 8888
```

#### 2. Start Client(s)

**Run in IDE**:
```
Right-click client/ClientMain.java → Run 'ClientMain.main()'
```

**Run from command line**:
```bash
java -cp "out/production/ChatRoom;lib/*" client.ClientMain
```

**First-time users**: Register a new account
**Returning users**: Login with existing credentials

#### 3. Test the Application

- Start at least 2 clients to test messaging
- Try both group chat and private messages
- Test file sharing, emoji, search features
- Close and reopen to verify message persistence

## 🎯 Usage Guide

### Basic Chat Operations

1. **Group Chat** (default view)
   - Click "Group Chat" in user list
   - Type message and press Enter or click Send
   - All online users will receive the message

2. **Private Chat**
   - Click on a user in the left sidebar
   - Send messages that only they can see
   - Header shows "User Name (Online)"

3. **Send Emoji**
   - Click the 😊 emoji button
   - Select from the emoji picker
   - Emoji inserted at cursor position

4. **Send Files**
   - Click the 📎 attachment button
   - Select file (max 10MB)
   - File sent with progress indicator

5. **Search Messages**
   - Click the 🔍 search button
   - Type search query
   - Navigate results with ↑ ↓ buttons

6. **Reply to Message**
   - Right-click on any message
   - Select "Reply"
   - Type response (quoted message shown)

7. **Recall Message**
   - Right-click on your sent message (within 2 minutes)
   - Select "Recall"
   - Message replaced with "Message recalled"

8. **Window Shake**
   - Click the shake icon (🔔) when in private chat
   - Other user's window will shake

### Advanced Features

**Message Persistence**:
- All chat history saved in `chat_history/[userId]/`
- Automatically loaded on login
- Separate files for group chat and each private conversation

**Read Receipts**:
- Single gray tick: Message sent
- Double gray ticks: Message delivered
- Double blue ticks: Message read

**Unread Badges**:
- Red circular badges on user list
- Shows unread message count
- Clears when you open that chat

## 🔧 Configuration

### Server Configuration

Edit `src/serverconfig.properties`:

```properties
# Server port (default: 8888)
server.port=8888

# Maximum connections
server.maxConnections=100
```

### Client Configuration

**Message Recall Time Limit** (in `SpringChatFrame.java`):
```java
private static final long RECALL_TIME_LIMIT = 2 * 60 * 1000; // 2 minutes
```

**File Size Limit** (in `SpringChatFrame.java`):
```java
private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
```

## 🐛 Troubleshooting

### Issue: "Cannot find package com.google.gson"

**Solution**: Reload Maven dependencies
```bash
# In IntelliJ IDEA
Right-click pom.xml → Maven → Reload Project

# Command line
mvn clean install
```

### Issue: "Illegal character '\ufeff'" or compilation errors

**Solution**: Remove UTF-8 BOM from source files
```powershell
# Windows PowerShell (in ChatRoom directory)
.\fix-bom.ps1
```

Then rebuild the project.

### Issue: "Module JDK not defined"

**Solution**: Configure Project SDK
1. `File → Project Structure` (Ctrl+Alt+Shift+S)
2. `Project → SDK → Select or Download JDK`
3. Choose JDK 8 or higher (JDK 17 recommended)
4. Click OK and rebuild

### Issue: Chinese characters appear as garbled text

**Solution**: All UI text has been converted to English. If you still see issues:
1. Check IDE file encoding: `File → Settings → Editor → File Encodings → UTF-8`
2. Run `fix-bom.ps1` to clean source files
3. Rebuild project

## 📝 Development Notes

### Code Style
- **Naming Convention**: camelCase for methods/variables, PascalCase for classes
- **Indentation**: 4 spaces
- **Encoding**: UTF-8 without BOM
- **Line Endings**: LF (Unix-style)

### Key Design Patterns
- **MVC Pattern**: Separation of UI, business logic, and data
- **Observer Pattern**: Real-time message updates
- **Singleton Pattern**: DataBuffer for shared state
- **Factory Pattern**: Request/Response creation

### Adding New Features

1. **New Message Type**:
   - Add enum to `ResponseType.java`
   - Handle in `ClientThread.java` (client)
   - Process in `RequestProcessor.java` (server)

2. **New UI Component**:
   - Create in `client/ui/` package
   - Follow existing UI patterns
   - Use `SpringChatFrame` as reference

3. **New Data Model**:
   - Add to `common/model/entity/` (if shared)
   - Implement `Serializable` interface
   - Add getters/setters

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**South China University of Technology (SCUT)**
- Java Course Project
- Academic Year: 2024-2025

## 🙏 Acknowledgments

- UI Design inspired by WhatsApp Web
- Icons and emoji support from Unicode Standard
- SQLite for lightweight database solution
- Gson for elegant JSON handling

## 📚 Documentation

For more detailed documentation, see:
- **Architecture Design**: [ARCHITECTURE.md](docs/ARCHITECTURE.md) (if created)
- **API Reference**: [API.md](docs/API.md) (if created)
- **User Manual**: [USER_GUIDE.md](docs/USER_GUIDE.md) (if created)

## 🔗 Links

- **GitHub Repository**: [SpringChat](https://github.com/yourusername/SpringChat)
- **Issue Tracker**: [Report Bugs](https://github.com/yourusername/SpringChat/issues)
- **Project Wiki**: [Documentation](https://github.com/yourusername/SpringChat/wiki)

---

**Built with ❤️ for Java Programming Course**

