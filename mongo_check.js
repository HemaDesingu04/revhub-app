// MongoDB Check Script for RevHub
// Run this in mongosh to check chat and notification collections

// Switch to revhubteam4 database
use revhubteam4

// Check if collections exist
print("=== Collections in revhubteam4 database ===")
db.getCollectionNames()

// Check chat messages collection
print("\n=== Chat Messages Collection ===")
print("Total chat messages:", db.chatMessage.countDocuments())
db.chatMessage.find().limit(5).pretty()

// Check notifications collection  
print("\n=== Notifications Collection ===")
print("Total notifications:", db.notificationMongo.countDocuments())
db.notificationMongo.find().limit(5).pretty()

// Check indexes
print("\n=== Chat Message Indexes ===")
db.chatMessage.getIndexes()

print("\n=== Notification Indexes ===")
db.notificationMongo.getIndexes()

// Create necessary indexes if missing
print("\n=== Creating Indexes ===")
db.chatMessage.createIndex({"senderId": 1, "receiverId": 1, "timestamp": -1})
db.chatMessage.createIndex({"senderUsername": 1, "receiverUsername": 1})
db.notificationMongo.createIndex({"userId": 1, "createdDate": -1})
db.notificationMongo.createIndex({"userId": 1, "readStatus": 1})

print("\n=== Setup Complete ===")