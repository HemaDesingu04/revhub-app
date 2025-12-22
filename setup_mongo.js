// MongoDB Setup Script for RevHub Chat & Notifications
use revhubteam4

// Create chat messages collection with sample data
db.chatMessage.insertMany([
  {
    senderId: "1",
    senderUsername: "user1",
    receiverId: "2", 
    receiverUsername: "user2",
    content: "Hello! How are you?",
    timestamp: new Date(),
    read: false
  },
  {
    senderId: "2",
    senderUsername: "user2", 
    receiverId: "1",
    receiverUsername: "user1",
    content: "Hi! I'm doing great, thanks!",
    timestamp: new Date(),
    read: false
  }
])

// Create notifications collection with sample data
db.notificationMongo.insertMany([
  {
    userId: "1",
    fromUserId: "2",
    fromUsername: "user2",
    fromUserProfilePicture: "default.jpg",
    type: "FOLLOW",
    message: "user2 started following you",
    readStatus: false,
    createdDate: new Date()
  },
  {
    userId: "1", 
    fromUserId: "3",
    fromUsername: "user3",
    fromUserProfilePicture: "default.jpg",
    type: "LIKE",
    message: "user3 liked your post",
    postId: NumberLong(1),
    readStatus: false,
    createdDate: new Date()
  }
])

// Create indexes for performance
db.chatMessage.createIndex({"senderId": 1, "receiverId": 1, "timestamp": -1})
db.chatMessage.createIndex({"senderUsername": 1, "receiverUsername": 1})
db.notificationMongo.createIndex({"userId": 1, "createdDate": -1})
db.notificationMongo.createIndex({"userId": 1, "readStatus": 1})

print("✅ MongoDB collections created successfully!")
print("Chat messages:", db.chatMessage.countDocuments())
print("Notifications:", db.notificationMongo.countDocuments())