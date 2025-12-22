// Test script for Chat & Notifications
use revhubteam4

// Clear existing data
db.chatMessage.deleteMany({})
db.notificationMongo.deleteMany({})

// Insert test users data (assuming users exist in MySQL with these IDs)
// User 1: ID=1, username=testuser1
// User 2: ID=2, username=testuser2

// Insert test chat messages
db.chatMessage.insertMany([
  {
    senderId: "1",
    senderUsername: "testuser1",
    receiverId: "2", 
    receiverUsername: "testuser2",
    content: "Hello testuser2! How are you?",
    timestamp: new Date(),
    read: false
  },
  {
    senderId: "2",
    senderUsername: "testuser2",
    receiverId: "1",
    receiverUsername: "testuser1", 
    content: "Hi testuser1! I'm doing great, thanks for asking!",
    timestamp: new Date(),
    read: false
  },
  {
    senderId: "1",
    senderUsername: "testuser1",
    receiverId: "2",
    receiverUsername: "testuser2",
    content: "That's awesome! Want to grab coffee sometime?",
    timestamp: new Date(),
    read: false
  }
])

// Insert test notifications
db.notificationMongo.insertMany([
  {
    userId: "1",
    fromUserId: "2",
    fromUsername: "testuser2",
    fromUserProfilePicture: "default.jpg",
    type: "FOLLOW",
    message: "testuser2 started following you",
    readStatus: false,
    createdDate: new Date()
  },
  {
    userId: "1",
    fromUserId: "2", 
    fromUsername: "testuser2",
    fromUserProfilePicture: "default.jpg",
    type: "MESSAGE",
    message: "testuser2 sent you a message: Hi testuser1! I'm doing great...",
    readStatus: false,
    createdDate: new Date()
  },
  {
    userId: "2",
    fromUserId: "1",
    fromUsername: "testuser1", 
    fromUserProfilePicture: "default.jpg",
    type: "LIKE",
    message: "testuser1 liked your post",
    postId: NumberLong(1),
    readStatus: false,
    createdDate: new Date()
  }
])

print("✅ Test data inserted successfully!")
print("Chat messages:", db.chatMessage.countDocuments())
print("Notifications:", db.notificationMongo.countDocuments())

// Verify data
print("\n=== Chat Messages ===")
db.chatMessage.find().pretty()

print("\n=== Notifications ===") 
db.notificationMongo.find().pretty()