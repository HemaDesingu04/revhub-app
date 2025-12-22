// Fix Chat Contacts - Add sample data with real usernames
use revhubteam4

// Clear existing chat messages
db.chatMessage.deleteMany({})

// Insert chat messages with actual usernames from your system
// Replace these usernames with actual users from your MySQL database
db.chatMessage.insertMany([
  {
    senderId: "1",
    senderUsername: "admin",  // Replace with actual username
    receiverId: "2", 
    receiverUsername: "user1", // Replace with actual username
    content: "Hello! How are you doing?",
    timestamp: new Date(),
    read: false
  },
  {
    senderId: "2",
    senderUsername: "user1",
    receiverId: "1",
    receiverUsername: "admin",
    content: "Hi! I'm doing great, thanks!",
    timestamp: new Date(),
    read: false
  },
  {
    senderId: "1", 
    senderUsername: "admin",
    receiverId: "3",
    receiverUsername: "user2", // Replace with actual username
    content: "Hey there! Want to chat?",
    timestamp: new Date(),
    read: false
  }
])

// Create indexes for better performance
db.chatMessage.createIndex({"senderId": 1, "receiverId": 1, "timestamp": -1})
db.chatMessage.createIndex({"senderUsername": 1, "receiverUsername": 1})

print("✅ Chat contacts data fixed!")
print("Total messages:", db.chatMessage.countDocuments())

// Show all messages
db.chatMessage.find().pretty()