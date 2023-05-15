const functions = require("firebase-functions");
const admin = require('firebase-admin');
admin.initializeApp();

//exports.sendNotification = functions.database.ref('/conversations').onWrite((change, context) => {
//    const payload = {
//        notification: {
//            title: 'New message',
//            body: 'There is a new message in the database',
//            click_action: 'OPEN_ACTIVITY_1'
//        }
//    };
//
//    return admin.messaging().sendToTopic('messages', payload);
//});

//exports.anotherSendNotification = functions.database.ref('/messages').onWrite((change, context) => {
//
//  console.log('fun anotherFCM on New message:');
//
//   const payload = {
//       notification: {
//           title: 'New message',
//           body: 'Test cloud functions. There is a new message in the database',
//           click_action: 'OPEN_ACTIVITY_1'
//       }
//   };
//
//   return admin.messaging().sendToTopic('messages', payload);
//});

exports.sendNotification = functions.database.ref('messages/{messageId}')
.onCreate((snapshot, context) => {


    const messageData = snapshot.val();
  console.log('fun FCM on Create New message:', messageData);
    const receiverId = messageData.receiverId;
    const senderId = messageData.senderId;
    const senderName = messageData.senderId;
    const message = messageData.message;
    const tokenRef = "/users/" + receiverId + "/fcmToken";

    // Lookup the receiver user's FCM registration token
    return admin.database().ref(tokenRef).once('value')
        .then( tokenSnapshot => {
            const fcmToken = tokenSnapshot.val();
            const token = 'cgSLkkrjRqWJOSP5-KajXK:APA91bHKM1jqKSb7BG4npMKY9GWxkUrsI9xgPYlwqMmKGwDSUw2maSVBB-pbXKE4sRsknO-VfcdPfcIt8jykUI43d470J4KYHk7xrnOOR9O6gWkDv5S3KIC0DeiyZWCaZLHkPtNHTm7r';

            const messageTitle = "token: " + fcmToken + " You Have new message.";
            // send a push notification
            const payload = {
                notification: {
                    title: messageTitle,
                    body: message,
                    click_action: "OPEN_CHAT_ACTIVITY"
                },
                data: {
                    senderName: "",
                    message: message
                }
            };

            return admin.messaging().sendToDevice(fcmToken, payload);
        });
});

//exports.myTestingFunction = functions.database.ref('/messages/{messageId}').onCreate((snapshot, context) => {
//  const message = snapshot.val();
//  console.log('Testing function New message:', message);
//});
//
//exports.functionWithAuthCheck = functions.https.onCall((data, context) => {
//  // Check if the user is authenticated
//  if (!context.auth) {
//    throw new functions.https.HttpsError('unauthenticated', 'You must be authenticated to call this function.');
//  }
//
//  // Your function code here
//  return "Hello, Firebase Functions!";
//});