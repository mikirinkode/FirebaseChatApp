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

exports.anotherSendNotification = functions.database.ref('/messages').onWrite((change, context) => {
   const payload = {
       notification: {
           title: 'New message',
           body: 'Test cloud functions. There is a new message in the database',
           click_action: 'OPEN_ACTIVITY_1'
       }
   };

   return admin.messaging().sendToTopic('messages', payload);
});

exports.sendNotification = functions.database.ref('messages/{messageId}')
.onCreate((snapshot, context) => {

    const messageData = snapshot.val();
    const receiverId = messageData.receiverId;
    const senderId = messageData.senderId;
    const message = messageData.message;

    // Lookup the receiver user's FCM registration token
    return admin.database().ref('/users/{receiverId}/fcmToken').once('value')
        .then( tokenSnapshot => {
            const fcmToken = tokenSnapshot.val();

            // send a push notification
            const payload = {
                notification: {
                    title: 'You have new message',
                    body: message,
                    click_action: "OPEN_CHAT_ACTIVITY"
                },
                data: {
                    senderName: "",
                    message: "message"
                }
            };

            return admin.messaging().sendToDevice(fcmToken, payload);
        });
});