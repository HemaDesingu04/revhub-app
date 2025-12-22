// Test script to verify chat and notification endpoints
// Run this after starting the backend server

const baseUrl = 'http://localhost:8080';

// Test data - you'll need to replace with actual JWT token
const testToken = 'your-jwt-token-here';

const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${testToken}`
};

console.log('Testing RevHub Chat and Notification Endpoints...\n');

// Test 1: Check if backend is running
fetch(`${baseUrl}/auth/test`)
    .then(response => {
        if (response.ok) {
            console.log('✅ Backend is running');
        } else {
            console.log('❌ Backend connection failed');
        }
    })
    .catch(error => {
        console.log('❌ Backend not accessible:', error.message);
    });

// Test 2: Test chat contacts endpoint (requires authentication)
console.log('\n📱 Testing Chat Endpoints:');
console.log('Note: These require valid authentication token');

// Test 3: Test notifications endpoint (requires authentication)
console.log('\n🔔 Testing Notification Endpoints:');
console.log('Note: These require valid authentication token');

console.log('\n📋 Manual Testing Steps:');
console.log('1. Start the backend: cd revHubBack && mvn spring-boot:run');
console.log('2. Start the frontend: cd RevHub/RevHub && ng serve');
console.log('3. Login to get a valid JWT token');
console.log('4. Check browser console for any errors');
console.log('5. Test chat and notification features');

console.log('\n🔍 MongoDB Data Check:');
console.log('Run: mongosh revhubteam4 --eval "db.chatMessage.find(); db.notificationMongo.find()"');